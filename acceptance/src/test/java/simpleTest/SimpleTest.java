package simpleTest;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.matcher.RestAssuredMatchers;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/*
 * References:
 * http://www.baeldung.com/rest-assured-tutorial
 * http://rest-assured.io
 * https://github.com/rest-assured/rest-assured
 * https://github.com/rest-assured/rest-assured/wiki/GettingStarted
 * https://github.com/rest-assured/rest-assured/wiki/Usage
 * 
 */

public class SimpleTest {

    private static String server;
    private static String rootPath;

    @BeforeAll
    public static void setup() {
        server = System.getenv("ENDPOINT");

        RestAssured.baseURI = server;
        RestAssured.port = 443;
    }

    @Test
    public void whenRequestGet_thenOK(){
        given().
        when().
            get("/Seattle").
        then().
            assertThat().statusCode(200);
    }

    @Test
    public void whenRequestGetWithParams_thenOK(){
        given().
            header("content-type", "application/json").
            header("day", "Thursday").
            header("x-amz-docs-region", "us-west-2").
            param("time", "evening").
        when().
            get("/Seattle").
        then().
            assertThat().statusCode(200);
    }

    @Test
    public void whenRequestPostWithLogs_thenOK(){
        given().log().all().
            contentType("application/json").
            header("day", "Thursday").
            header("x-amz-docs-region", "us-west-2").
            body("{ \"callerName\": \"John\"}").
        when().
            post("/Seattle").
        then().
            assertThat().statusCode(200);
    }

    @Test
    public void whenRequestPostWithRequestLogsWithResponseLogs_thenOK(){
        given().log().all().
            contentType("application/json").
            header("day", "Thursday").
            header("x-amz-docs-region", "us-west-2").
            body("{ \"callerName\": \"John\"}").
        when().
            post("/Seattle?time=evening").
        then().
            log().body().
            assertThat().statusCode(200);
    }

    @Test
    public void whenRequestPost_checkOutput(){
        String message = "Good day, John of Seattle. Happy Thursday!";
        given().
            contentType("application/json").
            header("day", "Thursday").
            header("x-amz-docs-region", "us-west-2").
            body("{ \"callerName\": \"John\"}").
        when().
            post("/Seattle?time=evening").
        then().
            assertThat().body("message", equalTo(message));
    }
}
