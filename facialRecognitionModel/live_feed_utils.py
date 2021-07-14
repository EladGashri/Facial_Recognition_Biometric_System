from images_classes import *
from torch.nn import functional as F
import torch
from DB_utils import *
from DB import db
import cv2 as cv
import os
from datetime import datetime
from bson.son import SON
import time
import torch.nn as nn
from ANN import NewNet


#name_to_id_dict = pickle.load(open("name_to_id_dict.pkl", "rb"))
#id_to_name_dict_load = {value: key for key, value in name_to_id_dict.items()}

id_to_name_dict_load = pickle.load(open("dict_cls2name.pickle", "rb"))


class LiveFeed:

    face_recognition_threshold=0.9
    save_image_in_db_threshold=0.95


    def __init__(self,db):
        self.db=db
        self.date=datetime.now().date()
        self.id_to_name_dict = id_to_name_dict_load
        #self.number_of_employees=db.get_number_of_employees()
        self.number_of_employees=len(id_to_name_dict_load.keys())
        self.employees_entry_today = [False] * self.number_of_employees
        self.number_of_faces_recognized = 0
        self.number_of_faces_not_recognized = 0
        self.number_of_faces_detected = 0
        self.number_of_face_not_detected = 0

    def update_employee_entry_today_by_db(self):
        entry_today_query_find = SON(
            {"date": SON({"year": self.date.year, "month": self.date.month, "day": self.date.day}),
             "entry": {"$ne": None}}), \
                                 SON({"employee id": 1, "_id": 0})
        entry_today_query = self.db.attendance_collection.find(entry_today_query_find[0], entry_today_query_find[1])
        for entry in entry_today_query:
            employee_id = entry["employee id"]
            self.employees_entry_today[employee_id - 1] = True

    def update_id_to_name_dict_by_db(self):
        employees_id_names_query=self.db.employees_collection.find({},{"_id":1,"name":1})
        self.id_to_name_dict=dict()
        for employee in employees_id_names_query:
            self.id_to_name_dict[employee["_id"]]=employee["name"]

    def register_entry(self, id_detected, date=None, time=datetime.now().time(), override=False):
        if date is None:
            date=self.date
        attendence_find = SON({"employee id": id_detected,"date": SON({"year": date.year,"month": date.month,"day": date.day})})
        attendence_query = self.db.attendance_collection.find(attendence_find)

        if len(list(attendence_query)) > 0:
            if override:
                date_query = {"employee id": id_detected,
                              "date": SON({"year": date.year, "month": date.month,"day": date.day})}
                update_entry = {"$set": {"entry": SON(
                    {"hour": time.hour, "minute": time.minute,"second": time.second})}}
                self.db.attendance_collection.update_one(date_query, update_entry)
                print("".join(["database entry updated for employee id=" + str(id_detected)]))
            else:
                raise QueryError(' '.join(
                    ["employee number", str(id_detected), "already registered entry at date", str(date) \
                    + "\nmust allow override in order to update entry"]))

        else:
            attendence_insert = SON({
                "_id": '-'.join([str(id_detected), str(date.year), str(date.day),str(date.day)]),
                "employee id": id_detected,
                "date": SON({"year": date.year, "month": date.month,
                             "day": date.day}),
                "entry": SON({"hour": time.hour, "minute": time.minute,
                              "second": time.second}),
                "exit": None,
                "total": None
            })
            self.db.attendance_collection.insert_one(attendence_insert)
            print("".join(["database entry registered for employee id=" + str(id_detected)]))


    def register_exit(self, id_detected, date=None, time=datetime.now().time(), override=False):
        if date is None:
            date=self.date
        entry_find = SON({"employee id": id_detected, "date": SON(
            {"year": int(date.year), "month": int(date.month), "day": int(date.day)}),
                          "entry": {"$ne": None}}), SON({"entry": 1, "_id": 0})
        entry_query = self.db.attendance_collection.find(entry_find[0], entry_find[1])
        entry_query_list = list(entry_query)
        if len(entry_query_list) == 0:
            raise QueryError(
                ' '.join(["employee id=", str(id_detected), "didn't register entry at date", str(date),
                          "and therefore cannot register exit"]))

        already_registered_exit_find = SON({"employee id": id_detected,
                                                     "date": SON({"year": date.year,
                                                                  "month": date.month,
                                                                  "day": date.day}),
                                                     "exit": {"$ne": None}})
        already_registered_exit_query = self.db.attendance_collection.find(
            already_registered_exit_find)
        if override == False and len(list(already_registered_exit_query)) > 0:
            raise QueryError(' '.join(
                ["employee id=", str(id_detected), "already registered exit at date", str(date),
                 "\nmust allow override in order to update exit"]))

        else:
            date_query = {"employee id": id_detected,
                          "date": SON({"year": date.year, "month": date.month, "day": date.day})}

            update_entry_date_and_time = [entry_query_list[0]["entry"]["hour"], entry_query_list[0]["entry"]["minute"],
                                          entry_query_list[0]["entry"]["second"]]
            update_exit_date_and_time = [time.hour, time.minute,time.second]
            hours, minutes, seconds = calculate_total(update_entry_date_and_time, update_exit_date_and_time)

            update_exit_and_total = {"$set": {"exit": SON({"hour": time.hour, "minute": time.minute, "second": time.second}),
                                              "total": SON({"hours": hours, "minutes": minutes, "seconds": seconds})}}

            self.db.attendance_collection.update_one(date_query, update_exit_and_total)
            if override:
                print("".join(["database exit and total updated for employee id=", str(id_detected)]))
            else:
                print("".join(["database exit and total registered for employee id=", str(id_detected)]))


class CapturedFrame(ImageInSet):

    #ann_model = InceptionResnetV1(classify=True, pretrained='vggface2', num_classes=len(id_to_name_dict_load.keys()))
    #ann_model.load_state_dict(torch.load("ann_model.pth",map_location=torch.device("cpu")))
    ann_model = NewNet(num_classes=len(id_to_name_dict_load.keys()))
    ann_model.load_state_dict(torch.load('ann_model.pth',map_location=torch.device("cpu")))

    #knn_model=pickle.load(open("knn_model.pkl","rb"))

    def __init__(self,values):
        self.values=values
        self.name=""
        self.path=None
        self.face_indexes=None
        self.face_image=None
        self.face_detected=False
        self.id_detected=None
        self.recognition_probability=None

    def set_face_image(self,live_feed):
        self.face_indexes=self.get_face_indexes()
        self.face_detected=True if (self.face_indexes is not None) and not (isinstance(self.face_indexes, type(None))) else False
        if self.face_detected:
            #self.save("face_image_temp.jpg")
            self.face_image = self.get_face_image(self.face_indexes)
            #os.remove("face_image_temp.jpg")
            #self.face_image=self.values[int(indexes_box[1]):int(indexes_box[3]), int(indexes_box[0]):int(indexes_box[2])]
            live_feed.number_of_faces_detected+=1
        else:
            live_feed.number_of_face_not_detected += 1

    def identify(self,model):
        if not self.face_detected:
            raise FrameException("face must be detected in order to perform identification")
        if model=="ann":
            img = self.norm_without_aug()
            with torch.no_grad():
                CapturedFrame.ann_model.eval()
                output = CapturedFrame.ann_model(img)
            self.recognition_probability=float(torch.max(F.softmax(output,dim=1),1)[0].item())
            self.id_detected = int(torch.max(F.softmax(output, dim=1), 1)[1].item())
        elif model=="knn":
            self.face_image.augmentate()
            face_embedding=self.face_image.get_embedding(None,as_numpy=True)
            self.id_detected = int(CapturedFrame.knn_model.predict([face_embedding])[0])
            self.recognition_probability = float(CapturedFrame.knn_model.predict_proba([face_embedding])[0][self.id_detected])
        print("recognition probability: " + str(self.recognition_probability))

    def set_name(self,id_to_name_dict):
        if self.id_detected is None:
            raise FrameException("id must be detected in order to set name")
        self.name=id_to_name_dict[self.id_detected]

    def save_image_to_db(self,db,number_of_employee_images=None):
        if number_of_employee_images is None:
            number_of_employee_images=db.images_collection.count_documents({"employee_id":self.id_detected})
        if len(str(number_of_employee_images))<4:
            number_of_employee_images_str="0"*(4-len(str(number_of_employee_images)))+str(number_of_employee_images)
        else:
            number_of_employee_images_str=str(number_of_employee_images)
        employee_images_path=db.employees_collection.find({"_id":self.id_detected},{"_id":0,"images directory path":1})[0]["images directory path"]
        self.save(employee_images_path+"\\"+self.name+"_"+number_of_employee_images_str+".jpg")
        #delete the next line later
        os.remove(self.path)
        image_doc=db.make_image_doc(self.path,self.id_detected,self.face_indexes,True,self.recognition_probability)
        try:
            db.images_collection.insert_one(image_doc)
            print("image saved to database of employee id=" + str(self.id_detected))
        except errors.DuplicateKeyError:
            self.save_image_to_db(db,number_of_employee_images+1)


class QueryError(Exception):
    pass


class FrameException(Exception):
    pass