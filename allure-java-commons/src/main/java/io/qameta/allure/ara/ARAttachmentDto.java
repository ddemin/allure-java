package io.qameta.allure.ara;

public class ARAttachmentDto {

    private final String type = "ARA";

    private IActionDto action;
    private IReactionDto reaction;

    public ARAttachmentDto() {
    }

    public ARAttachmentDto(IActionDto action, IReactionDto reaction) {
        this.action = action;
        this.reaction = reaction;
    }

    public String getType() {
        return type;
    }

    public IActionDto getAction() {
        return action;
    }

    public void setAction(IActionDto action) {
        this.action = action;
    }

    public IReactionDto getReaction() {
        return reaction;
    }

    public void setReaction(IReactionDto reaction) {
        this.reaction = reaction;
    }
}
