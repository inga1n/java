package com.triton.triton.backend.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.triton.triton.backend.api.model.LoginBody;
import com.triton.triton.backend.api.model.PasswordResetBody;
import com.triton.triton.backend.api.model.RegistrationBody;
import com.triton.triton.backend.exception.EmailFailureException;
import com.triton.triton.backend.exception.EmailNotFoundException;
import com.triton.triton.backend.exception.UserAlreadyExistsException;
import com.triton.triton.backend.exception.UserNotVerifiedException;
import com.triton.triton.backend.model.LocalUser;
import com.triton.triton.backend.model.VerificationToken;
import com.triton.triton.backend.model.repository.LocalUserRepository;
import com.triton.triton.backend.model.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

/**
 * Service for handling user actions.
 */
@Service
public class UserService {

    /** The LocalUserDAO. */
    private LocalUserRepository localUserRepository;
    /** The VerificationTokenDAO. */
    private VerificationTokenRepository verificationTokenRepository;
    /** The encryption service. */
    private EncryptionService encryptionService;
    /** The JWT service. */
    private JWTService jwtService;
    /** The email service. */
    private EmailService emailService;

    /**
     * Constructor injected by spring.
     *
     * @param localUserRepository
     * @param verificationTokenRepository
     * @param encryptionService
     * @param jwtService
     * @param emailService
     */
    public UserService(LocalUserRepository localUserRepository, VerificationTokenRepository verificationTokenRepository, EncryptionService encryptionService,
                       JWTService jwtService, EmailService emailService) {
        this.localUserRepository = localUserRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    /**
     * Attempts to register a user given the information provided.
     * @param registrationBody The registration information.
     * @return The local user that has been written to the database.
     * @throws UserAlreadyExistsException Thrown if there is already a user with the given information.
     */
    public LocalUser registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException, EmailFailureException {
        if (localUserRepository.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
                || localUserRepository.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        LocalUser user = new LocalUser();
        user.setEmail(registrationBody.getEmail());
        user.setUsername(registrationBody.getUsername());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        VerificationToken verificationToken     = createVerificationToken(user);
        emailService.sendVerificationEmail(verificationToken);
        return localUserRepository.save(user);
    }

    /**
     * Creates a VerificationToken object for sending to the user.
     * @param user The user the token is being generated for.
     * @return The object created.
     */
    private VerificationToken createVerificationToken(LocalUser user) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        verificationToken.setUser(user);
        user.getVerificationTokens().add(verificationToken);
        return verificationToken;
    }

    /**
     * Logins in a user and provides an authentication token back.
     * @param loginBody The login request.
     * @return The authentication token. Null if the request was invalid.
     */
    public String loginUser(LoginBody loginBody) throws UserNotVerifiedException, EmailFailureException {
        Optional<LocalUser> opUser = localUserRepository.findByUsernameIgnoreCase(loginBody.getUsername());
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            if (encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())) {
                if (user.isEmailVerified()) {
                    return jwtService.generateJWT(user);
                } else {
                    List<VerificationToken> verificationTokens = user.getVerificationTokens();
                    boolean resend = verificationTokens.size() == 0 ||
                            verificationTokens.get(0).getCreatedTimestamp().before(new Timestamp(System.currentTimeMillis() - (60 * 60 * 1000)));
                    if (resend) {
                        VerificationToken verificationToken = createVerificationToken(user);
                        verificationTokenRepository.save(verificationToken);
                        emailService.sendVerificationEmail(verificationToken);
                    }
                    throw new UserNotVerifiedException(resend);
                }
            }
        }
        return null;
    }

    /**
     * Verifies a user from the given token.
     * @param token The token to use to verify a user.
     * @return True if it was verified, false if already verified or token invalid.
     */
    @Transactional
    public boolean verifyUser(String token) {
        Optional<VerificationToken> opToken = verificationTokenRepository.findByToken(token);
        if (opToken.isPresent()) {
            VerificationToken verificationToken = opToken.get();
            LocalUser user = verificationToken.getUser();
            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                localUserRepository.save(user);
                verificationTokenRepository.deleteByUser(user);
                return true;
            }
        }
        return false;
    }

    /**
     * Sends the user a forgot password reset based on the email provided.
     * @param email The email to send to.
     * @throws EmailNotFoundException Thrown if there is no user with that email.
     * @throws EmailFailureException
     */
    public void forgotPassword(String email) throws EmailNotFoundException, EmailFailureException {
        Optional<LocalUser> opUser = localUserRepository.findByEmailIgnoreCase(email);
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            String token = jwtService.generatePasswordResetJWT(user);
            emailService.sendPasswordResetEmail(user, token);
        } else {
            throw new EmailNotFoundException();
        }
    }

    /**
     * Resets the users password using a given token and email.
     * @param body The password reset information.
     */
    public void resetPassword(PasswordResetBody body) {
        String email = jwtService.getResetPasswordEmail(body.getToken());
        Optional<LocalUser> opUser = localUserRepository.findByEmailIgnoreCase(email);
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            user.setPassword(encryptionService.encryptPassword(body.getPassword()));
            localUserRepository.save(user);
        }
    }

}
