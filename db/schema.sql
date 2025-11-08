/* ============================================================
   Health Platform consolidated schema for local bootstrap
   (MySQL 8.x, aligned with microservice JPA/Flyway models)
   ============================================================ */
SET NAMES utf8mb4;
SET time_zone = '+08:00';
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS healthdb;
CREATE DATABASE healthdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE healthdb;

/* ----------------------------
   Auth service – users / audit
   ---------------------------- */
CREATE TABLE users (
  user_id    BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  username   VARCHAR(50)  NOT NULL UNIQUE,
  password   VARCHAR(100) NOT NULL,
  name       VARCHAR(50),
  gender     VARCHAR(10),
  age        INT,
  phone      VARCHAR(20),
  email      VARCHAR(100),
  role       VARCHAR(20)  NOT NULL,
  status     VARCHAR(20)  NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE audit_log (
  id          BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  ts          DATETIME DEFAULT CURRENT_TIMESTAMP,
  user_id     BIGINT UNSIGNED,
  action      VARCHAR(100),
  target_type VARCHAR(50),
  target_id   VARCHAR(50),
  details     TEXT,
  ip          VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

/* ----------------------------
   User service – profiles / records
   ---------------------------- */
CREATE TABLE health_profile (
  profile_id        BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id           BIGINT UNSIGNED NOT NULL UNIQUE,
  full_name         VARCHAR(100),
  gender            VARCHAR(20),
  birth_date        DATE,
  blood_type        VARCHAR(10),
  phone             TEXT,
  email             TEXT,
  address           TEXT,
  id_number         TEXT,
  chronic_conditions VARCHAR(1024),
  allergies         VARCHAR(1024),
  medical_history   VARCHAR(1024),
  medications       VARCHAR(1024),
  emergency_contact VARCHAR(100),
  emergency_phone   TEXT,
  lifestyle_notes   VARCHAR(1024),
  last_updated      DATETIME(6),
  CONSTRAINT fk_hp_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE health_record (
  record_id   BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT UNSIGNED NOT NULL,
  record_date DATE NOT NULL,
  type        VARCHAR(50) NOT NULL,
  value       VARCHAR(50) NOT NULL,
  note        VARCHAR(200),
  INDEX idx_hr_user_type_date (user_id, type, record_date),
  CONSTRAINT fk_hr_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

/* ----------------------------
   Doctor service – departments / doctors / tags
   ---------------------------- */
CREATE TABLE department (
  dept_id     INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  dept_name   VARCHAR(100) NOT NULL,
  description TEXT
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE doctor (
  doctor_id     BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id       BIGINT UNSIGNED NOT NULL,
  dept_id       INT UNSIGNED NOT NULL,
  title         VARCHAR(50),
  specialty     VARCHAR(200),
  schedule_info TEXT,
  status        VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  INDEX idx_doctor_dept (dept_id),
  INDEX idx_doctor_status (status),
  CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES users(user_id),
  CONSTRAINT fk_doctor_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE doctor_symptom_tag (
  tag_id    BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT UNSIGNED NOT NULL,
  tag       VARCHAR(100) NOT NULL,
  INDEX idx_tag_doctor (doctor_id),
  INDEX idx_tag_name (tag),
  CONSTRAINT fk_dst_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

/* ----------------------------
   Appointment service – schedules / appointments / inbox / consult / guidance
   ---------------------------- */
CREATE TABLE schedule (
  schedule_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  doctor_id   BIGINT UNSIGNED NOT NULL,
  date        DATE NOT NULL,
  time_slot   VARCHAR(20) NOT NULL,
  capacity    INT NOT NULL,
  status      VARCHAR(20) DEFAULT 'OPEN',
  UNIQUE KEY uniq_doc_date_slot (doctor_id, date, time_slot),
  INDEX idx_schedule_status (status),
  INDEX idx_schedule_date (date),
  CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE appointment (
  appt_id    BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id    BIGINT UNSIGNED NOT NULL,
  doctor_id  BIGINT UNSIGNED NOT NULL,
  appt_date  DATE NOT NULL,
  appt_time  VARCHAR(20) NOT NULL,
  status     VARCHAR(20) NOT NULL,
  symptom    VARCHAR(200),
  UNIQUE KEY uniq_doctor_time (doctor_id, appt_date, appt_time),
  INDEX idx_appt_doc_date (doctor_id, appt_date),
  INDEX idx_appt_user_status (user_id, status, appt_date),
  CONSTRAINT fk_appt_user   FOREIGN KEY (user_id) REFERENCES users(user_id),
  CONSTRAINT fk_appt_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE appointment_status_history (
  id          BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  appt_id     BIGINT UNSIGNED NOT NULL,
  from_status VARCHAR(20),
  to_status   VARCHAR(20) NOT NULL,
  changed_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  remark      VARCHAR(255),
  INDEX idx_ash_appt (appt_id),
  CONSTRAINT fk_ash_appt FOREIGN KEY (appt_id) REFERENCES appointment(appt_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE notify_inbox (
  notify_id  BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id    BIGINT UNSIGNED NOT NULL,
  title      VARCHAR(200) NOT NULL,
  content    TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_flag  TINYINT(1) NOT NULL DEFAULT 0,
  INDEX idx_notify_user_time (user_id, created_at),
  CONSTRAINT fk_notify_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE guidance_plan (
  plan_id    BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id    BIGINT UNSIGNED NOT NULL,
  doctor_id  BIGINT UNSIGNED,
  type       VARCHAR(50),
  rules      TEXT,
  frequency  VARCHAR(20),
  daily_time VARCHAR(10),
  start_at   DATETIME,
  end_at     DATETIME,
  status     VARCHAR(20),
  INDEX idx_guidance_user (user_id),
  CONSTRAINT fk_guidance_user   FOREIGN KEY (user_id) REFERENCES users(user_id),
  CONSTRAINT fk_guidance_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE measurement_reminder (
  reminder_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT UNSIGNED NOT NULL,
  type        VARCHAR(50),
  daily_time  VARCHAR(10),
  enabled     TINYINT(1) DEFAULT 1,
  INDEX idx_measure_user (user_id),
  INDEX idx_measure_time_enabled (daily_time, enabled),
  CONSTRAINT fk_measure_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE consult_session (
  session_id       BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id          BIGINT UNSIGNED NOT NULL,
  doctor_id        BIGINT UNSIGNED NOT NULL,
  status           VARCHAR(20) NOT NULL,
  chief_complaint  TEXT,
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  closed_at        DATETIME,
  INDEX idx_consult_user (user_id),
  INDEX idx_consult_doctor (doctor_id),
  CONSTRAINT fk_consult_user   FOREIGN KEY (user_id) REFERENCES users(user_id),
  CONSTRAINT fk_consult_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE consult_message (
  msg_id       BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  session_id   BIGINT UNSIGNED NOT NULL,
  sender_type  VARCHAR(20) NOT NULL,
  content_type VARCHAR(20) NOT NULL,
  content      TEXT,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `read`       TINYINT(1) NOT NULL DEFAULT 0,
  INDEX idx_consult_msg_session (session_id, created_at),
  CONSTRAINT fk_consult_msg_session FOREIGN KEY (session_id) REFERENCES consult_session(session_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE prescription (
  presc_id       BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  appointment_id BIGINT UNSIGNED,
  doctor_id      BIGINT UNSIGNED NOT NULL,
  user_id        BIGINT UNSIGNED NOT NULL,
  presc_date     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  medicines      LONGTEXT,
  advice         LONGTEXT,
  INDEX idx_prescription_appt (appointment_id),
  CONSTRAINT fk_presc_appt   FOREIGN KEY (appointment_id) REFERENCES appointment(appt_id),
  CONSTRAINT fk_presc_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id),
  CONSTRAINT fk_presc_user   FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

/* ----------------------------
   Content service – articles & audit
   ---------------------------- */
CREATE TABLE article (
  article_id   BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  title        VARCHAR(200) NOT NULL,
  content      TEXT NOT NULL,
  category     VARCHAR(50),
  author_id    BIGINT UNSIGNED,
  publish_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  status       VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  reviewer_id  BIGINT UNSIGNED,
  reviewed_at  DATETIME,
  reject_reason TEXT,
  INDEX idx_article_category_time (category, publish_date),
  INDEX idx_article_status_time   (status, publish_date)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

/* ----------------------------
   Activity service – community events
   ---------------------------- */
CREATE TABLE community_event (
  event_id   BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  title      VARCHAR(200) NOT NULL,
  description TEXT,
  event_date DATETIME NOT NULL,
  location   VARCHAR(100),
  capacity   INT,
  organizer  VARCHAR(100),
  status     VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  INDEX idx_event_date (event_date)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE event_registration (
  reg_id    BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  event_id  BIGINT UNSIGNED NOT NULL,
  user_id   BIGINT UNSIGNED NOT NULL,
  reg_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uniq_event_user (event_id, user_id),
  CONSTRAINT fk_event_reg_event FOREIGN KEY (event_id) REFERENCES community_event(event_id),
  CONSTRAINT fk_event_reg_user  FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
