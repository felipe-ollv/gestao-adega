package com.adega.service.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BillingCycleCalculatorTest {
    @Test
    void firstCycleEndsOneDayBeforeNextMonthlyAnniversary() {
        BillingCycle cycle = BillingCycleCalculator.firstCycle(LocalDate.of(2026, 7, 20));

        assertEquals(LocalDate.of(2026, 7, 20), cycle.startDate());
        assertEquals(LocalDate.of(2026, 8, 19), cycle.endDate());
    }

    @Test
    void nextCycleStartsOnMonthlyAnniversary() {
        BillingCycle cycle = BillingCycleCalculator.currentCycle(
                LocalDate.of(2026, 7, 20),
                LocalDate.of(2026, 8, 20)
        );

        assertEquals(LocalDate.of(2026, 8, 20), cycle.startDate());
        assertEquals(LocalDate.of(2026, 9, 19), cycle.endDate());
    }

    @Test
    void endOfMonthRegistrationKeepsOriginalAnchor() {
        LocalDate registrationDate = LocalDate.of(2026, 1, 31);

        BillingCycle februaryCycle = BillingCycleCalculator.currentCycle(
                registrationDate,
                LocalDate.of(2026, 2, 28)
        );
        BillingCycle marchCycle = BillingCycleCalculator.currentCycle(
                registrationDate,
                LocalDate.of(2026, 3, 31)
        );

        assertEquals(LocalDate.of(2026, 2, 28), februaryCycle.startDate());
        assertEquals(LocalDate.of(2026, 3, 30), februaryCycle.endDate());
        assertEquals(LocalDate.of(2026, 3, 31), marchCycle.startDate());
        assertEquals(LocalDate.of(2026, 4, 29), marchCycle.endDate());
    }
}
