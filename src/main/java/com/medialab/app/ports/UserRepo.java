package com.medialab.app.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.medialab.app.model.User;

public interface UserRepo {
    Optional<User> findByUsername(String username);
    Optional<User> findById(UUID id); 
    List<User> findAll();
    User upsert(User u);
    void deleteById(UUID id); 
}