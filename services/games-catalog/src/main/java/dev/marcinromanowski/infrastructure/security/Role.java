package dev.marcinromanowski.infrastructure.security;

public enum Role {

    USER,
    ADMIN;

    @Override
    public String toString() {
        return "ROLE_" + this.name();
    }

}
