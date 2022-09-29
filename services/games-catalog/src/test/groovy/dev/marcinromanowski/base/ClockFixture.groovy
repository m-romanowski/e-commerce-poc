package dev.marcinromanowski.base

import groovy.transform.CompileStatic

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@CompileStatic
trait ClockFixture {

    Clock clock() {
        return Clock.fixed(Instant.parse("2005-09-28T18:35:24.00Z"), ZoneId.of("UTC"))
    }

}
