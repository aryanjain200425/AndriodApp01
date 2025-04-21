package com.example.andriodapp01.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Tag implements Serializable {
    private String id;
    private String name;
    private List<String> photoIds; // Store IDs of associated photos

    public Tag(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.photoIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPhotoIds() {
        return photoIds;
    }

    public void addPhotoId(String photoId) {
        if (!photoIds.contains(photoId)) {
            photoIds.add(photoId);
        }
    }

    public void removePhotoId(String photoId) {
        photoIds.remove(photoId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tag tag = (Tag) obj;
        return id.equals(tag.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}