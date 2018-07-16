package info.datamuse.currency;

import java.math.BigDecimal;

public abstract class CurrencyRatesProviderDecorator implements CurrencyRatesProvider {

    protected final CurrencyRatesProvider converter;

    protected CurrencyRatesProviderDecorator(final CurrencyRatesProvider converter) {
        this.converter = converter;
    }

    @Override
    public BigDecimal getExchangeRate(final String sourceCurrencyCode, final String targetCurrencyCode) {
        return converter.getExchangeRate(sourceCurrencyCode, targetCurrencyCode);
    }
}
