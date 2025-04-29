package com.example.andriodapp01.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp01.R;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private List<Album> albums;
    private Context context;
    private OnAlbumActionListener listener;

    // Interface for album actions
    public interface OnAlbumActionListener {
        void onAlbumClick(Album album);
        void onAlbumDelete(Album album);
    }

    public AlbumAdapter(List<Album> albums, Context context, OnAlbumActionListener listener) {
        this.albums = albums;
        this.context = context;
        this.listener = listener;
    }

    public void updateAlbums(List<Album> newAlbums) {
        this.albums = newAlbums;
        notifyDataSetChanged();
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
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAlbumClick(album);
            }
        });

        // Set click listener for delete button
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAlbumDelete(album);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public void removeAlbum(Album album) {
        int position = albums.indexOf(album);
        if (position != -1) {
            albums.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView albumCoverImageView;
        TextView albumNameTextView;
        TextView photoCountTextView;
        ImageButton deleteButton;

        AlbumViewHolder(View itemView) {
            super(itemView);
            albumCoverImageView = itemView.findViewById(R.id.albumCoverImageView);
            albumNameTextView = itemView.findViewById(R.id.albumNameTextView);
            photoCountTextView = itemView.findViewById(R.id.photoCountTextView);
            deleteButton = itemView.findViewById(R.id.btnDeleteAlbum);
        }
    }
}