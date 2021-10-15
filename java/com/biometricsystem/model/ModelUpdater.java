package com.biometricsystem.model;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import java.io.IOException;
import java.util.TimerTask;

@Component
public class ModelUpdater extends TimerTask {

    @Autowired
    private ModelUpdaterRequestSender modelUpdaterRequestSender;


    @Override
    public void run() {
        try {
            CloseableHttpResponse response= modelUpdaterRequestSender.sendRequest();
            checkResponse(response);
        } catch (IOException|InterruptedException|HttpServerErrorException e) {
            System.err.println("Could not train model");
        }
    }

    private void checkResponse(CloseableHttpResponse response) throws HttpServerErrorException{
        if (response.getStatusLine().getStatusCode()==HttpStatus.OK.value()){
            throw new HttpServerErrorException(HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
        }
    }

}
