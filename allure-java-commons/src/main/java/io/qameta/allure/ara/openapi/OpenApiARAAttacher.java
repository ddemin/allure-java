package io.qameta.allure.ara.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.FileSystemResultsWriter;
import io.qameta.allure.ara.ARAAttacher;
import io.qameta.allure.util.PropertiesUtils;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class OpenApiARAAttacher extends ARAAttacher {

    private final FileSystemResultsWriter fileSystemResultsWriter;

    public OpenApiARAAttacher() {
        final Properties properties = PropertiesUtils.loadAllureProperties();
        final String path = properties.getProperty("allure.results.directory", "allure-results");
        this.fileSystemResultsWriter = new FileSystemResultsWriter(Paths.get(path, "openapi"));
    }

    public void attachOpenApi(
            final String name,
            final OpenAPI openAPI
    ) throws JsonProcessingException {
        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            getObjectWriter().writeValue(os, openAPI);
            try (final ByteArrayInputStream in = new ByteArrayInputStream(os.toByteArray())) {
                this.fileSystemResultsWriter.write(name, in);
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
