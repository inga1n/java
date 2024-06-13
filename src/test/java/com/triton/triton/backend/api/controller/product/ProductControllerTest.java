package com.triton.triton.backend.api.controller.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Класс для тестирования ProductController
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    /** Mocked MVC. */
    @Autowired
    private MockMvc mvc;

    /**
     * Проверка получения листа продуктов.
     * @throws Exception
     */
    @Test
    public void testProductList() throws Exception {
        mvc.perform(get("/product")).andExpect(status().is(HttpStatus.OK.value()));
    }

}