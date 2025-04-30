# Photos Android App

An Android port of the JavaFX Photos application for CS213 (Software Methodology) at Rutgers University.

## Contributors
- Aditya Sharma
- Aryan Jain

## Project Overview
This Android application allows users to manage their photo collections through albums, with features for tagging and searching photos. The app is designed to run on Android API 34 devices with a display specification of 1080 x 2400 420dpi.

## Core Features

### Album Management (25 points)
- Create new albums
- Delete existing albums
- Rename albums
- View album contents with photo thumbnails
- Grid layout display of albums

### Photo Operations (25 points)
- Add photos to albums
- Remove photos from albums
- Display photos in full screen
- Slideshow functionality with manual navigation controls
- Maximum 20 photos per album

### Photo Tagging (15 points)
- Add tags to photos (limited to person and location types)
- Delete tags from photos
- View tags when displaying photos
- Case-insensitive tag handling

### Photo Movement (10 points)
- Move photos between albums
- Maintain tag information during moves

### Search Functionality (30 points)
- Search photos across all albums
- Search by tag-value pairs
- Support for AND/OR operations in searches
- Auto-completion for tag values
- Case-insensitive search matching

### Home Screen (15 points)
- Display all albums
- Load previous session data
- Quick access to all main features

## Technical Requirements

### Development Environment
- Android Studio
- Java (no Kotlin)
- Android API 34 (minimum SDK)
- Target device: Pixel 6 or equivalent (1080 x 2400 420dpi)

### Build Configuration
```gradle
compileSdk=34
minSdk=34
targetSdk=34
```

### Implementation Notes
- UI built using Android XML layouts
- Local storage for photo data
- Custom data serialization
- Error handling for invalid operations

## Installation

1. Clone the repository
2. Open in Android Studio
3. Configure an emulator with API 34 and 1080 x 2400 resolution
4. Build and run the application

## Usage

1. Launch the application
2. Create albums using the + button
3. Add photos to albums from device storage
4. Tag photos with person or location information
5. Use search to find photos across albums
6. Navigate photos using the slideshow feature

## Course Information
- Course: CS213 Software Methodology
- Assignment: Android App (Assignment 4)
- Semester: Spring 2024
- Institution: Rutgers University

## License

This project is an academic assignment and is not licensed for public use or distribution.