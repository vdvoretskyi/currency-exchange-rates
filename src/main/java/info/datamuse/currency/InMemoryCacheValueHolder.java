package info.datamuse.currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InMemoryCacheValueHolder<T> {
    private final BigDecimal value;
    private LocalDateTime lastAccessTimestamp;

    public InMemoryCacheValueHolder(BigDecimal value) {
        this.value = value;
        this.lastAccessTimestamp = LocalDateTime.now();
    }

    public LocalDateTime getLastAccessTimestamp() {
        return lastAccessTimestamp;
    }

    public void setLastAccessTimestamp(LocalDateTime lastAccessTimestamp) {
        this.lastAccessTimestamp = lastAccessTimestamp;
    }

    public BigDecimal getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "InMemoryCacheValueHolder [value=" + value + ", lastAccessTimestamp="
                + lastAccessTimestamp + "]";
    }
}
