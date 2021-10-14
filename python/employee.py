from enum import Enum
from random import randint


class EmployeeType(Enum):
    UNDEFINED=0
    STANDARD=1
    ADMIN=2
    CTO=3


class Employee:

    MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL=4
    BRANCHES=["TA", "SF", "TO", "BN"]

    def __init__(self,id,employee_number,name,images_directory_path, type, number_of_images):
        self.id=id
        self.employee_number=employee_number
        self.name=name
        self.images_directory_path=images_directory_path
        self.number_of_images=number_of_images
        self.branch=Employee.get_random_branch()
        self.model_class=-1
        self.employee_type=type.value

    @classmethod
    def get_random_branch(cls):
        return cls.BRANCHES[randint(0,3)]

