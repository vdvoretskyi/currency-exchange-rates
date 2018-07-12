package info.datamuse.currency.providers.http;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.HttpURLConnection;
import java.util.Map;

public class JsonHttpResponse extends HttpResponse {

    private String json;

    public JsonHttpResponse(final HttpURLConnection connection) {
        super(connection);
    }

    public Map<String, Object> parse() {
        if (this.json == null) {
            this.json = super.getBodyAsText();
        }
        if (json == null) {
            throw new RuntimeException(super.bodyToString());
        }
        return parse(this.json);
    }

    protected static Map<String, Object> parse(final String jsonString) {
        final JSONObject root = new JSONObject(new JSONTokener(jsonString));
        return root.toMap();
    }
}
