package com.proggertopper.doctorRegistrationSystem.util;

import com.proggertopper.doctorRegistrationSystem.exception.InvalidPhoneException;

import java.util.Set;

public class PhoneUtils {

    private static final Set<String> UKRAINIAN_OPERATOR_CODES = Set.of(
            "39", "50", "63", "66", "67", "68",
            "73", "91", "92", "93", "94", "95",
            "96", "97", "98", "99"
    );

    private PhoneUtils() {
    }

    public static String normalize(String phone) {
        if (phone == null) {
            return "";
        }

        String digits = phone.replaceAll("[^0-9]", "");

        if (digits.startsWith("0") && digits.length() == 10) {
            digits = "38" + digits;
        }

        return digits;
    }

    public static void validateUkrainianPhone(String phone) {
        String normalizedPhone = normalize(phone);

        if (normalizedPhone.length() != 12) {
            throw new InvalidPhoneException(
                    "Invalid phone number"
            );
        }

        if (!normalizedPhone.startsWith("380")) {
            throw new InvalidPhoneException(
                    "Invalid phone number"
            );
        }

        String operatorCode = normalizedPhone.substring(3, 5);

        if (!UKRAINIAN_OPERATOR_CODES.contains(operatorCode)) {
            throw new InvalidPhoneException(
                    "Invalid phone number"
            );
        }
    }
}
