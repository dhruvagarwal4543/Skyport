package com.skyport.app.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class PricingUtils {

    public static final int FEES_PER_PASSENGER = 350;
    public static final double GST_RATE = 0.05;

    public static int getAdultFare(int baseFare) {
        return baseFare;
    }

    public static int getChildFare(int baseFare) {
        return (int) (baseFare * 0.8);
    }

    public static int getInfantFare(int baseFare) {
        return (int) (baseFare * 0.2);
    }

    public static int calculateTotalBaseFare(int baseFare, int adults, int children, int infants) {
        return (adults * getAdultFare(baseFare)) +
               (children * getChildFare(baseFare)) +
               (infants * getInfantFare(baseFare));
    }

    public static int calculateTaxesAndFees(int adults, int children) {
        return (adults + children) * FEES_PER_PASSENGER;
    }

    public static int calculateGst(int totalBaseFare, int taxesAndFees) {
        return (int) ((totalBaseFare + taxesAndFees) * GST_RATE);
    }

    public static int calculateTotalPayable(int baseFare, int adults, int children, int infants) {
        int totalBaseFare = calculateTotalBaseFare(baseFare, adults, children, infants);
        int taxes = calculateTaxesAndFees(adults, children);
        int gst = calculateGst(totalBaseFare, taxes);
        return totalBaseFare + taxes + gst;
    }

    public static String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        return "₹ " + formatter.format(price);
    }

    public static int parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return 0;
        try {
            return Integer.parseInt(priceStr.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
