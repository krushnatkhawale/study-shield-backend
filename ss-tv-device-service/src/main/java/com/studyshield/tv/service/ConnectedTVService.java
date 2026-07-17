package com.studyshield.tv.service;

import com.studyshield.tv.dto.ConnectedTVRequest;
import com.studyshield.tv.dto.ConnectedTVResponse;
import com.studyshield.tv.entity.ConnectedTV;
import com.studyshield.tv.entity.WifiNetwork;
import com.studyshield.tv.exception.ResourceNotFoundException;
import com.studyshield.tv.repository.ConnectedTVRepository;
import com.studyshield.tv.repository.WifiNetworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ConnectedTVService {

    private final ConnectedTVRepository connectedTVRepository;
    private final WifiNetworkRepository wifiNetworkRepository;

    public ConnectedTVService(ConnectedTVRepository connectedTVRepository, WifiNetworkRepository wifiNetworkRepository) {
        this.connectedTVRepository = connectedTVRepository;
        this.wifiNetworkRepository = wifiNetworkRepository;
    }

    public ConnectedTVResponse create(ConnectedTVRequest request) {
        WifiNetwork wn = wifiNetworkRepository.findById(request.wifiNetworkId())
                .orElseThrow(() -> new ResourceNotFoundException("WifiNetwork", request.wifiNetworkId()));
        ConnectedTV tv = ConnectedTV.builder()
                .deviceName(request.deviceName())
                .macAddress(request.macAddress())
                .ipAddress(request.ipAddress())
                .wifiNetwork(wn)
                .active(request.active())
                .build();
        return mapToResponse(connectedTVRepository.save(tv));
    }

    @Transactional(readOnly = true)
    public ConnectedTVResponse getById(Long id) {
        return mapToResponse(connectedTVRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ConnectedTV", id)));
    }

    @Transactional(readOnly = true)
    public List<ConnectedTVResponse> getByWifiNetworkId(Long wifiNetworkId) {
        return connectedTVRepository.findByWifiNetworkId(wifiNetworkId).stream().map(this::mapToResponse).toList();
    }

    public ConnectedTVResponse update(Long id, ConnectedTVRequest request) {
        ConnectedTV tv = connectedTVRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ConnectedTV", id));
        WifiNetwork wn = wifiNetworkRepository.findById(request.wifiNetworkId())
                .orElseThrow(() -> new ResourceNotFoundException("WifiNetwork", request.wifiNetworkId()));
        tv.setDeviceName(request.deviceName());
        tv.setMacAddress(request.macAddress());
        tv.setIpAddress(request.ipAddress());
        tv.setWifiNetwork(wn);
        tv.setActive(request.active());
        return mapToResponse(connectedTVRepository.save(tv));
    }

    public void delete(Long id) {
        ConnectedTV tv = connectedTVRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ConnectedTV", id));
        connectedTVRepository.delete(tv);
    }

    private ConnectedTVResponse mapToResponse(ConnectedTV tv) {
        return new ConnectedTVResponse(tv.getId(), tv.getDeviceName(), tv.getMacAddress(),
                tv.getIpAddress(), tv.getWifiNetwork().getId(), tv.isActive(),
                tv.getCreatedAt(), tv.getUpdatedAt());
    }
}
