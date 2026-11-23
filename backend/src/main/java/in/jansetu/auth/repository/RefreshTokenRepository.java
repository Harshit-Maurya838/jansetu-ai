package in.jansetu.auth.repository;

import in.jansetu.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserIdAndRevokedAtIsNull(UUID userId);

    List<RefreshToken> findByUserIdAndDeviceIdAndRevokedAtIsNull(UUID userId, String deviceId);

    List<RefreshToken> findByFamilyIdAndRevokedAtIsNull(UUID familyId);

    Optional<RefreshToken> findByIdAndUserIdAndRevokedAtIsNull(UUID id, UUID userId);
}
