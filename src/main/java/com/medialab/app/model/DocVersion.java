package com.medialab.app.model;

import java.time.LocalDate;
import java.util.UUID;

public record DocVersion(
    UUID id,
    UUID documentId,
    int version,
    LocalDate date,
    String content

) {}
