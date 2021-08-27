from ImageInSet import *
import pandas as pd
import os
import pickle
from tqdm import tqdm


def make_df(max_pics=10): # make only df and insert to mongo -> check for valid -> print not the valid -> split to trn/val -> train the valids
    root_dir=os.getcwd()
    directory_name = '../resources/static/images/dataset'
    #dataset_dir = os.path.join(root_dir, input("please enter the dataset directory path")) \
     #       if directory_name is None else os.path.join(root_dir, directory_name)
    dataset_dir = root_dir + directory_name
    df = pd.DataFrame(columns=['path','name'])
    directories = [dir for dir in os.listdir(dataset_dir) if not '.' in dir]
    loop = tqdm(directories, position=0,leave=True)
    for dir in loop:
        dir_path = os.path.join(dataset_dir, dir)
        files = os.listdir(dir_path)
        images_paths = [os.path.join(dir_path, file) for file in files if ((len(file.split('.')) == 2) and (file.split('.')[1] in ['jpg', 'jpeg', 'png']))]
        for img in images_paths:
            df = df.append({'path': img,'name':" ".join(dir.split("_"))}, ignore_index=True)

    df = df.groupby(by='name',as_index=False).agg({'path': lambda x: list(x)})
    dict_cls2name = dict(zip([i for i in range(len(df))], df['name']))

    valid, indexes, pic_num = list(), list(), list()
    loop = tqdm(df['path'], position=0, leave=True)
    for index, paths in enumerate(loop):
        valid_pics, image_indexes = list(), list()
        for path in paths:
            if len(valid_pics) == max_pics:
                break
            image_object = ImageInSet(path)
            image_face_indexes = image_object.get_face_indexes()
            if (not image_face_indexes is None) and (not isinstance(image_face_indexes, type(None))):
                valid_pics.append(path)
                image_indexes.append(image_face_indexes)
        valid.append(valid_pics)
        indexes.append(image_indexes)
        pic_num.append(len(valid_pics))
    df['valid'] = valid
    df['indexes'] = indexes
    df['pic_num'] = pic_num
    return df.copy(),dict_cls2name


if __name__ == "__main__":

    df,cls2name = make_df(6)
    df.to_pickle("database\\db_df.pkl")
    pickle.dump(cls2name,open("id_to_name_dict.pkl","wb"))