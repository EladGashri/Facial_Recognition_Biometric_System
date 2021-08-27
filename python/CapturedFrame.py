from ImageInSet import *
from Ann import *
from LiveFeed import *
from pymongo import errors


class CapturedFrame(ImageInSet):

    #ann_model = InceptionResnetV1(classify=True, pretrained='vggface2', num_classes=len(id_to_name_dict_load.keys()))
    #ann_model.load_state_dict(torch.load("ann_model.pth",map_location=torch.device("cpu")))
    ann_model = NewNet(num_classes=len(id_to_name_dict_load.keys()))
    ann_model.load_state_dict(torch.load('..\\ANN\\ann_model.pth',map_location=torch.device("cpu")))


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


    def identify(self):
        if not self.face_detected:
            raise FrameException("face must be detected in order to perform identification")
        img = self.norm_without_aug()
        with torch.no_grad():
            CapturedFrame.ann_model.eval()
            output = CapturedFrame.ann_model(img)
        self.recognition_probability=float(torch.max(F.softmax(output,dim=1),1)[0].item())
        self.id_detected = int(torch.max(F.softmax(output, dim=1), 1)[1].item())
        print("recognition probability: " + str(self.recognition_probability))


    def set_name(self,id_to_name_dict):
        if self.id_detected is None:
            raise FrameException("id must be detected in order to set name")
        self.name=id_to_name_dict[self.id_detected]


    def save_image_to_db(self,db,number_of_employee_images=None):
        if number_of_employee_images is None:
            number_of_employee_images=db.images_collection.count_documents({"employee_id":self.id_detected})
        if len(str(number_of_employee_images))<Training.MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL:
            number_of_employee_images_str="0"*(4-len(str(number_of_employee_images)))+str(number_of_employee_images)
        else:
            number_of_employee_images_str=str(number_of_employee_images)
        employee_images_path=db.employees_collection.find({"_id":self.id_detected},{"_id":0,"images directory path":1})[0]["images directory path"]
        self.save(employee_images_path+"\\"+self.name+"_"+number_of_employee_images_str+".jpg")
        image_doc=db.make_image_doc(self.path,self.id_detected,self.face_indexes,True,self.recognition_probability)
        try:
            db.images_collection.insert_one(image_doc)
            print("image saved to database of employee id=" + str(self.id_detected))
        except errors.DuplicateKeyError:
            self.save_image_to_db(db,number_of_employee_images+1)



class FrameException(Exception):
    pass