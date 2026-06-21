package com.bonsai.service;

import com.bonsai.entity.Bonsai;
import com.bonsai.entity.LifecycleEvent;
import com.bonsai.repository.BonsaiRepository;
import com.bonsai.repository.LifecycleEventRepository;
import com.bonsai.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LifecycleEventService {

    private static final Set<String> VALID_EVENT_TYPES = Set.of(
            "acquire", "planting", "pruning", "wiring", "repotting", "other"
    );

    private final LifecycleEventRepository lifecycleEventRepository;
    private final BonsaiRepository bonsaiRepository;
    private final ImageUtil imageUtil;

    public List<LifecycleEvent> getEventsByBonsaiId(Long bonsaiId) {
        return lifecycleEventRepository.findByBonsaiIdOrderByEventDateAsc(bonsaiId);
    }

    public LifecycleEvent getEventById(Long id) {
        return lifecycleEventRepository.findById(id).orElse(null);
    }

    public LifecycleEvent createEvent(LifecycleEvent event) {
        validateEventType(event.getEventType());
        validateBonsaiOwnership(event.getBonsaiId(), event.getUserId());
        return lifecycleEventRepository.save(event);
    }

    @Transactional
    public LifecycleEvent updateEvent(LifecycleEvent event) {
        validateEventType(event.getEventType());
        LifecycleEvent existingEvent = lifecycleEventRepository.findById(event.getId()).orElse(null);
        if (existingEvent == null) {
            throw new IllegalArgumentException("事件不存在");
        }
        validateBonsaiOwnership(existingEvent.getBonsaiId(), event.getUserId());

        if (event.getImages() != null && !event.getImages().equals(existingEvent.getImages())) {
            imageUtil.deleteImages(existingEvent.getImages());
        }
        if (event.getBeforeImages() != null && !event.getBeforeImages().equals(existingEvent.getBeforeImages())) {
            imageUtil.deleteImages(existingEvent.getBeforeImages());
        }

        event.setBonsaiId(existingEvent.getBonsaiId());
        return lifecycleEventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id, Long userId) {
        LifecycleEvent event = lifecycleEventRepository.findById(id).orElse(null);
        if (event == null) {
            throw new IllegalArgumentException("事件不存在");
        }
        validateBonsaiOwnership(event.getBonsaiId(), userId);

        imageUtil.deleteImages(event.getImages());
        imageUtil.deleteImages(event.getBeforeImages());

        lifecycleEventRepository.deleteById(id);
    }

    private void validateBonsaiOwnership(Long bonsaiId, Long userId) {
        if (bonsaiId == null) {
            throw new IllegalArgumentException("盆景ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        Bonsai bonsai = bonsaiRepository.findById(bonsaiId).orElse(null);
        if (bonsai == null) {
            throw new IllegalArgumentException("盆景不存在，ID: " + bonsaiId);
        }
        if (!userId.equals(bonsai.getUserId())) {
            throw new IllegalArgumentException("无权操作该盆景的生命周期事件");
        }
    }

    private void validateEventType(String eventType) {
        if (eventType == null || !VALID_EVENT_TYPES.contains(eventType)) {
            throw new IllegalArgumentException(
                    "无效的事件类型: " + eventType + "。允许的类型: " + String.join(", ", VALID_EVENT_TYPES)
            );
        }
    }
}
