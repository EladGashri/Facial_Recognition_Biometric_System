package com.biometricsystem.model;
import com.biometricsystem.api.repository.JwtRepository;
import com.biometricsystem.main.BiometricSystem;
import com.biometricsystem.security.jwt.JwtUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ModelUpdaterRequestSender {

    @Autowired
    private JwtRepository jwtRepository;
    private final CloseableHttpClient client = HttpClients.createDefault();
    /*private final HttpClient client=HttpClient.newHttpClient();
    private HttpRequest request;*/
    private HttpPost httpPost;
    private final static String MODEL_TRAINING_URI= BiometricSystem.API_HOST+"/model/training";

    public ModelUpdaterRequestSender(JwtRepository jwtRepository){
        this.jwtRepository=jwtRepository;
        httpPost=new HttpPost(MODEL_TRAINING_URI);
        String jwt=jwtRepository.getCtoJwt();
        /*request = HttpRequest.newBuilder().
                POST(HttpRequest.BodyPublishers.ofString(null)).
                header(JwtUtil.HEADER,JwtUtil.TOKEN_PREFIX+jwt).
                uri(URI.create(uri)).
                build();*/
        httpPost.setHeader(JwtUtil.HEADER,JwtUtil.TOKEN_PREFIX+jwt);
    }

    public /*HttpResponse<String>*/ CloseableHttpResponse sendRequest() throws IOException, InterruptedException {
        System.out.println("Sending a request to train the model");
        return client.execute(httpPost);
        /*return client.send(request, HttpResponse.BodyHandlers.ofString());*/
    }

}