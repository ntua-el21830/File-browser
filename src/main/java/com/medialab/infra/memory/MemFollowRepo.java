package com.medialab.infra.memory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.medialab.app.model.Follow;
import com.medialab.app.ports.FollowRepo;

public class MemFollowRepo implements FollowRepo {
    private final Map<UUID, Follow> byId = new ConcurrentHashMap<>();

    public void loadData(List<Follow> follows) {
        byId.clear();
        for (Follow f : follows) {
            byId.put(f.id(), f);
        }
    }

    @Override public List<Follow> findAll() {
        return List.copyOf(byId.values());
    }

    @Override public List<Follow> findByUser(String username) {
        return byId.values().stream()
                .filter(f -> f.username().equalsIgnoreCase(username))
                .toList();
    }

    @Override public Optional<Follow> findByUserAndDoc(String username, UUID documentId) {
        return byId.values().stream()
                .filter(f -> f.username().equalsIgnoreCase(username))
                .filter(f -> Objects.equals(f.documentId(), documentId))
                .findFirst();
    }

    @Override public Follow upsert(Follow f) { 
        byId.put(f.id(), f); 
        return f; 
    }

    @Override public void deleteById(UUID id) { 
        byId.remove(id); 
    }

    @Override public void deleteByDocument(UUID documentId) {
        byId.values().removeIf(f -> Objects.equals(f.documentId(), documentId));
    }  
}