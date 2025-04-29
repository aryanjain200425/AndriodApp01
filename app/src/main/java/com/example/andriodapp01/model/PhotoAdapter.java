package com.example.andriodapp01.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp01.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private List<Photo> photos;
    private Context context;
    private OnPhotoActionListener listener;
    private Album currentAlbum;

    // Interface for photo actions
    public interface OnPhotoActionListener {
        void onPhotoClick(Photo photo, int position);
        void onPhotoDelete(Photo photo, int position);
        void onAddTag(Photo photo, int position);
        void onTagClick(String tagId, Photo photo);
        void onMovePhoto(Photo photo, int position);
    }

    public PhotoAdapter(List<Photo> photos, Context context, OnPhotoActionListener listener) {
        this.photos = photos;
        this.context = context;
        this.listener = listener;
        this.currentAlbum = null; // Will be set separately if needed
    }

    public void setCurrentAlbum(Album album) {
        this.currentAlbum = album;
    }

    public void updatePhotos(List<Photo> newPhotos) {
        this.photos = newPhotos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);
        final int adapterPosition = position;

        // Load photo bitmap
        Bitmap bitmap = photo.getBitmap();
        if (bitmap != null) {
            holder.photoImageView.setImageBitmap(bitmap);
        } else {
            holder.photoImageView.setImageResource(R.drawable.placeholder_album);
        }

        // Set up tag chips (if needed)
        setupTagChips(holder, photo);

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoClick(photo, adapterPosition);
            }
        });

        holder.btnAddTag.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddTag(photo, adapterPosition);
            }
        });

        holder.btnMovePhoto.setOnClickListener(v -> {
            if (listener != null && currentAlbum != null) {
                listener.onMovePhoto(photo, adapterPosition);
            }
        });

        holder.btnDeletePhoto.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoDelete(photo, adapterPosition);
            }
        });
    }

    private void setupTagChips(PhotoViewHolder holder, Photo photo) {
        // Clear existing chips
        holder.tagChipGroup.removeAllViews();

        List<String> tagIds = photo.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            holder.tagChipGroup.setVisibility(View.VISIBLE);

            // Add tag chips dynamically based on photo.getTagIds()
            for (String tagId : tagIds) {
                Chip chip = new Chip(context);
                chip.setText(tagId);
                chip.setCloseIconVisible(true);
                chip.setClickable(true);

                // Handle click events
                chip.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onTagClick(tagId, photo);
                    }
                });

                // Handle close icon clicks (to remove tags)
                chip.setOnCloseIconClickListener(v -> {
                    photo.removeTagId(tagId);
                    holder.tagChipGroup.removeView(chip);

                    // If no tags left, hide the chip group
                    if (photo.getTagIds().isEmpty()) {
                        holder.tagChipGroup.setVisibility(View.GONE);
                    }

                    // Save changes if part of an album
                    if (currentAlbum != null) {
                        AlbumManager.getInstance(context).saveAlbum(currentAlbum);
                    }
                });

                holder.tagChipGroup.addView(chip);
            }
        } else {
            holder.tagChipGroup.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public void removePhoto(Photo photo) {
        int position = photos.indexOf(photo);
        if (position != -1) {
            photos.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;
        ChipGroup tagChipGroup;
        ImageButton btnAddTag;
        ImageButton btnMovePhoto;
        ImageButton btnDeletePhoto;

        PhotoViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            tagChipGroup = itemView.findViewById(R.id.tagChipGroup);
            btnAddTag = itemView.findViewById(R.id.btnAddTag);
            btnMovePhoto = itemView.findViewById(R.id.btnMovePhoto);
            btnDeletePhoto = itemView.findViewById(R.id.btnDeletePhoto);
        }
    }
}