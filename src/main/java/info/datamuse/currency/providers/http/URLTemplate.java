package info.datamuse.currency.providers.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public final class URLTemplate {

    private final String template;

    public URLTemplate(final String template) {
        Objects.requireNonNull(template);

        this.template = template;
    }

    public URL build(final Object... values) {
        final String urlString = String.format(this.template, values);
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException("An invalid URL: " + urlString);
        }
    }

    public URL buildWithQuery(final String queryString, final Object... values) {
        final String urlString = new StringBuilder(template).
                append("?").
                append(String.format(queryString, values)).
                toString();
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException("An invalid URL: " + urlString);
        }
    }
}
