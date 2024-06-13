package com.triton.triton.backend.api.controller.order;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triton.triton.backend.model.WebOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Класс для тестирования OrderController.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    /** Mocked MVC. */
    @Autowired
    private MockMvc mvc;

    /**
     * Тест того что запрошенный authenticated order list принадлежит user A.
     * @throws Exception
     */
    @Test
    @WithUserDetails("UserA")
    public void testUserAAuthenticatedOrderList() throws Exception {
        testAuthenticatedListBelongsToUser("UserA");
    }

    /**
     * Тест того что запрошенный authenticated order list принадлежит user B.
     * @throws Exception
     */
    @Test
    @WithUserDetails("UserB")
    public void testUserBAuthenticatedOrderList() throws Exception {
        testAuthenticatedListBelongsToUser("UserB");
    }


    /**
     * Тест того что запрошенный authenticated order list принадлежит данному пользователю.
     * @param username имя пользователя для теста.
     * @throws Exception
     */
    private void testAuthenticatedListBelongsToUser(String username) throws Exception {
        mvc.perform(get("/order")).andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    List<WebOrder> orders = new ObjectMapper().readValue(json, new TypeReference<List<WebOrder>>() {});
                    for (WebOrder order : orders) {
                        Assertions.assertEquals(username, order.getUser().getUsername(), "Order list должны быть только заказы для этого пользователя");
                    }
                });
    }

    /**
     * Тест того что unauthenticated users не получают данных.
     * @throws Exception
     */
    @Test
    public void testUnauthenticatedOrderList() throws Exception {
        mvc.perform(get("/order")).andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

}
