package com.triton.triton.backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.MissingClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.triton.triton.backend.model.LocalUser;
import com.triton.triton.backend.model.dao.LocalUserDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Класс для теста JWTService.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class JWTServiceTest {

    /** The JWTService. */
    @Autowired
    private JWTService jwtService;
    /** The Local User DAO. */
    @Autowired
    private LocalUserDAO localUserDAO;
    /** Ключ алгоритма который мы используем в properties file. */
    @Value("${jwt.algorithm.key}")
    private String algorithmKey;

    /**
     * Тест того что токен верификации неиспользуем для логина.
     */
    @Test
    public void testVerificationTokenNotUsableForLogin() {
        LocalUser user = localUserDAO.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generateVerificationJWT(user);
        Assertions.assertNull(jwtService.getUsername(token), "В токене верификации не должно быть юзернейма");
    }

    /**
     * Проверка что генератор токена аутентификации все ещё возвращает юзернейм.
     */
    @Test
    public void testAuthTokenReturnsUsername() {
        LocalUser user = localUserDAO.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generateJWT(user);
        Assertions.assertEquals(user.getUsername(), jwtService.getUsername(token), "Токен для авторизации должен содержать юзернейм.");
    }

    /**
     * Тест на то что когда кто-то генерирует JWT алгоритмом, отличным от нашего
     * верификация отклоняет токен и не верифицирует.
     */
    @Test
    public void testLoginJWTNotGeneratedByUs() {
        String token =
                JWT.create().withClaim("USERNAME", "UserA").sign(Algorithm.HMAC256(
                        "NotTheRealSecret"));
        Assertions.assertThrows(SignatureVerificationException.class,
                () -> jwtService.getUsername(token));
    }

    /**
     * Тест того, что когда JWT токен сгенерирован и не содержит нас как
     * того, кто это делал, мы отклоняем его.
     */
    @Test
    public void testLoginJWTCorrectlySignedNoIssuer() {
        String token =
                JWT.create().withClaim("USERNAME", "UserA")
                        .sign(Algorithm.HMAC256(algorithmKey));
        Assertions.assertThrows(MissingClaimException.class,
                () -> jwtService.getUsername(token));
    }

    /**
     * Тест на то что когда кто-то генерирует JWT алгоритмом, отличным от нашего
     * верификация отклоняет токен и не верифицирует.
     */
    @Test
    public void testResetPasswordJWTNotGeneratedByUs() {
        String token =
                JWT.create().withClaim("RESET_PASSWORD_EMAIL", "UserA@junit.com").sign(Algorithm.HMAC256(
                        "NotTheRealSecret"));
        Assertions.assertThrows(SignatureVerificationException.class,
                () -> jwtService.getResetPasswordEmail(token));
    }

    /**
     * Тест того, что когда JWT токен сгенерирован и не содержит нас как
     * того, кто это делал, мы отклоняем его.
     */
    @Test
    public void testResetPasswordJWTCorrectlySignedNoIssuer() {
        String token =
                JWT.create().withClaim("RESET_PASSWORD_EMAIL", "UserA@junit.com")
                        .sign(Algorithm.HMAC256(algorithmKey));
        Assertions.assertThrows(MissingClaimException.class,
                () -> jwtService.getResetPasswordEmail(token));
    }

    /**
     * Тест ресета генерации пароля и верификации.
     */
    @Test
    public void testPasswordResetToken() {
        LocalUser user = localUserDAO.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generatePasswordResetJWT(user);
        Assertions.assertEquals(user.getEmail(),
                jwtService.getResetPasswordEmail(token), "Почта должна совпадать с той что внутри " +
                        "JWT.");
    }

}
