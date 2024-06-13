package com.triton.triton.backend.api.security;

import com.triton.triton.backend.model.LocalUser;
import com.triton.triton.backend.model.dao.LocalUserDAO;
import com.triton.triton.backend.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Класс для тестирования JWTRequestFilter.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class JWTRequestFilterTest {

    /** Mocked MVC. */
    @Autowired
    private MockMvc mvc;
    /** The JWT Service. */
    @Autowired
    private JWTService jwtService;
    /** The Local User DAO. */
    @Autowired
    private LocalUserDAO localUserDAO;
    /** Путь который должен пропускать только аутентифицированных пользователей. */
    private static final String AUTHENTICATED_PATH = "/auth/me";

    /**
     * Тест проверяющий отклонение неаутентифицированных пользователей.
     * @throws Exception
     */
    @Test
    public void testUnauthenticatedRequest() throws Exception {
        mvc.perform(get(AUTHENTICATED_PATH)).andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    /**
     * Тест проверяющий отклонение плохих токенов.
     * @throws Exception
     */
    @Test
    public void testBadToken() throws Exception {
        mvc.perform(get(AUTHENTICATED_PATH).header("Authorization", "BadTokenThatIsNotValid"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
        mvc.perform(get(AUTHENTICATED_PATH).header("Authorization", "Bearer BadTokenThatIsNotValid"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    /**
     * Тест того, что неверифицированные пользователя как-то получившие jwt отклонены.
     * @throws Exception
     */
    @Test
    public void testUnverifiedUser() throws Exception {
        LocalUser user = localUserDAO.findByUsernameIgnoreCase("UserB").get();
        String token = jwtService.generateJWT(user);
        mvc.perform(get(AUTHENTICATED_PATH).header("Authorization", "Bearer " + token))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    /**
     * Тест успешной аутентификации.
     * @throws Exception
     */
    @Test
    public void testSuccessful() throws Exception {
        LocalUser user = localUserDAO.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generateJWT(user);
        mvc.perform(get(AUTHENTICATED_PATH).header("Authorization", "Bearer " + token))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

}
