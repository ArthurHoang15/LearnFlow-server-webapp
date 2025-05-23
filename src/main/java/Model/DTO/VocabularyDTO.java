package Model.DTO;

public class VocabularyDTO {
    private String word;
    private String meaning;
    private String pronunciation;
    private String topic;
    private String partOfSpeech;
    private String audioUrl;
    private String exampleSentence;

    // Getters
    public String getWord() { return word; }
    public String getMeaning() { return meaning; }
    public String getPronunciation() { return pronunciation; }
    public String getTopic() { return topic; }
    public String getPartOfSpeech() { return partOfSpeech; }
    public String getAudioUrl() { return audioUrl; }
    public String getExampleSentence() { return exampleSentence; }

    // Setters
    public void setWord(String word) { this.word = word; }
    public void setMeaning(String meaning) { this.meaning = meaning; }
    public void setPronunciation(String pronunciation) { this.pronunciation = pronunciation; }
    public void setTopic(String topic) { this.topic = topic; }
    public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }
}
