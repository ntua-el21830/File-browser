package com.medialab.app.usecase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.medialab.app.Access;
import com.medialab.app.model.DocVersion;
import com.medialab.app.model.Document;
import com.medialab.app.model.Role;
import com.medialab.app.model.User;
import com.medialab.app.ports.DocRepo;
import com.medialab.app.ports.UserRepo;
import com.medialab.app.ports.VersRepo;

public class ReadingDocUseCase {
    private final DocRepo docRepo;
    private final VersRepo versionRepo;
    private final UserRepo userRepo;
    private final Access access = new Access();

    public ReadingDocUseCase(DocRepo docRepo, VersRepo versionRepo, UserRepo userRepo) {
        this.docRepo = docRepo;
        this.versionRepo = versionRepo;
        this.userRepo = userRepo;
    }

    public List<Document> searchAccessible(String username, String title, String author, UUID categoryId) {
        User u = userRepo.findByUsername(username).orElse(null);
        if (u == null) return List.of();

        return docRepo.search(title, author, categoryId).stream()
                .filter(d -> access.canAccessContent(u, d.categoryId()))
                .toList();
    }

    public List<DocVersion> visibleVersions(String username, UUID documentId) {
        User u = userRepo.findByUsername(username).orElse(null);
        if (u == null) return List.of();

        Document doc = docRepo.findById(documentId).orElse(null);
        if (doc == null) return List.of();

        if (!access.canAccessContent(u, doc.categoryId())) {
            return List.of();
        }

        List<DocVersion> all = new ArrayList<>(versionRepo.findByDocument(documentId));
        all.sort(Comparator.comparingInt(DocVersion::version));

        if (all.isEmpty()) return List.of();

        Role role = u.role();

        if (role == Role.USER) {
            return List.of(all.get(all.size() - 1));
        }

        int from = Math.max(0, all.size() - 3);
        return all.subList(from, all.size());
    }
}