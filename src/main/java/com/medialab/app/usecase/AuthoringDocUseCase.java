package com.medialab.app.usecase;

import java.time.LocalDate;
import java.util.UUID;

import com.medialab.app.Access;
import com.medialab.app.model.DocVersion;
import com.medialab.app.model.Document;
import com.medialab.app.model.User;
import com.medialab.app.ports.DocCategoriesRepo;
import com.medialab.app.ports.DocRepo;
import com.medialab.app.ports.FollowRepo;
import com.medialab.app.ports.UserRepo;
import com.medialab.app.ports.VersRepo;
import com.medialab.infra.Sys_Time;

public class AuthoringDocUseCase {
    private final UserRepo userRepo;
    private final DocCategoriesRepo categoryRepo;
    private final DocRepo docRepo;
    private final VersRepo versionRepo;
    private final FollowRepo followRepo;
    private final Sys_Time time;
    private final Access access = new Access();

    public AuthoringDocUseCase(
            UserRepo userRepo,
            DocCategoriesRepo categoryRepo,
            DocRepo docRepo,
            VersRepo versionRepo,
            FollowRepo followRepo,
            Sys_Time time
    ) {
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.docRepo = docRepo;
        this.versionRepo = versionRepo;
        this.followRepo = followRepo;
        this.time = time;
    }

    public Document createDocument(String authorUsername, UUID categoryId, String title, String content) {
        User author = requireUser(authorUsername);
        validateAuthorAccess(author, categoryId);
        requireCategory(categoryId);

        UUID docId = UUID.randomUUID();
        LocalDate now = time.today();

        Document newDoc = new Document(docId, title, author.username(), categoryId, now);
        docRepo.upsert(newDoc);

        DocVersion v1 = new DocVersion(UUID.randomUUID(), docId, 1, now, content);
        versionRepo.add(v1);

        return newDoc;
    }

    public DocVersion editDocument(String editorUsername, UUID documentId, String newContent) {
        User editor = requireUser(editorUsername);
        Document document = docRepo.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        validateAuthorAccess(editor, document.categoryId());

        int nextVersion = versionRepo.findLatest(documentId)
                .map(v -> v.version() + 1)
                .orElse(1);

        DocVersion newVersion = new DocVersion(
                UUID.randomUUID(), 
                documentId, 
                nextVersion, 
                time.today(), 
                newContent
        );

        versionRepo.add(newVersion);
        return newVersion;
    }

    public void deleteDocument(String requesterUsername, UUID documentId) {
        User req = requireUser(requesterUsername);
        Document doc = docRepo.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        validateAuthorAccess(req, doc.categoryId());

        versionRepo.deleteByDocument(documentId);
        followRepo.deleteByDocument(documentId);
        docRepo.deleteById(documentId);
    }


    private User requireUser(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + username));
    }

    private void requireCategory(UUID categoryId) {
        categoryRepo.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown category"));
    }

    private void validateAuthorAccess(User author, UUID categoryId) {
        if (!access.isAuthor(author) && !access.isAdmin(author)) {
            throw new IllegalStateException("Permission denied: User is not an author or admin");
        }
        if (!access.canAccessContent(author, categoryId)) {
            throw new IllegalStateException("Permission denied: Category access not allowed for this author");
        }
    }
}