package com.bonsai.service;

import com.bonsai.entity.LifecycleEvent;
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
    private final ImageUtil imageUtil;

    public List<LifecycleEvent> getEventsByBonsaiId(Long bonsaiId) {
        return lifecycleEventRepository.findByBonsaiIdOrderByEventDateAsc(bonsaiId);
    }

    public LifecycleEvent getEventById(Long id) {
        return lifecycleEventRepository.findById(id).orElse(null);
    }

    public LifecycleEvent createEvent(LifecycleEvent event) {
        validateEventType(event.getEventType());
        return lifecycleEventRepository.save(event);
    }

    public LifecycleEvent updateEvent(LifecycleEvent event) {
        validateEventType(event.getEventType());
        return lifecycleEventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        LifecycleEvent event = lifecycleEventRepository.findById(id).orElse(null);
        if (event == null) {
            return;
        }

        imageUtil.deleteImages(event.getImages());
        imageUtil.deleteImages(event.getBeforeImages());

        lifecycleEventRepository.deleteById(id);
    }

    private void validateEventType(String eventType) {
        if (eventType == null || !VALID_EVENT_TYPES.contains(eventType)) {
            throw new IllegalArgumentException(
                    "无效的事件类型: " + eventType + "。允许的类型: " + String.join(", ", VALID_EVENT_TYPES)
            );
        }
    }
}
