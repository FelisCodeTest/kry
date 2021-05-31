package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.commons.lang3.StringUtils;
import se.kry.codetest.model.Service;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

  private HashMap<String, Service> services = new HashMap<>();
  private DBConnector connector;
  private BackgroundPoller poller;

  private final String SELECT_ALL_SERVICES = "SELECT NAME, URL, CREATION_DATE, LAST_STATUS FROM SERVICE";
  private final String INSERT_INTO_SERVICE = "INSERT INTO SERVICE (NAME, URL, CREATION_DATE, LAST_STATUS) values('%s', '%s', '%s', '%s')";
  private final String DELETE_FROM_SERVICE = "DELETE FROM SERVICE WHERE NAME = '%s'";

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx);
    poller = new BackgroundPoller(vertx);
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    //retrieve existing services
    populateServices();

    vertx.setPeriodic(1000 * 10, timerId -> poller.pollServices(services));
    setRoutes(router);
    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8080, result -> {
          if (result.succeeded()) {
            System.out.println("KRY code test service started");
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        });
  }

  private void setRoutes(Router router){
    router.route("/*").handler(StaticHandler.create());
    router.get("/service").handler(req -> {
      List<JsonObject> jsonServices = services
          .entrySet()
          .stream()
          .map(key ->
              new JsonObject()
                  .put("name", key.getValue().getName())
                  .put("url", key.getValue().getUrl())
                  .put("status", key.getValue().getLastStatus())
                  .put("date", key.getValue().getCreationDate())
          )
          .collect(Collectors.toList());
      req.response()
          .putHeader("content-type", "application/json")
          .end(new JsonArray(jsonServices).encode());
    });
    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      Service service = new Service(
              jsonBody.getString("name"),
              jsonBody.getString("url")
      );
      if (service != null
              && StringUtils.isNotEmpty(service.getName())
              && !services.containsKey(service.getName())
      ){
        connector.query(String.format(
                  INSERT_INTO_SERVICE,
                  service.getName(),
                  service.getUrl(),
                  service.getCreationDate(),
                  service.getLastStatus()
          )).setHandler(
                  insertQuery->{
                    if (insertQuery.succeeded()){
                      services.put(service.getName(),service );
                      req.response()
                              .putHeader("content-type", "text/plain")
                              .end("OK");
                    }else{
                      req.response()
                              .putHeader("content-type", "text/plain")
                              .end("KO");
                    }
                  }
          );
       }else{
        req.response()
                .putHeader("content-type", "text/plain")
                .end("KO");
      }
    });

    router.delete("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      String name = jsonBody.getString("name");
      if (StringUtils.isNotEmpty(name) && services.containsKey(name)){
        connector.query(String.format(DELETE_FROM_SERVICE, name)).setHandler(
                deleteQuery -> {
                  if (deleteQuery.succeeded()) {
                    services.remove(name);
                    req.response()
                            .putHeader("content-type", "text/plain")
                            .end("OK");
                  }else{
                    req.response()
                            .putHeader("content-type", "text/plain")
                            .end("KO");
                  }
                }
        );
      }else{
        req.response()
                .putHeader("content-type", "text/plain")
                .end("KO");
      }

    });
  }

  private void populateServices() {
    connector.query(SELECT_ALL_SERVICES).setHandler(
      getQuery ->{
        if (getQuery.succeeded()) {
          // loop through the result set
          ResultSet rs = getQuery.result();
          if (rs != null) {
            List<JsonArray> dbServices = rs.getResults();
            for (JsonArray dbService : dbServices) {
              String name = dbService.getString(0);
              String url = dbService.getString(1);
              String creationDate = dbService.getString(2);
              String lastStatus = dbService.getString(3);
              Service service = new Service(name, url, creationDate, lastStatus);
              services.put(name, service);
            }
          }
        }
      }
    );
  }

}



