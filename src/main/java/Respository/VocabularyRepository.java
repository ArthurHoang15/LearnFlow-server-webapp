package Respository;

import Model.DTO.VocabularyDTO;
import Model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {
    @Query("SELECT new com.example.vocabulary.model.dto.VocabularyDTO(v.word, v.meaning, v.pronunciation, v.topic, v.partOfSpeech, v.audioUrl, v.exampleSentence) " +
            "FROM Vocabulary v WHERE v.topic = :topic")
    List<VocabularyDTO> findByTopic(String topic, int page, int size);
}
