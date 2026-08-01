# Walldo - Walls from Wallhaven (Android App)

![App Logo](images/icon.png)

## Overview

Walldo is an open-source Android application built around the
Wallhaven.cc API. The project emphasizes clean architecture,
maintainability, and modern Android development practices. It is built
using MVVM, Dependency Injection, REST APIs, and Material 3 while
gradually migrating to Jetpack Compose.

## Impact

-   📱 10,000+ Google Play downloads
-   ⭐ 200+ user reviews
-   ⭐ 4.3 average rating
-   🌍 Open source on GitHub

## Tech Stack

### Languages

-   Kotlin
-   Java

### Architecture

-   MVVM
-   Repository Pattern
-   Dependency Injection (Dagger Hilt)

### Networking

-   Retrofit
-   Wallhaven REST API

### Local Storage

-   Room
-   Android DataStore

### Background Processing

-   WorkManager

### UI

-   Material 3
-   Dynamic Colors
-   XML (currently migrating to Jetpack Compose)

### Other Technologies

-   Paging 3
-   Glide
-   Lottie
-   Firebase Analytics
-   Firebase Crashlytics
-   Google Play Billing v6

## Features

-   Minimal, Material 3 interface
-   Browse and search wallpapers from Wallhaven.cc
-   Favorites with local persistence
-   Automatic wallpaper scheduling using WorkManager
-   Dynamic color support
-   Infinite scrolling with Paging 3
-   Firebase Analytics and Crashlytics integration
-   Google Play Billing support

## Architecture

Walldo follows the MVVM architecture to separate presentation, business
logic, and data layers.

-   MVVM Architecture
-   Repository Pattern
-   Dependency Injection with Dagger Hilt
-   Kotlin Coroutines and Flow
-   Retrofit for REST API communication
-   Room for local persistence
-   WorkManager for background tasks

## Development Practices

-   Git version control
-   Open-source development on GitHub
-   Iterative feature development
-   Modular, maintainable code organization
-   Technical documentation for setup and maintenance

## Running the Project

1.  Add `google-services.json` to `app/src/main`.
2.  Add `googlePlayLicenseKey` to `local.properties`.
3.  Build and run the application.

## Project Structure

``` text
app/
├── data/
├── domain/
├── ui/
├── di/
├── workers/
└── utils/
```

## Key Libraries

  Library                  Purpose
  ------------------------ -----------------------
  Dagger Hilt              Dependency Injection
  Retrofit                 REST API client
  Room                     Local database
  Paging 3                 Infinite scrolling
  WorkManager              Background scheduling
  Glide                    Image loading
  Lottie                   Animations
  DataStore                Local preferences
  Firebase Analytics       Usage analytics
  Firebase Crashlytics     Crash reporting
  Google Play Billing v6   In-app purchases

## Screenshots

::: {style="display:flex; flex-wrap:wrap;"}
`<img alt="App image" src="images/walldo1.png" width="30%">`{=html}
`<img alt="App image" src="images/walldo2.png" width="30%">`{=html}
`<img alt="App image" src="images/walldo3.png" width="30%">`{=html}
`<img alt="App image" src="images/walldo4.png" width="30%">`{=html}
`<img alt="App image" src="images/walldo5.png" width="30%">`{=html}
`<img alt="App image" src="images/walldo6.png" width="30%">`{=html}
:::

## Roadmap

-   Complete Jetpack Compose migration
-   Improve offline wallpaper caching
-   Tablet optimization
-   Additional Material You enhancements

## Links

-   **GitHub:** https://github.com/wekomodo/walldo
-   **Wallhaven API:** https://wallhaven.cc/help/api
-   **Google Play:**
    https://play.google.com/store/apps/details?id=com.enigmaticdevs.wallhaven
