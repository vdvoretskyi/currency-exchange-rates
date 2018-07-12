package info.datamuse.currency.utils;

public final class HttpUtils {

    public static final int BUFFER_SIZE = 8192;

    public static final String HTTP_METHOD_GET = "GET";

    public static boolean isSuccess(final int responseCode) {
        return responseCode >= 200 && responseCode < 400;
    }
}
