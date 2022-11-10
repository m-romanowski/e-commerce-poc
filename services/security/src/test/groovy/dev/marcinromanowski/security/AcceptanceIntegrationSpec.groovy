package dev.marcinromanowski.security

import spock.lang.Ignore
import spock.lang.Specification

class AcceptanceIntegrationSpec extends Specification {

    @Ignore(value = "Provide integration test infrastructure - databases, etc")
    def "Users security acceptance integration test"() {
        given: "user not exists"

        when: "user tries to register with invalid credentials"
        then: "400 Bad Request is returned"

        when: "user tries to register with valid credentials"
        then: "registration attempt exists"
        and: "user cannot log in yet"
        and: "user activation code is sent"

        when: "user tries to register again with the same email"
        then: "user activation code is resend"

        when: "user's activation code expired"
        then: "a new code is resend"

        when: "user's account is activated"
        then: "user account is created"
        and: "user can log in"

        when: "user tries to register a new account with the same email address (bruteforce attack resistance)"
        then: "202 Accepted status code is returned"
    }

}
