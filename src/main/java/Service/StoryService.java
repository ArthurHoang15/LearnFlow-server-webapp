package Service;

import DTO.StoryDetailResponse;
import DTO.StoryResponse;

import java.util.List;

public interface StoryService {
    List<StoryResponse> getStories(String level, int page, int size);
    StoryDetailResponse getStoryDetail(Long storyId);
}
