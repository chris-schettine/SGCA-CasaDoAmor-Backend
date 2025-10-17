package br.com.casadoamor.sgca.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "bXktc2VjcmV0LWJhc2U2NC1mb3ItdGVzdHMtc2VjcmV0LTIyMjI=");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 3600L * 1000L); // 1 hour
    }

    @Test
    void JwtUtil_generateToken_WithValidUser_ReturnsToken() {
        UserDetails user = User.withUsername("12345678900").password("pwd").roles("RECEPCIONISTA").build();

        String token = jwtUtil.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void JwtUtil_extractUsername_FromValidToken_ReturnsUsername() {
        UserDetails user = User.withUsername("98765432100").password("pwd").roles("RECEPCIONISTA").build();
        String token = jwtUtil.generateToken(user);

        String username = jwtUtil.extractUsername(token);

        assertEquals("98765432100", username);
    }

    @Test
    void JwtUtil_isTokenValid_WithValidTokenAndUser_ReturnsTrue() {
        UserDetails user = User.withUsername("55566677788").password("pwd").roles("RECEPCIONISTA").build();
        String token = jwtUtil.generateToken(user);

        boolean valid = jwtUtil.validateToken(token, user);

        assertTrue(valid);
    }

    @Test
    void JwtUtil_isTokenValid_WithTokenSignedWithDifferentKey_ReturnsFalseOrThrows() {
        UserDetails user = User.withUsername("00011122233").password("pwd").roles("RECEPCIONISTA").build();
        String token = jwtUtil.generateToken(user);

        JwtUtil other = new JwtUtil();
        ReflectionTestUtils.setField(other, "jwtSecret", "YW5vdGhlci1zZWNyZXQtc2VjcmV0LWZvci10ZXN0cy0xMjM=");
        ReflectionTestUtils.setField(other, "jwtExpirationMs", 3600L * 1000L);

        try {
            boolean valid = other.validateToken(token, user);
            assertFalse(valid);
        } catch (Exception ex) {
            assertTrue(ex instanceof RuntimeException || ex instanceof io.jsonwebtoken.JwtException);
        }
    }
}
