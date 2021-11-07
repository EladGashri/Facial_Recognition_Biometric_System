The Facial Recognition Biometric System is a final project in Tel Aviv University's Faculty of Engineering. It was developed in collaboration with Amdocs.

In this project we developed a biometric system that uses a facial recognition algorithm. This algorithm was developed by us with Python and Pytorch. . 

As a dataset we chose "Labeled Faces in the Wild" which includes 13,233 images of 5,749 people. The people in the dataset represent the employees of the company.

In the preprocessing stage, the algorithm found the images and names of the people it should train on according to predetermined criteria. This resulted in a DataFrame data structure of the people and images. 

The DataFrame created in the preprocessing stage is used to create the database in MongoDB. The database is a NoSQL document oriented database. It holds details about the employees, their attendance and their images.

After the development of the database, the system queries the people it could train on and divides the images into two sets: a train set and a validation set.
The two sets are moved into a pre-trained artificial neural network (ANN) that was accommodated according to the dataset's needs.

The system uses few methods to avoid overfitting, like augmentation and a weighted random sampler.
The system also uses some optimization methods like "ReduceLROnPlateau".
After the training process is complete, the system updates a score (probability) for each person in the database. The score is the main method for keeping track of the system's recognition performance.

The system was designed to be a real-time attendance system for a large corporation. The system interacts with several cameras in different locations. Each location is called a "Camera stand". In those camera stands employees should come to be identified. If an employee has been identified by the facial recognition algorithm, and his attendance has yet to be registered today, his attendance is registered to the database. If the algorithm failed to identify the employee he could identify manually using his personal information. 

The facial recognition process takes place in a single location: The "Facial recognition algorithm stand". There can be many camera stands, separate from the single facial recognition algorithm stand. All the camera stands interact with the algorithm stand using the UDP protocol. If a face has been detected by the camera, the image is sent to the algorithm stand. The Facial recognition algorithm is activated and the result is sent back to the camera stand. The camera stand has a GUI that displays the result sent to him. 
The Live Feed was implemented using multithreading in Java.

The system can be interacted with by sending HTTP requests to its REST API. The REST API allows performing CRUD operations regarding the 3 collection it contains: employees, attendance and images. HTTP requests to the REST API can generate attendance reports of the employees, which is an important component of the system as an attendance system.

The system contains 3 types of employees: standard employees, admins and a CTO. A standard employee can only receive information about himself. An admin can receive information about all the employees. The CTO has all the privileges of an admin, plus the ability to use the facial recognition algorithm to identify an image and to train the algorithm. The REST API was developed with Java, Spring Boot and Spring Security.

A full documentation of the Facial Recognition Biometric System REST API with detailed examples for every endpoint can be found at the following Postman collection document:
https://documenter.getpostman.com/view/14799541/UUy1emcF
