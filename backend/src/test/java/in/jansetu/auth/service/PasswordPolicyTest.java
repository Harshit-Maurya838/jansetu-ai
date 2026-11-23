package in.jansetu.auth.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordPolicyTest {
    @ParameterizedTest
    @ValueSource(strings = {"abcdefg1", "Passw0rd", "हिन्दीabc12", "a1a1a1a1a1"})
    void accepts(String pw) {
        assertThat(PasswordPolicy.isValid(pw)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc12", "abcdefgh", "12345678", "        "})
    void rejects(String pw) {
        assertThat(PasswordPolicy.isValid(pw)).isFalse();
    }

    @org.junit.jupiter.api.Test
    void rejectsNullAndOver72Bytes() {
        assertThat(PasswordPolicy.isValid(null)).isFalse();
        assertThat(PasswordPolicy.isValid("a1" + "x".repeat(71))).isFalse();
    }
}
