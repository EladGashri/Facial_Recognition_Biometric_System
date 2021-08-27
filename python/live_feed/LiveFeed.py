from database.Database import *
from datetime import datetime
from bson.son import SON


id_to_name_dict_load = pickle.load(open("..\\id_to_name_dict.pkl", "rb"))

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



class QueryError(Exception):
    pass
