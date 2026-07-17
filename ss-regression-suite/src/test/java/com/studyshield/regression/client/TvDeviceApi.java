package com.studyshield.regression.client;

import com.studyshield.regression.context.ScenarioContext;
import io.restassured.response.Response;

public class TvDeviceApi {

    private final GatewayClient client;
    private final ScenarioContext context;

    public TvDeviceApi(GatewayClient client, ScenarioContext context) {
        this.client = client;
        this.context = context;
    }

    public Response createUser(String json) {
        return client.post("/api/v1/tv-users", json);
    }

    public Response createWifiNetwork(String json) {
        return client.post("/api/v1/wifi-networks", json);
    }

    public Response getWifiNetwork(Long id) {
        return client.get("/api/v1/wifi-networks/" + id);
    }

    public Response getWifiNetworksByUser(Long userId) {
        return client.get("/api/v1/wifi-networks/user/" + userId);
    }

    public Response deleteWifiNetwork(Long id) {
        return client.delete("/api/v1/wifi-networks/" + id);
    }

    public Response createConnectedTv(String json) {
        return client.post("/api/v1/connected-tvs", json);
    }

    public Response getConnectedTv(Long id) {
        return client.get("/api/v1/connected-tvs/" + id);
    }

    public Response getConnectedTvsByWifiNetwork(Long wifiNetworkId) {
        return client.get("/api/v1/connected-tvs/wifi-network/" + wifiNetworkId);
    }

    public Response updateConnectedTv(Long id, String json) {
        return client.put("/api/v1/connected-tvs/" + id, json);
    }

    public Response deleteConnectedTv(Long id) {
        return client.delete("/api/v1/connected-tvs/" + id);
    }
}
