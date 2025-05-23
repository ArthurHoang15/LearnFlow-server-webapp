package Service;

import DTO.MessageDTO;
import Model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.MessageRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    // Giả sử userId được lấy từ authentication context
    public Message sendMessage(Integer userId, MessageDTO messageDTO) {
        Message message = new Message();
        message.setSenderId(userId);
        message.setReceiverId(messageDTO.getFriendId());
        message.setMessageText(messageDTO.getMessage());
        return messageRepository.save(message);
    }

    public List<Message> getChatHistory(Integer userId, Integer friendId) {
        return messageRepository.findChatHistory(userId, friendId);
    }
}
