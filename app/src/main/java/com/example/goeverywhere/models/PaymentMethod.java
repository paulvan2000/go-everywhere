package com.example.goeverywhere.models;

public class PaymentMethod {
    private String id;
    private String cardNumber;
    private String expiryDate;
    private String cardType;
    private boolean isDefault;

    public PaymentMethod(String id, String cardNumber, String expiryDate, String cardType, boolean isDefault) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cardType = cardType;
        this.isDefault = isDefault;
    }

    public String getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getMaskedCardNumber() {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCardType() {
        return cardType;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public static String detectCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "Unknown";
        }

        // Remove any spaces or dashes
        cardNumber = cardNumber.replaceAll("[ -]", "");

        if (cardNumber.matches("^4[0-9]{12}(?:[0-9]{3})?$")) {
            return "Visa";
        } else if (cardNumber.matches("^5[1-5][0-9]{14}$")) {
            return "MasterCard";
        } else if (cardNumber.matches("^3[47][0-9]{13}$")) {
            return "American Express";
        } else if (cardNumber.matches("^6(?:011|5[0-9]{2})[0-9]{12}$")) {
            return "Discover";
        } else {
            return "Unknown";
        }
    }
} 