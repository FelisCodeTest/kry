package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import se.kry.codetest.model.Service;

import java.util.Map;

public class BackgroundPoller {

  Vertx vertx;

  BackgroundPoller(Vertx vertx){
    this.vertx = vertx;
  }

  public void pollServices(Map<String, Service> services) {
    for(Service service : services.values()){
        if (service.getHost() != null){
            WebClient.create(vertx)
                    .get(service.getHost(), "/")
                    .send(poll -> {
                        if (poll.succeeded()){
                            service.setLastStatus(Service.Status.ONLINE);
                        }else{
                            service.setLastStatus(Service.Status.OFFLINE);
                        }
                    });
        }else
            service.setLastStatus(Service.Status.WRONG_URL);
    }
  }
}
