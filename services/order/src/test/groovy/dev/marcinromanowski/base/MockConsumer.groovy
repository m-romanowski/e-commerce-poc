package dev.marcinromanowski.base

interface MockConsumer {
    void consumed(Object value)
    void consumed(Object key, Object value)
    List<Object> getAllValues()
    Optional<Object> getValue()
    Object getName()
}
