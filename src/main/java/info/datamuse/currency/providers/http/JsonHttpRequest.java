package info.datamuse.currency.providers.http;

import java.net.URL;

public class JsonHttpRequest extends HttpRequest {

    private String json;

    public JsonHttpRequest(final URL url, final String method) {
        super(url, method);

        this.addHeader("Content-Type", "application/json");
        this.addHeader("Accept", "application/json");
    }

    @Override
    public void setBody(String body) {
        super.setBody(body);
        this.json = body;
    }

    @Override
    protected String bodyToString() {
        return this.json;
    }
}
