package com.adetayo.verve_guard.enums;


public enum FraudSignal {

    CARD_BLACKLISTED(
            "This card has been blocked from processing transactions."),
    BLACKLISTED_MERCHANT(
            "This merchant is not authorised to process transactions."),
    RATE_LIMIT_EXCEEDED(
            "Too many requests detected from this source. Please try again later."),
    CARD_VELOCITY_EXCEEDED(
            "Unusual card activity detected. This card has been suspended."),
    MERCHANT_CATEGORY_LIMIT_EXCEEDED(
            "Transaction amount exceeds your merchant category limit. " +
                    "Please upgrade to process higher amounts."),
    AMOUNT_ANOMALY(
            "Transaction flagged due to unusual spending pattern."),
    PENDING_ADMIN_REVIEW(
            "Transaction is under review. Please contact support."),
    REPEAT_OFFENDER(
            "Suspicious activity pattern detected. This card has been suspended.");

    private final String message;

    FraudSignal(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
