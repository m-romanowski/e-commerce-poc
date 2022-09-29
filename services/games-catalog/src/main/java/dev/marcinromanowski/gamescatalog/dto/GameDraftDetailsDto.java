package dev.marcinromanowski.gamescatalog.dto;

import java.util.UUID;

public record GameDraftDetailsDto(
        UUID id,
        String title,
        String producer) {

}
