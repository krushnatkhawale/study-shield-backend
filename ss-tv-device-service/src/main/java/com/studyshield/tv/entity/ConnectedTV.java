package com.studyshield.tv.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "connected_tvs")
public class ConnectedTV {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceName;

    @Column(nullable = false, unique = true)
    private String macAddress;

    private String ipAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wifi_network_id", nullable = false)
    private WifiNetwork wifiNetwork;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public ConnectedTV() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public WifiNetwork getWifiNetwork() { return wifiNetwork; }
    public void setWifiNetwork(WifiNetwork wifiNetwork) { this.wifiNetwork = wifiNetwork; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String deviceName;
        private String macAddress;
        private String ipAddress;
        private WifiNetwork wifiNetwork;
        private boolean active = true;

        public Builder deviceName(String deviceName) { this.deviceName = deviceName; return this; }
        public Builder macAddress(String macAddress) { this.macAddress = macAddress; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder wifiNetwork(WifiNetwork wifiNetwork) { this.wifiNetwork = wifiNetwork; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public ConnectedTV build() {
            ConnectedTV tv = new ConnectedTV();
            tv.deviceName = this.deviceName;
            tv.macAddress = this.macAddress;
            tv.ipAddress = this.ipAddress;
            tv.wifiNetwork = this.wifiNetwork;
            tv.active = this.active;
            return tv;
        }
    }
}
