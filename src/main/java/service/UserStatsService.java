package service;

import dto.UserStatsResponse;
import exception.InvalidDateRangeException;
import org.springframework.stereotype.Service;

@Service
public class UserStatsService {

    public UserStatsResponse getUserStats(String dateRange) {
        // Kiểm tra dateRange hợp lệ
        if (dateRange == null || dateRange.isEmpty()) {
            throw new InvalidDateRangeException("dateRange cannot be null or empty");
        }

        // Logic giả lập: trả về dữ liệu mẫu
        // Trong thực tế, bạn sẽ truy vấn database dựa trên dateRange
        return new UserStatsResponse(1000, 800);
    }
}
