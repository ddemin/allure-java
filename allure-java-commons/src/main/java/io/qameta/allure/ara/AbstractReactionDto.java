package io.qameta.allure.ara;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.qameta.allure.ara.openapi.OpenApiReactionDto;

import static io.qameta.allure.ara.AbstractActionDto.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OpenApiReactionDto.class, name = TYPE_OPENAPI),
        @JsonSubTypes.Type(value = ExceptionReactionDto.class, name = TYPE_EXCEPTION),
})
public abstract class AbstractReactionDto {

    private String type;

    public AbstractReactionDto(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
