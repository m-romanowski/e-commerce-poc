package dev.marcinromanowski.gamescatalog.exception;

import java.util.UUID;

public class CannotFindGameDraftException extends GamesCatalogException {

    public CannotFindGameDraftException(UUID draftId) {
        super("Cannot find game draft, id: %s".formatted(draftId));
    }

}
