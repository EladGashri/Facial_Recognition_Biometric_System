from live_feed import *
from database import *
from captured_frame import *
import cv2 as cv
from datetime import time


if __name__ == "__main__":

    live_feed = LiveFeed(database)
    #live_feed.update_employee_entry_today_by_db()
    #live_feed.update_id_to_name_dict_by_db()

    faces_detected_dir='\\'.join([os.getcwd(),'faces detected in live feed'])
    if not os.path.isdir(faces_detected_dir):
        os.mkdir(faces_detected_dir)

    no_faces_detected_dir='\\'.join([os.getcwd(),'no faces detected in live feed'])
    if not os.path.isdir(no_faces_detected_dir):
        os.mkdir(no_faces_detected_dir)


    cap = cv.VideoCapture(0,cv.CAP_DSHOW)
    #cap = cv.VideoCapture(0)
    if not cap.isOpened():
        raise IOError("Cannot open webcam")

    while True:
        print("\n")
        start = time.time()
        ret, frame = cap.read()
        if (frame is  None) or (isinstance(frame, type(None))) :
            print("frame is none")
            continue
        frame_image = CapturedFrame(cv.resize(frame, None, fx=0.5, fy=0.5,interpolation=cv.INTER_AREA))
        frame_image.set_face_image(live_feed)
        if frame_image.face_detected:
            print("face detected number "+ str(live_feed.number_of_faces_detected))
            frame_image.face_image.save(os.path.join(faces_detected_dir,str(live_feed.number_of_faces_detected)+".jpg"))
            frame_image.identify()
            end = time.time()
            if frame_image.recognition_probability>=live_feed.face_recognition_threshold:
                live_feed.number_of_faces_recognized+=1
                frame_image.set_name(live_feed.id_to_name_dict)
                print("".join(["face recognized number " +str(live_feed.number_of_faces_recognized) +" as ",frame_image.name, " in " + "{:.3f}".format(end-start) + " seconds"]))
                if frame_image.recognition_probability>=live_feed.save_image_in_db_threshold:
                    frame_image.save_image_to_db(live_feed.db)
                #if not live_feed.employees_entry_today[frame_image.id_detected]:
                 #   live_feed.register_entry(frame_image.id_detected,override=True)
                  #  live_feed.employees_entry_today[frame_image.id_detected]=True
                #else:
                 #   print("".join(["employee id=", str(frame_image.id_detected)," already registered entry today"]))
            else:
                live_feed.number_of_faces_not_recognized+=1
                print("face not recognized number " + str(live_feed.number_of_faces_not_recognized))
        else:
            print("face not detected number "+ str(live_feed.number_of_face_not_detected))
            frame_image.save("".join([no_faces_detected_dir,"\\",str(live_feed.number_of_face_not_detected),".jpg"]))
        c = cv.waitKey(1)
        if c == 27:
            break

    cap.release()
    cv.destroyAllWindows()