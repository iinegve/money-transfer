package com.revolut.core;

import com.revolut.core.money.Money;
import com.revolut.core.money.MoneyException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class MoneyTest {

    @Test
    public void subtract() {
        assertThat(new Money(10).subtract(new Money(5))).isEqualTo(new Money(5));
        assertThat(new Money(10).subtract(new Money(10))).isEqualTo(new Money(0));
    }

    @Test
    public void subtract_wrong() {
        assertThatThrownBy(() -> new Money(10).subtract(new Money(20)))
            .isInstanceOf(MoneyException.class)
            .hasMessage(MoneyException.NO_NEGATIVES);
    }

    @Test
    public void add() {
        assertThat(new Money(10).add(new Money(5))).isEqualTo(new Money(15));
        assertThat(new Money(10).add(new Money(0))).isEqualTo(new Money(10));
    }
}