package com.medialab.app.usecase;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.medialab.app.model.Role;
import com.medialab.app.model.User;
import com.medialab.app.ports.Pass_Hash;
import com.medialab.app.ports.UserRepo;

public class AdminUseCase {
    private final UserRepo userRepo;
    private final Pass_Hash hasher;

    public AdminUseCase(UserRepo userRepo, Pass_Hash hasher) {
        this.userRepo = userRepo;
        this.hasher = hasher;
    }

    public List<User> list() { return userRepo.findAll(); }

    public User create(String username, String pass, String name, Role role, Set<UUID> cats) {
        String hash = hasher.hash(pass);
        return userRepo.upsert(new User(UUID.randomUUID(), username, hash, name, role, cats));
    }

    public User update(UUID id, String name, Role role, Set<UUID> cats) {
        User u = userRepo.findById(id).orElseThrow();
        return userRepo.upsert(new User(u.id(), u.username(), u.passwordHash(), name, role, cats));
    }

    public void delete(UUID id) { userRepo.deleteById(id); }
}