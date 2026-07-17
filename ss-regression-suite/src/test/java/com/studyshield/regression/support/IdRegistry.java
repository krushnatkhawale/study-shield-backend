package com.studyshield.regression.support;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IdRegistry {

    private final Map<String, Deque<Long>> registry = new ConcurrentHashMap<>();

    public void register(String entityType, Long id) {
        registry.computeIfAbsent(entityType, k -> new ArrayDeque<>()).push(id);
    }

    public List<Long> getAll(String entityType) {
        Deque<Long> deque = registry.get(entityType);
        return deque != null ? new ArrayList<>(deque) : Collections.emptyList();
    }

    public Long getLatest(String entityType) {
        Deque<Long> deque = registry.get(entityType);
        return (deque != null && !deque.isEmpty()) ? deque.peek() : null;
    }

    public Map<String, Deque<Long>> getAllRegistered() {
        return Collections.unmodifiableMap(registry);
    }

    public void clear() {
        registry.clear();
    }
}
