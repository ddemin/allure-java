package io.qameta.allure.ara.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.ara.ARAAttacher;
import io.swagger.v3.oas.models.OpenAPI;

import static java.nio.charset.StandardCharsets.UTF_8;

public class OpenApiARAAttacher extends ARAAttacher {

    public void attachOpenApi(
            final String name,
            final OpenAPI openAPI
            ) throws JsonProcessingException {
        getLifecycle().addAttachment(
                name,
                DEFAULT_MIME,
                DEFAULT_FILE_EXT,
                getObjectWriter().writeValueAsString(openAPI).getBytes(UTF_8)
        );
    }

}
