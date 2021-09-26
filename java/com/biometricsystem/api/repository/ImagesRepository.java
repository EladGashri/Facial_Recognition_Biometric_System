package com.biometricsystem.api.repository;
import com.biometricsystem.database.Database;
import com.biometricsystem.entity.image.ImageFromDatabase;
import com.biometricsystem.entity.image.ImageForIdentification;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.Iterator;
import java.util.NoSuchElementException;


@Repository
public class ImagesRepository {

    @Autowired
    private Database database;

    public long countEmployeeImagesFromDatabase(long employeeId) {
        return database.getImagesCollection().countDocuments(new Document("employee id", employeeId));
    }

    public ImageFromDatabase[] getEmployeeImagesFromDatabaseAsArray(long employeeId) {
        int numberOfImages = (int) countEmployeeImagesFromDatabase(employeeId);
        ImageFromDatabase[] images = new ImageFromDatabase[numberOfImages];
        Iterator<Document> iterator = database.getImagesCollection().find(new Document("employee id", employeeId)).iterator();
        for (int i = 0; i < numberOfImages; i++) {
            images[i] = new ImageFromDatabase(iterator.next());
        }
        return images;
    }

    public boolean saveImageToDatabase(ImageForIdentification image) {
        return database.insertImageDocument(image);
    }


    public boolean DeleteImageFromDatabase(String imageId, Long employeeId) {
        return database.getImagesCollection().deleteOne(new Document("_id", imageId).append("employee id", employeeId)).wasAcknowledged();
    }

    public Iterator<Document> getEmployeeImagesFromDatabase(long employeeId) {
        return database.getImagesCollection().find(new Document("employee id", employeeId)).iterator();
    }

    public ImageFromDatabase getImage(long employeeId, String imageId) {
        try {
            Iterator<Document> iterator = database.getImagesCollection().find(new Document("_id", imageId).append("employee id", employeeId)).iterator();
            return new ImageFromDatabase(iterator.next());
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return null;
        }
    }

}