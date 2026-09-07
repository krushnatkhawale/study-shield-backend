package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.entity.ContentPack;
import com.studyshield.studyshield.content.entity.ContentTier;
import com.studyshield.studyshield.content.entity.Quiz;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the delivery-pack preference rules and freemium catalog seeding.
 * <p>
 * The mobile app only receives quizzes from packs the bundle can see; a bank-loaded pack
 * ({@code Loaded Math}) must not be invisible when no {@code Freemium} pack exists.
 */
class QuizBundleSeederTest {

    @Test
    void aFreemiumNamedPackIsPreferredOverAPlainLoadedPack() {
        ContentPack loadedPack = pack("Loaded Math", true);
        ContentPack freemiumPack = pack("Freemium Math", true);

        ContentPack picked = QuizBundleSeeder.pickActiveDeliveryPack(
                List.of(loadedPack, freemiumPack));

        assertThat(picked.getName()).isEqualTo("Freemium Math");
    }

    @Test
    void aBankLoadedPackIsServedWhenNoFreemiumPackExists() {
        ContentPack loadedPack = pack("Loaded Math", true);

        ContentPack picked = QuizBundleSeeder.pickActiveDeliveryPack(List.of(loadedPack));

        assertThat(picked.getName()).isEqualTo("Loaded Math");
    }

    @Test
    void inactivePacksAreNeverSelected() {
        ContentPack inactive = pack("Loaded Math", false);

        ContentPack picked = QuizBundleSeeder.pickActiveDeliveryPack(List.of(inactive));

        assertThat(picked).isNull();
    }

    @Test
    void anActiveNonFreemiumPackIsServedWhenAllFreemiumPacksAreInactive() {
        ContentPack inactiveFreemium = pack("Freemium Math", false);
        ContentPack activeLoaded = pack("Loaded Math", true);

        ContentPack picked = QuizBundleSeeder.pickActiveDeliveryPack(
                List.of(inactiveFreemium, activeLoaded));

        assertThat(picked.getName()).isEqualTo("Loaded Math");
    }

    private static ContentPack pack(String name, boolean active) {
        ContentPack p = new ContentPack();
        p.setName(name);
        p.setActive(active);
        return p;
    }
}
