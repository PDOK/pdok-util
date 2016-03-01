package nl.pdok.datamanagement.util;

import java.util.UUID;

public class UUIDRandomGenerator implements RandomGenerator {

    @Override
    public String randomString() {
        return String.valueOf(UUID.randomUUID());
    }
}
