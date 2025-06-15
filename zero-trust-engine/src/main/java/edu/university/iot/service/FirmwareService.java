package edu.university.iot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.university.iot.repository.DeviceRegistryRepository;

@Service
public class FirmwareService {
  @Autowired
  private DeviceRegistryRepository repo;

  /**
   * returns true if reported version matches expected version in registry
   */
  public boolean isValid(String deviceId, String reportedVersion) {
    return repo.findById(deviceId)
               .map(d -> d.getFirmwareVersion().equals(reportedVersion))
               .orElse(false);
  }
}
