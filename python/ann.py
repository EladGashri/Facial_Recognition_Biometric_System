from training import *
from image_in_set import *
from augmentation import *
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader
from torch.utils.data import WeightedRandomSampler
import torch.optim as optim
from torch.nn import functional as F
from tqdm import tqdm
import torch
from PIL import Image


class NewNet(nn.Module):
    def __init__(self, num_classes=1):
        super(NewNet, self).__init__()
        self.num_classes = num_classes
        self.model = InceptionResnetV1(classify=True, pretrained='vggface2', num_classes=self.num_classes)
        self.change_model()
        # Linear, Relu, Batchnorm,Dropout
        self.layers = nn.ModuleList([
            nn.ReLU(),
            # nn.BatchNorm1d(self.num_classes),
            nn.Dropout(0.2),
            nn.Linear(self.num_classes, self.num_classes, bias=True),
            # nn.ReLU(),
            # nn.BatchNorm1d(self.num_classes),
            # nn.Dropout(0.2),
            # nn.Linear(self.num_classes,self.num_classes,bias=True),
        ])


    def forward(self, x):
        x = self.model(x)
        for layer in self.layers:
            x = layer(x)
        return x

    def change_model(self):
        self.model.last_linear.out_features = self.num_classes
        self.model.last_bn.num_features = self.num_classes
        self.model.logits.in_features = self.num_classes


# m = NewNet(len(id_to_name_dict))

class FRBSDataset(Dataset):
    def __init__(self, values,train_set=True):  # train=False
        self.values = values
        self.train_set = train_set
        self.transform = train_transforms
        self.norm = A.Compose([A.Resize(160,160,interpolation=1, always_apply=True, p=1),
                               A.Normalize(mean=[0, 0, 0], std=[1, 1, 1], max_pixel_value=255,),
                               ToTensorV2(),
                               ])

    def __getitem__(self, index): # 0: 'path', 1: 'cls', 2: 'face_indexes', 3 'aug'

        #indexes = self.values[index][2]
        x,y,w,h = np.array(self.values[index][2]).astype(int)
        #print(f"{index}, {indexes}")
        img = np.array(Image.open(self.values[index][0]))
        if x < 0:
            x = 0
        if y < 0:
            y = 0
        if w > img.shape[0]:
            w = img.shape[0]
        if h > img.shape[1]:
            h = img.shape[1]
        img = img[y:h, x:w]
        if self.train_set : #  and self.values[index][3]
            aug = self.transform(image=img)
            img = aug["image"]
            #img = aug_img(img)
        augmentations = self.norm(image=img)
        img = augmentations["image"]

        model_class = torch.tensor(self.values[index][1])

        return img, model_class

    def __len__(self):
        return len(self.values)


class Ann:
    def __init__(self, batch_size=100, epochs=10, lr=1e-3, l2=0.01, _patience=30):

        self.train_data = Training()
        self.sampler = self.create_sampler()
        self.batch_size = batch_size
        self.epochs = epochs
        self.lr = lr
        self.l2 = l2
        self.use_cuda = torch.cuda.is_available()
        self.device = torch.device("cuda" if self.use_cuda else "cpu")
        print(self.device)
        self.num_workers = 0
        self.train_dl = None
        self.val_dl = None
        self.init_dls()
        self.model = NewNet(num_classes=self.train_data.len_train).to(self.device)
        self.optimizer = self.optimizer()
        self.training_loss = 0.0
        self.val_loss = 0.0
        self.all_training_loss = list()
        self.all_val_loss = list()
        self.scheduler = optim.lr_scheduler.ReduceLROnPlateau(self.optimizer, 'min', verbose=True, patience=7,
                                                              factor=0.1, threshold=0.0001)
        # self.lr_scheduler = torch.optim.lr_scheduler.StepLR(self.optimizer,step_size=100,gamma=0.09)

        self._patience = _patience

    def create_sampler(self):
        class_weights = self.train_data.train['class'].value_counts().to_dict()
        for k,v in class_weights.items():
            class_weights[k] = 1. / torch.tensor(v, dtype=torch.float)
        sample_weights = [0] * len(self.train_data.train)
        for idx, label in enumerate(self.train_data.train['class']):
            sample_weights[idx] = class_weights[label]

        return WeightedRandomSampler(sample_weights,
                                     num_samples=len(sample_weights),
                                     replacement=True)

    def save_model(self, filename="drive/MyDrive/amd/ann_model.pth.tar"):
        torch.save(self.model.state_dict(), filename)

    def load_model(self, filename="drive/MyDrive/amd/ann_model.pth.tar"):
        self.model.load_state_dict(torch.load(filename))

    def save_checkpoint(self, filename="drive/MyDrive/amd/ann_model.pth.tar"):
        checkpoint = {"state_dict": self.model.state_dict(),
                      "optimizer": self.optimizer.state_dict()}
        torch.save(checkpoint, filename)

    def load_checkpoint(self, checkpoint_file="drive/MyDrive/amd/ann_model.pth.tar", ):
        checkpoint = torch.load(checkpoint_file, map_location=self.device)
        self.model.load_state_dict(checkpoint["state_dict"])
        self.optimizer.load_state_dict(checkpoint["optimizer"])
        for param_group in self.optimizer.param_groups:
            param_group["lr"] = self.lr

    def optimizer(self):
        # return optim.SGD(self.model.parameters(), lr=self.lr, weight_decay=self.l2,momentum=0.9) #weight_decay=self.l2,
        return optim.Adam(self.model.parameters(), lr=self.lr, weight_decay=self.l2)

    def init_dls(self):
        train_dataset = FRBSDataset(values=self.train_data.train.values)
        self.train_dl = DataLoader(train_dataset, batch_size=self.batch_size, num_workers=self.num_workers,
                                   pin_memory=self.use_cuda, sampler=self.sampler)#shuffle=True)
        val_dataset = FRBSDataset(values=self.train_data.val.values, train_set=False)
        self.val_dl = DataLoader(val_dataset, batch_size=self.batch_size, shuffle=False, num_workers=self.num_workers,
                                 pin_memory=self.use_cuda)

    def main(self, decay_learning=False):
        print('Start training')
        for epoch_ndx in range(self.epochs):
            print(f"\n epoch: {epoch_ndx}")
            if decay_learning:
                self.lr_schedule(epoch_ndx)
            trn_loss = self.training(epoch_ndx, self.train_dl)
            self.all_training_loss.append(trn_loss.detach() / len(self.train_dl))
            val_loss = self.validation(epoch_ndx, self.val_dl)
            #self.all_val_loss.append(val_loss.detach() / len(self.val_dl))

            # early stopping

            if epoch_ndx == 0 or val_loss < track_val_loss:
                track_val_loss =val_loss
                val_counter = 0
                self.save_model()
            else:
                val_counter += 1
            #self.all_val_loss.append(val_loss.detach() / len(self.val_dl))

            if val_counter == self._patience:
                print("Breaking due to early stopping, best val loss weights were saved to model")
                break
        self.load_model()

        self.train_data.update_database()
        print("Finished")

    def training(self, epoch_ndx, train_dl):
        self.model.train()
        self.training_loss = 0.0
        loop = tqdm(train_dl, position=0, leave=True)
        for batch_ndx, batch_tup in enumerate(loop, 0):
            self.optimizer.zero_grad()
            loss, _, _ = self.compute_batch_loss(batch_ndx, batch_tup, train_dl.batch_size)
            self.training_loss += loss
            loss.backward()
            self.optimizer.step()
            loop.set_postfix(loss=loss.item())
        return self.training_loss

    def validation(self, epoch_ndx, val_dl):
        with torch.no_grad():
            self.model.eval()
            self.val_loss = 0.0
            losses = []
            loop = tqdm(val_dl, position=0, leave=True)
            for batch_ndx, batch_tup in enumerate(loop, 0):
                loss, proba, pred = self.compute_batch_loss(batch_ndx, batch_tup, val_dl.batch_size)
                loop.set_postfix(loss=loss.item())
                self.val_loss += loss
                losses.append(loss.item())
                if (epoch_ndx + 1) % self.epochs == 0:
                    for i in pred:
                        self.train_data.y_pred.append(int(i))
                    for i in proba:
                        self.train_data.y_pred_proba.append(round(float(i), 2))
            self.scheduler.step(sum(losses) / len(losses))
        return self.val_loss

    def compute_batch_loss(self, batch_ndx, batch_tup, batch_size):
        input, model_class = batch_tup
        input = input.to(self.device, dtype=torch.float)
        model_class = torch.reshape(model_class, (-1,))
        model_class = model_class.type(torch.LongTensor)
        model_class = model_class.to(self.device)
        logits_g = self.model(input)
        loss_func = nn.CrossEntropyLoss(reduction='none')  # weight=self.class_weights
        loss_g = loss_func(logits_g, model_class)
        return loss_g.mean(), torch.max(F.softmax(logits_g.detach(), dim=1), 1)[0], \
               torch.max(F.softmax(logits_g.detach(), dim=1), 1)[1]
