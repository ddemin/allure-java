package io.qameta.allure.restassured;

import io.qameta.allure.restassured.ara.ExampleRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.internal.multipart.MultiPartSpecificationImpl;
import io.restassured.specification.MultiPartSpecification;
import org.apache.commons.io.FileUtils;
import org.apache.groovy.util.Maps;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static io.restassured.RestAssured.given;

public class AllureAraRestAssuredTest {

    @Test
    void testAraPostJson() throws IOException {
        RestAssured.replaceFiltersWith(new AllureAraRestAssured());

        given()
                .baseUri("https://reqbin.com")
                .basePath("/echo/post/json")
                .body(
                        new ExampleRequest(
                                1,
                                "test 123",
                                1234,
                                1.1234f
                        )
                )
                .contentType(ContentType.JSON)
                .when()
                .post()
                .then().statusCode(200);
    }


    @Test
    void testAraPostMultipart() throws IOException {
        RestAssured.replaceFiltersWith(new AllureAraRestAssured());

        File tempFile = Files.newTemporaryFile();
        FileUtils.writeLines(tempFile, Collections.singletonList("Hello world!"));

        given()
                .baseUri("https://reqbin.com")
                .basePath("/echo/post/json")
                .multiPart(
                        "part1",
                        new ExampleRequest(
                                1,
                                "test 123",
                                1234,
                                1.1234f
                        ),
                        "application/json"
                )
                .multiPart(
                        "part2",
                        new ExampleRequest(
                                1,
                                "test 123",
                                1234,
                                1.1234f
                        )
                )
                .multiPart(tempFile)
                .contentType(ContentType.MULTIPART)
                .when()
                .post()
                .then().statusCode(200);
    }
}
