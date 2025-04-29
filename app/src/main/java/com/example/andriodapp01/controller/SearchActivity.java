package com.example.andriodapp01.controller;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp01.R;
import com.example.andriodapp01.model.Album;
import com.example.andriodapp01.model.AlbumManager;
import com.example.andriodapp01.model.Photo;
import com.example.andriodapp01.model.PhotoAdapter;
import com.example.andriodapp01.model.Tag;
import com.example.andriodapp01.model.TagManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity implements PhotoAdapter.OnPhotoActionListener {

    private AutoCompleteTextView personAutoComplete;
    private AutoCompleteTextView locationAutoComplete;
    private RadioButton radioAnd;
    private RecyclerView searchResultsRecyclerView;
    private TextView resultsHeaderText;
    private Button btnPerformSearch;

    private AlbumManager albumManager;
    private TagManager tagManager;
    private PhotoAdapter photoAdapter;
    private List<Photo> searchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize managers
        albumManager = AlbumManager.getInstance(this);
        tagManager = TagManager.getInstance(this);

        // Initialize views
        Toolbar searchToolbar = findViewById(R.id.searchToolbar);
        personAutoComplete = findViewById(R.id.personAutoComplete);
        locationAutoComplete = findViewById(R.id.locationAutoComplete);
        radioAnd = findViewById(R.id.radioAnd);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        resultsHeaderText = findViewById(R.id.resultsHeaderText);
        btnPerformSearch = findViewById(R.id.btnPerformSearch);

        // Set up toolbar
        setSupportActionBar(searchToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set up RecyclerView
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        photoAdapter = new PhotoAdapter(searchResults, this, this);
        searchResultsRecyclerView.setAdapter(photoAdapter);

        // Set up auto-complete adapters
        setupAutoCompleteAdapters();

        // Set up search button
        btnPerformSearch.setOnClickListener(v -> performSearch());
    }

    private void setupAutoCompleteAdapters() {
        // Get all tags
        List<Tag> allTags = tagManager.getTags();

        // Filter person tags and get unique values
        List<String> personValues = allTags.stream()
                .filter(tag -> Tag.TYPE_PERSON.equals(tag.getType()))
                .map(Tag::getValue)
                .distinct()
                .collect(Collectors.toList());

        // Filter location tags and get unique values
        List<String> locationValues = allTags.stream()
                .filter(tag -> Tag.TYPE_LOCATION.equals(tag.getType()))
                .map(Tag::getValue)
                .distinct()
                .collect(Collectors.toList());

        // Create adapters
        ArrayAdapter<String> personAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, personValues);
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, locationValues);

        // Set the adapters to AutoCompleteTextViews
        personAutoComplete.setAdapter(personAdapter);
        locationAutoComplete.setAdapter(locationAdapter);

        // Set text change listeners for dynamic filtering
        personAutoComplete.addTextChangedListener(createAutoCompleteWatcher(Tag.TYPE_PERSON));
        locationAutoComplete.addTextChangedListener(createAutoCompleteWatcher(Tag.TYPE_LOCATION));
    }

    private TextWatcher createAutoCompleteWatcher(final String tagType) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Get typed prefix
                String prefix = s.toString().toLowerCase();

                // Get matching tag values
                List<Tag> allTags = tagManager.getTags();
                List<String> matchingValues = allTags.stream()
                        .filter(tag -> tag.getType().equals(tagType) &&
                                tag.getValue().toLowerCase().startsWith(prefix))
                        .map(Tag::getValue)
                        .distinct()
                        .collect(Collectors.toList());

                // Update adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        SearchActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        matchingValues);

                if (tagType.equals(Tag.TYPE_PERSON)) {
                    personAutoComplete.setAdapter(adapter);
                } else {
                    locationAutoComplete.setAdapter(adapter);
                }

                if (s.length() > 0) {
                    adapter.getFilter().filter(s);
                    if (tagType.equals(Tag.TYPE_PERSON)) {
                        personAutoComplete.showDropDown();
                    } else {
                        locationAutoComplete.showDropDown();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        };
    }

    private void performSearch() {
        String personQuery = personAutoComplete.getText().toString().trim().toLowerCase();
        String locationQuery = locationAutoComplete.getText().toString().trim().toLowerCase();

        // Skip search if both fields are empty
        if (personQuery.isEmpty() && locationQuery.isEmpty()) {
            Toast.makeText(this, "Please enter at least one search term", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get all photos from all albums
        List<Photo> allPhotos = new ArrayList<>();
        List<Album> albums = albumManager.getAlbums();
        for (Album album : albums) {
            allPhotos.addAll(album.getPhotos());
        }

        // Perform search
        Set<Photo> matchedPhotos = new HashSet<>();

        if (radioAnd.isChecked()) {
            // AND logic - photo must match both criteria if both are provided
            for (Photo photo : allPhotos) {
                boolean personMatch = personQuery.isEmpty() || matchesTag(photo, Tag.TYPE_PERSON, personQuery);
                boolean locationMatch = locationQuery.isEmpty() || matchesTag(photo, Tag.TYPE_LOCATION, locationQuery);

                if (personMatch && locationMatch) {
                    matchedPhotos.add(photo);
                }
            }
        } else {
            // OR logic - photo can match either criteria
            for (Photo photo : allPhotos) {
                if (!personQuery.isEmpty() && matchesTag(photo, Tag.TYPE_PERSON, personQuery)) {
                    matchedPhotos.add(photo);
                }

                if (!locationQuery.isEmpty() && matchesTag(photo, Tag.TYPE_LOCATION, locationQuery)) {
                    matchedPhotos.add(photo);
                }
            }
        }

        // Update UI with results
        searchResults.clear();
        searchResults.addAll(matchedPhotos);
        photoAdapter.updatePhotos(searchResults);

        // Show/hide results header
        if (searchResults.isEmpty()) {
            resultsHeaderText.setText(R.string.no_results_found);
        } else {
            resultsHeaderText.setText(getString(R.string.search_results) + " (" + searchResults.size() + ")");
        }
        resultsHeaderText.setVisibility(View.VISIBLE);
    }

    private boolean matchesTag(Photo photo, String tagType, String query) {
        List<String> tagIds = photo.getTagIds();
        for (String tagId : tagIds) {
            Tag tag = tagManager.getTagById(tagId);
            if (tag != null && tag.getType().equals(tagType)) {
                // Check if tag value starts with the query (case insensitive)
                if (tag.getValue().toLowerCase().startsWith(query.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // PhotoAdapter.OnPhotoActionListener implementations
    @Override
    public void onPhotoClick(Photo photo, int position) {
        // Navigate to photo detail view if needed
        Toast.makeText(this, "Photo clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPhotoDelete(Photo photo, int position) {
        // Not applicable in search results
        Toast.makeText(this, "Cannot delete photos from search results", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddTag(Photo photo, int position) {
        // Not applicable in search results
        Toast.makeText(this, "Cannot add tags from search results", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTagClick(String tagId, Photo photo) {
        // Show tag details or filter by this tag
        Tag tag = tagManager.getTagById(tagId);
        if (tag != null) {
            Toast.makeText(this, "Tag: " + tag.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMovePhoto(Photo photo, int position) {
        // Not applicable in search results
        Toast.makeText(this, "Cannot move photos from search results", Toast.LENGTH_SHORT).show();
    }
}