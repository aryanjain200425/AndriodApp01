package com.example.andriodapp01.controller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity implements PhotoAdapter.OnPhotoActionListener {

    private static final String SEARCH_HISTORY_PREF = "search_history";
    private static final int MAX_HISTORY_ITEMS = 10;
    private static final int MIN_SEARCH_LENGTH = 2;
    private static final double SIMILARITY_THRESHOLD = 0.8;
    private AutoCompleteTextView personAutoComplete;
    private AutoCompleteTextView locationAutoComplete;
    private RadioButton radioAnd;
    private RecyclerView searchResultsRecyclerView;
    private TextView resultsHeaderText;
    private Button btnPerformSearch;
    private ImageButton clearSearchButton;
    private Button showHistoryButton;

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
        clearSearchButton = findViewById(R.id.clearSearchButton);
        showHistoryButton = findViewById(R.id.showHistoryButton);

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

        // Set up clear button
        setupClearButton();
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

        // After performing the search, save to history
        if (!personQuery.isEmpty() || !locationQuery.isEmpty()) {
            saveSearchToHistory(personQuery, locationQuery);
        }

        // Add fuzzy matching for better results
        searchResults.addAll(performFuzzySearch(personQuery, locationQuery));
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

    private void saveSearchToHistory(String person, String location) {
        SharedPreferences prefs = getSharedPreferences(SEARCH_HISTORY_PREF, MODE_PRIVATE);
        Set<String> history = new HashSet<>(prefs.getStringSet("history", new HashSet<>()));

        String searchEntry = person + "|" + location;
        history.add(searchEntry);

        // Limit history size
        if (history.size() > MAX_HISTORY_ITEMS) {
            List<String> historyList = new ArrayList<>(history);
            history = new HashSet<>(historyList.subList(historyList.size() - MAX_HISTORY_ITEMS, historyList.size()));
        }

        prefs.edit().putStringSet("history", history).apply();
    }

    private List<Photo> performFuzzySearch(String person, String location) {
        List<Photo> fuzzyMatches = new ArrayList<>();

        for (Album album : albumManager.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                if (fuzzyMatchesTags(photo, person, location)) {
                    fuzzyMatches.add(photo);
                }
            }
        }

        return fuzzyMatches;
    }

    private boolean fuzzyMatchesTags(Photo photo, String person, String location) {
        double threshold = SIMILARITY_THRESHOLD; // Similarity threshold

        for (String tagId : photo.getTagIds()) {
            Tag tag = tagManager.getTagById(tagId);
            if (tag != null) {
                if (tag.getType().equals(Tag.TYPE_PERSON) && !person.isEmpty()) {
                    double similarity = calculateSimilarity(tag.getValue().toLowerCase(), person.toLowerCase());
                    if (similarity >= threshold) return true;
                }
                if (tag.getType().equals(Tag.TYPE_LOCATION) && !location.isEmpty()) {
                    double similarity = calculateSimilarity(tag.getValue().toLowerCase(), location.toLowerCase());
                    if (similarity >= threshold) return true;
                }
            }
        }
        return false;
    }

    private double calculateSimilarity(String s1, String s2) {
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        return 1.0 - ((double) distance / maxLength);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private void setupClearButton() {
        clearSearchButton.setOnClickListener(v -> {
            personAutoComplete.setText("");
            locationAutoComplete.setText("");
            searchResults.clear();
            photoAdapter.notifyDataSetChanged();
            resultsHeaderText.setVisibility(View.GONE);
        });
    }

    private void showSearchHistory() {
        SharedPreferences prefs = getSharedPreferences(SEARCH_HISTORY_PREF, MODE_PRIVATE);
        Set<String> history = prefs.getStringSet("history", new HashSet<>());

        if (history.isEmpty()) {
            Toast.makeText(this, "No search history", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> historyList = new ArrayList<>(history);
        Collections.sort(historyList);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Search History")
                .setItems(historyList.toArray(new String[0]), (dialog, which) -> {
                    String[] parts = historyList.get(which).split("\\|");
                    personAutoComplete.setText(parts[0]);
                    locationAutoComplete.setText(parts.length > 1 ? parts[1] : "");
                    performSearch();
                })
                .setNeutralButton("Clear History", (dialog, which) -> {
                    prefs.edit().clear().apply();
                    Toast.makeText(this, "Search history cleared", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void setupSearchFilters() {
        // Add chips for quick filters
        ChipGroup filterChipGroup = findViewById(R.id.filterChipGroup);

        // Get common tags
        List<Tag> commonPersonTags = getCommonTags(Tag.TYPE_PERSON, 5);
        List<Tag> commonLocationTags = getCommonTags(Tag.TYPE_LOCATION, 5);

        // Add person filter chips
        for (Tag tag : commonPersonTags) {
            Chip chip = new Chip(this);
            chip.setText(tag.getValue());
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    personAutoComplete.setText(tag.getValue());
                } else if (personAutoComplete.getText().toString().equals(tag.getValue())) {
                    personAutoComplete.setText("");
                }
                performSearch();
            });
            filterChipGroup.addView(chip);
        }

        // Add location filter chips
        for (Tag tag : commonLocationTags) {
            Chip chip = new Chip(this);
            chip.setText(tag.getValue());
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    locationAutoComplete.setText(tag.getValue());
                } else if (locationAutoComplete.getText().toString().equals(tag.getValue())) {
                    locationAutoComplete.setText("");
                }
                performSearch();
            });
            filterChipGroup.addView(chip);
        }
    }

    private List<Tag> getCommonTags(String tagType, int limit) {
        Map<String, Integer> tagFrequency = new HashMap<>();

        // Count tag occurrences
        for (Album album : albumManager.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                for (String tagId : photo.getTagIds()) {
                    Tag tag = tagManager.getTagById(tagId);
                    if (tag != null && tag.getType().equals(tagType)) {
                        tagFrequency.merge(tag.getValue(), 1, Integer::sum);
                    }
                }
            }
        }

        // Sort by frequency and return top results
        return tagFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new Tag(tagType, entry.getKey()))
                .collect(Collectors.toList());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupSearchFilters();
        updateSearchSuggestions();
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