package Controller;

import Model.DTO.PersonalVocabularyRequest;
import Model.DTO.VocabularyDTO;
import Service.VocabularyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vocabulary")
public class VocabularyController {
    @Autowired
    private VocabularyService vocabularyService;

    @GetMapping
    public List<VocabularyDTO> getVocabulary(
            @RequestParam String topic,
            @RequestParam int page,
            @RequestParam int size) {
        return vocabularyService.getVocabularyByTopic(topic, page, size);
    }

    @PostMapping("/personal")
    public ResponseEntity<String> managePersonalVocabulary(
            @RequestBody PersonalVocabularyRequest request) {
        return vocabularyService.managePersonalVocabulary(request);
    }
}
