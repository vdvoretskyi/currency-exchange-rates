package info.datamuse.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public final class CleanupTaskInMemoryCache<K, V> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CleanupTaskInMemoryCache.class);

    private final InMemoryCurrencyRatesProvider instance;

    public CleanupTaskInMemoryCache(InMemoryCurrencyRatesProvider instance) {
        this.instance = instance;
    }

    @Override
    public void run() {
        while(true){
            try {
                TimeUnit.SECONDS.sleep(1);
                instance.cleanUp(true);
            }

            catch(InterruptedException ie){
                logger.error("Error: " + ie);
            }
        }

    }
}
