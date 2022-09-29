package dev.marcinromanowski.common;

import java.math.BigDecimal;

public record Price(BigDecimal amount, String currencyCode) {

}
