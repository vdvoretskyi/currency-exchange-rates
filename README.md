# Currency Exchange Rates

[![Java 10+](https://img.shields.io/badge/java-10%2B-blue.svg)](http://java.oracle.com)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/vdvoretskyi/currency-exchange-rates/master/LICENSE.md)

The _Currency Exchange Rates_ library implements getting live currency exchange rates from different providers.
It is designed for resilience — e.g. if the first-choice provider's API is temporarily inaccessible, the application can be configured to fallback to a different provider.

The library also implements multiple ways of _caching_ currency exchange rates for quick retrieval and for overcoming daily API call limits of the currency rate providers.


## Supported providers

* [European Central Bank](https://www.ecb.europa.eu/)
* [currencylayer.com](https://currencylayer.com/)
* [currencyconverterapi.com](https://currencyconverterapi.com)
* [openexchangerates.org](https://openexchangerates.org)


## Supported caches

* Redis
* In-memory (via `ConcurrentHashMap`)


## Examples

TODO:


## Key features

* **Flexibility** — the library allows a very flexible configuration of exchange rate providers, fallback chains and caching.
For example, you may configure a provider with some API key as a fallback for the same provider with a different API key 
(for the case that the first API key runs out of available API calls). 

* **Precision** — Some existing utilities work with "base currencies" and do conversions in two steps: convert source currency to the base currency, then the base currency to target currency.
Such implementation may produce inaccurate or wrong results, so this library doesn't do this.
All conversions are performed directly in one step.

* **Minimal dependencies** — the library has very few dependencies on third-party libraries, to avoid dependency conflicts for the users.


## Authors and contributors

* [Vlad Dvoretskyi](https://www.linkedin.com/in/vladislav-dvoretskiy-17528419/)

* [Alex Shesterov](https://www.linkedin.com/in/alexshesterov/)

