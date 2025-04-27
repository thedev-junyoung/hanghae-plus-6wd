package kr.hhplus.be.server.common.vo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

import java.util.Objects;

@Value
public class Money {
    @JsonValue
    long value;
    @JsonCreator
    public static Money from(long value) {
        return new Money(value);
    }
    private Money(long value) {
        this.value = value;
    }

    public static Money wons(long amount) {
        return new Money(amount);
    }

    public Money add(Money other) {
        return new Money(this.value + other.value);
    }

    public Money subtract(Money other) {
        return new Money(this.value - other.value);
    }

    public Money multiply(int multiplier) {
        return new Money(this.value * multiplier);
    }

    public Money multiply(long multiplier) {
        return new Money(this.value * multiplier);
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.value >= other.value;
    }

    public long value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return value == money.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value + "원";
    }

    public Money multiplyPercent(int percent) {
        return new Money((this.value * percent) / 100);
    }
    public boolean isNegative() {
        return this.value < 0;
    }

}
