package com.studyshield.tv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.tv.dto.WifiNetworkRequest;
import com.studyshield.tv.entity.User;
import com.studyshield.tv.entity.WifiNetwork;
import com.studyshield.tv.repository.UserRepository;
import com.studyshield.tv.repository.WifiNetworkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WifiNetworkControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private WifiNetworkRepository wifiNetworkRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        wifiNetworkRepository.deleteAll();
        userRepository.deleteAll();
        user = userRepository.save(User.builder().externalId("ext-1").name("Test User").build());
    }

    @Test
    void shouldCreateWifiNetwork() throws Exception {
        WifiNetworkRequest request = new WifiNetworkRequest("MyWiFi", "AA:BB:CC:DD:EE:FF", user.getId());
        mockMvc.perform(post("/api/v1/wifi-networks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ssid").value("MyWiFi"));
    }

    @Test
    void shouldGetWifiNetworkById() throws Exception {
        WifiNetwork wn = wifiNetworkRepository.save(
                WifiNetwork.builder().ssid("MyWiFi").user(user).build());
        mockMvc.perform(get("/api/v1/wifi-networks/{id}", wn.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ssid").value("MyWiFi"));
    }

    @Test
    void shouldGetByUserId() throws Exception {
        wifiNetworkRepository.save(WifiNetwork.builder().ssid("WiFi1").user(user).build());
        mockMvc.perform(get("/api/v1/wifi-networks/user/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldDeleteWifiNetwork() throws Exception {
        WifiNetwork wn = wifiNetworkRepository.save(
                WifiNetwork.builder().ssid("MyWiFi").user(user).build());
        mockMvc.perform(delete("/api/v1/wifi-networks/{id}", wn.getId()))
                .andExpect(status().isNoContent());
    }
}
