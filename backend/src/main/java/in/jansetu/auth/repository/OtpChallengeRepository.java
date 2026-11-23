package in.jansetu.auth.repository;

import in.jansetu.auth.entity.OtpChallenge;
import in.jansetu.auth.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {
    /** Only the newest challenge for a target+purpose is ever valid. */
    Optional<OtpChallenge> findFirstByTargetAndPurposeOrderByCreatedAtDesc(String target, OtpPurpose purpose);

    boolean existsByTargetAndCreatedAtAfter(String target, Instant after);

    long countByTargetAndCreatedAtAfter(String target, Instant after);

    @Modifying
    @Query("delete from OtpChallenge c where c.createdAt < :before")
    int deleteCreatedBefore(Instant before);
}
