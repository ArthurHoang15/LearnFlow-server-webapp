package model;

import jakarta.persistence.*;
import model.User.User;

import java.time.Instant;

import lombok.*;

@Entity
@Table(name = "refresh_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // PHƯƠNG THỨC isExpired() CẦN THÊM VÀO
    public boolean isExpired() {
        return this.expiryDate.isBefore(Instant.now());
    }
}
