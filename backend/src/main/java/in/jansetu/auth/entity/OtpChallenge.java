package in.jansetu.auth.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** Table `otp_challenges`. Spec: docs/modules/01-authentication.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "otp_challenges")
public class OtpChallenge extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String target;  // E.164 mobile or lower-cased email

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private OtpChannel channel;

    @Column(length = 36)
    private UUID userId;  // FK -> users.id; null for REGISTER

    @Column(nullable = false, length = 100)
    private String otpHash;  // never store the OTP itself

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private OtpPurpose purpose;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Integer attempts;

    @Column
    private Instant consumedAt;
}
