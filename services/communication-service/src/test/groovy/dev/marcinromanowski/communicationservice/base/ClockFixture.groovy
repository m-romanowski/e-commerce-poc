package dev.marcinromanowski.communicationservice.base

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

trait ClockFixture {

    Clock clock() {
        return Clock.fixed(Instant.parse("2022-10-05T11:38:24.00Z"), ZoneId.of("UTC"))
    }

}
