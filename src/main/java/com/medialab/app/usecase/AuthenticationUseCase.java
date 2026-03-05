package com.medialab.app.usecase;

import java.util.Set;
import java.util.UUID;

import com.medialab.app.model.Role;
import com.medialab.app.model.User;
import com.medialab.app.ports.Pass_Hash;
import com.medialab.app.ports.UserRepo;

public class AuthenticationUseCase {

    private final UserRepo userRepo;
    private final Pass_Hash passHash;

    public AuthenticationUseCase(UserRepo userRepo, Pass_Hash passHash) {
        this.userRepo = userRepo;
        this.passHash = passHash;
        ensureDefaultAdmin(); // Eventually delete this
    }

    public User login(String username, String password) throws IllegalArgumentException {
        var u = userRepo.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Invalid username"));
        if (!passHash.verify(password, u.passwordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }
        return u;
    }

    // Eventually delete this
    private void ensureDefaultAdmin() {
        if (userRepo.findByUsername("medialab").isEmpty()) {
            User admin = new User(
            UUID.randomUUID(),
            "medialab",
            passHash.hash("medialab_2025"),
            "Default Admin",
            Role.ADMIN,
            Set.of()
        );
        userRepo.upsert(admin);
        }
    }
}