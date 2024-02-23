package io.qameta.allure.ara;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.ara.openapi.OpenApiActionDto;

import static com.fasterxml.jackson.databind.MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ARAAttacher {

    protected static final String DEFAULT_MIME = "text/yaml";
    protected static final String DEFAULT_FILE_EXT = ".yaml";

    private final AllureLifecycle lifecycle;
    private final ObjectWriter objectWriter;

    public ARAAttacher() {
        this.lifecycle = Allure.getLifecycle();
        final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory())
                .configure(USE_WRAPPER_NAME_AS_PROPERTY_NAME, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
    }

    public AllureLifecycle getLifecycle() {
        return lifecycle;
    }

    public ObjectWriter getObjectWriter() {
        return objectWriter;
    }

    public void attachAra(
            final String name,
            final ARAttachmentDto<OpenApiActionDto> arAttachmentDto
    ) throws JsonProcessingException {
        lifecycle.addAttachment(
                name,
                DEFAULT_MIME,
                DEFAULT_FILE_EXT,
                objectWriter.writeValueAsString(arAttachmentDto).getBytes(UTF_8)
        );
    }

}
