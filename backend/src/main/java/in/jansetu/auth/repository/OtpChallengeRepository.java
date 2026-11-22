package in.jansetu.auth.repository;

import in.jansetu.auth.entity.OtpChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {
}
