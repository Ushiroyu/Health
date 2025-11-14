/*
  Consolidated bootstrap for docker-compose / cloud SQL
  Source: merged schema + data
  Usage: mysql -uroot -p < health-platform-backend/database/init.sql
*/
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
   Auth service 鈥?users / audit
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
   User service 鈥?profiles / records
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
   Doctor service 鈥?departments / doctors / tags
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
   Appointment service 鈥?schedules / appointments / inbox / consult / guidance
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
   Content service 鈥?articles & audit
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
   Activity service 鈥?community events
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

USE healthdb;

/* =======================
   1) 鍩虹绉嶅瓙锛氱敤鎴?閮ㄩ棬/鍖荤敓/鏍囩
   ======================= */
INSERT INTO users (user_id, username, password, name, gender, age, phone, email, role, status, created_at)
VALUES
    (1,'admin','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','绯荤粺绠＄悊鍛?,'MALE',30,'13000000001','admin@demo.local','ADMIN','ENABLED',NOW()),
    (2,'u_zhangsan','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','寮犱笁','MALE',35,'13100000001','zhangsan@demo.local','RESIDENT','ENABLED',NOW()),
    (3,'u_lisi','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','鏉庡洓','MALE',40,'13100000002','lisi@demo.local','RESIDENT','ENABLED',NOW()),
    (4,'u_wangwu','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','鐜嬩簲','MALE',27,'13100000003','wangwu@demo.local','RESIDENT','ENABLED',NOW()),
    (5,'u_zhouqi','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','鍛ㄧ惁','FEMALE',24,'13100000004','zhouqi@demo.local','RESIDENT','ENABLED',NOW()),
    (6,'d_chenfm','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','闄堝嚒','MALE',38,'13200000001','drchen@demo.local','DOCTOR','ENABLED',NOW()),
    (7,'d_lizx','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','鏉庡摬鏂?,'MALE',42,'13200000002','drlizx@demo.local','DOCTOR','ENABLED',NOW()),
    (8,'d_wangxl','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','鐜嬫檽纾?,'MALE',36,'13200000003','drwang@demo.local','DOCTOR','ENABLED',NOW()),
    (9,'d_zhangtt','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','寮犲┓濠?,'FEMALE',34,'13200000004','drzhang@demo.local','DOCTOR','ENABLED',NOW());

INSERT INTO department (dept_id, dept_name, description) VALUES
                                                             (1,'鍏ㄧ','甯歌鐥呫€佸鍙戠梾棣栬瘖涓庡垎璇?),
                                                             (2,'鍐呯','鍛煎惛/娑堝寲/鍐呭垎娉岀瓑'),
                                                             (3,'蹇冨唴绉?,'蹇冭绠＄柧鐥?),
                                                             (4,'鍎跨','鍎跨闈掑皯骞村仴搴?),
                                                             (5,'鑰抽蓟鍠夌','榧荤値/鍜界値/涓€崇値');

INSERT INTO doctor (doctor_id, user_id, dept_id, title, specialty, schedule_info, status) VALUES
                                                                                              (1,6,1,'涓绘不鍖诲笀','鍏ㄧ,涓夐珮绠＄悊','{\"days\":[\"Mon\"],\"start\":\"09:00\",\"end\":\"11:00\",\"slot\":15}','ENABLED'),
                                                                                              (2,7,3,'鍓富浠诲尰甯?,'蹇冨緥澶卞父,楂樿鍘?,'{\"days\":[\"Tue\"],\"start\":\"09:00\",\"end\":\"11:00\",\"slot\":15}','ENABLED'),
                                                                                              (3,8,2,'涓绘不鍖诲笀','鍛煎惛,鍐呭垎娉?,'{\"days\":[\"Wed\"],\"start\":\"09:00\",\"end\":\"11:00\",\"slot\":15}','ENABLED'),
                                                                                              (4,9,4,'涓绘不鍖诲笀','鍎夸繚,鎰熷啋鍙戠儹','{\"days\":[\"Thu\"],\"start\":\"09:00\",\"end\":\"11:00\",\"slot\":15}','ENABLED');

INSERT INTO doctor_symptom_tag (doctor_id, tag) VALUES
                                                    (1,'楂樿鍘?),(1,'绯栧翱鐥?),(1,'鎰熷啋鍙戠儹'),
                                                    (2,'鍐犲績鐥?),(2,'蹇冨緥澶卞父'),
                                                    (3,'鍜冲椊'),(3,'鍝枠'),(3,'鍜藉枆鐐?),
                                                    (4,'鍎夸繚'),(4,'鍙戠儹');

/* =======================
   2) 鐢ㄦ埛鍩燂細妗ｆ涓庡仴搴疯褰?
   ======================= */
INSERT INTO health_profile
(profile_id, user_id, full_name, gender, birth_date, blood_type, phone, email, address, id_number,
 chronic_conditions, allergies, medical_history, medications, emergency_contact, emergency_phone, lifestyle_notes, last_updated)
VALUES
    (1,2,'寮犱笁','MALE','1990-06-15','A','13100000001','zhangsan@demo.local','涓婃捣甯傛郸涓滄柊鍖哄窛娌欒矾99鍙?,'310*********001',
     '楂樿鍘?,'','','缂矙鍧?,'鏉庡洓','13100000002','浣庣洂楗锛岃寰嬭繍鍔?,NOW(6)),
    (2,3,'鏉庡洓','MALE','1985-02-20','O','13100000002','lisi@demo.local','鍖椾含甯傛湞闃冲尯闃滈€氫笢澶ц6鍙?,'110*********002',
     '绯栧翱鐥?,'','','浜岀敳鍙岃儘','鐜嬩簲','13100000003','鎺у埗浣撻噸锛岄€傞噺鏈夋哀',NOW(6)),
    (3,4,'鐜嬩簲','MALE','1998-11-05','B','13100000003','wangwu@demo.local','骞垮窞甯傚ぉ娌冲尯浣撹偛瑗胯矾88鍙?,'440*********003',
     '','闈掗湁绱犺繃鏁?,'','','璧靛叚','13100000004','浣滄伅瑙勫緥',NOW(6)),
    (4,5,'鍛ㄧ惁','FEMALE','2001-04-10','AB','13100000004','zhouqi@demo.local','鏉窞甯傝タ婀栧尯绱噾娓矾188鍙?,'330*********004',
     '','鑺辩矇杩囨晱','','','闄堝嚒','13200000001','娉ㄦ剰琛ラ搧',NOW(6));

-- 鏈€杩?0澶╀綋寰侊紙寮犱笁绀轰緥锛?
INSERT INTO health_record (user_id, record_date, type, value, note) VALUES
                                                                        (2,'2025-10-02','BP','135/88','AM'),
                                                                        (2,'2025-10-02','HR','78',''),
                                                                        (2,'2025-10-03','BP','136/86','AM'),
                                                                        (2,'2025-10-03','BP','139/91','PM'),
                                                                        (2,'2025-10-03','HR','77',''),
                                                                        (2,'2025-10-04','BP','134/85','AM'),
                                                                        (2,'2025-10-04','BP','137/89','PM'),
                                                                        (2,'2025-10-04','HR','75',''),
                                                                        (2,'2025-10-05','BP','133/84','AM'),
                                                                        (2,'2025-10-05','BP','136/88','PM'),
                                                                        (2,'2025-10-06','BP','132/83','AM'),
                                                                        (2,'2025-10-07','BP','131/82','AM'),
                                                                        (2,'2025-10-08','BP','130/82','AM'),
                                                                        (2,'2025-10-09','BP','129/81','AM'),
                                                                        (2,'2025-10-10','BP','128/80','AM'),
                                                                        (3,'2025-10-10','BG','6.8','绌鸿吂'),
                                                                        (3,'2025-10-11','BG','6.6','绌鸿吂'),
                                                                        (3,'2025-10-11','BT','36.8','');

/* =======================
   3) 鏃ョ▼涓庨绾?
   ======================= */
-- 鐢熸垚 2025-10-11 ~ 2025-10-13 鏃舵锛堟瘡鍖诲笀6涓彿锛宑apacity=1锛?
INSERT INTO schedule (doctor_id, date, time_slot, capacity) VALUES
                                                                -- 2025-10-11
                                                                (1,'2025-10-11','09:00-09:15',1),(1,'2025-10-11','09:15-09:30',1),
                                                                (1,'2025-10-11','09:30-09:45',1),(1,'2025-10-11','09:45-10:00',1),
                                                                (1,'2025-10-11','10:00-10:15',1),(1,'2025-10-11','10:15-10:30',1),
                                                                (2,'2025-10-11','09:00-09:15',1),(2,'2025-10-11','09:15-09:30',1),
                                                                (2,'2025-10-11','09:30-09:45',1),(2,'2025-10-11','09:45-10:00',1),
                                                                (2,'2025-10-11','10:00-10:15',1),(2,'2025-10-11','10:15-10:30',1),
                                                                (3,'2025-10-11','09:00-09:15',1),(3,'2025-10-11','09:15-09:30',1),
                                                                (3,'2025-10-11','09:30-09:45',1),(3,'2025-10-11','09:45-10:00',1),
                                                                (3,'2025-10-11','10:00-10:15',1),(3,'2025-10-11','10:15-10:30',1),
                                                                (4,'2025-10-11','09:00-09:15',1),(4,'2025-10-11','09:15-09:30',1),
                                                                (4,'2025-10-11','09:30-09:45',1),(4,'2025-10-11','09:45-10:00',1),
                                                                (4,'2025-10-11','10:00-10:15',1),(4,'2025-10-11','10:15-10:30',1),
                                                                -- 2025-10-12
                                                                (1,'2025-10-12','09:00-09:15',1),(1,'2025-10-12','09:15-09:30',1),
                                                                (1,'2025-10-12','09:30-09:45',1),(1,'2025-10-12','09:45-10:00',1),
                                                                (1,'2025-10-12','10:00-10:15',1),(1,'2025-10-12','10:15-10:30',1),
                                                                (2,'2025-10-12','09:00-09:15',1),(2,'2025-10-12','09:15-09:30',1),
                                                                (2,'2025-10-12','09:30-09:45',1),(2,'2025-10-12','09:45-10:00',1),
                                                                (2,'2025-10-12','10:00-10:15',1),(2,'2025-10-12','10:15-10:30',1),
                                                                (3,'2025-10-12','09:00-09:15',1),(3,'2025-10-12','09:15-09:30',1),
                                                                (3,'2025-10-12','09:30-09:45',1),(3,'2025-10-12','09:45-10:00',1),
                                                                (3,'2025-10-12','10:00-10:15',1),(3,'2025-10-12','10:15-10:30',1),
                                                                (4,'2025-10-12','09:00-09:15',1),(4,'2025-10-12','09:15-09:30',1),
                                                                (4,'2025-10-12','09:30-09:45',1),(4,'2025-10-12','09:45-10:00',1),
                                                                (4,'2025-10-12','10:00-10:15',1),(4,'2025-10-12','10:15-10:30',1),
                                                                -- 2025-10-13锛堝皯閲忥級
                                                                (1,'2025-10-13','09:00-09:15',1),(1,'2025-10-13','09:15-09:30',1),
                                                                (2,'2025-10-13','09:00-09:15',1),(2,'2025-10-13','09:15-09:30',1),
                                                                (3,'2025-10-13','09:00-09:15',1),(3,'2025-10-13','09:15-09:30',1),
                                                                (4,'2025-10-13','09:00-09:15',1),(4,'2025-10-13','09:15-09:30',1);

-- 婕旂ず棰勭害锛堝惈涓嶅悓鐘舵€侊級
INSERT INTO appointment (user_id, doctor_id, appt_date, appt_time, status, symptom) VALUES
                                                                                        (2,1,'2025-10-11','09:00-09:15','COMPLETED','澶存檿銆佷箯鍔?),
                                                                                        (3,2,'2025-10-11','09:15-09:30','CANCELED','鑳搁椃銆佸績鎮?),
                                                                                        (4,3,'2025-10-11','09:30-09:45','BOOKED','鍜冲椊涓ゅ懆銆佸捊鐥?),
                                                                                        (5,4,'2025-10-11','09:00-09:15','COMPLETED','鍎跨鍙戠儹宸查€€'),
                                                                                        (2,1,'2025-10-12','09:15-09:30','CANCELED','澶嶈瘖');

-- 鍒濆鐘舵€佸巻鍙插揩鐓?
INSERT INTO appointment_status_history (appt_id, from_status, to_status, changed_at, remark)
SELECT appt_id, NULL, status, NOW(), 'init' FROM appointment;

-- 鍚屾 schedule.status
UPDATE schedule s
    LEFT JOIN appointment a
ON a.doctor_id = s.doctor_id AND a.appt_date = s.date AND a.appt_time = s.time_slot
    SET s.status = CASE
        WHEN a.appt_id IS NULL THEN 'OPEN'
        WHEN a.status IN ('COMPLETED','NO_SHOW') THEN 'CLOSED'
        WHEN a.status = 'CANCELED' THEN 'OPEN'
        ELSE 'BOOKED'
END;

/* =======================
   4) 閫氱煡 / 璁″垝 / 娴嬮噺鎻愰啋
   ======================= */
INSERT INTO notify_inbox (user_id, title, content, created_at, read_flag) VALUES
                                                                              (2,'娴嬮噺鎻愰啋','浠婃櫄19:00娴嬭鍘?,NOW(),0),
                                                                              (3,'鐢ㄨ嵂鎻愰啋','鏃╅鍚庢湇鐢ㄤ簩鐢插弻鑳?,NOW(),0),
                                                                              (5,'浣撴瀹夋帓','鏈懆棰勭害骞村害浣撴',NOW(),0);

INSERT INTO guidance_plan (user_id, doctor_id, type, rules, frequency, daily_time, start_at, end_at, status) VALUES
                                                                                                                 (2,1,'LIFESTYLE','浣庣洂楗 + 姣忓懆3娆″揩璧?0鍒嗛挓','DAILY','19:00','2025-10-12 00:00:00','2025-11-12 00:00:00','ACTIVE'),
                                                                                                                 (3,3,'DIET','鎺у埗鎬荤儹閲忥紝灏戠硸','DAILY','08:00','2025-10-12 00:00:00','2025-12-12 00:00:00','ACTIVE');

INSERT INTO measurement_reminder (user_id, type, daily_time, enabled) VALUES
                                                                          (2,'BP','08:00',1),
                                                                          (2,'BP','20:00',1),
                                                                          (3,'BG','07:30',1);

/* =======================
   5) 鍦ㄧ嚎闂瘖 / 澶勬柟
   ======================= */
INSERT INTO consult_session (session_id, user_id, doctor_id, status, chief_complaint, created_at)
VALUES
    (1,2,1,'OPEN','杩戜竴鍛ㄥご鏅曚箯鍔?,NOW()),
    (2,3,3,'CLOSED','涔呭挸涓ゅ懆',NOW());

INSERT INTO consult_message (session_id, sender_type, content_type, content, created_at, `read`) VALUES
                                                                                                     (1,'USER','TEXT','鍖荤敓鎮ㄥソ锛屾渶杩戞€诲ご鏅?,NOW(),0),
                                                                                                     (1,'DOCTOR','TEXT','鍏堢洃娴嬩笁澶╄鍘嬶紝璁板綍鏃╂櫄鏁版嵁',NOW(),0),
                                                                                                     (2,'USER','TEXT','鍜冲椊涓ゅ懆锛屾湁鐥?,NOW(),1),
                                                                                                     (2,'DOCTOR','TEXT','寤鸿鍋氫釜鑳哥墖锛屾敞鎰忎紤鎭?,NOW(),1);

-- 涓哄凡瀹屾垚棰勭害鍒涘缓澶勬柟锛堢敤 JSON 瀛樿嵂鍝佹竻鍗曪級
INSERT INTO prescription (appointment_id, doctor_id, user_id, presc_date, medicines, advice)
SELECT a.appt_id, a.doctor_id, a.user_id, NOW(),
       JSON_ARRAY(
               JSON_OBJECT('name','姘ㄦ捍绱㈠彛鏈嶆憾娑?,'spec','100ml:0.3g','dose','10ml','freq','TID','days',3),
               JSON_OBJECT('name','甯冩礇鑺紦閲婅兌鍥?,'spec','0.3g*10绮?,'dose','1绮?,'freq','BID','days',2)
       ),
       '澶氬枬姘达紝娉ㄦ剰浼戞伅'
FROM appointment a
WHERE a.status='COMPLETED' AND a.doctor_id=4 AND a.appt_date='2025-10-11' AND a.appt_time='09:00-09:15'
    LIMIT 1;

INSERT INTO prescription (appointment_id, doctor_id, user_id, presc_date, medicines, advice)
SELECT a.appt_id, a.doctor_id, a.user_id, NOW(),
       JSON_ARRAY(
               JSON_OBJECT('name','缂矙鍧︾墖','spec','80mg*14鐗?,'dose','1鐗?,'freq','QD','days',14)
       ),
       '浣庣洂楗锛岃寰嬭繍鍔?
FROM appointment a
WHERE a.status='COMPLETED' AND a.doctor_id=1 AND a.appt_date='2025-10-11' AND a.appt_time='09:00-09:15'
    LIMIT 1;

/* =======================
   6) 鍐呭 / 娲诲姩
   ======================= */
INSERT INTO article (article_id, title, content, category, author_id, publish_date, status)
VALUES
    (1,'绉嬪啲瀛ｅ懠鍚搁亾鐤剧梾濡備綍棰勯槻','鍕ゆ礂鎵嬨€佹埓鍙ｇ僵銆佸姞寮洪敾鐐尖€︹€?,'鐤剧梾棰勯槻',1,NOW(),'PUBLISHED'),
    (2,'楂樿鍘嬫偅鑰呯殑鐢熸椿鏂瑰紡绠＄悊','浣庣洂楗銆佹帶鍒朵綋閲嶃€佽寰嬭繍鍔?,'鍋ュ悍鐢熸椿',1,NOW(),'PUBLISHED');

INSERT INTO community_event (event_id, title, description, event_date, location, capacity, organizer, status)
VALUES
    (1,'楂樿鍘嬬鐞嗚搴?,'鐢熸椿鏂瑰紡骞查涓庣敤鑽€夋嫨','2025-10-15 19:00:00','绀惧尯鏈嶅姟涓績鎶ュ憡鍘?,200,'绀惧尯鍗敓鏈嶅姟涓績','OPEN'),
    (2,'鍏嶈垂浣撴鏃?,'琛€鍘?琛€绯?浣撻噸/BMI娴嬮噺','2025-10-20 08:30:00','绀惧尯鍗敓鏈嶅姟绔?,300,'绀惧尯鍗敓鏈嶅姟涓績','OPEN');

INSERT INTO event_registration (event_id, user_id) VALUES
                                                       (1,2),(1,3),(2,2);

/* =======================
   7) 瀹¤鏃ュ織鏍蜂緥
   ======================= */
INSERT INTO audit_log (ts, user_id, action, target_type, target_id, details, ip) VALUES
                                                                                     (NOW(),1,'CREATE','DOCTOR','1','鍒涘缓鍖荤敓妗ｆ','127.0.0.1'),
                                                                                     (NOW(),2,'APPOINT','APPOINTMENT',(SELECT appt_id FROM appointment WHERE user_id=2 AND doctor_id=1 ORDER BY appt_id LIMIT 1),'棰勭害鎸傚彿','127.0.0.1'),
  (NOW(),2,'COMPLETE','APPOINTMENT',(SELECT appt_id FROM appointment WHERE status='COMPLETED' AND doctor_id=1 ORDER BY appt_id LIMIT 1),'灏辫瘖瀹屾垚','127.0.0.1');




