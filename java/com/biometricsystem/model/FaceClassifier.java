package com.biometricsystem.model;
import ai.djl.*;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.translate.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import ai.djl.modality.cv.Image.Interpolation;
import com.biometricsystem.api.repository.EmployeesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class FaceClassifier {

    @Autowired
    private EmployeesRepository employeesRepository;
    private Path modelPath=Paths.get(System.getProperty("user.home")+"\\IdeaProjects\\FacialRecognitionBiometricSystem\\src\\main\\resources\\models\\model4.zip");
    private Model model=Model.newInstance("cnn");
    private Predictor<Image, Classifications> predictor;

    public FaceClassifier(EmployeesRepository employeesRepository) {
        this.employeesRepository=employeesRepository;
        loadModel();
        List<String> classes = IntStream.range(0, employeesRepository.getNumberOfEmployees()).mapToObj(String::valueOf).collect(Collectors.toList());
        Pipeline pipeline = new Pipeline().
                                add(new Resize(160, 160, Interpolation.BILINEAR)).
                                add(new ToTensor()).
                                add(new Normalize(new float[]{0, 0, 0}, new float[]{1, 1, 1}));
        ImageClassificationTranslator translator = ImageClassificationTranslator.builder()
                .setPipeline(pipeline)
                .optSynset(classes)
                .build();
        predictor = model.newPredictor(translator);
    }

    public void loadModel(){
        try {
            model.load(modelPath);
        } catch (IOException | MalformedModelException e) {
            System.err.println("could not load model");
        }
    }

    public void setNewModel(String path){
        modelPath=Paths.get(path);
        loadModel();
    }

    public Classifications predictPerson(Image image) {
        try {
            return predictor.predict(image);
        } catch (TranslateException e) {
            e.printStackTrace();
            return null;
        }
    }

}
