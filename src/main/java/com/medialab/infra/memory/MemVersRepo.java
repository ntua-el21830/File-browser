package com.medialab.infra.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.medialab.app.model.DocVersion;
import com.medialab.app.ports.VersRepo;

public class MemVersRepo implements VersRepo {
    private final Map<UUID, List<DocVersion>> byDoc = new ConcurrentHashMap<>();

    public void loadData(List<DocVersion> versions) {
        byDoc.clear();
        for (DocVersion v : versions) {
            byDoc.computeIfAbsent(v.documentId(), k -> new ArrayList<>()).add(v);
        }
    }

    @Override
        public List<DocVersion> findAll() {
        return byDoc.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    @Override public List<DocVersion> findByDocument(UUID documentId) {
        return List.copyOf(byDoc.getOrDefault(documentId, List.of()));
    }

    @Override public Optional<DocVersion> findLatest(UUID documentId) {
        return Optional.ofNullable(byDoc.get(documentId))
            .filter(list -> !list.isEmpty())
            .map(list -> list.get(list.size() - 1));
    }

    @Override public Optional<DocVersion> findByDocumentAndVersion(UUID documentId, int version) {
        return byDoc.getOrDefault(documentId, List.of()).stream()
                .filter(v -> v.version() == version)
                .findFirst();
    }

    @Override public DocVersion add(DocVersion ver) {
        byDoc.computeIfAbsent(ver.documentId(), k -> new ArrayList<>()).add(ver);
        return ver;
    }

    @Override public void deleteByDocument(UUID documentId) {
        byDoc.remove(documentId);
    }
}