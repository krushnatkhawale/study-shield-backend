package com.studyshield.studyshield.content.seed;

import com.studyshield.studyshield.content.entity.BoardClass;
import com.studyshield.studyshield.content.service.QuizBundleSeeder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * On a bundle miss the request must stay fast: one seed is queued per class on the
 * catalog thread, concurrent misses share it, and the next request reads the result.
 */
class CatalogMissSeederTest {

    private final AtomicInteger seedCalls = new AtomicInteger();
    private final List<Runnable> queued = new ArrayList<>();

    private final QuizBundleSeeder seeder = new QuizBundleSeeder(null, null, null, null) {
        @Override
        public BoardClass ensureCatalogForClass(String className, String boardCode) {
            seedCalls.incrementAndGet();
            return null;
        }
    };

    @Test
    void concurrentMissesForTheSameClassShareOneQueuedSeed() {
        CatalogMissSeeder missSeeder = new CatalogMissSeeder(seeder, queued::add, true);

        assertThat(missSeeder.seedAsync("Class 5", "all")).isTrue();
        assertThat(missSeeder.seedAsync("Class 5", "ALL")).isFalse();
        assertThat(queued).hasSize(1);
        assertThat(seedCalls).hasValue(0);

        queued.getFirst().run();

        assertThat(seedCalls).hasValue(1);
        assertThat(missSeeder.seedAsync("Class 5", "all")).as("a finished seed can run again").isTrue();
        assertThat(queued).hasSize(2);
    }

    @Test
    void seedingIsSkippedWhenAsyncOnMissIsDisabled() {
        CatalogMissSeeder missSeeder = new CatalogMissSeeder(seeder, queued::add, false);

        assertThat(missSeeder.seedAsync("Class 5", "all")).isFalse();
        assertThat(queued).isEmpty();
    }
}
