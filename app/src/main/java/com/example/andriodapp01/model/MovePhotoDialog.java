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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        View view = getLayoutInflater().inflate(R.layout.dialog_move_photo, null);
        
        // Load albums excluding source album
        AlbumManager albumManager = AlbumManager.getInstance(requireContext());
        availableAlbums = new ArrayList<>();
        
        for (Album album : albumManager.getAlbums()) {
            if (!album.getId().equals(sourceAlbum.getId())) {
                availableAlbums.add(album);
            }
        }

        RecyclerView recyclerView = view.findViewById(R.id.albumRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        if (availableAlbums.isEmpty()) {
            view.findViewById(R.id.emptyTextView).setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            AlbumSelectorAdapter adapter = new AlbumSelectorAdapter(availableAlbums, 
                album -> {
                    listener.onPhotoMoved(photoToMove, sourceAlbum, album);
                    dismiss();
                });
            recyclerView.setAdapter(adapter);
        }

        return builder.setTitle("Move to Album")
                     .setView(view)
                     .setNegativeButton("Cancel", (dialog, id) -> dismiss())
                     .create();
    }
}