The Facial Recognition Biometric System project is a final project in the "High-Tech Sciences" B.Sc. degree in Tel Aviv University's Faculty of Engineering. It was developed in collaboration with Amdocs.

In this project we designed and implemented a biometric system that uses a facial recognition algorithm. The algorithm was developed by us from scratch with Python and Pytorch.

In the preprocessing, the system found the pictures and names of the people it should train on and detect.
The DataFrame that has been built in the preprocessing is used to create the database in MongoDB.
After that, we have a reliable database, and the system queries the people it could train on and divides it into two sets (train and validation).
The two sets are moved into a pre-trained artificial neural network (ANN) that was accommodated according to the dataset's needs.

The system uses few methods to avoid overfitting, like augmentation and a weighted random sampler.
The system also uses some optimization methods like "ReduceLROnPlateau."
After the learning process, the system updates a score (probability) for each person in the database. That's the way that the user of the system can keep track of the system performance.
 
The system was designed to be a real-time attendance system for a large corporation.
The system interacts with several cameras in different locations, where employees should come to be identified. If an employee has been identified by the facial recognition algorithm, his attendance will be registered at the database. Otherwise, the employee could be identified manually using his personal information.

The facial identification takes place in one location.  All the cameras interact with that location using the UDP protocol. If a face has been detected by the camera, the image is sent to the facial identification location. The Facial recognition algorithm is activated and the result is sent back to the camera. The Live Feed was implemented using multithreading in Java.

One of the main purposes of this system is to function as an attendance system. The database holds details about the employees, their attendance and their images.

The system can be interacted with by sending HTTP requests to its REST API. A standard employee can only receive information about himself. An admin can receive information about all the employees. The CTO can also use the facial recognition algorithm to identify an image and to retrain the algorithm. The REST API was implemented with Java, Spring Boot and Spring Security.


A full documentation of the Facial Recognition Biometric System REST API with detailed examples for every endpoint can be found at the following Postman collection document:
https://documenter.getpostman.com/view/14799541/UUy1emcF
