package com.adega.service.billing;

import java.time.LocalDate;

public record BillingCycle(
        LocalDate startDate,
        LocalDate endDate
) {
}
