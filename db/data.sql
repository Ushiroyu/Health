USE healthdb;

/* =======================
   1) 基础种子：用户/部门/医生/标签
   ======================= */
INSERT INTO users (user_id, username, password, name, gender, age, phone, email, role, status, created_at)
VALUES
    (1,'admin','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','系统管理员','MALE',30,'13000000001','admin@demo.local','ADMIN','ENABLED',NOW()),
    (2,'u_zhangsan','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','张三','MALE',35,'13100000001','zhangsan@demo.local','RESIDENT','ENABLED',NOW()),
    (3,'u_lisi','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','李四','MALE',40,'13100000002','lisi@demo.local','RESIDENT','ENABLED',NOW()),
    (4,'u_wangwu','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','王五','MALE',27,'13100000003','wangwu@demo.local','RESIDENT','ENABLED',NOW()),
    (5,'u_zhouqi','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','周琦','FEMALE',24,'13100000004','zhouqi@demo.local','RESIDENT','ENABLED',NOW()),
    (6,'d_chenfm','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','陈凡','MALE',38,'13200000001','drchen@demo.local','DOCTOR','ENABLED',NOW()),
    (7,'d_lizx','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','李哲新','MALE',42,'13200000002','drlizx@demo.local','DOCTOR','ENABLED',NOW()),
    (8,'d_wangxl','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','王晓磊','MALE',36,'13200000003','drwang@demo.local','DOCTOR','ENABLED',NOW()),
    (9,'d_zhangtt','$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghiJK','张婷婷','FEMALE',34,'13200000004','drzhang@demo.local','DOCTOR','ENABLED',NOW());

INSERT INTO department (dept_id, dept_name, description) VALUES
                                                             (1,'全科','常见病、多发病首诊与分诊'),
                                                             (2,'内科','呼吸/消化/内分泌等'),
                                                             (3,'心内科','心血管疾病'),
                                                             (4,'儿科','儿童青少年健康'),
                                                             (5,'耳鼻喉科','鼻炎/咽炎/中耳炎');

INSERT INTO doctor (doctor_id, user_id, dept_id, title, specialty, schedule_info, status) VALUES
                                                                                              (1,6,1,'主治医师','全科,三高管理','{\"days\":[\"Mon\"],\"start\":\"09:00\",\"end\":\"11:00\",\"slot\":15}','ENABLED'),
                                                                                              (2,7,3,'副主任医师','心律失常,高血压','{\"days\":[\"Tue\"],\"start\":\"09:00\",\"end\":\"11:00\",\"slot\":15}','ENABLED'),
                                                                                              (3,8,2,'主治医师','呼吸,内分泌','{\"days\":[\"Wed\"],\"start\":\"09:00\",\"end\":\"11:00\",\"slot\":15}','ENABLED'),
                                                                                              (4,9,4,'主治医师','儿保,感冒发热','{\"days\":[\"Thu\"],\"start\":\"09:00\",\"end\":\"11:00\",\"slot\":15}','ENABLED');

INSERT INTO doctor_symptom_tag (doctor_id, tag) VALUES
                                                    (1,'高血压'),(1,'糖尿病'),(1,'感冒发热'),
                                                    (2,'冠心病'),(2,'心律失常'),
                                                    (3,'咳嗽'),(3,'哮喘'),(3,'咽喉炎'),
                                                    (4,'儿保'),(4,'发热');

/* =======================
   2) 用户域：档案与健康记录
   ======================= */
INSERT INTO health_profile
(profile_id, user_id, full_name, gender, birth_date, blood_type, phone, email, address, id_number,
 chronic_conditions, allergies, medical_history, medications, emergency_contact, emergency_phone, lifestyle_notes, last_updated)
VALUES
    (1,2,'张三','MALE','1990-06-15','A','13100000001','zhangsan@demo.local','上海市浦东新区川沙路99号','310*********001',
     '高血压','','','缬沙坦','李四','13100000002','低盐饮食，规律运动',NOW(6)),
    (2,3,'李四','MALE','1985-02-20','O','13100000002','lisi@demo.local','北京市朝阳区阜通东大街6号','110*********002',
     '糖尿病','','','二甲双胍','王五','13100000003','控制体重，适量有氧',NOW(6)),
    (3,4,'王五','MALE','1998-11-05','B','13100000003','wangwu@demo.local','广州市天河区体育西路88号','440*********003',
     '','青霉素过敏','','','赵六','13100000004','作息规律',NOW(6)),
    (4,5,'周琦','FEMALE','2001-04-10','AB','13100000004','zhouqi@demo.local','杭州市西湖区紫金港路188号','330*********004',
     '','花粉过敏','','','陈凡','13200000001','注意补铁',NOW(6));

-- 最近10天体征（张三示例）
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
                                                                        (3,'2025-10-10','BG','6.8','空腹'),
                                                                        (3,'2025-10-11','BG','6.6','空腹'),
                                                                        (3,'2025-10-11','BT','36.8','');

/* =======================
   3) 日程与预约
   ======================= */
-- 生成 2025-10-11 ~ 2025-10-13 时段（每医师6个号，capacity=1）
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
                                                                -- 2025-10-13（少量）
                                                                (1,'2025-10-13','09:00-09:15',1),(1,'2025-10-13','09:15-09:30',1),
                                                                (2,'2025-10-13','09:00-09:15',1),(2,'2025-10-13','09:15-09:30',1),
                                                                (3,'2025-10-13','09:00-09:15',1),(3,'2025-10-13','09:15-09:30',1),
                                                                (4,'2025-10-13','09:00-09:15',1),(4,'2025-10-13','09:15-09:30',1);

-- 演示预约（含不同状态）
INSERT INTO appointment (user_id, doctor_id, appt_date, appt_time, status, symptom) VALUES
                                                                                        (2,1,'2025-10-11','09:00-09:15','COMPLETED','头晕、乏力'),
                                                                                        (3,2,'2025-10-11','09:15-09:30','CANCELED','胸闷、心悸'),
                                                                                        (4,3,'2025-10-11','09:30-09:45','BOOKED','咳嗽两周、咽痛'),
                                                                                        (5,4,'2025-10-11','09:00-09:15','COMPLETED','儿童发热已退'),
                                                                                        (2,1,'2025-10-12','09:15-09:30','CANCELED','复诊');

-- 初始状态历史快照
INSERT INTO appointment_status_history (appt_id, from_status, to_status, changed_at, remark)
SELECT appt_id, NULL, status, NOW(), 'init' FROM appointment;

-- 同步 schedule.status
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
   4) 通知 / 计划 / 测量提醒
   ======================= */
INSERT INTO notify_inbox (user_id, title, content, created_at, read_flag) VALUES
                                                                              (2,'测量提醒','今晚19:00测血压',NOW(),0),
                                                                              (3,'用药提醒','早餐后服用二甲双胍',NOW(),0),
                                                                              (5,'体检安排','本周预约年度体检',NOW(),0);

INSERT INTO guidance_plan (user_id, doctor_id, type, rules, frequency, daily_time, start_at, end_at, status) VALUES
                                                                                                                 (2,1,'LIFESTYLE','低盐饮食 + 每周3次快走30分钟','DAILY','19:00','2025-10-12 00:00:00','2025-11-12 00:00:00','ACTIVE'),
                                                                                                                 (3,3,'DIET','控制总热量，少糖','DAILY','08:00','2025-10-12 00:00:00','2025-12-12 00:00:00','ACTIVE');

INSERT INTO measurement_reminder (user_id, type, daily_time, enabled) VALUES
                                                                          (2,'BP','08:00',1),
                                                                          (2,'BP','20:00',1),
                                                                          (3,'BG','07:30',1);

/* =======================
   5) 在线问诊 / 处方
   ======================= */
INSERT INTO consult_session (session_id, user_id, doctor_id, status, chief_complaint, created_at)
VALUES
    (1,2,1,'OPEN','近一周头晕乏力',NOW()),
    (2,3,3,'CLOSED','久咳两周',NOW());

INSERT INTO consult_message (session_id, sender_type, content_type, content, created_at, `read`) VALUES
                                                                                                     (1,'USER','TEXT','医生您好，最近总头晕',NOW(),0),
                                                                                                     (1,'DOCTOR','TEXT','先监测三天血压，记录早晚数据',NOW(),0),
                                                                                                     (2,'USER','TEXT','咳嗽两周，有痰',NOW(),1),
                                                                                                     (2,'DOCTOR','TEXT','建议做个胸片，注意休息',NOW(),1);

-- 为已完成预约创建处方（用 JSON 存药品清单）
INSERT INTO prescription (appointment_id, doctor_id, user_id, presc_date, medicines, advice)
SELECT a.appt_id, a.doctor_id, a.user_id, NOW(),
       JSON_ARRAY(
               JSON_OBJECT('name','氨溴索口服溶液','spec','100ml:0.3g','dose','10ml','freq','TID','days',3),
               JSON_OBJECT('name','布洛芬缓释胶囊','spec','0.3g*10粒','dose','1粒','freq','BID','days',2)
       ),
       '多喝水，注意休息'
FROM appointment a
WHERE a.status='COMPLETED' AND a.doctor_id=4 AND a.appt_date='2025-10-11' AND a.appt_time='09:00-09:15'
    LIMIT 1;

INSERT INTO prescription (appointment_id, doctor_id, user_id, presc_date, medicines, advice)
SELECT a.appt_id, a.doctor_id, a.user_id, NOW(),
       JSON_ARRAY(
               JSON_OBJECT('name','缬沙坦片','spec','80mg*14片','dose','1片','freq','QD','days',14)
       ),
       '低盐饮食，规律运动'
FROM appointment a
WHERE a.status='COMPLETED' AND a.doctor_id=1 AND a.appt_date='2025-10-11' AND a.appt_time='09:00-09:15'
    LIMIT 1;

/* =======================
   6) 内容 / 活动
   ======================= */
INSERT INTO article (article_id, title, content, category, author_id, publish_date, status)
VALUES
    (1,'秋冬季呼吸道疾病如何预防','勤洗手、戴口罩、加强锻炼……','疾病预防',1,NOW(),'PUBLISHED'),
    (2,'高血压患者的生活方式管理','低盐饮食、控制体重、规律运动','健康生活',1,NOW(),'PUBLISHED');

INSERT INTO community_event (event_id, title, description, event_date, location, capacity, organizer, status)
VALUES
    (1,'高血压管理讲座','生活方式干预与用药选择','2025-10-15 19:00:00','社区服务中心报告厅',200,'社区卫生服务中心','OPEN'),
    (2,'免费体检日','血压/血糖/体重/BMI测量','2025-10-20 08:30:00','社区卫生服务站',300,'社区卫生服务中心','OPEN');

INSERT INTO event_registration (event_id, user_id) VALUES
                                                       (1,2),(1,3),(2,2);

/* =======================
   7) 审计日志样例
   ======================= */
INSERT INTO audit_log (ts, user_id, action, target_type, target_id, details, ip) VALUES
                                                                                     (NOW(),1,'CREATE','DOCTOR','1','创建医生档案','127.0.0.1'),
                                                                                     (NOW(),2,'APPOINT','APPOINTMENT',(SELECT appt_id FROM appointment WHERE user_id=2 AND doctor_id=1 ORDER BY appt_id LIMIT 1),'预约挂号','127.0.0.1'),
  (NOW(),2,'COMPLETE','APPOINTMENT',(SELECT appt_id FROM appointment WHERE status='COMPLETED' AND doctor_id=1 ORDER BY appt_id LIMIT 1),'就诊完成','127.0.0.1');
