package edu.university.iot.service;

import edu.university.iot.model.DeviceSession;
import edu.university.iot.repository.DeviceSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionManagementService {

    private final DeviceSessionRepository sessionRepo;

    public SessionManagementService(DeviceSessionRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    /**
     * Starts or refreshes a session, returning its ID.
     */
    public String startOrRefreshSession(String deviceId) {
        DeviceSession session = sessionRepo.findByDeviceId(deviceId)
            .orElseGet(() -> createNewSession(deviceId));

        session.setLastActivityTime(Instant.now());
        session.setStatus("ACTIVE");
        sessionRepo.save(session);
        return session.getSessionId();
    }

    private DeviceSession createNewSession(String deviceId) {
        DeviceSession session = new DeviceSession();
        session.setDeviceId(deviceId);
        session.setSessionId(UUID.randomUUID().toString());
        session.setStartTime(Instant.now());
        session.setLastActivityTime(Instant.now());
        session.setStatus("ACTIVE");
        return sessionRepo.save(session);
    }

    /**
     * Pauses an existing session.
     */
    public void pauseSession(String sessionId, String reason) {
        sessionRepo.findBySessionId(sessionId).ifPresent(s -> {
            s.setStatus("PAUSED");
            s.setLastActivityTime(Instant.now());
            sessionRepo.save(s);
        });
    }

    /**
     * Terminates a session.
     */
    public void terminateSession(String sessionId, String reason) {
        sessionRepo.findBySessionId(sessionId).ifPresent(s -> {
            s.setStatus("TERMINATED");
            s.setLastActivityTime(Instant.now());
            sessionRepo.save(s);
        });
    }

    public Optional<DeviceSession> getSessionByDevice(String deviceId) {
    return sessionRepo.findByDeviceId(deviceId);
}
}
