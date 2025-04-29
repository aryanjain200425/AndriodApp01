package com.example.andriodapp01.model;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp01.R;
import com.example.andriodapp01.model.Album;
import com.example.andriodapp01.model.AlbumManager;
import com.example.andriodapp01.model.Photo;

import java.util.ArrayList;
import java.util.List;

public class MovePhotoDialog extends DialogFragment {

    private Photo photoToMove;
    private Album sourceAlbum;
    private List<Album> availableAlbums;
    private OnPhotoMoveListener listener;

    public interface OnPhotoMoveListener {
        void onPhotoMoved(Photo photo, Album sourceAlbum, Album destinationAlbum);
    }

    public static MovePhotoDialog newInstance(Photo photo, Album sourceAlbum) {
        MovePhotoDialog dialog = new MovePhotoDialog();
        dialog.photoToMove = photo;
        dialog.sourceAlbum = sourceAlbum;
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnPhotoMoveListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement OnPhotoMoveListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_move_photo, null);

        // Load albums
        AlbumManager albumManager = AlbumManager.getInstance(requireContext());
        availableAlbums = new ArrayList<>();

        // Filter out the source album
        for (Album album : albumManager.getAlbums()) {
            if (!album.getId().equals(sourceAlbum.getId())) {
                availableAlbums.add(album);
            }
        }

        RecyclerView recyclerView = view.findViewById(R.id.albumRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // No albums to move to
        if (availableAlbums.isEmpty()) {
            TextView emptyText = view.findViewById(R.id.emptyTextView);
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            AlbumSelectorAdapter adapter = new AlbumSelectorAdapter(availableAlbums, album -> {
                // Handle album selection
                listener.onPhotoMoved(photoToMove, sourceAlbum, album);
                dismiss();
            });
            recyclerView.setAdapter(adapter);
        }

        builder.setTitle("Move to Album")
                .setView(view)
                .setNegativeButton("Cancel", (dialog, id) -> dismiss());

        return builder.create();
    }

    // Album selector adapter for the dialog
    private static class AlbumSelectorAdapter extends RecyclerView.Adapter<AlbumSelectorAdapter.ViewHolder> {

        private List<Album> albums;
        private OnAlbumSelectedListener listener;

        interface OnAlbumSelectedListener {
            void onAlbumSelected(Album album);
        }

        AlbumSelectorAdapter(List<Album> albums, OnAlbumSelectedListener listener) {
            this.albums = albums;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Album album = albums.get(position);
            holder.textView.setText(album.getName());
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAlbumSelected(album);
                }
            });
        }

        @Override
        public int getItemCount() {
            return albums.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}