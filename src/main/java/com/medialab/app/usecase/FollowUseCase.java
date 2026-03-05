package com.medialab.app.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.medialab.app.model.DocVersion;
import com.medialab.app.model.Document;
import com.medialab.app.model.Follow;
import com.medialab.app.ports.DocRepo;
import com.medialab.app.ports.FollowRepo;
import com.medialab.app.ports.VersRepo;

public class FollowUseCase {
    private final FollowRepo followRepo;
    private final VersRepo versionRepo;
    private final DocRepo docRepo;

    public FollowUseCase(FollowRepo follow, VersRepo version, DocRepo doc) {
        this.followRepo = follow;
        this.versionRepo = version;
        this.docRepo = doc;
    }

    public Follow follow(String username, UUID documentId) {
        int latest = versionRepo.findLatest(documentId).map(DocVersion::version).orElse(0);
        
        return followRepo.findByUserAndDoc(username, documentId)
                .map(f0 -> followRepo.upsert(new Follow(f0.id(), username, documentId, latest)))
                .orElseGet(() -> followRepo.upsert(new Follow(UUID.randomUUID(), username, documentId, latest)));
    }

    public void unfollow(String username, UUID documentId) {
        followRepo.findByUserAndDoc(username, documentId)
                .ifPresent(f -> followRepo.deleteById(f.id()));
    }

    public List<NotificationItem> notifyOnLogin(String username) {
        return followRepo.findByUser(username).stream()
            .map(this::processFollow)
            .flatMap(Optional::stream)
            .sorted(Comparator.comparing(a -> a.document().title().toLowerCase()))
            .toList();
    }

    private Optional<NotificationItem> processFollow(Follow f) {
        return versionRepo.findLatest(f.documentId())
            .filter(latest -> latest.version() > f.lastSeenVersion())
            .flatMap(latest -> docRepo.findById(f.documentId())
                .map(d -> {
                    followRepo.upsert(new Follow(f.id(), f.username(), f.documentId(), latest.version()));
                    return new NotificationItem(d, latest.version(), f.lastSeenVersion());
                })
            );
    }

    public record NotificationItem(Document document, int latestVersion, int lastSeenBefore) {}

    public void markSeen(String username, UUID documentId, int version) {
        var f = followRepo.findByUserAndDoc(username, documentId)
            .orElseThrow(() -> new IllegalArgumentException("follow not found"));
        followRepo.upsert(new Follow(f.id(), f.username(), f.documentId(), Math.max(f.lastSeenVersion(), version)));
}

}