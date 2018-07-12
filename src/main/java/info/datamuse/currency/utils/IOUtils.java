package info.datamuse.currency.utils;

import java.io.Closeable;
import java.io.IOException;

public final class IOUtils {

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
}
