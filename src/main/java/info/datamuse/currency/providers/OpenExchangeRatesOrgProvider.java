package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyRatesProvider;
import info.datamuse.currency.NotAvailableRateException;
import info.datamuse.currency.providers.http.HttpRequest;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static info.datamuse.currency.utils.CurrencyUtils.validateCurrencyCode;
import static info.datamuse.currency.utils.HttpUtils.HTTP_METHOD_GET;

/**
 * <a href="https://openexchangerates.org/">openexchangerates.org</a> rates provider.
 */
public final class OpenExchangeRatesOrgProvider implements CurrencyRatesProvider {

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
    public BigDecimal getExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode) {
        validateCurrencyCode(sourceCurrencyCode);
        validateCurrencyCode(targetCurrencyCode);

        final String liveRateApiUrl = String.format(
            Locale.ROOT,
            "https://openexchangerates.org/api/latest.json?app_id=%s&base=%s&symbols=%s",
            appId, sourceCurrencyCode, targetCurrencyCode
        );

        try {
            final String apiResponseJsonString = new HttpRequest(new URL(liveRateApiUrl), HTTP_METHOD_GET).send().getBodyAsString();
            final JSONObject apiResponseJson = new JSONObject(apiResponseJsonString);

            if (!apiResponseJson.getString("base").equals(sourceCurrencyCode)) {
                throw new NotAvailableRateException(String.format(Locale.ROOT,
                    "Base currency in the response doesn't match the requested source currency; request=%s, response=%s",
                    liveRateApiUrl, apiResponseJsonString
                ));
            }
            return apiResponseJson.getJSONObject("rates").getBigDecimal(targetCurrencyCode);
        } catch (final MalformedURLException | RuntimeException e) {
            throw new NotAvailableRateException(e);
        }
    }

}
