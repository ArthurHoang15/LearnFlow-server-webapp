package Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class WordProgress {
    @Id
    private Long id;
    private Long userId;
    private Long wordId;

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getWordId() { return wordId; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setWordId(Long wordId) { this.wordId = wordId; }
}
