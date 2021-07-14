from images_classes import *
import pandas as pd
import os
import pickle
from datetime import datetime
from random import randint
from math import floor
from bson.son import SON
from pymongo import MongoClient,errors


#name_to_id_dict = pickle.load(open("name_to_id_dict.pkl", "rb"))
#id_to_name_dict = {value: key for key, value in name_to_id_dict.items()}

#id_to_name_dict = pickle.load(open("dict_cls2name.pkl", "rb"))


MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL=4


def calculate_total(entry_time, exit_time):
    hours = exit_time[0] - entry_time[0] - (exit_time[1] < entry_time[1])
    minutes = exit_time[1] - entry_time[1] - (exit_time[2] < entry_time[2])
    if minutes < 0:
        minutes += 60
    seconds = exit_time[2] - entry_time[2]
    if seconds < 0:
        seconds += 60
    return hours, minutes, seconds


class BiometricSystemDb:

    def __init__(self,client,db,employees_collection,images_collection,attendance_collection):
        self.client = client
        self.db=db
        self.employees_collection=employees_collection
        self.images_collection=images_collection
        self.attendance_collection=attendance_collection


    def make_image_doc(self, path, employee_id, face_indexes,recognized=False):
        now = datetime.now()
        return \
            SON({
                "_id": path.split('\\')[-1],
                "employee id": employee_id,
                "face indexes": face_indexes,
                "recognized": recognized,
                "uploaded": SON({"date": SON({"year": now.year, "month": now.month, "day": now.day}),
                                 "time": SON({"hour": now.hour, "minute": now.minute, "second": now.second})})
            })


    def make_attendance_doc(self, employee_id, year, month, day, entry_time=(8, 0, 0), exit_time=(17, 0, 0)):
        hours, minutes, seconds = calculate_total(entry_time, exit_time)
        return \
            SON({
                "_id": '-'.join([str(employee_id), str(year), str(month), str(day)]),
                "employee id": employee_id,
                "date": SON({"year": year, "month": month, "day": day}),
                "entry": SON({"hour": entry_time[0], "minute": entry_time[1], "second": entry_time[2]}),
                "exit": SON({"hour": exit_time[0], "minute": exit_time[1], "second": exit_time[2]}),
                "total": SON({"hours": hours, "minutes": minutes, "seconds": seconds})
            })


    def make_employee_doc(self, employee_id, employee_number, name, images_directory_path,
                          number_of_images,branch=None,accuracy=None,
                          model_class=-1,admin=False):
        if branch==None:
            branch=["TA", "SF", "TO", "BN"][randint(0,3)]
        return \
            SON({
                "_id": employee_id,
                "employee number": employee_number,
                "name": name,
                "images directory path": images_directory_path,
                "branch": branch,
                "admin": admin,
                "number of images": number_of_images,
                "accuracy": accuracy,
                "model class": model_class,
                "included in model":number_of_images>=MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL
            })


    def get_number_of_employees(self):
        return self.employees_collection.count_documents({})