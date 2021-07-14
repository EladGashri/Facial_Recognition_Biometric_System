from images_classes import *
from augmentation import *
import os
import shutil
import math
import pickle


#all the train set images are saved in a list which is a class variable. this list is used in order or extract the mean and std of the
#train set images for normalization for the rest of the images
root_dir=os.getcwd()
dataset_dir = input("please enter the dataset directory path")
os.chdir(dataset_dir)

directories = [dir for dir in os.listdir(dataset_dir) if not '.' in dir] #directories contain all the people that have images in the dataset

#new direstories are being made for the train, validation and test sets
sets_dir='\\'.join([dataset_dir, 'sets'])
train_dir = '\\'.join([sets_dir, 'train'])
validation_dir = '\\'.join([sets_dir, 'validation'])
test_dir = '\\'.join([sets_dir, 'test'])

if os.path.isdir(sets_dir):
    delete_sets=None
    while delete_sets not in ["y","n"]:
        delete_sets=input("delete all current sets directories? y/n")
        if delete_sets=="y":
            shutil.rmtree(sets_dir)
            os.mkdir(sets_dir)
            os.mkdir(train_dir)
            os.mkdir(validation_dir)
            os.mkdir(test_dir)
else:
    os.mkdir(sets_dir)
    os.mkdir(train_dir)
    os.mkdir(validation_dir)
    os.mkdir(test_dir)

train_paths = list()
validation_paths = list()
test_paths = list()
augmentation_paths = list()
no_faces_detected = list()


for dir in directories:
    dir_path='\\'.join([dataset_dir, dir])
    files = os.listdir(dir_path)
    images=[file for file in files if ((len(file.split('.')) == 2) and (file.split('.')[1] in ['jpg', 'jpeg', 'png']))] #contains all the images of the person in the current directory
    if len(images)<2:
        continue

    #if a person has less than 2 images he will not be part of any set
    #if a person has 2 images all the images will be put in the train set and test set
    #if a person has 3 images 1 image will be in the train, validation and test set
    #if a person has more than 3 50 % of the images will be in the train set and 25% and 25% will be in the validation and test set
    for image_name in images:
        cur_image=ImageInSet('\\'.join([dir_path, image_name]))
        face=cur_image.get_face_image()
        if (face is None) or (isinstance(face, type(None))):
            images.remove(image_name)
            print(' '.join(["No face detected for",cur_image.name]))
            no_faces_detected.append(cur_image.path)

    cur_train_set= list()
    cur_test_set = list()
    cur_validation_set = list()

    if len(images)<2:
        continue
    if len(images)==2:
        cur_train_set.append(ImageInSet('\\'.join([dir_path, images[0]])))
        cur_test_set.append(ImageInSet('\\'.join([dir_path, images[1]])))
    elif len(images)==3:
        cur_train_set.append(ImageInSet('\\'.join([dir_path, images[0]])))
        cur_validation_set.append(ImageInSet('\\'.join([dir_path, images[1]])))
        cur_test_set.append(ImageInSet('\\'.join([dir_path, images[2]])))
    elif len(images)>3:
        cur_train_set=[ImageInSet('\\'.join([dir_path, cur_image])) for cur_image in images[:math.ceil(len(images)*0.5)]]
        cur_validation_set=[ImageInSet('\\'.join([dir_path, cur_image])) for cur_image in images[math.ceil(len(images)*0.5):round(len(images)*0.75)]]
        cur_test_set=[ImageInSet('\\'.join([dir_path, cur_image])) for cur_image in images[round(len(images) * 0.75):]]

    #every image that goes to the train set generates 5 new augmentad images
    #the image face locations are saved by the method get_face_image
    number_of_train_images_per_person = 100
    number_of_augmentations_per_train_image = round((number_of_train_images_per_person - len(cur_train_set)) / len(cur_train_set))
    for new_train_image in cur_train_set:
        new_train_dir='\\'.join([train_dir, dir])
        if not os.path.isdir(new_train_dir):
            os.mkdir(new_train_dir)
        new_face_image = new_train_image.get_face_image()
        try:
            new_face_image.resize_image()
        except (cv.error, AttributeError) as e:
            continue
        new_path='\\'.join([train_dir, dir, new_train_image.file_name])
        old_path=new_train_image.path
        new_face_image.save(new_path)
        train_paths.append((new_path, old_path))

        image_for_aug = new_train_image.values.reshape((1,) + new_train_image.values.shape)

        i=0
        #for batch in datagen.flow(image_for_aug, batch_size=1, save_to_dir=new_train_dir, save_prefix='aug', save_format='jpg'):
         #   i += 1
          #  if i>= number_of_augmentations_per_train_image:
           #     break

    for new_validation_image in cur_validation_set:
        new_validation_dir='\\'.join([validation_dir, dir])
        if not os.path.isdir(new_validation_dir):
            os.mkdir(new_validation_dir)
        new_face_image = new_validation_image.get_face_image()
        try:
            new_face_image.resize_image()
        except (cv.error, AttributeError) as e:
            continue
        new_path='\\'.join([validation_dir, dir, new_validation_image.file_name])
        old_path=new_validation_image.path
        new_face_image.save(new_path)
        validation_paths.append((new_path, old_path))

    for new_test_image in cur_test_set:
        new_test_dir='\\'.join([test_dir, dir])
        if not os.path.isdir(new_test_dir):
            os.mkdir(new_test_dir)
        new_face_image = new_test_image.get_face_image()
        try:
            new_face_image.resize_image()
        except (cv.error,AttributeError) as e:
            continue
        new_path='\\'.join([test_dir, dir, new_test_image.file_name])
        old_path=new_test_image.path
        new_face_image.save(new_path)
        test_paths.append((new_path, old_path))


train_directories = [dir for dir in os.listdir(train_dir) if not '.' in dir]
for dir in train_directories:
    dir_path='\\'.join([train_dir, dir])
    files = os.listdir(dir_path)
    augmentation_images=[file for file in files if ((len(file.split('.'))==2) and (file.split('.')[1] in ['jpg', 'jpeg', 'png']) and file.split('_')[0]=='aug') ]

    for cur_image in augmentation_images:
        new_augmentation_image=ImageInSet('\\'.join([dir_path, cur_image]))
        new_face_image=new_augmentation_image.get_face_image()
        os.remove(new_augmentation_image.path)
        if (new_face_image is None) or (isinstance(new_face_image, type(None))) or\
        (new_face_image.values.shape[0]<80) or (new_face_image.values.shape[1]<80) or\
        (abs(new_face_image.values.shape[0]-new_face_image.values.shape[1])>80):
            continue
        else:
            try:
                new_face_image.resize_image()
            except (cv.error, AttributeError) as e:
                continue
            new_face_image.save(new_augmentation_image.path)
            augmentation_paths.append(new_face_image.path)


no_faces_detected_in_dataset_dir='\\'.join([dataset_dir, 'no faces detected in dataset'])
if not os.path.isdir(no_faces_detected_in_dataset_dir):
    os.mkdir(no_faces_detected_in_dataset_dir)
for image_path in no_faces_detected:
    no_face_image=ImageInSet(image_path)
    no_face_image.save('\\'.join([no_faces_detected_in_dataset_dir, no_face_image.file_name]))


os.chdir(root_dir)

pickle.dump(train_paths,open("train_paths.pkl","wb"))
pickle.dump(augmentation_paths,open("augmentation_paths.pkl","wb"))
pickle.dump(validation_paths,open("validation_paths.pkl","wb"))
pickle.dump(test_paths,open("test_paths.pkl","wb"))

pickle.dump(dataset_dir,open("dataset_dir.pkl","wb"))
pickle.dump(root_dir,open("root_dir.pkl","wb"))