package dev.marcinromanowski.base

import dev.marcinromanowski.infrastructure.security.Role
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
trait AuthFixture {

    @Autowired
    private JwtGenerator generator

    String generateUserToken(String userId) {
        return generateToken(userId, Role.USER)
    }

    String generateAdminToken(String userId) {
        return generateToken(userId, Role.ADMIN)
    }

    private String generateToken(String userId, Role role) {
        return generateToken(userId, role.toString(), UUID.randomUUID())
    }

    private String generateToken(String userId, String authorities, UUID sessionId) {
        return generator.generateToken(10, userId, authorities, sessionId)
    }

}
