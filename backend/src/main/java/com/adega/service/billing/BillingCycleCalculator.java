package com.adega.service.billing;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class BillingCycleCalculator {
    private BillingCycleCalculator() {
    }

    public static BillingCycle firstCycle(LocalDate registrationDate) {
        return cycleAtOffset(registrationDate, 0);
    }

    public static BillingCycle currentCycle(LocalDate registrationDate, LocalDate referenceDate) {
        Objects.requireNonNull(registrationDate, "A data de cadastro é obrigatória.");
        Objects.requireNonNull(referenceDate, "A data de referência é obrigatória.");

        if (referenceDate.isBefore(registrationDate)) {
            return firstCycle(registrationDate);
        }

        long monthOffset = ChronoUnit.MONTHS.between(
                YearMonth.from(registrationDate),
                YearMonth.from(referenceDate)
        );
        BillingCycle cycle = cycleAtOffset(registrationDate, monthOffset);

        if (cycle.startDate().isAfter(referenceDate)) {
            cycle = cycleAtOffset(registrationDate, monthOffset - 1);
        }

        return cycle;
    }

    private static BillingCycle cycleAtOffset(LocalDate registrationDate, long monthOffset) {
        LocalDate startDate = registrationDate.plusMonths(Math.max(monthOffset, 0));
        LocalDate nextStartDate = registrationDate.plusMonths(Math.max(monthOffset + 1, 1));
        return new BillingCycle(startDate, nextStartDate.minusDays(1));
    }
}
