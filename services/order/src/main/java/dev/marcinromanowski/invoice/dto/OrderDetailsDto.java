package dev.marcinromanowski.invoice.dto;

import java.util.UUID;

// TODO: add user details required to send an invoice (e.g. email)
public record OrderDetailsDto(UUID id, String userId) {

}
