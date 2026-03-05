package com.medialab.infra.memory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.medialab.app.model.Document;
import com.medialab.app.ports.DocRepo;

public class MemDocRepo implements DocRepo {
    private final Map<UUID, Document> byId = new ConcurrentHashMap<>();

    public void loadData(List<Document> docs) {
        byId.clear();
        for (Document d : docs) {
            byId.put(d.id(), d);
        }
    }

    @Override public Optional<Document> findById(UUID id) { return Optional.ofNullable(byId.get(id)); }

    @Override public List<Document> findAll() { return List.copyOf(byId.values()); }

    @Override public List<Document> findByCategory(UUID categoryId) {
        return byId.values().stream()
                .filter(d -> Objects.equals(d.categoryId(), categoryId)).toList();
    }

    @Override public List<Document> findByAuthor(String authorUsername) {
        return byId.values().stream()
                .filter(d -> d.authorUsername().equalsIgnoreCase(authorUsername)).toList();           
    }

    @Override public List<Document> search(String titleLike, String authorLike, UUID categoryId) {
        return byId.values().stream()
                .filter(d -> matches(d.title(), titleLike))
                .filter(d -> matches(d.authorUsername(), authorLike))
                .filter(d -> categoryId == null || Objects.equals(d.categoryId(), categoryId))
                .toList();
    }

    private boolean matches(String value, String query) {
        if (query == null || query.isBlank()) return true;
        return value.toLowerCase().contains(query.toLowerCase());
    }

    @Override public Document upsert(Document doc) {
        byId.put(doc.id(), doc);
        return doc;
    }

    @Override public void deleteById(UUID id) {
        byId.remove(id);
    }
}