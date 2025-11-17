-- 마피아 게임 확장 스키마
-- 기존 user 테이블 위에 rooms, room_players 테이블 추가

USE mafia;

--
-- Table structure for table `rooms`
--

DROP TABLE IF EXISTS `room_players`;
DROP TABLE IF EXISTS `rooms`;

CREATE TABLE `rooms` (
  `room_id` int NOT NULL AUTO_INCREMENT,
  `room_name` varchar(100) NOT NULL,
  `channel_name` varchar(50) NOT NULL DEFAULT '일반 채널',
  `max_players` int NOT NULL DEFAULT 8,
  `current_players` int NOT NULL DEFAULT 0,
  `game_status` varchar(20) NOT NULL DEFAULT 'WAITING',
  `created_by` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`room_id`),
  KEY `fk_created_by` (`created_by`),
  CONSTRAINT `fk_created_by` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `room_players`
--

CREATE TABLE `room_players` (
  `room_player_id` int NOT NULL AUTO_INCREMENT,
  `room_id` int NOT NULL,
  `user_id` int NOT NULL,
  `joined_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `player_role` varchar(20) DEFAULT NULL,
  `is_alive` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`room_player_id`),
  UNIQUE KEY `unique_room_user` (`room_id`, `user_id`),
  KEY `fk_room` (`room_id`),
  KEY `fk_user` (`user_id`),
  CONSTRAINT `fk_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Insert sample rooms for testing
--

INSERT INTO `rooms` (`room_name`, `channel_name`, `max_players`, `current_players`, `game_status`, `created_by`)
VALUES
  ('마피아42 테스트방', '1채널', 8, 0, 'WAITING', 1),
  ('초보자 환영방', '1채널', 8, 0, 'WAITING', 1),
  ('랭크 전용방', '랭크 채널', 8, 0, 'WAITING', 2),
  ('빠른 게임방', '2채널', 8, 0, 'WAITING', 2),
  ('친선 게임방', '3채널', 8, 0, 'WAITING', 3);

--
-- Clear room players (for testing)
--
-- Note: Room players will be added dynamically when users join rooms
