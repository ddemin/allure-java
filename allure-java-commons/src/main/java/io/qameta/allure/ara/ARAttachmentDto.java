package io.qameta.allure.ara;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.qameta.allure.ara.openapi.OpenApiActionDto;

public class ARAttachmentDto<T extends AbstractActionDto> {

    private final String type = "ARA";

    private T action;
    private AbstractReactionDto reaction;

    public ARAttachmentDto() {
    }

    public ARAttachmentDto(T action, AbstractReactionDto reaction) {
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

    public AbstractReactionDto getReaction() {
        return reaction;
    }

    public void setReaction(AbstractReactionDto reaction) {
        this.reaction = reaction;
    }
}
