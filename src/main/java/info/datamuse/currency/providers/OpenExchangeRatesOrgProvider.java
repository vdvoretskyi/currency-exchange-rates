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
 * <a href="https://openexchangerates.org/">openexchangerates.org</a> rates provider.
 */
public final class OpenExchangeRatesOrgProvider extends AbstractCurrencyRatesProvider {

    private final String appId;

    /**
     * Provider constructor.
     *
     * @param appId openexchangerates App ID
     */
    public OpenExchangeRatesOrgProvider(final String appId) {
        this.appId = appId;
    }

    @Override
    protected BigDecimal doGetExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode) throws MalformedURLException {
        final String liveRateApiUrl = String.format(
            Locale.ROOT,
            "https://openexchangerates.org/api/latest.json?app_id=%s&base=%s&symbols=%s",
            appId, sourceCurrencyCode, targetCurrencyCode
        );

        final String apiResponseJsonString = new HttpRequest(new URL(liveRateApiUrl), HTTP_METHOD_GET).send().getBodyAsString();
        final JSONObject apiResponseJson = new JSONObject(apiResponseJsonString);

        if (!apiResponseJson.getString("base").equals(sourceCurrencyCode)) {
            throw new NotAvailableRateException(String.format(Locale.ROOT,
                "Base currency in the response doesn't match the requested source currency; request=%s, response=%s",
                liveRateApiUrl, apiResponseJsonString
            ));
        }
        return apiResponseJson.getJSONObject("rates").getBigDecimal(targetCurrencyCode);
    }

}
