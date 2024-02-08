package io.qameta.allure.ara;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.qameta.allure.ara.http.OpenApiReactionDto;

import static io.qameta.allure.ara.IActionDto.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OpenApiReactionDto.class, name = TYPE_OPENAPI),
        @JsonSubTypes.Type(value = ExceptionReactionDto.class, name = TYPE_EXCEPTION),
})
public interface IReactionDto {
}
