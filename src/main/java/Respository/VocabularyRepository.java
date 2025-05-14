package Respository;

import Model.DTO.VocabularyDTO;
import Model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {
    List<VocabularyDTO> findByTopic(String topic, int page, int size);
}
