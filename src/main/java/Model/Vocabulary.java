package Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Vocabulary {
    @Id
    private Long id;
    private String word;
    private String meaning;
    private String pronunciation;
    private String topic;

    // Getters
    public Long getId() { return id; }
    public String getWord() { return word; }
    public String getMeaning() { return meaning; }
    public String getPronunciation() { return pronunciation; }
    public String getTopic() { return topic; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setWord(String word) { this.word = word; }
    public void setMeaning(String meaning) { this.meaning = meaning; }
    public void setPronunciation(String pronunciation) { this.pronunciation = pronunciation; }
    public void setTopic(String topic) { this.topic = topic; }
}
