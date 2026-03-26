# HexShadersPremium ProGuard Rules

# Keep wallpaper service (referenced from AndroidManifest.xml)
-keep class ru.serjik.hexshaders.premium.HexShadersService { *; }

# Keep preference controllers instantiated via Class.forName() reflection
-keep class ru.serjik.preferences.controllers.**Controller { *; }

# Keep WallpaperService engine inner classes
-keep class ru.serjik.wallpaper.GLWallpaperService$* { *; }
