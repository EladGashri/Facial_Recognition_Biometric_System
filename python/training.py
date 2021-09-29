from database import *
import pandas as pd
from tqdm import tqdm

class Training:

    MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL=4

    def __init__(self):
        self.worker_id_to_cls = dict()
        self.len_train = None
        self.train = pd.DataFrame(columns=['path', 'class', 'face_indexes'])
        self.val = pd.DataFrame(columns=['path', 'class', 'face_indexes'])
        self.make_training_sets()
        self.y_pred = list()
        self.y_pred_proba = list()

    def make_training_sets(self):
        for i, v in enumerate(database.employees_collection.find({"number of images": {'$gte': Training.MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL}},
                                                                 {'images directory path': 1, "number of images": 1})):
            self.worker_id_to_cls[i] = (v['_id'], v['images directory path'], v['number of images'])
        self.len_train = len(self.worker_id_to_cls)
        for key in self.worker_id_to_cls.keys():
            database.employees_collection.update_one({'_id': self.worker_id_to_cls[key][0]},
                                                     {"$set": {"model class": key}})

        loop = tqdm(range(self.len_train), position=0, leave=True)
        for i in loop:
            id_num = self.worker_id_to_cls[i][0]
            cur = database.images_collection.find({"employee id": id_num}, {'face indexes': 1})

            self.val = self.val.append(
                {'path': self.worker_id_to_cls[i][1] + '/' + cur[0]['_id'],'class': i,
                 'face_indexes': cur[0]['face indexes']}, ignore_index=True)

            for j in range(1,self.worker_id_to_cls[i][2]):
                self.train = self.train.append(
                    {'path': self.worker_id_to_cls[i][1] + '/' + cur[j]['_id'], 'class': i,
                     'face_indexes': cur[j]['face indexes']}, ignore_index=True)

        #self.train = self.train.sample(frac=1).reset_index(drop=True)

    def update_database(self):
        for i, v in enumerate(self.y_pred):
            if v == self.val['class'][i]:
                database.employees_collection.update_one({"model class": self.val['class'][i]},
                                                         {"$set": {"model accuracy": self.y_pred_proba[i]}})

    def check_accuracy(self, threshold):
        tp, fp, fn, tn = 0, 0, 0, 0
        for i, v in enumerate(self.y_pred):
            if self.y_pred_proba[i] >= threshold:
                if v == self.val['class'][i]:
                    tp += 1
                else:
                    fp += 1
            else:
                if v == self.val['class'][i]:
                    fn += 1
                else:
                    tn += 1
        print(
            f"when threshold: {threshold} : Accuracy: {(tp + tn) / len(self.y_pred):0.3f}, Precision: {tp / (tp + fp):0.3f}, Recall: {tp / (tp + fn):0.3f}")
        print(f"tp: {tp}, fp: {fp}, fn: {fn}, tn: {tn}")
        # print(f"cls: {i}, pred: {v}, true: {y_true[i]}, proba: {y_proba[i]}, pred name: {dict_cls2name[v]}, true name: {dict_cls2name[i]}")


