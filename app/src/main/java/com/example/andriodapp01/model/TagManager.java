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
}