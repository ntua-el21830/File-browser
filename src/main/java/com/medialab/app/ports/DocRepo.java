package com.medialab.app.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.medialab.app.model.Document;


public interface DocRepo {
    Optional<Document> findById(UUID id);
    List<Document> findAll();
    List<Document> findByCategory(UUID categoryId);
    List<Document> findByAuthor(String authorUsername);
    List<Document> search(String titleLike, String authorLike, UUID categoryId);

    Document upsert(Document doc);
        void deleteById(UUID id);
    }
