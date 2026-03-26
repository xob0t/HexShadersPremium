package ru.serjik.utils;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class AssetsUtils {
    public static String readText(String path, AssetManager assets) {
        InputStream is = null;
        try {
            is = assets.open(path);
            return StreamUtils.toString(is);
        } catch (IOException e) {
            SerjikLog.log(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            StreamUtils.closeQuietly(is);
        }
    }

    public static Bitmap readBitmap(String path, AssetManager assets) {
        InputStream is = null;
        try {
            is = assets.open(path);
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            SerjikLog.log(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            StreamUtils.closeQuietly(is);
        }
    }
}
