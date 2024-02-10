package io.qameta.allure.okhttp3;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.qameta.allure.model.Attachment;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.test.AllureResults;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.qameta.allure.test.RunUtils.runWithinTestContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class AllureAraOkHttp3Test {

    private static final String BODY_STRING = "Hello world!";

    private WireMockServer server;

    @BeforeEach
    void setUp() {
        server = new WireMockServer(options().dynamicPort());
        server.start();
        configureFor(server.port());

        stubFor(get(urlEqualTo("/hello"))
                .willReturn(aResponse()
                        .withBody(BODY_STRING)));

        stubFor(post(urlEqualTo("/hello"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"test\": 123}")));
    }

    @AfterEach
    void tearDown() {
        if (Objects.nonNull(server)) {
            server.stop();
        }
    }

    @Test
    void shouldCreateRequestAttachment() throws IOException {
        File tempFile = Files.newTemporaryFile();
        FileUtils.writeLines(tempFile, Collections.singletonList("Hello world!"));

        MultipartBody body = new MultipartBody.Builder()
                .addPart(RequestBody.create(MediaType.parse("application/octet-stream"), tempFile))
                .addPart(RequestBody.create(MediaType.parse("application/json"), "{}"))
                .build();
        final Request request = new Request.Builder()
                .post(body)
                .url(server.url("hello"))
                .build();

        final AllureResults results = execute(request, checkBody(BODY_STRING));

        assertThat(results.getTestResults())
                .flatExtracting(TestResult::getAttachments)
                .extracting(Attachment::getName)
                .contains("Request");
    }

    @Test
    void shouldCreateResponseAttachment() {
        final Request request = new Request.Builder()
                .url(server.url("hello"))
                .build();

        final AllureResults results = execute(request, checkBody(BODY_STRING));

        assertThat(results.getTestResults())
                .flatExtracting(TestResult::getAttachments)
                .extracting(Attachment::getName)
                .contains("Response");
    }

    @SafeVarargs
    protected final AllureResults execute(final Request request, final Consumer<Response>... matchers) {
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AllureAraOkHttp3())
                .build();

        return runWithinTestContext(() -> {
            try {
                final Response response = client.newCall(request).execute();
                Stream.of(matchers).forEach(matcher -> matcher.accept(response));
            } catch (IOException e) {
                throw new RuntimeException("Could not execute request " + request, e);
            }
        });
    }

    protected Consumer<Response> checkBody(final String expectedBody) {
        return response -> {
            try {
                final ResponseBody body = response.body();
                if (Objects.isNull(body)) {
                    fail("empty response body");
                }
                assertThat(body.string()).isEqualTo(expectedBody);
            } catch (IOException e) {
                fail("could not read response body");
            }
        };
    }
}
