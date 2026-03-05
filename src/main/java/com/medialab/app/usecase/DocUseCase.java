package com.medialab.app.usecase;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.medialab.app.model.DocVersion;
import com.medialab.app.model.Document;
import com.medialab.app.ports.DocRepo;
import com.medialab.app.ports.FollowRepo;
import com.medialab.app.ports.VersRepo;

/**
 * Use case για τις λειτουργίες εγγράφων
 * Η κλάση δημιουργεί νέα {@link DocVersion} σε κάθε τροποποίηση,
 * ενώ η διαγραφή καθαρίζει σχετικές εκδόσεις και παρακολουθήσεις.
 */
public class DocUseCase {
    private final DocRepo docs;
    private final VersRepo versions;
    private final FollowRepo follows;

    /**
     * Κατασκευάζει το use case με τα απαιτούμενα repositories.
     *
     * @param docs repository για τα μεταδεδομένα των εγγράφων
     * @param versions version history των εγγράφων
     * @param follows παρακολουθήσεις εγγράφων από χρήστες
     */
    public DocUseCase(DocRepo docs, VersRepo versions, FollowRepo follows) {
        this.docs = docs;
        this.versions = versions;
        this.follows = follows;
    }

    /**
     * Αποθηκεύει:
     * <ul>
     * <li>ένα {@link Document} με νέο {@link UUID}</li>
     * <li>μία {@link DocVersion} με {@code version=1} και περιεχόμενο</li>
     * </ul>
     *
     * @param title τίτλος εγγράφου
     * @param author όνομα συγγραφέα
     * @param catId αναγνωριστικό κατηγορίας
     * @param date ημερομηνία δημιουργίας (και ημερομηνία της 1ης έκδοσης)
     * @param content κείμενο
     * @return το {@link Document} που δημιουργήθηκε
     */
    public Document createDocument(String title, String author, UUID catId, LocalDate date, String content) {
        UUID docId = UUID.randomUUID();

        Document doc = new Document(docId, title, author, catId, date);
        docs.upsert(doc);

        DocVersion v1 = new DocVersion(UUID.randomUUID(), docId, 1, date, content);
        versions.add(v1);

        return doc;
    }

    /**
     * Η μέθοδος δημιουργεί νέο {@link DocVersion}
     * αυξάνοντας την έκδοση κατά 1 από την τελευταία αποθηκευμένη.
     * Ενώ αν δεν υπάρχει προηγούμενη έκδοση, ξεκινά από {@code version=1}.
     *
     * @param docId αναγνωριστικό εγγράφου
     * @param when ημερομηνία της νέας έκδοσης
     * @param newContent νέο κείμενο εγγράφου
     * @return η νέα {@link DocVersion} που προστέθηκε
     */
    public DocVersion editDocument(UUID docId, LocalDate when, String newContent) {
        int nextVersion = versions.findLatest(docId)
                .map(v -> v.version() + 1)
                .orElse(1);

        DocVersion newV = new DocVersion(UUID.randomUUID(), docId, nextVersion, when, newContent);
        versions.add(newV);
        return newV;
    }

    /**
     * Αφαιρεί το {@link Document}, το ιστορικό {@link DocVersion} 
     * και τα {@code follows} που δείχνουν προς αυτό.
     */
    public void deleteDocument(UUID docId) {
        docs.deleteById(docId);
        versions.deleteByDocument(docId);
        follows.deleteByDocument(docId);
    }

    /**
     * Επιστρέφει την τελευταία διαθέσιμη έκδοση ενός εγγράφου, αν υπάρχει.
     */
    public Optional<DocVersion> getLatestVersion(UUID docId) {
        return versions.findLatest(docId);
    }
}
