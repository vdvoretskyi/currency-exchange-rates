package info.datamuse.currency.providers;

import info.datamuse.currency.NotAvailableRateException;
import info.datamuse.currency.utils.http.HttpRequest;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Locale;

import static info.datamuse.currency.utils.CurrencyUtils.validateCurrencyCode;
import static info.datamuse.currency.utils.HttpUtils.HTTP_METHOD_GET;

/**
 * <a href="https://www.ecb.europa.eu/">European Central Bank</a> daily Euro rates provider.
 *
 * <p>
 *     Note: this provider can only convert a limited number of currencies from and to {@code EUR}.
 *     Exchange rates from EUR to target currencies are provided daily by ECB.
 *     Inverse rates are calculated as {@code CUR_TO_EUR = 1 / EUR_TO_CUR}.
 *     See the <a href="https://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_reference_exchange_rates/html/index.en.html">ECB service description page</a> for details.
 * </p>
 */
public final class EcbProvider extends AbstractCurrencyRatesProvider {

    private static final String EUR_CURRENCY_CODE = "EUR";
    private static final MathContext CURRENCY_RATE_INVERSION_MATH_CONTEXT = new MathContext(8, RoundingMode.UP);
    private static QName ECB_CUBE_ELEMENT_QNAME = new QName("http://www.ecb.int/vocabulary/2002-08-01/eurofxref", "Cube");
    private static QName ECB_CURRENCY_ATTRIBUTE_QNAME = new QName("", "currency");
    private static QName ECB_RATE_ATTRIBUTE_QNAME = new QName("", "rate");

    @Override
    protected BigDecimal doGetExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode) throws IOException, XMLStreamException {
        validateCurrencyCode(sourceCurrencyCode);
        validateCurrencyCode(targetCurrencyCode);

        if (sourceCurrencyCode.equals(EUR_CURRENCY_CODE)) {
            return getEurToTargetCurrencyExchangeRate(targetCurrencyCode);
        } else if (targetCurrencyCode.equals(EUR_CURRENCY_CODE)) {
            // Note: we assume the EUR-to-CUR-rate is never zero. Otherwise, an (arithmetic) exception is wanted.
            return BigDecimal.ONE.divide(getEurToTargetCurrencyExchangeRate(sourceCurrencyCode), CURRENCY_RATE_INVERSION_MATH_CONTEXT);
        } else {
            throw new NotAvailableRateException(String.format(Locale.ROOT,
                "{} only supports conversions from/to {}. Conversion from {} to {} is not supported",
                EcbProvider.class.getSimpleName(), EUR_CURRENCY_CODE, sourceCurrencyCode, targetCurrencyCode
            ));
        }
    }

    private static BigDecimal getEurToTargetCurrencyExchangeRate(final String targetCurrencyCode) throws IOException, XMLStreamException {
        final String dailyRateApiUrl = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
        try (InputStream apiResponseStream = new HttpRequest(new URL(dailyRateApiUrl), HTTP_METHOD_GET).send().getBody()) {
            final XMLEventReader apiResponseReader = XMLInputFactory.newInstance().createXMLEventReader(apiResponseStream);
            try {
                while (apiResponseReader.hasNext()) {
                    final XMLEvent xmlTagEvent = apiResponseReader.nextEvent();
                    if (xmlTagEvent.isStartElement()) {
                        final StartElement startElement = xmlTagEvent.asStartElement();
                        if (startElement.getName().equals(ECB_CUBE_ELEMENT_QNAME)) {
                            final /* @Nullable */ Attribute currencyAttribute = startElement.getAttributeByName(ECB_CURRENCY_ATTRIBUTE_QNAME);
                            if (currencyAttribute != null && targetCurrencyCode.equals(currencyAttribute.getValue())) {
                                final /* @Nullable */ Attribute rateAttribute = startElement.getAttributeByName(ECB_RATE_ATTRIBUTE_QNAME);
                                if (rateAttribute != null) {
                                    return new BigDecimal(rateAttribute.getValue());
                                }
                            }
                        }
                    }
                }
                throw new NotAvailableRateException(String.format(Locale.ROOT,
                    "Exchange rate %s->%s is not available",
                    EUR_CURRENCY_CODE, targetCurrencyCode
                ));
            } finally {
                apiResponseReader.close();
            }
        }
    }

}
