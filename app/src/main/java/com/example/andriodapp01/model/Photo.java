package com.example.andriodapp01.model;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Photo implements Serializable {
    private String id;
    private String localPath; // Path to a local copy of the image
    private Date dateAdded;
    private List<String> tagIds; // Store IDs of associated tags

    public Photo(String id) {
        this.id = id;
        this.dateAdded = new Date();
        this.tagIds = new ArrayList<>();
    }

    // Constructor for new photos
    public Photo() {
        this(UUID.randomUUID().toString());
    }

    // Save photo content from URI to app's internal storage
    public boolean savePhotoFromUri(Context context, Uri uri) {
        try {
            ContentResolver resolver = context.getContentResolver();
            InputStream inputStream = resolver.openInputStream(uri);

            if (inputStream == null) {
                Log.e("Photo", "Failed to open input stream for URI: " + uri);
                return false;
            }

            // Create directory for photos if it doesn't exist
            File photoDir = new File(context.getFilesDir(), "photos");
            if (!photoDir.exists()) {
                photoDir.mkdirs();
            }

            // Create file to save the photo
            File photoFile = new File(photoDir, id + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(photoFile);

            // Copy the image data
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            inputStream.close();
            outputStream.flush();
            outputStream.close();

            // Save the local path
            this.localPath = photoFile.getAbsolutePath();

            return true;
        } catch (IOException e) {
            Log.e("Photo", "Error saving photo: " + e.getMessage());
            return false;
        }
    }

    public String getId() {
        return id;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public Uri getUri() {
        return Uri.parse("file://" + localPath);
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public List<String> getTagIds() {
        return tagIds;
    }

    public void addTagId(String tagId) {
        if (!tagIds.contains(tagId)) {
            tagIds.add(tagId);
        }
    }

    public void removeTagId(String tagId) {
        tagIds.remove(tagId);
    }

    // Helper method to check if photo has a tag of specific type
    public boolean hasTagOfType(Context context, String tagType) {
        TagManager tagManager = TagManager.getInstance(context);
        for (String tagId : tagIds) {
            Tag tag = tagManager.getTagById(tagId);
            if (tag != null && tag.getType().equals(tagType)) {
                return true;
            }
        }
        return false;
    }

    // Get tags of a specific type
    public List<Tag> getTagsOfType(Context context, String tagType) {
        List<Tag> result = new ArrayList<>();
        TagManager tagManager = TagManager.getInstance(context);

        for (String tagId : tagIds) {
            Tag tag = tagManager.getTagById(tagId);
            if (tag != null && tag.getType().equals(tagType)) {
                result.add(tag);
            }
        }

        return result;
    }

    // Get a Bitmap for the photo
    public Bitmap getBitmap() {
        if (localPath != null) {
            return BitmapFactory.decodeFile(localPath);
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Photo photo = (Photo) obj;
        return id.equals(photo.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}