package model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String picture;

    @Column(name = "goal_id")
    private Long goalId;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "access_token")
    private String accessToken;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Phương thức tiện ích cho Spring Security
    public boolean hasGoogleLogin() {
        return googleId != null && !googleId.isEmpty();
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
