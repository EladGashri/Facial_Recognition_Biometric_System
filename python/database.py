from employee import *
from pickle import *
from datetime import datetime
from random import randint
from bson.son import SON
from pymongo import MongoClient


class Database:
    
    DATABASE_ADDRESS = 'localhost:27017'

    def __init__(self):
        self.__client = MongoClient(''.join(['mongodb://',Database.DATABASE_ADDRESS]))
        with self.__client:
            self.__database = self.__client["biometric_system"]
            self.employees_collection = self.__database["employees"]
            self.images_collection = self.__database["images"]
            self.attendances_collection = self.__database["attendances"]

    def create_database_from_dataframe(self,db_df,id_to_name_dict):
        employees_list = list()
        images_list = list()
        attendances_list = list()

        for index in range(db_df.shape[0]):
            
            employee_type_str = db_df.iloc[index]['employee_type']
            if employee_type_str=='standard':
                employee_type = EmployeeType.STANDARD
            elif employee_type_str=='admin':
                employee_type = EmployeeType.ADMIN
            elif employee_type_str=='cto':
                employee_type = EmployeeType.CTO
            else:
                continue
                
            employee = Employee(index, index, id_to_name_dict[index],
                                '/'.join(db_df.iloc[index]['path'][0].split('\\')[:-1]), employee_type, int(db_df['pic_num'][index]))

            employees_list.append(database.make_employee_doc(employee))

            for path, face_indexes in zip(db_df.iloc[index]['path'], db_df.iloc[index]['indexes']):
                images_list.append(database.make_image_doc(path, employee, list(map(float, face_indexes))))

            attendances_list.append(database.make_attendance_doc(employee, 2021, 1, 1))
            attendances_list.append(
                database.make_attendance_doc(employee, 2021, 1, 2, (8, randint(0, 59), randint(0, 59)),
                                             (17, randint(0, 59), randint(0, 59))))


        self.employees_collection.insert_many(employees_list)
        self.images_collection.insert_many(images_list)
        self.attendances_collection.insert_many(attendances_list)


    def make_employee_doc(self, employee):
        return \
            SON({
                "_id": employee.id,
                "employee number": employee.employee_number,
                "name": employee.name,
                "images directory path": employee.images_directory_path,
                "branch": employee.branch,
                "employee type": employee.employee_type,
                "number of images": employee.number_of_images,
                "model class": employee.model_class,
                "included in model": employee.number_of_images >= Employee.MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL
            })


    def make_image_doc(self, path, employee, face_indexes,recognized_by_model=False):
        now = datetime.now()
        return \
            SON({
                "_id": path.split('\\')[-1],
                "employee id": employee.id,
                "face indexes": face_indexes,
                "recognized by model": recognized_by_model,
                "uploaded": SON({"date": SON({"year": now.year, "month": now.month, "day": now.day}),
                                 "time": SON({"hour": now.hour, "minute": now.minute, "second": now.second})})
            })


    def make_attendance_doc(self, employee, year, month, day, entry_time=(8,0,0), exit_time=(17,0,0)):
        hours, minutes, seconds = Database.calculate_total(entry_time, exit_time)
        return \
            SON({
                "_id": '-'.join([str(employee.id), str(year), str(month), str(day)]),
                "employee id": employee.id,
                "date": SON({"year": year, "month": month, "day": day}),
                "entry": SON({"hour": entry_time[0], "minute": entry_time[1], "second": entry_time[2]}),
                "exit": SON({"hour": exit_time[0], "minute": exit_time[1], "second": exit_time[2]}),
                "total": SON({"hours": hours, "minutes": minutes, "seconds": seconds})
            })


    def get_number_of_employees(self):
        return self.employees_collection.count_documents({})


    @staticmethod
    def calculate_total(entry_time, exit_time):
        hours = exit_time[0] - entry_time[0] - (exit_time[1] < entry_time[1])
        minutes = exit_time[1] - entry_time[1] - (exit_time[2] < entry_time[2])
        if minutes < 0:
            minutes += 60
        seconds = exit_time[2] - entry_time[2]
        if seconds < 0:
            seconds += 60
        return hours, minutes, seconds


database = Database()


if __name__ == "__main__":

    db_df = pickle.load(open("db_df.pkl", "rb"))
    id_to_name_dict = pickle.load(open("id_to_name_dict.pkl", "rb"))
    database.create_database_from_dataframe(db_df,id_to_name_dict)
