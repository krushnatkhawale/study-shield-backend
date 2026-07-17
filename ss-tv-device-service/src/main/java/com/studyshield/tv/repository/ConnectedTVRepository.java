package com.studyshield.tv.repository;

import com.studyshield.tv.entity.ConnectedTV;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectedTVRepository extends JpaRepository<ConnectedTV, Long> {
    List<ConnectedTV> findByWifiNetworkId(Long wifiNetworkId);
    Optional<ConnectedTV> findByMacAddress(String macAddress);
}
