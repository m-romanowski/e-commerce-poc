package dev.marcinromanowski.base

import groovy.transform.CompileStatic

@CompileStatic
class RandomizerFixture {

    static String randomUserId(String base) {
        return "$base-${UUID.randomUUID().toString().split("-")[0]}"
    }

    static BigDecimal randomBigDecimal() {
        return new BigDecimal(Math.random()).stripTrailingZeros()
    }

}
