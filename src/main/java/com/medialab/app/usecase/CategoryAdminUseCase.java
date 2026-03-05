package com.medialab.app.usecase;

import java.util.List;
import java.util.UUID;

import com.medialab.app.model.DocCategory;
import com.medialab.app.ports.DocCategoriesRepo;
import com.medialab.app.ports.DocRepo;
import com.medialab.app.ports.FollowRepo;
import com.medialab.app.ports.VersRepo;


public class CategoryAdminUseCase {
    private final DocCategoriesRepo categoryRepo;
    private final DocRepo docRepo;
    private final VersRepo versionRepo; 
    private final FollowRepo followRepo;

    public CategoryAdminUseCase(DocCategoriesRepo categoryRepo, DocRepo docRepo, VersRepo versionRepo, FollowRepo followRepo) {
        this.categoryRepo = categoryRepo;
        this.docRepo = docRepo;
        this.versionRepo = versionRepo;
        this.followRepo = followRepo;
    }

    public List<DocCategory> listAll() {
        return categoryRepo.findAll();
    }

    public DocCategory create(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name cannot be empty");
        DocCategory c = new DocCategory(UUID.randomUUID(), name.trim());
        return categoryRepo.upsert(c);
    }

    public DocCategory rename(UUID id, String newName) {
        if (newName == null || newName.isBlank()) throw new IllegalArgumentException("New name cannot be empty");
        
        return categoryRepo.findById(id)
                .map(c -> new DocCategory(c.id(), newName.trim()))
                .map(categoryRepo::upsert)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    public void deleteCascade(UUID categoryId) {
        categoryRepo.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("Category not found"));
        var docsInCat = docRepo.findByCategory(categoryId);
        
        for (var d : docsInCat) {
            versionRepo.deleteByDocument(d.id());
            followRepo.deleteByDocument(d.id());
            docRepo.deleteById(d.id());
        }
        
        categoryRepo.deleteById(categoryId);
    }
}