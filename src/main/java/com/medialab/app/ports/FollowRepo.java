package com.medialab.app.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.medialab.app.model.Follow;

public interface FollowRepo {
    List<Follow> findByUser(String username);
    Optional<Follow> findByUserAndDoc(String username, UUID documentId);
    
    Follow upsert(Follow f);
    void deleteById(UUID id);
    void deleteByDocument(UUID documentId);

    List<Follow> findAll();
    
}
