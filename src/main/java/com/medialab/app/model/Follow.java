package com.medialab.app.model;

import java.util.UUID;

public record Follow(
    UUID id,
    String username,
    UUID documentId,
    int lastSeenVersion

) {}