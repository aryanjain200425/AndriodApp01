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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private List<Photo> photos;
    private Context context;
    private OnPhotoActionListener listener;

    // Interface for photo actions
    public interface OnPhotoActionListener {
        void onPhotoClick(Photo photo, int position);
        void onPhotoDelete(Photo photo, int position);
        void onAddTag(Photo photo, int position);
        void onTagClick(String tagId, Photo photo);
    }

    public PhotoAdapter(List<Photo> photos, Context context, OnPhotoActionListener listener) {
        this.photos = photos;
        this.context = context;
        this.listener = listener;
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

        // Load photo image
        Bitmap bitmap = photo.getBitmap();
        if (bitmap != null) {
            holder.photoImageView.setImageBitmap(bitmap);
        } else {
            holder.photoImageView.setImageResource(R.drawable.placeholder_album);
        }

        // Clear any existing tags
        holder.tagChipGroup.removeAllViews();

        // Add tags if the photo has any
        List<String> tagIds = photo.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            // Show tag chip group if there are tags
            holder.tagChipGroup.setVisibility(View.VISIBLE);

            for (String tagId : tagIds) {
                // Here you would get the actual tag name from a TagManager
                // For now, just using the tagId as the display text
                Chip chip = new Chip(context);
                chip.setText(tagId);
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(v -> {
                    // Remove tag from photo
                    photo.removeTagId(tagId);
                    holder.tagChipGroup.removeView(chip);

                    // Hide the chip group if no tags left
                    if (holder.tagChipGroup.getChildCount() == 0) {
                        holder.tagChipGroup.setVisibility(View.GONE);
                    }
                });
                chip.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onTagClick(tagId, photo);
                    }
                });
                holder.tagChipGroup.addView(chip);
            }
        } else {
            // Hide tag chip group if there are no tags
            holder.tagChipGroup.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.photoImageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoClick(photo, holder.getAdapterPosition());
            }
        });

        holder.btnDeletePhoto.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Delete Photo")
                    .setMessage("Are you sure you want to delete this photo?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                            listener.onPhotoDelete(photo, adapterPosition);
                        }
                    })
                    .show();
        });

        holder.btnAddTag.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddTag(photo, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public void removePhoto(int position) {
        if (position >= 0 && position < photos.size()) {
            photos.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;
        ChipGroup tagChipGroup;
        ImageButton btnAddTag;
        ImageButton btnDeletePhoto;

        PhotoViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            tagChipGroup = itemView.findViewById(R.id.tagChipGroup);
            btnAddTag = itemView.findViewById(R.id.btnAddTag);
            btnDeletePhoto = itemView.findViewById(R.id.btnDeletePhoto);
        }
    }
}