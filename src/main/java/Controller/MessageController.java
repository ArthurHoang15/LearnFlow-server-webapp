package Controller;

import DTO.MessageDTO;
import Model.Message;
import Service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;

    private Integer getCurrentUserId() {
        return 1; // Hardcode tạm thời, thay bằng SecurityContextHolder trong thực tế
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@Valid @RequestBody MessageDTO messageDTO) {
        Integer userId = getCurrentUserId();
        Message message = messageService.sendMessage(userId, messageDTO);
        return ResponseEntity.ok(Map.of("message", "Message sent successfully"));
    }

    @GetMapping("/history/{friendId}")
    public ResponseEntity<List<Map<String, Object>>> getChatHistory(@PathVariable Integer friendId) {
        Integer userId = getCurrentUserId();
        List<Message> messages = messageService.getChatHistory(userId, friendId);
        List<Map<String, Object>> response = messages.stream()
                .map(m -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("sender_id", m.getSenderId());
                    map.put("message_text", m.getMessageText());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
