package com.medialab.launcher;

import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.medialab.app.model.DocCategory;
import com.medialab.app.model.DocVersion;
import com.medialab.app.model.Document;
import com.medialab.app.model.Follow;
import com.medialab.app.model.Role;
import com.medialab.app.model.User;
import com.medialab.app.ports.DocCategoriesRepo;
import com.medialab.app.ports.DocRepo;
import com.medialab.app.ports.FollowRepo;
import com.medialab.app.ports.Pass_Hash;
import com.medialab.app.ports.UserRepo;
import com.medialab.app.ports.VersRepo;
import com.medialab.app.usecase.AdminUseCase;
import com.medialab.app.usecase.AuthenticationUseCase;
import com.medialab.app.usecase.AuthoringDocUseCase;
import com.medialab.app.usecase.CategoryAdminUseCase;
import com.medialab.app.usecase.DocUseCase;
import com.medialab.app.usecase.FollowUseCase;
import com.medialab.app.usecase.ReadingDocUseCase;
import com.medialab.infra.Plain_Pass;
import com.medialab.infra.Sys_Time;
import com.medialab.infra.json.JsonStorage;
import com.medialab.infra.memory.MemDocCatRepo;
import com.medialab.infra.memory.MemDocRepo;
import com.medialab.infra.memory.MemFollowRepo;
import com.medialab.infra.memory.MemUsersRepo;
import com.medialab.infra.memory.MemVersRepo;

public class Boot {
    
    public final UserRepo users = new MemUsersRepo();
    public final DocCategoriesRepo categories = new MemDocCatRepo();
    public final Sys_Time time = new Sys_Time();
    public final Pass_Hash pass = new Plain_Pass();

    public final AuthenticationUseCase auth = new AuthenticationUseCase(users, pass);

    public final DocRepo docs = new MemDocRepo();
    public final VersRepo versions = new MemVersRepo();
    public final FollowRepo follows = new MemFollowRepo();

    public final DocUseCase docsUse = new DocUseCase(docs, versions, follows);
    public final FollowUseCase followUse = new FollowUseCase(follows, versions, docs);
    public final AuthoringDocUseCase authoring = new AuthoringDocUseCase(users, categories, docs, versions, follows, time);
public final ReadingDocUseCase reading = new ReadingDocUseCase(docs, versions, users);
    public final CategoryAdminUseCase catAdmin = new CategoryAdminUseCase(categories, docs, versions, follows);

    public final AdminUseCase usersAdmin = new AdminUseCase(users, pass);

    private final JsonStorage storage = new JsonStorage(Path.of("medialab_data"));

    public void initFromJson() {
    System.out.println("Φόρτωση Δεδομένων");
    
    List<DocCategory> cats = storage.readList("categories.json", new TypeReference<List<DocCategory>>() {});
    ((MemDocCatRepo) categories).loadData(cats);

    List<User> userList = storage.readList("users.json", new TypeReference<List<User>>() {});
    if (userList.isEmpty()) {
        System.out.println("Η λίστα χρηστών είναι κενή. Δημιουργία Admin.");
        usersAdmin.create("medialab", "medialab_2025", "", Role.ADMIN, null);
    } else {
        ((MemUsersRepo) users).loadData(userList);
    }

    List<Document> docList = storage.readList("docs.json", new TypeReference<List<Document>>() {});
    ((MemDocRepo) docs).loadData(docList);

    List<DocVersion> verList = storage.readList("versions.json", new TypeReference<List<DocVersion>>() {});
    ((MemVersRepo) versions).loadData(verList);

    List<Follow> folList = storage.readList("follows.json", new TypeReference<List<Follow>>() {});
    ((MemFollowRepo) follows).loadData(folList);
    
    System.out.println("Η φόρτωση δεδομένων ολοκληρώθηκε");
}


    public void saveAll() {
        System.out.println("Αποθήκευση Δεδομένων");
        storage.writeList("categories.json", categories.findAll());
        storage.writeList("users.json", users.findAll());
        storage.writeList("docs.json", docs.findAll());
        storage.writeList("versions.json", versions.findAll());
        storage.writeList("follows.json", follows.findAll());
}

    public void installShutdownSave() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveAll();
        }));
    }
}