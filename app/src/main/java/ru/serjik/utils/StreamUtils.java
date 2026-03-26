package ru.serjik.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class StreamUtils {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static OutputStream copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        return out;
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    public static byte[] toByteArray(InputStream in) {
        try {
            return ((ByteArrayOutputStream) copy(in, new ByteArrayOutputStream())).toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toString(InputStream in) {
        return new String(toByteArray(in), UTF8);
    }
}
