package io.qameta.allure.ara;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.qameta.allure.ara.openapi.OpenApiActionDto;

public class ARAttachmentDto<T extends AbstractActionDto, V extends AbstractReactionDto> {

    private final String type = "ARA";

    private T action;
    private V reaction;

    public ARAttachmentDto() {
    }

    public ARAttachmentDto(T action, V reaction) {
        this.action = action;
        this.reaction = reaction;
    }

    public String getType() {
        return type;
    }

    public T getAction() {
        return action;
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = OpenApiActionDto.class),
            @JsonSubTypes.Type(value = ExceptionActionDto.class),
    })
    public void setAction(T action) {
        this.action = action;
    }

    public V getReaction() {
        return reaction;
    }

    public void setReaction(V reaction) {
        this.reaction = reaction;
    }
}
