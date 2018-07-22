package info.datamuse.currency.providers;

import info.datamuse.currency.utils.http.HttpRequest;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static info.datamuse.currency.utils.HttpUtils.HTTP_METHOD_GET;

/**
 * <a href="https://free.currencyconverterapi.com">free.currencyconverterapi.com</a> rates provider.
 */
public final class FreeCurrencyConverterApiComProvider extends AbstractCurrencyRatesProvider {

    @Override
    protected BigDecimal doGetExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode) throws MalformedURLException {
        final String currencyPair = sourceCurrencyCode + '_' + targetCurrencyCode;
        final String liveRateApiUrl = String.format(
            Locale.ROOT,
            "https://free.currencyconverterapi.com/api/v5/convert?q=%s&compact=ultra",
            currencyPair
        );
        final String apiResponseJsonString = new HttpRequest(new URL(liveRateApiUrl), HTTP_METHOD_GET).send().getBodyAsString();
        return new JSONObject(apiResponseJsonString).getBigDecimal(currencyPair);
    }

}
