package dto.OTP;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // Lombok: tự động tạo getters, setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
public class OTPRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
