package Service;

import Model.DTO.PersonalVocabularyRequest;
import Model.DTO.VocabularyDTO;
import Respository.VocabularyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Service
public class VocabularyService {
    @Autowired
    private VocabularyRepository vocabularyRepository;

    public List<VocabularyDTO> getVocabularyByTopic(String topic, int page, int size) {
        return vocabularyRepository.findByTopic(topic, page, size);
    }

    public ResponseEntity<String> managePersonalVocabulary(PersonalVocabularyRequest request) {
        // Logic thêm/xóa từ vựng cá nhân, cập nhật word_progress
        return ResponseEntity.ok("Success");
    }
}
