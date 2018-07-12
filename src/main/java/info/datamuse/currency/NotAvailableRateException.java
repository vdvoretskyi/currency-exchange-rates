package info.datamuse.currency;

public class NotAvailableRateException extends RuntimeException {

    public NotAvailableRateException() {
    }

    public NotAvailableRateException(final String message) {
        super(message);
    }

    public NotAvailableRateException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NotAvailableRateException(final Throwable cause) {
        super(cause);
    }

    public NotAvailableRateException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
