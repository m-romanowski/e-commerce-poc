package dev.marcinromanowski.gamescatalog.exception;

public class GamesCatalogInconsistencyStateException extends GamesCatalogException {

    public GamesCatalogInconsistencyStateException() {
        super("Games catalog is in inconsistency state");
    }

}
