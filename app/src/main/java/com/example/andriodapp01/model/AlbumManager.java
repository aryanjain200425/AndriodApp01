package com.example.andriodapp01.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.andriodapp01.model.Album;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AlbumManager {
    private static final String PREF_NAME = "AlbumData";
    private static final String KEY_ALBUMS = "albums";

    private static AlbumManager instance;
    private final SharedPreferences preferences;
    private final Gson gson;
    private List<Album> albums;

    private AlbumManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadAlbums();
    }

    public static synchronized AlbumManager getInstance(Context context) {
        if (instance == null) {
            instance = new AlbumManager(context);
        }
        return instance;
    }

    private void loadAlbums() {
        String albumsJson = preferences.getString(KEY_ALBUMS, null);
        if (albumsJson != null) {
            Type type = new TypeToken<ArrayList<Album>>() {}.getType();
            albums = gson.fromJson(albumsJson, type);
        } else {
            albums = new ArrayList<>();
        }
    }

    private void saveAlbumsToStorage() {
        String albumsJson = gson.toJson(albums);
        preferences.edit().putString(KEY_ALBUMS, albumsJson).apply();
    }

    public List<Album> getAlbums() {
        return new ArrayList<>(albums); // Return a copy to prevent accidental modification
    }

    public void saveAlbum(Album album) {
        // Check if album already exists (by ID)
        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getId().equals(album.getId())) {
                albums.set(i, album); // Replace existing album
                saveAlbumsToStorage();
                return;
            }
        }

        // If we get here, this is a new album
        albums.add(album);
        saveAlbumsToStorage();
    }

    public void deleteAlbum(String albumId) {
        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getId().equals(albumId)) {
                albums.remove(i);
                saveAlbumsToStorage();
                return;
            }
        }
    }

    public Album getAlbumById(String albumId) {
        for (Album album : albums) {
            if (album.getId().equals(albumId)) {
                return album;
            }
        }
        return null;
    }
}