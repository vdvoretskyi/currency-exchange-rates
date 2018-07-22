package info.datamuse.currency.providers;

import info.datamuse.currency.NotAvailableRateException;
import info.datamuse.currency.utils.http.HttpRequest;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static info.datamuse.currency.utils.HttpUtils.HTTP_METHOD_GET;

/**
 * <a href="https://currencylayer.com/">currencylayer.com</a> rates provider.
 */
public final class CurrencyLayerComProvider extends AbstractCurrencyRatesProvider {

    private final String apiKey;
    private final boolean useHttps;

    /**
     * Provider constructor.
     *
     * @param apiKey currencylayer API key
     * @param useHttps set to {@code true} to use HTTPS (requires a paid subscription) or {@code false} for HTTP
     */
    public CurrencyLayerComProvider(final String apiKey, final boolean useHttps) {
        this.apiKey = apiKey;
        this.useHttps = useHttps;
    }

    @Override
    protected BigDecimal doGetExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode) throws MalformedURLException {
        final String liveRateApiUrl = String.format(
            Locale.ROOT,
            "%s://apilayer.net/api/live?access_key=%s&source=%s&currencies=%s&format=1",
            useHttps ? "https" : "http", apiKey, sourceCurrencyCode, targetCurrencyCode
        );

        final String apiResponseJsonString = new HttpRequest(new URL(liveRateApiUrl), HTTP_METHOD_GET).send().getBodyAsString();
        final JSONObject apiResponseJson = new JSONObject(apiResponseJsonString);

        if (!apiResponseJson.getBoolean("success")) {
            throw new NotAvailableRateException(String.format(Locale.ROOT, "`success` flag is false; request=%s, response=%s", liveRateApiUrl, apiResponseJsonString));
        }
        return apiResponseJson.getJSONObject("quotes").getBigDecimal(sourceCurrencyCode + targetCurrencyCode);
    }

}
