package com.medialab.infra.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.medialab.app.model.User;
import com.medialab.app.ports.UserRepo;

public class MemUsersRepo implements UserRepo {
    private final Map<UUID, User> byId = new ConcurrentHashMap<>();
    
    private final Map<String, UUID> idByUsername = new ConcurrentHashMap<>();

    private static String key(String username) {
        return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    }

    public void loadData(List<User> users) {
        byId.clear();
        idByUsername.clear();
        for (User u : users) {
            internalPut(u);
        }
    }

    private void internalPut(User u) {
        byId.put(u.id(), u);
        String k = key(u.username());
        if (k != null) {
            idByUsername.put(k, u.id());
        }
    }
    
    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String k = key(username);
        if (k == null) return Optional.empty();
        
        UUID id = idByUsername.get(k);
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public User upsert(User user) {
        String k = key(user.username());
        if (k == null || k.isEmpty()) throw new IllegalArgumentException("Το username είναι υποχρεωτικό");

        UUID existingId = idByUsername.get(k);
        UUID idToUse = (existingId != null) ? existingId : user.id();

        User toSave = new User(
            idToUse,
            user.username().trim(),
            user.passwordHash(),
            user.fullName(),
            user.role(),
            user.allowedCategoryUuids()
        );

        internalPut(toSave);
        return toSave;
    }

    @Override
    public void deleteById(UUID id) {
        User removed = byId.remove(id);
        if (removed != null) {
            idByUsername.remove(key(removed.username()));
        }
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(byId.values());
    }
}