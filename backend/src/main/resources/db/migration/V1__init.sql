-- JanSetu AI – initial schema (MySQL 8, InnoDB, utf8mb4).
-- Generated alongside the JPA entities in backend/src/main/java/in/jansetu/*/entity; keep them in sync.
-- Conventions: CHAR(36) UUID primary keys (client-generated for offline rows), soft delete, optimistic locking.
-- See docs/data-model.md.

-- Module: docs/modules/15-admin-dashboard-reports.md
CREATE TABLE anganwadi_centres (
    id          CHAR(36)    NOT NULL,
    code VARCHAR(30) NOT NULL COMMENT 'Govt AWC code',
    name VARCHAR(120) NOT NULL,
    village VARCHAR(80) NULL,
    block VARCHAR(80) NULL,
    district VARCHAR(80) NOT NULL,
    state VARCHAR(80) NOT NULL,
    latitude DECIMAL(9,6) NULL,
    longitude DECIMAL(9,6) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_anganwadi_centres_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/15-admin-dashboard-reports.md
CREATE TABLE ngos (
    id          CHAR(36)    NOT NULL,
    name VARCHAR(160) NOT NULL,
    registration_no VARCHAR(60) NULL COMMENT 'TODO(team): confirm which registration id partner NGOs share',
    district VARCHAR(80) NOT NULL,
    state VARCHAR(80) NOT NULL,
    contact_phone VARCHAR(15) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/01-authentication.md
CREATE TABLE users (
    id          CHAR(36)    NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    mobile VARCHAR(15) NOT NULL COMMENT 'E.164, e.g. +919876543210',
    email VARCHAR(160) NULL,
    password_hash VARCHAR(100) NULL COMMENT 'BCrypt; null for OTP-only users',
    role VARCHAR(30) NOT NULL,
    preferred_language VARCHAR(5) NOT NULL COMMENT 'en | hi',
    centre_id CHAR(36) NULL COMMENT 'Anganwadi workers/supervisors',
    ngo_id CHAR(36) NULL COMMENT 'NGO staff',
    active BOOLEAN NOT NULL,
    last_login_at DATETIME(6) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_mobile (mobile),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/01-authentication.md
CREATE TABLE refresh_tokens (
    id          CHAR(36)    NOT NULL,
    user_id CHAR(36) NOT NULL,
    token_hash VARCHAR(64) NOT NULL COMMENT 'SHA-256 of the opaque token',
    device_id VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token_hash (token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/01-authentication.md
CREATE TABLE otp_challenges (
    id          CHAR(36)    NOT NULL,
    mobile VARCHAR(15) NOT NULL,
    otp_hash VARCHAR(100) NOT NULL COMMENT 'never store the OTP itself',
    purpose VARCHAR(30) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    attempts INT NOT NULL,
    consumed_at DATETIME(6) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/02-child-management.md
CREATE TABLE guardians (
    id          CHAR(36)    NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    relation VARCHAR(30) NOT NULL,
    mobile VARCHAR(15) NULL,
    alternate_mobile VARCHAR(15) NULL,
    address VARCHAR(300) NULL,
    user_id CHAR(36) NULL COMMENT 'set when the guardian has an app login',
    consent_given_at DATETIME(6) NULL,
    consent_method VARCHAR(30) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/02-child-management.md
CREATE TABLE children (
    id          CHAR(36)    NOT NULL,
    child_code VARCHAR(24) NOT NULL COMMENT 'human-readable unique ID printed on the card',
    full_name VARCHAR(120) NOT NULL,
    date_of_birth DATE NOT NULL,
    sex VARCHAR(30) NOT NULL,
    centre_id CHAR(36) NOT NULL,
    guardian_id CHAR(36) NOT NULL,
    birth_weight_kg DECIMAL(4,2) NULL,
    blood_group VARCHAR(5) NULL,
    allergies VARCHAR(500) NULL,
    medical_history TEXT NULL,
    photo_url VARCHAR(300) NULL,
    enrolled_on DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_children_child_code (child_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/03-growth-nutrition.md
CREATE TABLE growth_measurements (
    id          CHAR(36)    NOT NULL,
    child_id CHAR(36) NOT NULL,
    measured_on DATE NOT NULL,
    age_days INT NOT NULL COMMENT 'computed from DOB at measuredOn',
    weight_kg DECIMAL(5,2) NULL,
    height_cm DECIMAL(5,1) NULL COMMENT 'length if < 2 years',
    measurement_position VARCHAR(30) NULL,
    muac_cm DECIMAL(4,1) NULL COMMENT 'mid-upper arm circumference, 6-59 months',
    oedema BOOLEAN NOT NULL COMMENT 'bilateral pitting oedema => SAM regardless of z-score',
    waz DECIMAL(5,2) NULL COMMENT 'weight-for-age z',
    haz DECIMAL(5,2) NULL COMMENT 'height-for-age z',
    whz DECIMAL(5,2) NULL COMMENT 'weight-for-height z',
    nutrition_status VARCHAR(30) NOT NULL,
    measured_by CHAR(36) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/03-growth-nutrition.md
CREATE TABLE referrals (
    id          CHAR(36)    NOT NULL,
    child_id CHAR(36) NOT NULL,
    reason VARCHAR(30) NOT NULL,
    referred_to VARCHAR(160) NOT NULL COMMENT 'e.g. NRC, PHC, CHC',
    referred_on DATE NOT NULL,
    follow_up_on DATE NULL,
    status VARCHAR(30) NOT NULL,
    outcome VARCHAR(500) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/04-vaccination-healthcare.md
CREATE TABLE vaccine_schedule (
    id          CHAR(36)    NOT NULL,
    vaccine_code VARCHAR(20) NOT NULL,
    name VARCHAR(80) NOT NULL,
    dose_label VARCHAR(30) NOT NULL,
    due_age_days INT NOT NULL,
    max_age_days INT NULL COMMENT 'last age at which the dose may still be given',
    sequence_no INT NOT NULL,
    active BOOLEAN NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vaccine_schedule_vaccine_code_dose_label (vaccine_code, dose_label)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/04-vaccination-healthcare.md
CREATE TABLE vaccination_records (
    id          CHAR(36)    NOT NULL,
    child_id CHAR(36) NOT NULL,
    schedule_item_id CHAR(36) NOT NULL,
    due_on DATE NOT NULL,
    given_on DATE NULL,
    status VARCHAR(30) NOT NULL,
    batch_no VARCHAR(40) NULL,
    facility VARCHAR(160) NULL,
    notes VARCHAR(500) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vaccination_records_child_id_schedule_item_id (child_id, schedule_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/04-vaccination-healthcare.md
CREATE TABLE checkup_records (
    id          CHAR(36)    NOT NULL,
    child_id CHAR(36) NOT NULL,
    checkup_on DATE NOT NULL,
    doctor_name VARCHAR(120) NULL,
    facility VARCHAR(160) NULL,
    findings TEXT NULL,
    prescriptions TEXT NULL,
    next_visit_on DATE NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/05-attendance.md
CREATE TABLE attendance_records (
    id          CHAR(36)    NOT NULL,
    child_id CHAR(36) NOT NULL,
    centre_id CHAR(36) NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    absence_reason VARCHAR(30) NULL,
    marked_by CHAR(36) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_attendance_records_child_id_attendance_date (child_id, attendance_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/06-qr-health-card.md
CREATE TABLE health_cards (
    id          CHAR(36)    NOT NULL,
    subject_type VARCHAR(30) NOT NULL,
    subject_id CHAR(36) NOT NULL COMMENT 'children.id or elderly_beneficiaries.id',
    token_id VARCHAR(64) NOT NULL COMMENT 'random id inside the signed QR token',
    issued_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL COMMENT 'set on reissue / loss',
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_health_cards_token_id (token_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/07-midday-meal-quality.md
CREATE TABLE suppliers (
    id          CHAR(36)    NOT NULL,
    name VARCHAR(160) NOT NULL,
    contact_phone VARCHAR(15) NULL,
    fssai_licence_no VARCHAR(20) NULL,
    district VARCHAR(80) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/07-midday-meal-quality.md
CREATE TABLE meal_records (
    id          CHAR(36)    NOT NULL,
    centre_id CHAR(36) NOT NULL,
    meal_date DATE NOT NULL,
    meal_type VARCHAR(30) NOT NULL,
    menu_items VARCHAR(300) NOT NULL,
    children_served INT NOT NULL,
    quantity_kg DECIMAL(6,2) NULL,
    serving_temp_celsius DECIMAL(4,1) NULL,
    freshness_rating SMALLINT NOT NULL COMMENT '1-5',
    hygiene_rating SMALLINT NOT NULL COMMENT '1-5',
    child_feedback_score SMALLINT NULL COMMENT '1-3 smiley scale',
    photo_url VARCHAR(300) NULL,
    supplier_id CHAR(36) NULL,
    ai_quality_label VARCHAR(30) NULL COMMENT 'AI suggestion only',
    ai_quality_score DECIMAL(4,3) NULL,
    review_status VARCHAR(30) NOT NULL,
    recorded_by CHAR(36) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_meal_records_centre_id_meal_date_meal_type (centre_id, meal_date, meal_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/07-midday-meal-quality.md
CREATE TABLE kitchen_checklists (
    id          CHAR(36)    NOT NULL,
    meal_record_id CHAR(36) NOT NULL,
    handwash_available BOOLEAN NOT NULL,
    utensils_clean BOOLEAN NOT NULL,
    drinking_water_safe BOOLEAN NOT NULL,
    food_covered BOOLEAN NOT NULL,
    cook_head_covered BOOLEAN NOT NULL,
    storage_clean BOOLEAN NOT NULL,
    pest_free BOOLEAN NOT NULL,
    remarks VARCHAR(300) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_kitchen_checklists_meal_record_id (meal_record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/08-elderly-registration.md
CREATE TABLE elderly_beneficiaries (
    id          CHAR(36)    NOT NULL,
    beneficiary_code VARCHAR(24) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    date_of_birth DATE NULL COMMENT 'often unknown',
    approx_age_years SMALLINT NULL COMMENT 'used when DOB unknown',
    mobile VARCHAR(15) NULL COMMENT 'many beneficiaries have no phone',
    address VARCHAR(300) NOT NULL,
    village VARCHAR(80) NULL,
    district VARCHAR(80) NOT NULL,
    marital_status VARCHAR(30) NULL,
    living_arrangement VARCHAR(30) NOT NULL,
    medical_history TEXT NULL,
    disabilities VARCHAR(300) NULL,
    welfare_needs VARCHAR(300) NULL,
    ngo_id CHAR(36) NOT NULL,
    assigned_worker_id CHAR(36) NULL,
    consent_given_at DATETIME(6) NOT NULL,
    location_consent BOOLEAN NOT NULL COMMENT 'required before SOS shares GPS',
    status VARCHAR(30) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_elderly_beneficiaries_beneficiary_code (beneficiary_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/08-elderly-registration.md
CREATE TABLE emergency_contacts (
    id          CHAR(36)    NOT NULL,
    beneficiary_id CHAR(36) NOT NULL,
    name VARCHAR(120) NOT NULL,
    relation VARCHAR(40) NOT NULL,
    mobile VARCHAR(15) NOT NULL,
    priority SMALLINT NOT NULL COMMENT '1 = call first',
    notify_on_sos BOOLEAN NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/09-elderly-healthcare.md
CREATE TABLE vital_readings (
    id          CHAR(36)    NOT NULL,
    beneficiary_id CHAR(36) NOT NULL,
    recorded_at DATETIME(6) NOT NULL,
    systolic SMALLINT NULL COMMENT 'mmHg',
    diastolic SMALLINT NULL COMMENT 'mmHg',
    pulse SMALLINT NULL COMMENT 'bpm',
    blood_glucose_mg_dl SMALLINT NULL,
    glucose_context VARCHAR(30) NULL,
    weight_kg DECIMAL(5,2) NULL,
    recorded_by CHAR(36) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/09-elderly-healthcare.md
CREATE TABLE medications (
    id          CHAR(36)    NOT NULL,
    beneficiary_id CHAR(36) NOT NULL,
    name VARCHAR(120) NOT NULL,
    dosage VARCHAR(60) NOT NULL COMMENT 'e.g. 1 tablet 5 mg',
    times_of_day VARCHAR(60) NOT NULL COMMENT 'HH:mm list, e.g. 08:00,20:00',
    start_date DATE NOT NULL,
    end_date DATE NULL COMMENT 'null = ongoing',
    prescribed_by VARCHAR(120) NULL,
    reminders_enabled BOOLEAN NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/09-elderly-healthcare.md
CREATE TABLE appointments (
    id          CHAR(36)    NOT NULL,
    beneficiary_id CHAR(36) NOT NULL,
    scheduled_at DATETIME(6) NOT NULL,
    doctor_name VARCHAR(120) NULL,
    facility VARCHAR(160) NOT NULL,
    purpose VARCHAR(200) NOT NULL,
    status VARCHAR(30) NOT NULL,
    outcome_notes TEXT NULL,
    follow_up_on DATE NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/10-emergency-sos.md
CREATE TABLE sos_incidents (
    id          CHAR(36)    NOT NULL,
    beneficiary_id CHAR(36) NOT NULL,
    triggered_at DATETIME(6) NOT NULL COMMENT 'device time',
    channel VARCHAR(30) NOT NULL,
    latitude DECIMAL(9,6) NULL COMMENT 'only if locationConsent',
    longitude DECIMAL(9,6) NULL,
    location_accuracy_meters INT NULL,
    status VARCHAR(30) NOT NULL,
    acknowledged_by CHAR(36) NULL,
    acknowledged_at DATETIME(6) NULL,
    resolved_at DATETIME(6) NULL,
    resolution_notes VARCHAR(500) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/10-emergency-sos.md
CREATE TABLE sos_events (
    id          CHAR(36)    NOT NULL,
    incident_id CHAR(36) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    actor_user_id CHAR(36) NULL,
    note VARCHAR(500) NULL,
    occurred_at DATETIME(6) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/11-welfare-ngo-assistance.md
CREATE TABLE welfare_records (
    id          CHAR(36)    NOT NULL,
    beneficiary_id CHAR(36) NOT NULL,
    category VARCHAR(30) NOT NULL,
    description VARCHAR(300) NOT NULL,
    quantity VARCHAR(60) NULL COMMENT 'e.g. 5 kg rice',
    amount_inr DECIMAL(10,2) NULL,
    provided_on DATE NOT NULL,
    provided_by CHAR(36) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/11-welfare-ngo-assistance.md
CREATE TABLE scheme_applications (
    id          CHAR(36)    NOT NULL,
    beneficiary_id CHAR(36) NOT NULL,
    scheme_name VARCHAR(160) NOT NULL,
    application_ref VARCHAR(60) NULL COMMENT 'reference from the govt portal',
    applied_on DATE NULL,
    status VARCHAR(30) NOT NULL,
    last_status_on DATE NULL,
    notes VARCHAR(500) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/12-ngo-worker-caregiver.md
CREATE TABLE caregiver_assignments (
    id          CHAR(36)    NOT NULL,
    beneficiary_id CHAR(36) NOT NULL,
    worker_id CHAR(36) NOT NULL,
    assigned_from DATE NOT NULL,
    assigned_to DATE NULL COMMENT 'null = current',
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/12-ngo-worker-caregiver.md
CREATE TABLE visits (
    id          CHAR(36)    NOT NULL,
    worker_id CHAR(36) NOT NULL,
    beneficiary_id CHAR(36) NOT NULL,
    scheduled_for DATE NOT NULL,
    visited_at DATETIME(6) NULL,
    status VARCHAR(30) NOT NULL,
    purpose VARCHAR(200) NOT NULL,
    notes VARCHAR(1000) NULL,
    follow_up_on DATE NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/13-ai-analytics.md
CREATE TABLE risk_alerts (
    id          CHAR(36)    NOT NULL,
    subject_type VARCHAR(30) NOT NULL,
    subject_id CHAR(36) NOT NULL,
    alert_type VARCHAR(30) NOT NULL,
    risk_score DECIMAL(4,3) NOT NULL COMMENT '0-1',
    risk_level VARCHAR(30) NOT NULL,
    model_name VARCHAR(60) NOT NULL,
    model_version VARCHAR(20) NOT NULL,
    explanation VARCHAR(500) NULL COMMENT 'top contributing features, plain language',
    review_status VARCHAR(30) NOT NULL,
    reviewed_by CHAR(36) NULL,
    reviewed_at DATETIME(6) NULL,
    review_note VARCHAR(500) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/14-notifications.md
CREATE TABLE notifications (
    id          CHAR(36)    NOT NULL,
    recipient_user_id CHAR(36) NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(120) NOT NULL,
    body VARCHAR(500) NOT NULL COMMENT 'no health values in push text',
    ref_type VARCHAR(40) NULL,
    ref_id CHAR(36) NULL,
    channel VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    scheduled_for DATETIME(6) NOT NULL,
    sent_at DATETIME(6) NULL,
    read_at DATETIME(6) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/14-notifications.md
CREATE TABLE device_tokens (
    id          CHAR(36)    NOT NULL,
    user_id CHAR(36) NOT NULL,
    fcm_token VARCHAR(255) NOT NULL,
    platform VARCHAR(10) NOT NULL COMMENT 'android | ios',
    last_seen_at DATETIME(6) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_tokens_fcm_token (fcm_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Module: docs/modules/16-offline-sync.md
CREATE TABLE sync_operations (
    id          CHAR(36)    NOT NULL,
    op_id CHAR(36) NOT NULL COMMENT 'client-generated; makes push idempotent',
    device_id VARCHAR(64) NOT NULL,
    user_id CHAR(36) NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    entity_id CHAR(36) NOT NULL,
    operation VARCHAR(30) NOT NULL,
    client_updated_at DATETIME(6) NOT NULL,
    received_at DATETIME(6) NOT NULL,
    result VARCHAR(30) NOT NULL,
    detail VARCHAR(500) NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sync_operations_op_id (op_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cross-cutting: docs/privacy-and-security.md#audit-logging
CREATE TABLE audit_logs (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    actor_user_id CHAR(36)     NULL,
    action        VARCHAR(40)  NOT NULL,
    entity_type   VARCHAR(40)  NOT NULL,
    entity_id     CHAR(36)     NULL,
    device_id     VARCHAR(64)  NULL,
    details       VARCHAR(500) NULL,
    occurred_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY ix_audit_logs_entity (entity_type, entity_id),
    KEY ix_audit_logs_actor (actor_user_id, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Foreign keys (added after all tables exist)
ALTER TABLE users ADD CONSTRAINT fk_users_centre_id FOREIGN KEY (centre_id) REFERENCES anganwadi_centres (id);
ALTER TABLE users ADD CONSTRAINT fk_users_ngo_id FOREIGN KEY (ngo_id) REFERENCES ngos (id);
ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE guardians ADD CONSTRAINT fk_guardians_user_id FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE children ADD CONSTRAINT fk_children_centre_id FOREIGN KEY (centre_id) REFERENCES anganwadi_centres (id);
ALTER TABLE children ADD CONSTRAINT fk_children_guardian_id FOREIGN KEY (guardian_id) REFERENCES guardians (id);
ALTER TABLE growth_measurements ADD CONSTRAINT fk_growth_measurements_child_id FOREIGN KEY (child_id) REFERENCES children (id);
ALTER TABLE growth_measurements ADD CONSTRAINT fk_growth_measurements_measured_by FOREIGN KEY (measured_by) REFERENCES users (id);
ALTER TABLE referrals ADD CONSTRAINT fk_referrals_child_id FOREIGN KEY (child_id) REFERENCES children (id);
ALTER TABLE vaccination_records ADD CONSTRAINT fk_vaccination_records_child_id FOREIGN KEY (child_id) REFERENCES children (id);
ALTER TABLE vaccination_records ADD CONSTRAINT fk_vaccination_records_schedule_item_id FOREIGN KEY (schedule_item_id) REFERENCES vaccine_schedule (id);
ALTER TABLE checkup_records ADD CONSTRAINT fk_checkup_records_child_id FOREIGN KEY (child_id) REFERENCES children (id);
ALTER TABLE attendance_records ADD CONSTRAINT fk_attendance_records_child_id FOREIGN KEY (child_id) REFERENCES children (id);
ALTER TABLE attendance_records ADD CONSTRAINT fk_attendance_records_centre_id FOREIGN KEY (centre_id) REFERENCES anganwadi_centres (id);
ALTER TABLE attendance_records ADD CONSTRAINT fk_attendance_records_marked_by FOREIGN KEY (marked_by) REFERENCES users (id);
ALTER TABLE meal_records ADD CONSTRAINT fk_meal_records_centre_id FOREIGN KEY (centre_id) REFERENCES anganwadi_centres (id);
ALTER TABLE meal_records ADD CONSTRAINT fk_meal_records_supplier_id FOREIGN KEY (supplier_id) REFERENCES suppliers (id);
ALTER TABLE meal_records ADD CONSTRAINT fk_meal_records_recorded_by FOREIGN KEY (recorded_by) REFERENCES users (id);
ALTER TABLE kitchen_checklists ADD CONSTRAINT fk_kitchen_checklists_meal_record_id FOREIGN KEY (meal_record_id) REFERENCES meal_records (id);
ALTER TABLE elderly_beneficiaries ADD CONSTRAINT fk_elderly_beneficiaries_ngo_id FOREIGN KEY (ngo_id) REFERENCES ngos (id);
ALTER TABLE elderly_beneficiaries ADD CONSTRAINT fk_elderly_beneficiaries_assigned_worker_id FOREIGN KEY (assigned_worker_id) REFERENCES users (id);
ALTER TABLE emergency_contacts ADD CONSTRAINT fk_emergency_contacts_beneficiary_id FOREIGN KEY (beneficiary_id) REFERENCES elderly_beneficiaries (id);
ALTER TABLE vital_readings ADD CONSTRAINT fk_vital_readings_beneficiary_id FOREIGN KEY (beneficiary_id) REFERENCES elderly_beneficiaries (id);
ALTER TABLE vital_readings ADD CONSTRAINT fk_vital_readings_recorded_by FOREIGN KEY (recorded_by) REFERENCES users (id);
ALTER TABLE medications ADD CONSTRAINT fk_medications_beneficiary_id FOREIGN KEY (beneficiary_id) REFERENCES elderly_beneficiaries (id);
ALTER TABLE appointments ADD CONSTRAINT fk_appointments_beneficiary_id FOREIGN KEY (beneficiary_id) REFERENCES elderly_beneficiaries (id);
ALTER TABLE sos_incidents ADD CONSTRAINT fk_sos_incidents_beneficiary_id FOREIGN KEY (beneficiary_id) REFERENCES elderly_beneficiaries (id);
ALTER TABLE sos_incidents ADD CONSTRAINT fk_sos_incidents_acknowledged_by FOREIGN KEY (acknowledged_by) REFERENCES users (id);
ALTER TABLE sos_events ADD CONSTRAINT fk_sos_events_incident_id FOREIGN KEY (incident_id) REFERENCES sos_incidents (id);
ALTER TABLE sos_events ADD CONSTRAINT fk_sos_events_actor_user_id FOREIGN KEY (actor_user_id) REFERENCES users (id);
ALTER TABLE welfare_records ADD CONSTRAINT fk_welfare_records_beneficiary_id FOREIGN KEY (beneficiary_id) REFERENCES elderly_beneficiaries (id);
ALTER TABLE welfare_records ADD CONSTRAINT fk_welfare_records_provided_by FOREIGN KEY (provided_by) REFERENCES users (id);
ALTER TABLE scheme_applications ADD CONSTRAINT fk_scheme_applications_beneficiary_id FOREIGN KEY (beneficiary_id) REFERENCES elderly_beneficiaries (id);
ALTER TABLE caregiver_assignments ADD CONSTRAINT fk_caregiver_assignments_beneficiary_id FOREIGN KEY (beneficiary_id) REFERENCES elderly_beneficiaries (id);
ALTER TABLE caregiver_assignments ADD CONSTRAINT fk_caregiver_assignments_worker_id FOREIGN KEY (worker_id) REFERENCES users (id);
ALTER TABLE visits ADD CONSTRAINT fk_visits_worker_id FOREIGN KEY (worker_id) REFERENCES users (id);
ALTER TABLE visits ADD CONSTRAINT fk_visits_beneficiary_id FOREIGN KEY (beneficiary_id) REFERENCES elderly_beneficiaries (id);
ALTER TABLE risk_alerts ADD CONSTRAINT fk_risk_alerts_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users (id);
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_recipient_user_id FOREIGN KEY (recipient_user_id) REFERENCES users (id);
ALTER TABLE device_tokens ADD CONSTRAINT fk_device_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE sync_operations ADD CONSTRAINT fk_sync_operations_user_id FOREIGN KEY (user_id) REFERENCES users (id);

-- Hot query paths
CREATE INDEX ix_children_centre ON children (centre_id, status);
CREATE INDEX ix_children_dedupe ON children (full_name, date_of_birth);
CREATE INDEX ix_growth_child_date ON growth_measurements (child_id, measured_on);
CREATE INDEX ix_vacc_due ON vaccination_records (status, due_on);
CREATE INDEX ix_attendance_centre_date ON attendance_records (centre_id, attendance_date);
CREATE INDEX ix_meal_centre_date ON meal_records (centre_id, meal_date);
CREATE INDEX ix_vitals_beneficiary ON vital_readings (beneficiary_id, recorded_at);
CREATE INDEX ix_sos_status ON sos_incidents (status, triggered_at);
CREATE INDEX ix_visits_worker_date ON visits (worker_id, scheduled_for);
CREATE INDEX ix_risk_alerts_review ON risk_alerts (review_status, risk_level);
CREATE INDEX ix_notifications_recipient ON notifications (recipient_user_id, status);
CREATE INDEX ix_health_cards_subject ON health_cards (subject_type, subject_id);
