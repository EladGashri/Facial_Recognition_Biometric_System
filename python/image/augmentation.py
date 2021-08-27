#pip install git+https://github.com/albumentations-team/albumentations
import albumentations as A
from albumentations.pytorch import ToTensorV2
import cv2 as cv

train_transforms = A.Compose(
    [A.HorizontalFlip(p=0.3),
     A.RandomBrightnessContrast(p=0.1),
     A.OneOf([A.ShiftScaleRotate(rotate_limit=18, p=1, border_mode=cv.BORDER_CONSTANT),
              A.IAAAffine(shear=10, p=1, mode="constant"),
              #A.Perspective(scale=(0.05, 0.15), keep_size=True, pad_mode=0, pad_val=0,
               #             mask_pad_val=0, fit_output=False, interpolation=1, always_apply=False, p=1),
              ],p=1.0,),
     A.OneOf([
              A.FancyPCA (alpha=0.1, always_apply=False, p=1),
              A.Blur(p=1),
              A.ToGray(p=0.8),
              A.ColorJitter(brightness=0.1, contrast=0.1, saturation=0.1, hue=0.1, p=1),
              A.ChannelDropout((1,1),fill_value=0,always_apply=False,p=1),
              ],p=0.3,),
     A.OneOf([#A.GaussNoise (var_limit=(10.0, 50.0), mean=0, per_channel=True, always_apply=False, p=0.5),
              A.Equalize (mode='cv', by_channels=True, mask=None, mask_params=(), always_apply=False, p=0.8),
              A.MotionBlur(blur_limit=4,p=1),
              ],p=0.1,)
     ])