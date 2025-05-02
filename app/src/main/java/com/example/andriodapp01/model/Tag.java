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

    public boolean isValidValue(String newValue) {
        if (newValue == null || newValue.trim().isEmpty()) {
            return false;
        }

        if (TYPE_PERSON.equals(type)) {
            return !newValue.matches(".*\\d+.*");
        }

        return true;
    }


    public boolean matchesQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        
        String lowercaseQuery = query.toLowerCase();
        String lowercaseValue = value.toLowerCase();
        
        return lowercaseValue.contains(lowercaseQuery);
    }


    public int compareTo(Tag other) {
        int typeCompare = this.type.compareTo(other.type);
        if (typeCompare != 0) {
            return typeCompare;
        }
        return this.value.compareToIgnoreCase(other.value);
    }


    public String getDisplayText() {
        return String.format("%s: %s", 
            type.substring(0, 1).toUpperCase() + type.substring(1),
            value);
    }

    public boolean isEmpty() {
        return photoIds.isEmpty();
    }

    public Tag clone() {
        Tag clone = new Tag(this.type, this.value);
        clone.id = this.id;
        clone.photoIds = new ArrayList<>(this.photoIds);
        return clone;
    }

    @Override
    public String toString() {
        return String.format("Tag{id='%s', type='%s', value='%s', photoCount=%d}", 
            id, type, value, photoIds.size());
    }
}