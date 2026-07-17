package com.studyshield.tv.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wifi_networks")
public class WifiNetwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ssid;

    private String bssid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.studyshield.tv.entity.User user;

    @OneToMany(mappedBy = "wifiNetwork", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConnectedTV> connectedTVs = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public WifiNetwork() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSsid() { return ssid; }
    public void setSsid(String ssid) { this.ssid = ssid; }
    public String getBssid() { return bssid; }
    public void setBssid(String bssid) { this.bssid = bssid; }
    public com.studyshield.tv.entity.User getUser() { return user; }
    public void setUser(com.studyshield.tv.entity.User user) { this.user = user; }
    public List<ConnectedTV> getConnectedTVs() { return connectedTVs; }
    public void setConnectedTVs(List<ConnectedTV> connectedTVs) { this.connectedTVs = connectedTVs; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String ssid;
        private String bssid;
        private com.studyshield.tv.entity.User user;

        public Builder ssid(String ssid) { this.ssid = ssid; return this; }
        public Builder bssid(String bssid) { this.bssid = bssid; return this; }
        public Builder user(com.studyshield.tv.entity.User user) { this.user = user; return this; }

        public WifiNetwork build() {
            WifiNetwork wn = new WifiNetwork();
            wn.ssid = this.ssid;
            wn.bssid = this.bssid;
            wn.user = this.user;
            return wn;
        }
    }
}
