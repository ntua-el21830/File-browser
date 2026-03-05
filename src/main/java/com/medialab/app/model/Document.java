package com.medialab.app.model;

import java.time.LocalDate;
import java.util.UUID;

public record Document(
    UUID id,
    String title,
    String authorUsername,
    UUID categoryId,
    LocalDate createdDate
    
){}
