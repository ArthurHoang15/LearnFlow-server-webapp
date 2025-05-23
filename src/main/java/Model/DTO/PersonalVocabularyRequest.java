package Model.DTO;

public class PersonalVocabularyRequest {
    private Long wordId;
    private String action; // "add" hoặc "remove"

    // Getters
    public Long getWordId() { return wordId; }
    public String getAction() { return action; }

    // Setters
    public void setWordId(Long wordId) { this.wordId = wordId; }
    public void setAction(String action) { this.action = action; }
}
