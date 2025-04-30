# Photo Album Android App

A feature-rich Android application for managing photo albums with tagging and search capabilities.

## Features

### Album Management
- Create new albums with custom names
- Edit album names
- Delete albums
- View all albums in a grid layout
- Sort albums by name or date
- Filter albums by name

### Photo Management
- Add photos to albums (up to 20 photos per album)
- Remove photos from albums
- Move photos between albums
- Batch operations (delete/move multiple photos)
- Drag and drop to reorder photos
- Full-screen photo preview

### Photo Tags
- Add person and location tags to photos
- Auto-complete suggestions for existing tags
- Search photos by tags
- Filter photos within albums
- Tag-based photo organization

### Slideshow
- View photos in slideshow mode
- Swipe gestures for navigation
- Photo captions display
- Rotation animations
- Automatic photo transitions

### Search
- Search across all albums
- Search by person tags
- Search by location tags
- Combine searches with AND/OR operators
- Search history tracking
- Auto-complete suggestions

## Technical Details

### Architecture
- Model-View-Controller (MVC) pattern
- Singleton pattern for managers
- Observer pattern for UI updates

### Key Components
- `AlbumManager`: Handles album CRUD operations
- `TagManager`: Manages photo tags
- `PhotoAdapter`: Displays photos in RecyclerView
- `AlbumAdapter`: Displays albums in RecyclerView

### Storage
- Local storage for photos
- SharedPreferences for app settings
- In-memory cache for better performance

## Requirements

- Android SDK 21 or higher
- Storage permission for accessing photos
- 2GB RAM minimum
- 100MB free storage space

## Installation

1. Clone the repository
2. Open in Android Studio
3. Build and run on your device

## Usage

1. Launch the app
2. Create a new album using the + button
3. Add photos to your album
4. Add tags to organize photos
5. Use search to find specific photos
6. Enjoy the slideshow feature

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Android Material Design Components
- Glide Image Loading Library
- AndroidX Libraries