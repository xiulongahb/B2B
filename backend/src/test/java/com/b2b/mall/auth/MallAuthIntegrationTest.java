package com.b2b.mall.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MallAuthIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void sendLoginCode_thenLogin_thenMe() throws Exception {
        String phone = "13800138000";

        MvcResult send =
                mockMvc.perform(
                                post("/api/mall/auth/sms/send")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"phone\":\"" + phone + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.message").exists())
                        .andExpect(jsonPath("$.debugCode").exists())
                        .andReturn();

        JsonNode sendJson = objectMapper.readTree(send.getResponse().getContentAsString());
        String code = sendJson.get("debugCode").asText();

        MvcResult login =
                mockMvc.perform(
                                post("/api/mall/auth/sms/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"phone\":\""
                                                        + phone
                                                        + "\",\"code\":\""
                                                        + code
                                                        + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken").exists())
                        .andExpect(jsonPath("$.memberId").exists())
                        .andReturn();

        String token =
                objectMapper
                        .readTree(login.getResponse().getContentAsString())
                        .get("accessToken")
                        .asText();

        mockMvc.perform(
                        get("/api/mall/auth/me")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").exists())
                .andExpect(jsonPath("$.phone").value(phone));
    }

    @Test
    void me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/mall/auth/me")).andExpect(status().isUnauthorized());
    }
}
