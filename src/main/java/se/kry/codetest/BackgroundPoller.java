package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
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
        if (service.getHost() != null && !service.isPolling()){
            service.setPolling(true);
            WebClient.create(vertx)
                    .get(service.getHost(), "/")
                    .timeout(60 * 1000)
                    .send(poll -> {
                        if (poll.succeeded()){
                            service.setLastStatus(Service.Status.ONLINE);
                            service.setPolling(false);
                        }else{
                            service.setLastStatus(Service.Status.OFFLINE);
                            service.setPolling(false);
                        }
                    });
        }else
            service.setLastStatus(Service.Status.WRONG_URL);
    }
  }
}
