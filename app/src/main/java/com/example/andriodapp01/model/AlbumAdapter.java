package com.example.andriodapp01.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp01.R;
import com.example.andriodapp01.model.Album;
import com.example.andriodapp01.model.Photo;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private List<Album> albums;
    private Context context;

    public AlbumAdapter(List<Album> albums, Context context) {
        this.albums = albums;
        this.context = context;
    }

    public void updateAlbums(List<Album> newAlbums) {
        this.albums = newAlbums;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);

        // Set album name
        holder.albumNameTextView.setText(album.getName());

        // Set photo count
        String photoCountText = album.getPhotoCount() + " " +
                (album.getPhotoCount() == 1 ? "photo" : "photos");
        holder.photoCountTextView.setText(photoCountText);

        // Set cover image if available
        Photo coverPhoto = album.getCoverPhoto();
        if (coverPhoto != null) {
            // Load bitmap from local storage
            Bitmap bitmap = coverPhoto.getBitmap();
            if (bitmap != null) {
                holder.albumCoverImageView.setImageBitmap(bitmap);
            } else {
                // Set placeholder if bitmap couldn't be loaded
                holder.albumCoverImageView.setImageResource(R.drawable.placeholder_album);
            }
        } else {
            // Set placeholder if no cover photo
            holder.albumCoverImageView.setImageResource(R.drawable.placeholder_album);
        }

        // Set click listener to open album
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Open album detail activity
                // This functionality could be implemented in future updates
            }
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView albumCoverImageView;
        TextView albumNameTextView;
        TextView photoCountTextView;

        AlbumViewHolder(View itemView) {
            super(itemView);
            albumCoverImageView = itemView.findViewById(R.id.albumCoverImageView);
            albumNameTextView = itemView.findViewById(R.id.albumNameTextView);
            photoCountTextView = itemView.findViewById(R.id.photoCountTextView);
        }
    }
}