package service;

public interface SenderService {
    /**
     * Gửi một thông điệp (ví dụ: email, SMS).
     *
     * @param to      Người nhận (email, số điện thoại).
     * @param subject Chủ đề của thông điệp (chủ yếu cho email).
     * @param content Nội dung của thông điệp.
     */
    void send(String to, String subject, String content);
}
