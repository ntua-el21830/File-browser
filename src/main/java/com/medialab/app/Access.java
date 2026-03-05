package com.medialab.app;

import java.util.UUID;

import com.medialab.app.model.Role;
import com.medialab.app.model.User;

public class Access {
    public boolean isAdmin(User u) {
        return u.role() == Role.ADMIN;
    }

    public boolean isAuthor(User u) {
        return u.role() == Role.AUTHOR;
    }

    public boolean isUser(User u) {
        return u.role() == Role.USER;
    }

    public boolean canAccessContent(User u, UUID categoryID) {
        return isAdmin(u) || (u.allowedCategoryUuids() != null && u.allowedCategoryUuids().contains(categoryID));
    }
    
}