package com.studyshield.regression.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyshield.regression.client.TvDeviceApi;
import com.studyshield.regression.context.ScenarioContext;
import com.studyshield.regression.support.IdRegistry;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TvDeviceSteps {

    private final TvDeviceApi tvDeviceApi;
    private final ScenarioContext context;
    private final IdRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();

    public TvDeviceSteps(TvDeviceApi tvDeviceApi, ScenarioContext context, IdRegistry registry) {
        this.tvDeviceApi = tvDeviceApi;
        this.context = context;
        this.registry = registry;
    }

    @Given("a WiFi network {string} exists for current user")
    public void aWifiNetworkExistsForCurrentUser(String ssid) throws Exception {
        Long tvUserId = ensureTvUserExists();
        String json = mapper.writeValueAsString(Map.of(
                "ssid", context.uniqueName(ssid),
                "bssid", "AA:BB:CC:DD:EE:FF",
                "userId", tvUserId
        ));
        Response response = tvDeviceApi.createWifiNetwork(json);
        assertThat(response.getStatusCode()).isEqualTo(201);
        context.setLastResponse(response);
        context.setLastStatusCode(response.getStatusCode());
        Long id = response.jsonPath().getLong("id");
        context.setCurrentWifiNetworkId(id);
        registry.register("wifi-network", id);
    }

    @When("I create a WiFi network with SSID {string}")
    public void iCreateWifiNetworkWithSSID(String ssid) throws Exception {
        Long tvUserId = ensureTvUserExists();
        String json = mapper.writeValueAsString(Map.of(
                "ssid", context.uniqueName(ssid),
                "bssid", "AA:BB:CC:DD:EE:FF",
                "userId", tvUserId
        ));
        Response response = tvDeviceApi.createWifiNetwork(json);
        updateContext(response);
        if (response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            context.setCurrentWifiNetworkId(id);
            registry.register("wifi-network", id);
        }
    }

    private Long ensureTvUserExists() throws Exception {
        if (context.getCurrentTvUserId() != null) {
            return context.getCurrentTvUserId();
        }
        String externalId = String.valueOf(context.getCurrentUserId());
        Response getResp = tvDeviceApi.createUser(mapper.writeValueAsString(Map.of(
                "externalId", externalId,
                "name", "TV User " + externalId
        )));
        Long tvUserId;
        if (getResp.getStatusCode() == 201) {
            tvUserId = getResp.jsonPath().getLong("id");
        } else {
            Response existing = tvDeviceApi.createUser(mapper.writeValueAsString(Map.of(
                    "externalId", externalId,
                    "name", "TV User " + externalId
            )));
            if (existing.getStatusCode() == 201) {
                tvUserId = existing.jsonPath().getLong("id");
            } else {
                throw new RuntimeException("Could not create or find TV user for externalId: " + externalId);
            }
        }
        context.setCurrentTvUserId(tvUserId);
        return tvUserId;
    }

    @When("I get WiFi networks for current user")
    public void iGetWifiNetworksForCurrentUser() {
        Response response = tvDeviceApi.getWifiNetworksByUser(context.getCurrentTvUserId());
        updateContext(response);
    }

    @Given("a connected TV {string} exists on current WiFi network")
    public void aConnectedTvExistsOnCurrentWifiNetwork(String deviceName) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "deviceName", context.uniqueName(deviceName),
                "macAddress", "11:22:33:44:55:66",
                "ipAddress", "192.168.1.100",
                "wifiNetworkId", context.getCurrentWifiNetworkId(),
                "active", true
        ));
        Response response = tvDeviceApi.createConnectedTv(json);
        assertThat(response.getStatusCode()).isEqualTo(201);
        context.setLastResponse(response);
        context.setLastStatusCode(response.getStatusCode());
        Long id = response.jsonPath().getLong("id");
        context.setCurrentConnectedTvId(id);
        registry.register("connected-tv", id);
    }

    @When("I create a connected TV {string}")
    public void iCreateConnectedTv(String deviceName) throws Exception {
        String json = mapper.writeValueAsString(Map.of(
                "deviceName", context.uniqueName(deviceName),
                "macAddress", "11:22:33:44:55:66",
                "ipAddress", "192.168.1.100",
                "wifiNetworkId", context.getCurrentWifiNetworkId(),
                "active", true
        ));
        Response response = tvDeviceApi.createConnectedTv(json);
        updateContext(response);
        if (response.getStatusCode() == 201) {
            Long id = response.jsonPath().getLong("id");
            context.setCurrentConnectedTvId(id);
            registry.register("connected-tv", id);
        }
    }

    @When("I get connected TVs for current WiFi network")
    public void iGetConnectedTvsForCurrentWifiNetwork() {
        Response response = tvDeviceApi.getConnectedTvsByWifiNetwork(context.getCurrentWifiNetworkId());
        updateContext(response);
    }

    private void updateContext(Response response) {
        context.setLastStatusCode(response.getStatusCode());
        context.setLastResponseBody(response.getBody().asString());
        context.setLastResponseTimeMs(response.getTime());
        context.setLastResponse(response);
    }
}
