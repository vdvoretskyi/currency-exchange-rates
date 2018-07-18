package info.datamuse.currency.providers;

import info.datamuse.currency.NotAvailableRateException;
import info.datamuse.currency.providers.http.HttpRequest;
import info.datamuse.currency.providers.http.HttpResponse;
import info.datamuse.currency.providers.http.JsonHttpRequest;
import info.datamuse.currency.providers.http.URLTemplate;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.net.URL;

import static info.datamuse.currency.utils.HttpUtils.HTTP_METHOD_GET;

public final class CurrencyRatesProviderAPIProvider extends AbstractCurrencyRatesProvider {

    private static final URLTemplate CURRENCY_CONVERTER_API_URL_TEMPLATE = new URLTemplate("https://free.currencyconverterapi.com/api/v5/convert");

    @Override
    protected BigDecimal doGetExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode) {
        final String currencyPair = currencyPair(sourceCurrencyCode, targetCurrencyCode);
        final URL url = CURRENCY_CONVERTER_API_URL_TEMPLATE.buildWithQuery(
                "q=%s&compact=y", currencyPair);
        final HttpRequest httpRequest = new JsonHttpRequest(url, HTTP_METHOD_GET);
        try {
            final HttpResponse httpResponse = httpRequest.send();
            return parseResponse(httpResponse, currencyPair);
        } catch(final RuntimeException e) {
            throw new NotAvailableRateException(e);
        }
    }

    private BigDecimal parseResponse(final HttpResponse httpResponse, final String currencyPair) {
        final JSONObject jsonObject = new JSONObject(httpResponse.getBodyAsString());
        final JSONObject pairKey = jsonObject.getJSONObject(currencyPair);
        if (pairKey != null) {
            return pairKey.getBigDecimal("val");
        }
        return null;
    }

    private static String currencyPair(final String source, final String target) {
        return source + PAIR_SEPARATOR + target;
    }
    private static final String PAIR_SEPARATOR = "_";
}
