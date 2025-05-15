package Controller;

import DTO.StoryDetailResponse;
import DTO.StoryResponse;
import Service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @GetMapping
    public List<StoryResponse> getStories(
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return storyService.getStories(level, page, size);
    }

    @GetMapping("/{storyId}")
    public StoryDetailResponse getStoryDetail(@PathVariable Long storyId) {
        return storyService.getStoryDetail(storyId);
    }
}
