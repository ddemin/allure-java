package io.qameta.allure.ara;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.qameta.allure.ara.http.OpenApiActionDto;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OpenApiActionDto.class, name = IActionDto.TYPE_OPENAPI),
        @JsonSubTypes.Type(value = ExceptionActionDto.class, name = IActionDto.TYPE_EXCEPTION),
})
public interface IActionDto {

    String TYPE_OPENAPI = "openapi";
    String TYPE_EXCEPTION = "exception";

}
