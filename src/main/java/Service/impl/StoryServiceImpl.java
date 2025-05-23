package Service.impl;

import DTO.StoryDetailResponse;
import DTO.StoryResponse;
import model.Story;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import Service.StoryService;
import java.util.List;
import java.util.stream.Collectors;
import repository.StoryRepository;
@Service
public class StoryServiceImpl implements StoryService {

    @Autowired
    private StoryRepository storyRepository;

    @Override
    public List<StoryResponse> getStories(String level, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<Story> storyPage = level == null ?
                storyRepository.findAll(pageRequest) :
                storyRepository.findByLevel(level, pageRequest);

        return storyPage.getContent().stream()
                .map(story -> new StoryResponse(story.getTitle(), story.getCoverImage()))
                .collect(Collectors.toList());
    }

    @Override
    public StoryDetailResponse getStoryDetail(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));
        return new StoryDetailResponse(story.getContentUrl(), story.getAudioUrl());
    }
}
