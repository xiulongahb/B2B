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
    void register_passwordLogin_thenMe() throws Exception {
        String phone = "13800138001";
        String username = "testuser01";
        String password = "Test1234a";

        String code = sendSmsAndGetCode(phone);

        mockMvc.perform(
                        post("/api/mall/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"username\":\""
                                                + username
                                                + "\",\"password\":\""
                                                + password
                                                + "\",\"phone\":\""
                                                + phone
                                                + "\",\"smsCode\":\""
                                                + code
                                                + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.phone").value(phone));

        MvcResult login =
                mockMvc.perform(
                                post("/api/mall/auth/password/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"username\":\""
                                                        + username
                                                        + "\",\"password\":\""
                                                        + password
                                                        + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken").exists())
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
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.phone").value(phone));
    }

    @Test
    void register_thenSmsLogin() throws Exception {
        String phone = "13800138002";
        String username = "testuser02";
        String password = "Test1234b";

        String code1 = sendSmsAndGetCode(phone);
        mockMvc.perform(
                        post("/api/mall/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"username\":\""
                                                + username
                                                + "\",\"password\":\""
                                                + password
                                                + "\",\"phone\":\""
                                                + phone
                                                + "\",\"smsCode\":\""
                                                + code1
                                                + "\"}"))
                .andExpect(status().isOk());

        String code2 = sendSmsAndGetCode(phone);

        MvcResult login =
                mockMvc.perform(
                                post("/api/mall/auth/sms/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"phone\":\""
                                                        + phone
                                                        + "\",\"code\":\""
                                                        + code2
                                                        + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username").value(username))
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
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void smsLogin_withoutRegister_returns401() throws Exception {
        String phone = "13800138999";
        String code = sendSmsAndGetCode(phone);

        mockMvc.perform(
                        post("/api/mall/auth/sms/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"phone\":\""
                                                + phone
                                                + "\",\"code\":\""
                                                + code
                                                + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/mall/auth/me")).andExpect(status().isUnauthorized());
    }

    private String sendSmsAndGetCode(String phone) throws Exception {
        MvcResult send =
                mockMvc.perform(
                                post("/api/mall/auth/sms/send")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"phone\":\"" + phone + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.debugCode").exists())
                        .andReturn();

        JsonNode sendJson = objectMapper.readTree(send.getResponse().getContentAsString());
        return sendJson.get("debugCode").asText();
    }
}
