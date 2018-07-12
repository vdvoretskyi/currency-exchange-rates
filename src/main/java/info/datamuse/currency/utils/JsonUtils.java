package info.datamuse.currency.utils;

import org.json.JSONObject;

public final class JsonUtils {

    public static JSONObject parse(final String jsonString) {
        return new JSONObject(jsonString);
    }
}
