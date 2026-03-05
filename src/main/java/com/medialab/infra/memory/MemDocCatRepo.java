package com.medialab.infra.memory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.medialab.app.model.DocCategory;
import com.medialab.app.ports.DocCategoriesRepo;

public class MemDocCatRepo implements DocCategoriesRepo {
    private final Map<UUID, DocCategory> byId = new ConcurrentHashMap<>();

    public void loadData(List<DocCategory> cats) {
        byId.clear();
        for (DocCategory c : cats) {
            byId.put(c.id(), c);
        }
    }

    @Override public Optional<DocCategory> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override public Optional<DocCategory> findByName(String name) {
        return byId.values().stream()
                .filter(c -> c.name().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override public DocCategory upsert(DocCategory c) {
        byId.put(c.id(), c);
        return c;
    }

    @Override public void deleteById(UUID id) {
        byId.remove(id);
    }

    @Override public List<DocCategory> findAll() {
        return List.copyOf(byId.values());
    }
}