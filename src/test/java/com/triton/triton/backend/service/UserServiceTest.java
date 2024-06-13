package com.triton.triton.backend.service;

import java.util.List;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.triton.triton.backend.api.model.LoginBody;
import com.triton.triton.backend.api.model.PasswordResetBody;
import com.triton.triton.backend.api.model.RegistrationBody;
import com.triton.triton.backend.exception.EmailFailureException;
import com.triton.triton.backend.exception.EmailNotFoundException;
import com.triton.triton.backend.exception.UserAlreadyExistsException;
import com.triton.triton.backend.exception.UserNotVerifiedException;
import com.triton.triton.backend.model.LocalUser;
import com.triton.triton.backend.model.VerificationToken;
import com.triton.triton.backend.model.dao.LocalUserDAO;
import com.triton.triton.backend.model.dao.VerificationTokenDAO;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * Тестовый класс для юнит-теста UserService класса.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {

    /** Расширение чтобы пародировать отправку почты. */
    @RegisterExtension
    private static GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
            .withPerMethodLifecycle(true);
    /** UserService для теста. */
    @Autowired
    private UserService userService;
    /** The JWT Service. */
    @Autowired
    private JWTService jwtService;
    /** The Local User DAO. */
    @Autowired
    private LocalUserDAO localUserDAO;
    /** The encryption Service. */
    @Autowired
    private EncryptionService encryptionService;
    /** The Verification Token DAO. */
    @Autowired
    private VerificationTokenDAO verificationTokenDAO;

    /**
     * Тестирует процесс регистрации пользователя.
     * @throws MessagingException Срабатывает если пародирование отправки почты почему-то не сработает.
     */
    @Test
    @Transactional
    public void testRegisterUser() throws MessagingException {
        RegistrationBody body = new RegistrationBody();
        body.setUsername("UserA");
        body.setEmail("UserServiceTest$testRegisterUser@junit.com");
        body.setFirstName("FirstName");
        body.setLastName("LastName");
        body.setPassword("MySecretPassword123");
        Assertions.assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerUser(body), "Имя пользователя уже должно использоваться.");
        body.setUsername("UserServiceTest$testRegisterUser");
        body.setEmail("UserA@junit.com");
        Assertions.assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerUser(body), "Почта уже должна использоваться.");
        body.setEmail("UserServiceTest$testRegisterUser@junit.com");
        Assertions.assertDoesNotThrow(() -> userService.registerUser(body),
                "User should register successfully.");
        Assertions.assertEquals(body.getEmail(), greenMailExtension.getReceivedMessages()[0]
                .getRecipients(Message.RecipientType.TO)[0].toString());
    }

    /**
     * Тест loginUser метода.
     * @throws UserNotVerifiedException
     * @throws EmailFailureException
     */
    @Test
    @Transactional
    public void testLoginUser() throws UserNotVerifiedException, EmailFailureException {
        LoginBody body = new LoginBody();
        body.setUsername("UserA-NotExists");
        body.setPassword("PasswordA123-BadPassword");
        Assertions.assertNull(userService.loginUser(body), "Пользователь не должен существовать.");
        body.setUsername("UserA");
        Assertions.assertNull(userService.loginUser(body), "Пароль должен быть неверен.");
        body.setPassword("PasswordA123");
        Assertions.assertNotNull(userService.loginUser(body), "Пользователь должен успешно подключиться.");
        body.setUsername("UserB");
        body.setPassword("PasswordB123");
        try {
            userService.loginUser(body);
            Assertions.assertTrue(false, "Пользователь должен быть без подтвержденной почты.");
        } catch (UserNotVerifiedException ex) {
            Assertions.assertTrue(ex.isNewEmailSent(), "Письмо на подтверждение почты не должно быть отправлено.");
            Assertions.assertEquals(1, greenMailExtension.getReceivedMessages().length);
        }
        try {
            userService.loginUser(body);
            Assertions.assertTrue(false, "Пользователь должен быть без подтвержденной почты");
        } catch (UserNotVerifiedException ex) {
            Assertions.assertFalse(ex.isNewEmailSent(), "Письмо на подтверждение почты не должно быть отправлено повторно.");
            Assertions.assertEquals(1, greenMailExtension.getReceivedMessages().length);
        }
    }

    /**
     * Тест verifyUser.
     * @throws EmailFailureException
     */
    @Test
    @Transactional
    public void testVerifyUser() throws EmailFailureException {
        Assertions.assertFalse(userService.verifyUser("Bad Token"), "Плохие или несуществующие токены должны вернуть false.");
        LoginBody body = new LoginBody();
        body.setUsername("UserB");
        body.setPassword("PasswordB123");
        try {
            userService.loginUser(body);
            Assertions.assertTrue(false, "Пользователь должен быть без подтвержденной почты");
        } catch (UserNotVerifiedException ex) {
            List<VerificationToken> tokens = verificationTokenDAO.findByUser_IdOrderByIdDesc(2L);
            String token = tokens.get(0).getToken();
            Assertions.assertTrue(userService.verifyUser(token), "Токен должен быть корректным.");
            Assertions.assertNotNull(body, "Пользователь должен пройти верификацию.");
        }
    }

    /**
     * Тестирует forgotPassword метод из User Service.
     * @throws MessagingException
     */
    @Test
    @Transactional
    public void testForgotPassword() throws MessagingException {
        Assertions.assertThrows(EmailNotFoundException.class,
                () -> userService.forgotPassword("UserNotExist@junit.com"));
        Assertions.assertDoesNotThrow(() -> userService.forgotPassword(
                "UserA@junit.com"), "Несуществующие почты должны быть отклонены");
        Assertions.assertEquals("UserA@junit.com",
                greenMailExtension.getReceivedMessages()[0]
                        .getRecipients(Message.RecipientType.TO)[0].toString(), "Пароль " +
                        "reset-письмо должны быть отправлены.");
    }

    /**
     * Тест resetPassword метода из User Service.
     * @throws MessagingException
     */
    public void testResetPassword() {
        LocalUser user = localUserDAO.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generatePasswordResetJWT(user);
        PasswordResetBody body = new PasswordResetBody();
        body.setToken(token);
        body.setPassword("Password123456");
        userService.resetPassword(body);
        user = localUserDAO.findByUsernameIgnoreCase("UserA").get();
        Assertions.assertTrue(encryptionService.verifyPassword("Password123456",
                user.getPassword()), "Смена пароля должна быть записана в БД");
    }

}
