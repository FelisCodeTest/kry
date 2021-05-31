package se.kry.codetest;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.buffer.Buffer;
import java.util.LinkedList;
import java.util.List;
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
        String randomName = "test1_" + RandomStringUtils.randomAlphabetic(10);
        requestBody.put("name", randomName);
        requestBody.put("url", "https://www.kry.se");
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, response ->testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    String body = response.result().bodyAsString();
                    assertEquals("OK", body);
                    WebClient.create(vertx)
                            .delete(8080, "::1", "/service")
                            .sendJsonObject(requestBody,deleteResponse -> testContext.verify(() -> {
                                testContext.completeNow();
                            }));
                }));
    }


    @Test
    @DisplayName("Try to add two services with same name one after the other")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void add_two_service_same_name(Vertx vertx, VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject();
        String randomName = "test2_" + RandomStringUtils.randomAlphabetic(10);
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
                                WebClient.create(vertx)
                                        .delete(8080, "::1", "/service")
                                        .sendJsonObject(requestBody,deleteResponse -> testContext.verify(() -> {
                                            testContext.completeNow();
                                        }));
                            }));
                }));
    }

    @Test
    @DisplayName("validate required field in GET /service")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void get_validate_fields(Vertx vertx, VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject();
        String randomName = "test3_" + RandomStringUtils.randomAlphabetic(10);
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
                                WebClient.create(vertx)
                                        .delete(8080, "::1", "/service")
                                        .sendJsonObject(requestBody,deleteResponse -> testContext.verify(() -> {
                                            testContext.completeNow();
                                        }));
                            }));

                }));
    }

    @Test
    @DisplayName("add and delete a service")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void add_delete_service(Vertx vertx, VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject();
        String randomName = "test4_" + RandomStringUtils.randomAlphabetic(10);
        requestBody.put("name", randomName);
        requestBody.put("url", "https://www.kry.se");
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
                                            for(int ii = 0; ii < body.size(); ii++){
                                                assertEquals(false, (randomName.equals(body.getJsonObject(0).getString("name"))));
                                            }

                                            testContext.completeNow();
                                        }));
                            }));
                }));


    }
    @Test
    @DisplayName("delete a nonexisting service")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void delete_not_existing_service(Vertx vertx, VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject();
        String randomName = "test5_" + RandomStringUtils.randomAlphabetic(10);
        requestBody.put("name", randomName);
        WebClient.create(vertx)
                .delete(8080, "::1", "/service")
                .sendJsonObject(requestBody, deleteResponse -> testContext.verify(() -> {
                    assertEquals(200, deleteResponse.result().statusCode());
                    assertEquals("KO", deleteResponse.result().bodyAsString());
                    testContext.completeNow();
                }));

    }


    @Test
    @DisplayName("Try to add services with unvalid url")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void invalidate_url(Vertx vertx, VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject();

        requestBody.put("name", "test6_" + RandomStringUtils.randomAlphabetic(10));
        requestBody.put("url", "ftp://www.kry.se");
        Future<HttpResponse<Buffer>> f1 = Future.future();
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, f1.completer());

        requestBody.put("name", "test6_" + RandomStringUtils.randomAlphabetic(10));
        requestBody.put("url", "httpt://www.kry.se");
        Future<HttpResponse<Buffer>> f2 = Future.future();
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, f2.completer());


        requestBody.put("name", "test6_" + RandomStringUtils.randomAlphabetic(10));
        requestBody.put("url", "http://www.kry se");
        Future<HttpResponse<Buffer>> f3 = Future.future();
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, f3.completer());


        requestBody.put("name", "test6_" + RandomStringUtils.randomAlphabetic(10));
        requestBody.put("url", "https ://www.kry.se");
        Future<HttpResponse<Buffer>> f4 = Future.future();
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, f4.completer());


        requestBody.put("name", "test6_" + RandomStringUtils.randomAlphabetic(10));
        requestBody.put("url", "https://www kry.se");
        Future<HttpResponse<Buffer>> f5 = Future.future();
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, f5.completer());


        requestBody.put("name", "test6_" + RandomStringUtils.randomAlphabetic(10));
        requestBody.put("url", "https:// www.kry.se");
        Future<HttpResponse<Buffer>> f6 = Future.future();
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, f6.completer());
        CompositeFuture.all(f1, f2, f3, f4, f5, f6).setHandler(result -> {
            assertEquals("KO", f1.result().bodyAsString());
            assertEquals("KO", f2.result().bodyAsString());
            assertEquals("KO", f3.result().bodyAsString());
            assertEquals("KO", f4.result().bodyAsString());
            assertEquals("KO", f5.result().bodyAsString());
            assertEquals("KO", f6.result().bodyAsString());
            testContext.completeNow();
        });
    }

    @Test
    @DisplayName("Try to add services with valid url")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void validate_url(Vertx vertx, VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject();
        List<String> names = new LinkedList<String>();
        for (int i = 0; i <6;i++){
            names.add("test7_" + RandomStringUtils.randomAlphabetic(10));
        }
        requestBody.put("name", names.get(0));
        requestBody.put("url", "http://www.kry.se");
        Future<HttpResponse<Buffer>> f1 = Future.future();
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, f1.completer());

        requestBody.put("name", names.get(1));
        requestBody.put("url", "https://www.kry.se");
        Future<HttpResponse<Buffer>> f2 = Future.future();
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, f2.completer());


        requestBody.put("name", names.get(2));
        requestBody.put("url", "http://kry.se");
        Future<HttpResponse<Buffer>> f3 = Future.future();
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, f3.completer());


        requestBody.put("name", names.get(3));
        requestBody.put("url", "https://kry.se");
        Future<HttpResponse<Buffer>> f4 = Future.future();
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, f4.completer());


        requestBody.put("name", names.get(4));
        requestBody.put("url", "http://www.kry.se/career");
        Future<HttpResponse<Buffer>> f5 = Future.future();
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, f5.completer());


        requestBody.put("name", names.get(5));
        requestBody.put("url", "http://kry.se/career?debug=true");
        Future<HttpResponse<Buffer>> f6 = Future.future();
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(requestBody, f6.completer());
        CompositeFuture.all(f1, f2, f3, f4, f5, f6).setHandler(result -> {
            assertEquals("OK", f1.result().bodyAsString());
            assertEquals("OK", f2.result().bodyAsString());
            assertEquals("OK", f3.result().bodyAsString());
            assertEquals("OK", f4.result().bodyAsString());
            assertEquals("OK", f5.result().bodyAsString());
            assertEquals("OK", f6.result().bodyAsString());


            Future<HttpResponse<Buffer>> d1 = Future.future();
            Future<HttpResponse<Buffer>> d2 = Future.future();
            Future<HttpResponse<Buffer>> d3 = Future.future();
            Future<HttpResponse<Buffer>> d4 = Future.future();
            Future<HttpResponse<Buffer>> d5 = Future.future();
            Future<HttpResponse<Buffer>> d6 = Future.future();
            requestBody.put("name", names.get(0));
            WebClient.create(vertx)
                    .delete(8080, "::1", "/service")
                    .sendJsonObject(requestBody, d1.completer());
            requestBody.put("name", names.get(1));
            WebClient.create(vertx)
                    .delete(8080, "::1", "/service")
                    .sendJsonObject(requestBody, d2.completer());
            requestBody.put("name", names.get(2));
            WebClient.create(vertx)
                    .delete(8080, "::1", "/service")
                    .sendJsonObject(requestBody, d3.completer());
            requestBody.put("name", names.get(3));
            WebClient.create(vertx)
                    .delete(8080, "::1", "/service")
                    .sendJsonObject(requestBody, d4.completer());
            requestBody.put("name", names.get(4));
            WebClient.create(vertx)
                    .delete(8080, "::1", "/service")
                    .sendJsonObject(requestBody, d5.completer());
            requestBody.put("name", names.get(5));
            WebClient.create(vertx)
                    .delete(8080, "::1", "/service")
                    .sendJsonObject(requestBody, d6.completer());
            CompositeFuture.all(d1, d2, d3, d4, d5, d6).setHandler(delete -> {
                testContext.completeNow();
            });
        });



    }
}
