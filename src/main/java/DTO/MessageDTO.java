package DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MessageDTO {
    @NotNull(message = "friendId is required")
    private Integer friendId;

    @NotBlank(message = "message is required")
    private String message;

    public Integer getFriendId() { return friendId; }
    public void setFriendId(Integer friendId) { this.friendId = friendId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
