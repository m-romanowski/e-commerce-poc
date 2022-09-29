package dev.marcinromanowski.product.exception;

import java.util.Set;
import java.util.UUID;

public class CannotValidateProductsException extends ProductException {

    private static final String ERROR_MESSAGE = "Cannot validate product ids: %s";

    public CannotValidateProductsException(Set<UUID> ids) {
        super(ERROR_MESSAGE.formatted(ids));
    }

    public CannotValidateProductsException(Set<UUID> ids, Throwable cause) {
        super(ERROR_MESSAGE.formatted(ids), cause);
    }

}
