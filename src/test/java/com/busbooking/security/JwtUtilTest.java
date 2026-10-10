package com.busbooking.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET = "unit-test-secret-key-that-is-at-least-32-chars-long";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000);

    @Test
    void generatedToken_containsEmailAsSubject() {
        String token = jwtUtil.generateToken("ravi@test.com");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("ravi@test.com");
    }

    @Test
    void tamperedToken_isRejected() {
        String token = jwtUtil.generateToken("ravi@test.com");
        String tampered = token.substring(0, token.length() - 3) + "abc";
        assertThatThrownBy(() -> jwtUtil.extractEmail(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void expiredToken_isRejected() {
        JwtUtil shortLived = new JwtUtil(SECRET, -1000);
        String token = shortLived.generateToken("ravi@test.com");
        assertThatThrownBy(() -> jwtUtil.extractEmail(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenSignedWithDifferentSecret_isRejected() {
        JwtUtil other = new JwtUtil("a-completely-different-secret-key-of-32-plus-chars", 60_000);
        String token = other.generateToken("ravi@test.com");
        assertThatThrownBy(() -> jwtUtil.extractEmail(token)).isInstanceOf(JwtException.class);
    }
}
