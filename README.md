# Currency Exchange Rates

[![Java 10+](https://img.shields.io/badge/java-10%2B-blue.svg)](http://java.oracle.com)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/vdvoretskyi/currency-exchange-rates/master/LICENSE.md)

The _Currency Exchange Rates_ library implements getting live currency exchange rates from different providers.
It is designed for resilience - e.g. if the first-choice provider's API is temporarily inaccessible, the application can be configured to fallback to a different provider.

The library also implements multiple ways of _caching_ currency exchange rates for quick retrieval and for overcoming daily API call limits of the currency rate providers.

## Authors and contributors

* [Vlad Dvoretskyi](https://www.linkedin.com/in/vladislav-dvoretskiy-17528419/)
* [Alex Shesterov](https://www.linkedin.com/in/alexshesterov/)

## Usage

TODO:

## Supported providers

* [currencylayer.com](https://currencylayer.com/)
* [currencyconverterapi.com](https://currencyconverterapi.com)
