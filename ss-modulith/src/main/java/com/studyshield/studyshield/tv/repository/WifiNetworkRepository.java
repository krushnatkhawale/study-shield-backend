package com.studyshield.studyshield.tv.repository;

import com.studyshield.studyshield.tv.entity.WifiNetwork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WifiNetworkRepository extends JpaRepository<WifiNetwork, Long> {
    List<WifiNetwork> findByUserId(Long userId);
}
