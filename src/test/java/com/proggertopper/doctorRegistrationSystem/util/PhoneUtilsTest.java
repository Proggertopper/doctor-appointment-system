package com.proggertopper.doctorRegistrationSystem.util;

import com.proggertopper.doctorRegistrationSystem.exception.InvalidPhoneException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneUtilsTest {

    @Test
    void normalizesLocalUkrainianPhone() {
        assertThat(PhoneUtils.normalize("067 123 45 67"))
                .isEqualTo("380671234567");
    }

    @Test
    void acceptsValidUkrainianPhone() {
        PhoneUtils.validateUkrainianPhone("+380 67 123 45 67");
    }

    @Test
    void rejectsInvalidOperatorCode() {
        assertThatThrownBy(() -> PhoneUtils.validateUkrainianPhone("+380 11 123 45 67"))
                .isInstanceOf(InvalidPhoneException.class);
    }
}
