package com.medialab.app.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.medialab.app.model.DocCategory;

public interface DocCategoriesRepo {
    Optional<DocCategory> findById(UUID id);
    Optional<DocCategory> findByName(String name);
    DocCategory upsert(DocCategory c);
    void deleteById(UUID id);
    List<DocCategory> findAll();
}
