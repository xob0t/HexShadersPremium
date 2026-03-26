# HexShaders

Android live wallpaper that renders animated GLSL shader effects through a hexagonal grid of point sprites using OpenGL ES 2.0. Includes 25 shader effects with per-shader configuration (detail level, animation speed, colors).

Based on [HexShaders](https://github.com/KonyukhovSergey/HexShaders) by [KonyukhovSergey](https://github.com/KonyukhovSergey).

## Features

- 25 animated GLSL shader effects (flame, water, galaxy, metaballs, etc.)
- Per-shader settings: detail level, animation speed, RGB color customization
- FPS-adaptive rendering for battery efficiency
- Dynamic WallpaperColors (Android 12+) — system accent matches the active shader and user color choices
- Slideshow rendering mode with triple-buffered render-to-texture

## Building

Requires Android SDK with platform 36 and JDK 17+.

```
./gradlew :app:assembleDebug
```

APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Install

```
./gradlew :app:installDebug
```

## Tech Stack

- Kotlin, Android SDK 36 (minSdk 21)
- OpenGL ES 2.0 with GLSL shaders
- ViewBinding
- R8 minification + resource shrinking for release builds
- Gradle 8.12, AGP 8.8.2

## License

[MIT](https://github.com/KonyukhovSergey/HexShaders/blob/master/LICENSE) (same as the original repo)
