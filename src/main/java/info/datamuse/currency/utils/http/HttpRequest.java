package info.datamuse.currency.utils.http;

import info.datamuse.currency.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;


public class HttpRequest {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private final URL url;
    private final List<RequestHeader> headers = new ArrayList<>();

    private final String method;
    private long bodyLength;
    private InputStream body;
    private int timeout;

    public HttpRequest(final URL url, final String method) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(method);

        this.url = url;
        this.method = method;

        this.addHeader("Accept-Charset", UTF_8.name());
    }

    public void addHeader(final String key, final String value) {
        this.headers.add(new RequestHeader(key, value));
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setBody(final InputStream stream) {
        this.body = stream;
    }

    public void setBody(final String body) {
        final byte[] bytes = body.getBytes(UTF_8);
        this.bodyLength = bytes.length;
        this.body = new ByteArrayInputStream(bytes);
    }

    protected String bodyToString() {
        return null;
    }

    protected void writeBody(final HttpURLConnection connection) {
        if (this.body == null) {
            return;
        }
        connection.setDoOutput(true);
        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            IOUtils.copy(body, output);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't connect due to a network error.", e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    protected void resetBody() throws IOException {
        if (this.body != null) {
            this.body.reset();
        }
    }

    public HttpResponse send() {
        //TODO: retry
        return this.trySend();
    }

    private HttpResponse trySend() {
        final HttpURLConnection connection = this.createConnection();

        if (this.bodyLength > 0) {
            connection.setFixedLengthStreamingMode((int) this.bodyLength);
            connection.setDoOutput(true);
        }

        this.writeBody(connection);

        // Ensure that we're connected in case writeBody() didn't write anything.
        try {
            connection.connect();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't connect due to a network error.", e);
        }

        logger.debug(connection.toString());

        return new HttpResponse(connection);
    }

    private HttpURLConnection createConnection() {
        HttpURLConnection connection = null;

        try {
            //connection
            connection = (HttpURLConnection) this.url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't connect due to a network error.", e);
        }

        try {
            //method
            connection.setRequestMethod(this.method);
        } catch (ProtocolException e) {
            throw new RuntimeException("Couldn't connect because the request's method was invalid.", e);
        }

        //timeout
        connection.setConnectTimeout(this.timeout);
        connection.setReadTimeout(this.timeout);

        //headers
        for (RequestHeader header : this.headers) {
            connection.addRequestProperty(header.getKey(), header.getValue());
        }

        return connection;
    }

    private static final class RequestHeader {
        private final String key;
        private final String value;

        public RequestHeader(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return this.key;
        }

        public String getValue() {
            return this.value;
        }
    }
}
