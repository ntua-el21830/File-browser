package com.medialab.app.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.medialab.app.model.DocVersion;

public interface VersRepo {
    List<DocVersion> findByDocument(UUID documentId);
    Optional<DocVersion> findLatest(UUID documentId);
    Optional<DocVersion> findByDocumentAndVersion(UUID documentId, int version);

    DocVersion add(DocVersion ver);
    void deleteByDocument(UUID documentId);

List<DocVersion> findAll();    
}
