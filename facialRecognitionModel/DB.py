from DB_utils import *


client = MongoClient('mongodb://localhost:27017/')
with client:
    biometric_system_db = client["biometric_system_new"]
    employees_collection = biometric_system_db["employees"]
    images_collection = biometric_system_db["images"]
    attendance_collection = biometric_system_db["attendance"]
    db=BiometricSystemDb(client,biometric_system_db,employees_collection,images_collection,attendance_collection)


if __name__ == "__main__":

    id_to_name_dict = pickle.load(open("dict_cls2name.pkl", "rb"))
    db_df=pickle.load(open(os.path.join(os.getcwd(),"db_df.pkl"),"rb"))

    employees=list()
    images=list()
    attendance=list()


    for index in range(db_df.shape[0]):

        #employee_id=int(db_df.iloc[index]['employee_id'])
        employee_id = index
        name=id_to_name_dict[index]

        employees.append(db.make_employee_doc(employee_id,employee_id,name,'/'.join(db_df.iloc[index]['path'][0].split('\\')[:-1]),int(db_df['pic_num'][index])))

        for path,face_indexes in zip(db_df.iloc[index]['path'],db_df.iloc[index]['indexes']):

            images.append(db.make_image_doc(path,employee_id, list(map(float,face_indexes))))

        attendance.append(db.make_attendance_doc(employee_id,2021,1,1))
        attendance.append(db.make_attendance_doc(employee_id,2021,1,2,(8,randint(0,59),randint(0,59)),(17,randint(0,59),randint(0,59))))


    for employee_index in range(round(len(employees)/10)):
        employees[employee_index]["admin"]=True


    db.employees_collection.insert_many(employees)
    db.images_collection.insert_many(images)
    db.attendance_collection.insert_many(attendance)