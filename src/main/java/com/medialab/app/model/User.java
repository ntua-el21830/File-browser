package com.medialab.app.model;

import java.util.Set;
import java.util.UUID;

public record User(
    UUID id,
    String username,
    String passwordHash,
    String fullName,
    Role role,
    Set<UUID> allowedCategoryUuids

) {}