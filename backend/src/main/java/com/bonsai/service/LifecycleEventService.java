package com.bonsai.service;

import com.bonsai.entity.LifecycleEvent;
import com.bonsai.repository.LifecycleEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LifecycleEventService {

    private final LifecycleEventRepository lifecycleEventRepository;

    public List<LifecycleEvent> getEventsByBonsaiId(Long bonsaiId) {
        return lifecycleEventRepository.findByBonsaiIdOrderByEventDateAsc(bonsaiId);
    }

    public LifecycleEvent getEventById(Long id) {
        return lifecycleEventRepository.findById(id).orElse(null);
    }

    public LifecycleEvent createEvent(LifecycleEvent event) {
        return lifecycleEventRepository.save(event);
    }

    public LifecycleEvent updateEvent(LifecycleEvent event) {
        return lifecycleEventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        lifecycleEventRepository.deleteById(id);
    }
}
