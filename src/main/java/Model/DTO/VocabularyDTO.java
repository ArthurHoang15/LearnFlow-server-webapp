package Model.DTO;

public class VocabularyDTO {
    private String word;
    private String meaning;
    private String pronunciation;

    // Getters
    public String getWord() { return word; }
    public String getMeaning() { return meaning; }
    public String getPronunciation() { return pronunciation; }

    // Setters
    public void setWord(String word) { this.word = word; }
    public void setMeaning(String meaning) { this.meaning = meaning; }
    public void setPronunciation(String pronunciation) { this.pronunciation = pronunciation; }
}
