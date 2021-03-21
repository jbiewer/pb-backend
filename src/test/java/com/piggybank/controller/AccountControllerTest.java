package com.piggybank.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
//@AutoConfigureMockMvc
public class AccountControllerTest {
//    @Autowired
//    private MockMvc mockMvc;

    @Test
    public void testTestReturnsAnything() {
//        try {
//            mockMvc.perform(get("/account/test"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andReturn();

//        ResponseEntity<String> response = restTemplate.getForEntity(url + "/test", String.class);
//        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        Assertions.assertNotNull(response.getBody());
//        } catch (Exception e) {
//            Assertions.fail(e);
//        }
    }
}
