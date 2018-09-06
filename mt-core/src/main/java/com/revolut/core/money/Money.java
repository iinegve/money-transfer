package com.revolut.core.money;

import lombok.Data;

@Data
public class Money {
    private final int cents;

    public Money subtract(Money money) {
        int newAmount = this.cents - money.getCents();
        if (newAmount < 0)
            throw new MoneyException(MoneyException.NO_NEGATIVES);
        return new Money(newAmount);
    }

    public Money add(Money money) {
        return new Money(this.cents + money.getCents());
    }
}
