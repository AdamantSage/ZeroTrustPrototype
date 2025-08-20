package edu.university.iot.repository;

import edu.university.iot.model.TrustScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrustScoreHistoryRepository extends JpaRepository<TrustScoreHistory, Long> {

    /**
     * Find trust score history for a device after a certain timestamp, ordered by timestamp descending
     */
    List<TrustScoreHistory> findByDeviceIdAndTimestampAfterOrderByTimestampDesc(
        String deviceId, Instant timestamp);

    /**
     * Find trust score history for a device after a certain timestamp, ordered by timestamp ascending
     */
    List<TrustScoreHistory> findByDeviceIdAndTimestampAfterOrderByTimestampAsc(
        String deviceId, Instant timestamp);

    /**
     * Find all trust score history for a device, ordered by timestamp descending
     */
    List<TrustScoreHistory> findByDeviceIdOrderByTimestampDesc(String deviceId);

    /**
     * Find the most recent trust score change for a device
     */
    Optional<TrustScoreHistory> findTopByDeviceIdOrderByTimestampDesc(String deviceId);

    /**
     * Find recent critical/high severity changes across all devices
     */
    @Query("SELECT h FROM TrustScoreHistory h " +
           "WHERE h.timestamp > :cutoff " +
           "AND h.severity IN ('CRITICAL', 'HIGH') " +
           "ORDER BY h.timestamp DESC")
    List<TrustScoreHistory> findRecentCriticalChanges(@Param("cutoff") Instant cutoff);

    /**
     * Find devices that have experienced recent trust score degradation
     */
    @Query("SELECT DISTINCT h.deviceId FROM TrustScoreHistory h " +
           "WHERE h.timestamp > :cutoff " +
           "AND h.scoreChange < -5.0 " +
           "ORDER BY h.deviceId")
    List<String> findDevicesWithRecentDegradation(@Param("cutoff") Instant cutoff);

    /**
     * Get trust score statistics for a device over a time period
     */
    @Query("SELECT " +
           "COUNT(h) as totalChanges, " +
           "SUM(h.scoreChange) as netChange, " +
           "AVG(h.scoreChange) as avgChange, " +
           "MIN(h.newScore) as minScore, " +
           "MAX(h.newScore) as maxScore " +
           "FROM TrustScoreHistory h " +
           "WHERE h.deviceId = :deviceId AND h.timestamp > :cutoff")
    Object[] getTrustScoreStatistics(@Param("deviceId") String deviceId, 
                                   @Param("cutoff") Instant cutoff);

    /**
     * Count trust score changes by severity for a device
     */
    @Query("SELECT h.severity, COUNT(h) FROM TrustScoreHistory h " +
           "WHERE h.deviceId = :deviceId AND h.timestamp > :cutoff " +
           "GROUP BY h.severity")
    List<Object[]> countBySeverityForDevice(@Param("deviceId") String deviceId, 
                                          @Param("cutoff") Instant cutoff);

    /**
     * Find devices with frequent trust score fluctuations
     */
    @Query("SELECT h.deviceId, COUNT(h) as changeCount FROM TrustScoreHistory h " +
           "WHERE h.timestamp > :cutoff " +
           "GROUP BY h.deviceId " +
           "HAVING COUNT(h) >= :threshold " +
           "ORDER BY COUNT(h) DESC")
    List<Object[]> findDevicesWithFrequentChanges(@Param("cutoff") Instant cutoff, 
                                                @Param("threshold") long threshold);

    /**
     * Get trust score changes by factor for analysis
     */
    @Query("SELECT " +
           "SUM(CASE WHEN h.identityPassed = false THEN 1 ELSE 0 END) as identityFailures, " +
           "SUM(CASE WHEN h.contextPassed = false THEN 1 ELSE 0 END) as contextFailures, " +
           "SUM(CASE WHEN h.firmwareValid = false THEN 1 ELSE 0 END) as firmwareFailures, " +
           "SUM(CASE WHEN h.anomalyDetected = true THEN 1 ELSE 0 END) as anomalies, " +
           "SUM(CASE WHEN h.compliancePassed = false THEN 1 ELSE 0 END) as complianceFailures " +
           "FROM TrustScoreHistory h " +
           "WHERE h.deviceId = :deviceId AND h.timestamp > :cutoff")
    Object[] getFactorAnalysis(@Param("deviceId") String deviceId, 
                             @Param("cutoff") Instant cutoff);

    /**
     * Find trust score changes at specific locations (for pattern analysis)
     */
    List<TrustScoreHistory> findByLocationAtChangeAndTimestampAfterOrderByTimestampDesc(
        String location, Instant timestamp);

    /**
     * Get location-based trust score patterns
     */
    @Query("SELECT h.locationAtChange, COUNT(h), AVG(h.scoreChange) FROM TrustScoreHistory h " +
           "WHERE h.deviceId = :deviceId AND h.timestamp > :cutoff " +
           "AND h.locationAtChange IS NOT NULL " +
           "GROUP BY h.locationAtChange " +
           "ORDER BY COUNT(h) DESC")
    List<Object[]> getLocationPatterns(@Param("deviceId") String deviceId, 
                                     @Param("cutoff") Instant cutoff);

    /**
     * Find trust score changes during specific hours (for temporal pattern analysis)
     */
    @Query("SELECT EXTRACT(HOUR FROM h.timestamp) as hour, COUNT(h), AVG(h.scoreChange) " +
           "FROM TrustScoreHistory h " +
           "WHERE h.deviceId = :deviceId AND h.timestamp > :cutoff " +
           "GROUP BY EXTRACT(HOUR FROM h.timestamp) " +
           "ORDER BY COUNT(h) DESC")
    List<Object[]> getTemporalPatterns(@Param("deviceId") String deviceId, 
                                     @Param("cutoff") Instant cutoff);

    /**
     * Clean up old trust score history (for maintenance)
     */
    void deleteByTimestampBefore(Instant cutoff);
}