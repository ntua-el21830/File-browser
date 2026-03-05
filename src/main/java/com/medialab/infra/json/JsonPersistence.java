package com.medialab.infra.json;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.medialab.app.model.DocCategory;
import com.medialab.app.model.DocVersion;
import com.medialab.app.model.Document;
import com.medialab.app.model.Follow;
import com.medialab.app.model.User;
import com.medialab.app.ports.DocCategoriesRepo;
import com.medialab.app.ports.DocRepo;
import com.medialab.app.ports.FollowRepo;
import com.medialab.app.ports.UserRepo;
import com.medialab.app.ports.VersRepo;

public class JsonPersistence {
    private final JsonStorage store;

    public JsonPersistence(Path medialabDir) {
        this.store = new JsonStorage(medialabDir);
    }

    public void loadAll(UserRepo users, DocCategoriesRepo cats, DocRepo docs, VersRepo versions, FollowRepo follows) {
        var usersList = store.readList("users.json", new TypeReference<List<User>>() {});
        usersList.forEach(users::upsert);

        var catList = store.readList("categories.json", new TypeReference<List<DocCategory>>() {});
        catList.forEach(cats::upsert);

        var docList = store.readList("documents.json", new TypeReference<List<Document>>() {});
        docList.forEach(docs::upsert);

        var verList = store.readList("versions.json", new TypeReference<List<DocVersion>>() {});
        verList.forEach(versions::add);

        var followList = store.readList("follows.json", new TypeReference<List<Follow>>() {});
        followList.forEach(follows::upsert);
    }

    public void saveAll(UserRepo users, DocCategoriesRepo cats, DocRepo docs, VersRepo versions, FollowRepo follows) {
        var usersList = users.findAll();
        store.writeList("users.json", usersList);

        var catList = cats.findAll();
        store.writeList("categories.json", catList);

        var docList = docs.findAll();
        store.writeList("documents.json", docList);

        var allVers = docList.stream()
                .flatMap(d -> versions.findByDocument(d.id()).stream())
                .collect(Collectors.toList());
        store.writeList("versions.json", allVers);

        var allFollows = usersList.stream()
                .flatMap(u -> follows.findByUser(u.username()).stream())
                .collect(Collectors.toList());
        store.writeList("follows.json", allFollows);
    }
}