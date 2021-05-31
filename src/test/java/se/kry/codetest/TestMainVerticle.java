package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  @DisplayName("Start a web server on localhost responding to path /service on port 8080")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void start_http_server(Vertx vertx, VertxTestContext testContext) {
      WebClient.create(vertx)
        .get(8080, "::1", "/service")
        .send(response -> testContext.verify(() -> {
          assertEquals(200, response.result().statusCode());
          testContext.completeNow();
        }));
  }

    @Test
    @DisplayName("Try to add a service")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void add_service(Vertx vertx, VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject();
        String randomName = RandomStringUtils.randomAlphabetic(10);
        requestBody.put("name", randomName);
        requestBody.put("url", "https://www.kry.se");
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, response ->testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    String body = response.result().bodyAsString();
                    assertEquals("OK", body);
                    testContext.completeNow();
                }));
    }


    @Test
    @DisplayName("Try to add two services with same name one after the other")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void add_two_service_same_name(Vertx vertx, VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject();
        String randomName = RandomStringUtils.randomAlphabetic(10);
        requestBody.put("name", randomName);
        requestBody.put("url", "https://www.kry.se");
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, response ->testContext.verify(() -> {
                    WebClient.create(vertx)
                            .post(8080, "::1", "/service")
                            .sendJsonObject(requestBody, innerResponse ->testContext.verify(() -> {
                                assertEquals(200, innerResponse.result().statusCode());
                                String body = innerResponse.result().bodyAsString();
                                assertEquals("KO", body);
                                testContext.completeNow();
                            }));
                }));
    }

    @Test
    @DisplayName("validate required field in GET /service")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_validate_fields(Vertx vertx, VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject();
        String randomName = RandomStringUtils.randomAlphabetic(10);
        requestBody.put("name", randomName);
        requestBody.put("url", "https://www.kry.se");
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, postResponse ->testContext.verify(() -> {
                    WebClient.create(vertx)
                            .get(8080, "::1", "/service")
                            .send(getResponse -> testContext.verify(() -> {
                                assertEquals(200, getResponse.result().statusCode());
                                JsonArray body = getResponse.result().bodyAsJsonArray();
                                JsonObject elem = body.getJsonObject(0);
                                assertNotNull(elem.getString("name"));
                                assertNotNull(elem.getString("url"));
                                assertNotNull(elem.getString("status"));
                                assertNotNull(elem.getString("date"));
                                testContext.completeNow();
                            }));

                }));
    }

    @Test
    @DisplayName("add and delete a service")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void add_delete_service(Vertx vertx, VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject();
        String randomName = RandomStringUtils.randomAlphabetic(10);
        requestBody.put("name", randomName);
        requestBody.put("url", "https://www.kry.se");
        WebClient.create(vertx)
                .get(8080, "::1", "/service")
                .send(initialGetResponse -> testContext.verify(() -> {
                    assertEquals(200, initialGetResponse.result().statusCode());
                    int serviceCount = initialGetResponse.result().bodyAsJsonArray().size();
                    WebClient.create(vertx)
                            .post(8080, "::1", "/service")
                            .sendJsonObject(requestBody, postResponse ->testContext.verify(() -> {
                                WebClient.create(vertx)
                                        .delete(8080, "::1", "/service")
                                        .sendJsonObject(requestBody, deleteResponse -> testContext.verify(() -> {
                                            assertEquals(200, deleteResponse.result().statusCode());
                                            assertEquals("OK", deleteResponse.result().bodyAsString());
                                            WebClient.create(vertx)
                                                    .get(8080, "::1", "/service")
                                                    .send(getResponse -> testContext.verify(() -> {
                                                        assertEquals(200, getResponse.result().statusCode());
                                                        JsonArray body = getResponse.result().bodyAsJsonArray();
                                                        assertEquals(serviceCount, body.size());
                                                        testContext.completeNow();
                                                    }));
                                        }));
                            }));
                }));


    }
    @Test
    @DisplayName("delete a nonexisting service")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void delete_not_existing_service(Vertx vertx, VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject();
        String randomName = RandomStringUtils.randomAlphabetic(10);
        requestBody.put("name", randomName);
        WebClient.create(vertx)
                .delete(8080, "::1", "/service")
                .sendJsonObject(requestBody, deleteResponse -> testContext.verify(() -> {
                    assertEquals(200, deleteResponse.result().statusCode());
                    assertEquals("KO", deleteResponse.result().bodyAsString());
                    testContext.completeNow();
                }));

    }
}
