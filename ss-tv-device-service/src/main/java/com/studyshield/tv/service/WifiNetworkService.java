package com.studyshield.tv.service;

import com.studyshield.tv.dto.WifiNetworkRequest;
import com.studyshield.tv.dto.WifiNetworkResponse;
import com.studyshield.tv.entity.User;
import com.studyshield.tv.entity.WifiNetwork;
import com.studyshield.tv.exception.ResourceNotFoundException;
import com.studyshield.tv.repository.UserRepository;
import com.studyshield.tv.repository.WifiNetworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WifiNetworkService {

    private final WifiNetworkRepository wifiNetworkRepository;
    private final UserRepository userRepository;

    public WifiNetworkService(WifiNetworkRepository wifiNetworkRepository, UserRepository userRepository) {
        this.wifiNetworkRepository = wifiNetworkRepository;
        this.userRepository = userRepository;
    }

    public WifiNetworkResponse create(WifiNetworkRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));
        WifiNetwork wn = WifiNetwork.builder()
                .ssid(request.ssid())
                .bssid(request.bssid())
                .user(user)
                .build();
        return mapToResponse(wifiNetworkRepository.save(wn));
    }

    @Transactional(readOnly = true)
    public WifiNetworkResponse getById(Long id) {
        return mapToResponse(wifiNetworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WifiNetwork", id)));
    }

    @Transactional(readOnly = true)
    public List<WifiNetworkResponse> getByUserId(Long userId) {
        return wifiNetworkRepository.findByUserId(userId).stream().map(this::mapToResponse).toList();
    }

    public void delete(Long id) {
        WifiNetwork wn = wifiNetworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WifiNetwork", id));
        wifiNetworkRepository.delete(wn);
    }

    private WifiNetworkResponse mapToResponse(WifiNetwork wn) {
        return new WifiNetworkResponse(wn.getId(), wn.getSsid(), wn.getBssid(),
                wn.getUser().getId(), wn.getCreatedAt(), wn.getUpdatedAt());
    }
}
