package Model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "words")
public class Vocabulary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String meaning;

    private String topic;

    @Column(name = "part_of_speech")
    private String partOfSpeech;

    private String pronunciation;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    private String exampleSentence;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters
    public Long getId() { return id; }
    public String getWord() { return word; }
    public String getMeaning() { return meaning; }
    public String getTopic() { return topic; }
    public String getPartOfSpeech() { return partOfSpeech; }
    public String getPronunciation() { return pronunciation; }
    public String getAudioUrl() { return audioUrl; }
    public String getExampleSentence() { return exampleSentence; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setWord(String word) { this.word = word; }
    public void setMeaning(String meaning) { this.meaning = meaning; }
    public void setTopic(String topic) { this.topic = topic; }
    public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }
    public void setPronunciation(String pronunciation) { this.pronunciation = pronunciation; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
