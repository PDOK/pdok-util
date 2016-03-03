package nl.pdok.java.util;

import java.util.UUID;

public class UUIDRandomGenerator implements RandomGenerator {

    @Override
    public String randomString() {
        return String.valueOf(UUID.randomUUID());
    }
}
