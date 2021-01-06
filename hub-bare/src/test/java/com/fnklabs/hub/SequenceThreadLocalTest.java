package com.fnklabs.hub;

import com.fnklabs.hub.core.SequenceLimitsExceeded;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Thread local sequence")
class SequenceThreadLocalTest {
    public static final long MAX_VALUE = 5;

    private SequenceThreadLocal sequenceThreadLocal;


    @Nested
    @DisplayName("when new")
    class WhenNew {
        @BeforeEach
        void createStack() {
            sequenceThreadLocal = new SequenceThreadLocal(1, 0, MAX_VALUE);
        }

        @Test
        public void checkCurrentValue() {
            assertEquals(0, sequenceThreadLocal.getValue());
        }

        @Test
        public void checkDomain() {
            assertEquals(1, sequenceThreadLocal.getDomain());
        }

        @Nested
        @DisplayName("after create")
        class AfterCreate {
            @Test
            void next() {
                assertEquals(1, sequenceThreadLocal.next());
                assertEquals(2, sequenceThreadLocal.next());
                assertEquals(3, sequenceThreadLocal.next());
            }

            @Test
            void nextOverflow() {
                assertEquals(1, sequenceThreadLocal.next());
                assertEquals(2, sequenceThreadLocal.next());
                assertEquals(3, sequenceThreadLocal.next());
                assertEquals(4, sequenceThreadLocal.next());
                assertEquals(5, sequenceThreadLocal.next());

                assertThrows(SequenceLimitsExceeded.class, () -> {
                    sequenceThreadLocal.next();

                    fail("must throw exception");
                });
            }
        }

    }
}