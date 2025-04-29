package com.example.andriodapp01.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Tag implements Serializable {
    public static final String TYPE_LOCATION = "location";
    public static final String TYPE_PERSON = "person";

    private String id;
    private String type; // "location" or "person"
    private String value; // The actual name of place or person
    private List<String> photoIds; // Store IDs of associated photos

    public Tag(String type, String value) {
        this.id = UUID.randomUUID().toString();
        // Validate type is either "location" or "person"
        if (!TYPE_LOCATION.equals(type) && !TYPE_PERSON.equals(type)) {
            throw new IllegalArgumentException("Tag type must be either 'location' or 'person'");
        }
        this.type = type;
        this.value = value;
        this.photoIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    // Get the full tag name in format "type:value"
    public String getName() {
        return type + ":" + value;
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