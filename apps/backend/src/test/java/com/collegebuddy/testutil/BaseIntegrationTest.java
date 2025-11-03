package com.collegebuddy.testutil;

import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for integration tests.
 * You can wire Testcontainers/Postgres here.
 */
public abstract class BaseIntegrationTest {

    @BeforeEach
    void commonSetup() {
        // TODO: start containers, clean DB, etc.
    }
}
