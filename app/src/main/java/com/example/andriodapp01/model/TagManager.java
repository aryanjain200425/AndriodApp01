package com.example.andriodapp01.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TagManager {
    private static final String PREF_NAME = "TagData";
    private static final String KEY_TAGS = "tags";
    public static final String TYPE_PERSON = "person";
    public static final String TYPE_LOCATION = "location";
    private static final int MIN_SEARCH_CHARS = 2;

    private static TagManager instance;
    private final SharedPreferences preferences;
    private final Gson gson;
    private List<Tag> tags;

    private TagManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadTags();
    }

    public static synchronized TagManager getInstance(Context context) {
        if (instance == null) {
            instance = new TagManager(context);
        }
        return instance;
    }

    private void loadTags() {
        String tagsJson = preferences.getString(KEY_TAGS, null);
        if (tagsJson != null) {
            Type type = new TypeToken<ArrayList<Tag>>() {}.getType();
            tags = gson.fromJson(tagsJson, type);
        } else {
            tags = new ArrayList<>();
        }
    }

    private void saveTagsToStorage() {
        String tagsJson = gson.toJson(tags);
        preferences.edit().putString(KEY_TAGS, tagsJson).apply();
    }

    public List<Tag> getTags() {
        return new ArrayList<>(tags); // Return a copy to prevent accidental modification
    }

    public Tag createTag(String type, String value) {
        // First, check if a tag with this type and value already exists
        for (Tag tag : tags) {
            if (tag.getType().equals(type) && tag.getValue().equals(value)) {
                return tag; // Return existing tag
            }
        }

        // If not, create a new one
        Tag newTag = new Tag(type, value);
        tags.add(newTag);
        saveTagsToStorage();
        return newTag;
    }

    public void saveTag(Tag tag) {
        // Check if tag already exists (by ID)
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).getId().equals(tag.getId())) {
                tags.set(i, tag); // Replace existing tag
                saveTagsToStorage();
                return;
            }
        }

        // If we get here, this is a new tag
        tags.add(tag);
        saveTagsToStorage();
    }

    public void deleteTag(String tagId) {
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).getId().equals(tagId)) {
                tags.remove(i);
                saveTagsToStorage();
                return;
            }
        }
    }

    public Tag getTagById(String tagId) {
        for (Tag tag : tags) {
            if (tag.getId().equals(tagId)) {
                return tag;
            }
        }
        return null;
    }

    /**
     * Gets suggestions for tag values based on existing tags
     */
    public List<String> getTagSuggestions(String type, String prefix) {
        prefix = prefix.toLowerCase();
        List<String> suggestions = new ArrayList<>();
        
        for (Tag tag : tags) {
            if (tag.getType().equals(type) && 
                tag.getValue().toLowerCase().startsWith(prefix)) {
                suggestions.add(tag.getValue());
            }
        }
        return suggestions;
    }

    /**
     * Validates tag type and value
     */
    public boolean isValidTag(String type, String value) {
        // Check type
        if (!type.equals(TYPE_PERSON) && !type.equals(TYPE_LOCATION)) {
            return false;
        }
        
        // Check value
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Get all tags of a specific type
     */
    public List<Tag> getTagsByType(String type) {
        List<Tag> typedTags = new ArrayList<>();
        for (Tag tag : tags) {
            if (tag.getType().equals(type)) {
                typedTags.add(tag);
            }
        }
        return typedTags;
    }

    /**
     * Search for tags using fuzzy matching
     */
    public List<Tag> searchTags(String query) {
        if (query == null || query.length() < MIN_SEARCH_CHARS) {
            return new ArrayList<>();
        }
        
        query = query.toLowerCase();
        List<Tag> matches = new ArrayList<>();
        
        for (Tag tag : tags) {
            if (tag.getValue().toLowerCase().contains(query)) {
                matches.add(tag);
            }
        }
        return matches;
    }

    /**
     * Check if a tag value already exists for a given type
     */
    public boolean tagExists(String type, String value) {
        value = value.toLowerCase();
        for (Tag tag : tags) {
            if (tag.getType().equals(type) && 
                tag.getValue().toLowerCase().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get tag statistics
     */
    public TagStats getTagStats() {
        int personCount = 0;
        int locationCount = 0;
        
        for (Tag tag : tags) {
            if (tag.getType().equals(TYPE_PERSON)) {
                personCount++;
            } else if (tag.getType().equals(TYPE_LOCATION)) {
                locationCount++;
            }
        }
        
        return new TagStats(personCount, locationCount);
    }

    /**
     * Inner class for tag statistics
     */
    public static class TagStats {
        public final int personTagCount;
        public final int locationTagCount;
        
        public TagStats(int personTagCount, int locationTagCount) {
            this.personTagCount = personTagCount;
            this.locationTagCount = locationTagCount;
        }
    }
}