package dev.marcinromanowski

import dev.marcinromanowski.base.IntegrationSpec

class AcceptanceIntegrationSpec extends IntegrationSpec {

    def "Orders acceptance test"() {
        when: "user creates a new order"
        and: "successfully payment event come from external system"
        then: "order ends with 'SUCCESS' status"
        then: "user's invoice is generated"
        and: "user's invoice is sent to the user"
    }

}
