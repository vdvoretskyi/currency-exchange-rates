package info.datamuse.currency.providers;

import info.datamuse.currency.CurrencyConverter;
import info.datamuse.currency.NotAvailableRateException;
import info.datamuse.currency.providers.http.*;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

import static info.datamuse.currency.utils.CurrencyUtils.format;
import static info.datamuse.currency.utils.HttpMethods.METHOD_GET;

public final class CurrencyConverterProvider implements CurrencyConverter {

    private static final URLTemplate CURRENCY_CONVERTER_API_URL_TEMPLATE = new URLTemplate("https://free.currencyconverterapi.com/api/v5/convert");

    @Override
    public BigDecimal convert(final String source, final String target) {
        Objects.requireNonNull(source, "Source currency must not be null");
        Objects.requireNonNull(source, "Target currency must not be null");

        final String currencyPair = currencyPair(source, target);
        final URL url = CURRENCY_CONVERTER_API_URL_TEMPLATE.buildWithQuery(
                "q=%s&compact=y", currencyPair);
        final HttpRequest httpRequest = new JsonHttpRequest(url, METHOD_GET);
        return parseResponse(httpRequest.send(), currencyPair);
    }

    private BigDecimal parseResponse(final HttpResponse httpResponse, final String currencyPair) {
        if (httpResponse instanceof JsonHttpResponse) {
            final Map<String, Object> currencyPairRate = (Map<String, Object>) ((JsonHttpResponse) httpResponse).parse().
                    getOrDefault(currencyPair, Map.<String, Object>of());
            final String rate = (String)currencyPairRate.get("val");
            if (rate == null) {
                throw new NotAvailableRateException();
            }
            return new BigDecimal(rate);
        }
        throw new RuntimeException(httpResponse.bodyToString());
    }

    private static String currencyPair(final String source, final String target) {
        return format(source) + PAIR_SEPARATOR + format(target);
    }

    private static final String PAIR_SEPARATOR = "_";
}
