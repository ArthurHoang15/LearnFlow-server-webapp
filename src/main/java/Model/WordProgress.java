package Model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "word_progress")
public class WordProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "word_id", nullable = false)
    private Long wordId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private WordStatus status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Enum for word_status
    public enum WordStatus {
        FORGOTTEN,
        LEARNED
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getWordId() { return wordId; }
    public WordStatus getStatus() { return status; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setWordId(Long wordId) { this.wordId = wordId; }
    public void setStatus(WordStatus status) { this.status = status; }
    public void setNote(String note) { this.note = note; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
