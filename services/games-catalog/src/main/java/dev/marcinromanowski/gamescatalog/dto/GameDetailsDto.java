package dev.marcinromanowski.gamescatalog.dto;

import java.time.Instant;
import java.util.UUID;

public record GameDetailsDto(
        UUID id,
        String title,
        String producer,
        Instant publishedAt) {

}
