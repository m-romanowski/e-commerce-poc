package dev.marcinromanowski.security.registration;

import java.util.HashSet;
import java.util.Set;

class ValidationRules {

    private final Set<ValidationRule> rules = new HashSet<>();

    ValidationRules with(ValidationRule rule) {
        rules.add(rule);
        return this;
    }

    boolean isInvalid() {
        return !rules.stream()
            .allMatch(ValidationRule::validate);
    }

}
