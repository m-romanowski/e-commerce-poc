package dev.marcinromanowski.security.user;

import dev.marcinromanowski.security.user.dto.UserDetailsDto;

import java.util.Optional;

public class UserFacade {

    public Optional<UserDetailsDto> findUser(String email) {
        return Optional.empty();
    }

}
