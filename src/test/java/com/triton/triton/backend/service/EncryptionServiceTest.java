package com.triton.triton.backend.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Класс для теста Encryption Service.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class EncryptionServiceTest {

    /** The EncryptionService. */
    @Autowired
    private EncryptionService encryptionService;

    /**
     * Тест того что зашифрованный пароль все ещё валидный для верификации.
     */
    @Test
    public void testPasswordEncryption() {
        String password = "PasswordIsASecret!123";
        String hash = encryptionService.encryptPassword(password);
        Assertions.assertTrue(encryptionService.verifyPassword(password, hash), "Хешированный пароль должен совпадать с оригиналом.");
        Assertions.assertFalse(encryptionService.verifyPassword(password + "Sike!", hash), "Измененный пароль не должен быть валидным");
    }

}