from augmentation import *
import torch
import numpy as np
from facenet_pytorch import MTCNN, InceptionResnetV1
#from face_recognition import face_locations
from PIL import Image
import albumentations as A
from albumentations.pytorch import ToTensorV2


class ImageInSet:

    mtcnn = MTCNN(post_process=False, image_size=160)
    face_detection_threshold=0.95
    face_recognition_model = InceptionResnetV1(pretrained='vggface2').eval()
    name_to_id_dict=dict()
    image_size=(160,160)
    norm = A.Compose([A.Resize(160, 160, interpolation=1, always_apply=True, p=1),
                      A.Normalize(mean=[0, 0, 0], std=[1, 1, 1], max_pixel_value=255, ), ToTensorV2(), ])

    #face_recognition_threshold = 0.95
    #face_cascade = cv.CascadeClassifier('haarcascade_frontalface_default.xml')

    def __init__(self,path):
        #self.values=cv.imread(path)
        self.values = Image.open(path)
        self.path=path
        self.dir=self.path.split('\\')[-2]
        self.file_name=self.path.split('\\')[-1]
        self.name=' '.join(self.dir.split('_'))
        self.employee_id=ImageInSet.name_to_id(self.name)

    @classmethod
    def name_to_id(cls,name):
        if name in cls.name_to_id_dict.keys():
            return cls.name_to_id_dict[name]
        else:
            new_employee_id=len(cls.name_to_id_dict.keys())
            cls.name_to_id_dict[name]=new_employee_id
            return new_employee_id

    '''
    def get_face_image(self):
        face_loc=face_locations(self.values)
        try:
            face=self.values[face_loc[0][0]:face_loc[0][1],face_loc[0][3]:face_loc[0][2]]
        except IndexError:
            return None
        if (not face is None) and (not isinstance(face, type(None))) and len(face):
            return Face_image(face,self.name)
        else:
            return None
    '''

    def norm_without_aug(self):
        img = self.values
        #indexes = self.get_face_indexes()
        #img = img[indexes[1]:indexes[3], indexes[0]:indexes[2]]
        aug = ImageInSet.norm(image=img)
        img = aug["image"]
        self.values = img.unsqueeze(0)

    def get_face_indexes(self):
        boxes,_ = ImageInSet.mtcnn.detect(self.values, landmarks=False)
        if (not boxes is None) and (not isinstance(boxes, type(None))):
            box = list(boxes[0])
            if any(box[2:]) < 0:
                return None
            face_indexes=[int(b) for b in box]
            return face_indexes
        else:
            return None

    """"
    def get_face_indexes(self):
        boxes, probs = ImageInSet.mtcnn.detect(self.values, landmarks=False)
        if (not boxes is None) and (not isinstance(boxes, type(None))) and (probs[0]>= ImageInSet.face_detection_threshold) and (all(boxes[0]>0)):
            face_indexes=[int(b) for b in boxes[0]]
            return face_indexes
            #return boxes[0]
        else:
            return None
    """

    def get_face_image(self,indexes=None):
        if indexes is None:
            indexes=self.get_face_indexes()
        return FaceImage(self.values[indexes[1]:indexes[3], indexes[0]:indexes[2]])

        #face_image=pil_image.crop(indexes_box)
        #return FaceImage(np.array(face_image))

    def normalize_by_train_values(self,train_mean,train_std):
        return (self.values - train_mean) / train_std

    def normalize_by_image_values(self):
        return (self.values- self.values.mean())/self.values.std()

    def save(self, new_path):
        self.path = new_path
        cv.imwrite(self.path, self.values)

    def resize_image(self):
        self.values = cv.resize(self.values, ImageInSet.image_size)

    def get_embedding(self, normalization_method, train_mean=None, train_std=None ,as_numpy=False):
        if normalization_method == "normalize_by_train_values":
            assert (not train_mean is None) and (not train_std is None), "enter train paths list in order to use the normalize by train values method"
            norm_values = self.normalize_by_train_values(train_mean,train_std)
        elif normalization_method=="normalize_by_image_values":
            norm_values = self.normalize_by_image_values()
        else:
            norm_values=self.values
        #four_dim_values = np.expand_dims(norm_values, axis=0)
        #embedding = model.predict(four_dim_values)[0]
        #tensor_norm_values=from_numpy(norm_values).unsqueeze(0)
        tensor_norm_values=torch.tensor(norm_values).float()
        resized_img=tensor_norm_values.permute(2, 0, 1)
        embedding = ImageInSet.face_recognition_model(resized_img.unsqueeze(0))
        if as_numpy:
            return np.array(embedding.detach()[0])
        else:
            return embedding.detach()[0]


class FaceImage(ImageInSet):

    def __init__(self,values):
        self.values=values
        self.name=None
        self.path=None

    '''
    def save(self,path):
        self.values.save(path)
        self.path=path
    '''

def get_images_mean(paths_list):
    assert len(paths_list), "paths list must not be empty"
    train_mean=list()
    for path in paths_list:
        train_image=ImageInSet(path[0])
        train_mean.append(train_image.values)
    return np.mean(train_mean, axis=(0, 1, 2))

def get_images_std(paths_list):
    assert len(paths_list), "paths list must not be empty"
    train_std=list()
    for path in paths_list:
        train_image=ImageInSet(path[0])
        train_std.append(train_image.values)
    return np.std(train_std,axis=(0,1,2))