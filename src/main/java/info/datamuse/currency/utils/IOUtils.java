package info.datamuse.currency.utils;

import java.io.*;

public final class IOUtils {

    public static final int EOF = -1;

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    public static long copy(final InputStream input, final OutputStream output) throws IOException {
        return copy(input, output, DEFAULT_BUFFER_SIZE);
    }

    public static long copy(final InputStream input, final OutputStream output, final int bufferSize) throws IOException {
        final byte[] buffer = new byte[bufferSize];
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static String toString(final InputStreamReader reader) throws IOException {
        return toString(reader, DEFAULT_BUFFER_SIZE);
    }

    public static String toString(final InputStreamReader reader, final int bufferSize) throws IOException {
        final char[] buffer = new char[bufferSize];
        final StringBuilder builder = new StringBuilder();

        int read = reader.read(buffer, 0, bufferSize);
        while (read != EOF) {
            builder.append(buffer, 0, read);
            read = reader.read(buffer, 0, bufferSize);
        }

        return builder.toString();
    }
}
