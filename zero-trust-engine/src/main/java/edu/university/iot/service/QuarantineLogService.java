// src/main/java/edu/university/iot/service/QuarantineLogService.java
package edu.university.iot.service;
import edu.university.iot.model.dtoModel.QuarantineLogDto;
import edu.university.iot.repository.QuarantineLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuarantineLogService {
    private final QuarantineLogRepository repo;

    public QuarantineLogService(QuarantineLogRepository repo) {
        this.repo = repo;
    }

    public List<QuarantineLogDto> getLogs(String deviceId) {
        return repo.findAllByOrderByTimestampDesc().stream()
            .filter(log -> log.getDeviceId().equals(deviceId))
            .map(log -> new QuarantineLogDto(
                log.getDeviceId(),
                log.getReason(),
                log.getStatus().name(),
                log.getTimestamp(),
                log.getErrorMessage()
            ))
            .collect(Collectors.toList());
    }
}
