package info.datamuse.currency.providers.http;

import info.datamuse.currency.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpResponse {

    private static final int BUFFER_SIZE = 8192;
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private final HttpURLConnection connection;

    private int responseCode;
    private String bodyString;

    private InputStream inputStream;

    public HttpResponse(final HttpURLConnection connection) {
        this.connection = connection;

        try {
            this.responseCode = this.connection.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't connect due to a network error.", e);
        }

        if (!isSuccess(this.responseCode)) {
            logger.debug(this.toString());
            String response = null;
            try {
                response = this.bodyToString();
                if (response == null || response.length() == 0) {
                    response = this.getBodyAsText();
                }
            } catch(Exception e) {
                logger.debug("Couldn't get more detailed error information");
            }
            throw new RuntimeException(String.format("Insight Server returned an error code: %s. %s", this.responseCode, response));
        }
        logger.debug(this.toString());
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getBodyAsText() {
        final InputStream bodyStream = this.getBody();
        final InputStreamReader reader = new InputStreamReader(bodyStream, UTF_8);
        final StringBuilder builder = new StringBuilder();
        final char[] buffer = new char[BUFFER_SIZE];

        try {
            int read = reader.read(buffer, 0, BUFFER_SIZE);
            while (read != -1) {
                builder.append(buffer, 0, read);
                read = reader.read(buffer, 0, BUFFER_SIZE);
            }

            this.disconnect();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't connect to Insight Server due to a network error.", e);
        }  finally {
            IOUtils.closeQuietly(reader);
        }

        return builder.toString();
    }

    public InputStream getBody() {
        if (this.inputStream == null) {
            try {
                if (this.inputStream == null) {
                    this.inputStream = this.connection.getInputStream();
                }

                this.inputStream = this.inputStream;
            } catch (IOException e) {
                throw new RuntimeException("Couldn't connect due to a network error.", e);
            }
        }
        return this.inputStream;
    }

    public String bodyToString() {
        if (this.bodyString == null && !isSuccess(this.responseCode)) {
            this.bodyString = readErrorStream(this.getErrorStream());
        }
        return this.bodyString;
    }

    private InputStream getErrorStream() {
        final InputStream errorStream = this.connection.getErrorStream();
        return errorStream;
    }

    private static String readErrorStream(final InputStream stream) {
        if (stream == null) {
            return null;
        }

        final InputStreamReader reader = new InputStreamReader(stream, UTF_8);
        final StringBuilder builder = new StringBuilder();
        final char[] buffer = new char[BUFFER_SIZE];

        try {
            int read = reader.read(buffer, 0, BUFFER_SIZE);
            while (read != -1) {
                builder.append(buffer, 0, read);
                read = reader.read(buffer, 0, BUFFER_SIZE);
            }

        } catch (IOException e) {
            return null;

        } finally {
            IOUtils.closeQuietly(reader);
        }

        return builder.toString();
    }

    public void disconnect() {
        if (this.connection == null) {
            return;
        }

        try {
            if (this.inputStream == null) {
                this.inputStream = this.connection.getInputStream();
            }

            byte[] buffer = new byte[BUFFER_SIZE];
            int n = this.inputStream.read(buffer);
            while (n != -1) {
                n = this.inputStream.read(buffer);
            }
            this.inputStream.close();

            if (this.inputStream != null) {
                this.inputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't finish closing the connection", e);
        }
    }

    @Override
    public String toString() {
        String lineSeparator = System.getProperty("line.separator");
        StringBuilder builder = new StringBuilder();
        builder.append("Http Response").
                append(lineSeparator).
                append(this.connection.getRequestMethod()).
                append(lineSeparator).
                append(this.connection.getURL().toString()).
                append(lineSeparator).
                append(this.connection.getHeaderFields().toString()).toString();
        if (this.bodyString != null) {
            builder.append(lineSeparator);
            builder.append(bodyString);
        }
        return builder.toString().trim();
    }

    private static boolean isSuccess(final int responseCode) {
        return responseCode >= 200 && responseCode < 400;
    }

}
