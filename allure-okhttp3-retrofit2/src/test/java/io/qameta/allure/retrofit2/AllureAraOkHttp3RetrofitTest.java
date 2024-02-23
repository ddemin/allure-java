package io.qameta.allure.retrofit2;

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
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.qameta.allure.test.RunUtils.runWithinTestContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class AllureAraOkHttp3RetrofitTest {

    private static final String BODY_STRING = "Hello world!";

    private WireMockServer server;
    private Retrofit retrofit;


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

        retrofit = new Retrofit
                .Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl(server.baseUrl())
                .client(
                        new OkHttpClient.Builder()
                                .addInterceptor(new AllureAraOkHttp3Retrofit())
                                .build()
                )
                .validateEagerly(false)
                .build();
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

        final IMultipartPost client = retrofit.create(IMultipartPost.class);
        final retrofit2.Call<Object> call = client.sendMultipart(
                MultipartBody.Part.create(RequestBody.create(MediaType.parse("application/octet-stream"), tempFile)),
                Collections.singletonMap("hello", "world")
        );

        final AllureResults results = execute(call);

        assertThat(results.getTestResults())
                .flatExtracting(TestResult::getAttachments)
                .extracting(Attachment::getName)
                .contains("Request");
    }

//    @Test
//    void shouldCreateResponseAttachment() {
//        final Request request = new Request.Builder()
//                .url(server.url("hello"))
//                .build();
//
//        final AllureResults results = execute(request, checkBody(BODY_STRING));
//
//        assertThat(results.getTestResults())
//                .flatExtracting(TestResult::getAttachments)
//                .extracting(Attachment::getName)
//                .contains("Response");
//    }

    protected final AllureResults execute(final retrofit2.Call<Object> call) {
        return runWithinTestContext(() -> {
            try {
                call.execute();
            } catch (IOException e) {
                throw new RuntimeException("Could not execute request " + call, e);
            }
        });
    }

}
