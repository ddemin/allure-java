package io.qameta.allure.ara;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.qameta.allure.ara.openapi.OpenApiActionDto;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OpenApiActionDto.class),
        @JsonSubTypes.Type(value = ExceptionActionDto.class),
})
public abstract class AbstractActionDto {

    public final static String TYPE_OPENAPI = "openapi";
    public final static String TYPE_EXCEPTION = "exception";

    private String type;

    public AbstractActionDto(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

}
