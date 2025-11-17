# 마피아42 - Java 소켓 프로그래밍 프로젝트

Java Swing GUI와 소켓 프로그래밍을 활용한 멀티플레이어 마피아 게임

## 📋 프로젝트 개요

- **서버-클라이언트 소켓 통신** 기반 네트워크 애플리케이션
- **멀티스레드** 처리로 여러 클라이언트 동시 접속 지원
- **MySQL 데이터베이스** 연동 (사용자 계정, 방 관리)
- **실시간 채팅** 시스템 구현
- **Java Swing** GUI로 직관적인 사용자 인터페이스
- **17가지 역할** 기반 마피아 게임 시스템

## 🎮 주요 기능

### ✅ 구현 완료
- 로그인 / 회원가입 시스템
- 서버-클라이언트 소켓 연결
- 멀티플레이어 채팅 (방별 격리)
- 실시간 플레이어 목록
- 방 입장/퇴장 알림
- DB 기반 방 관리 시스템
- **채널 선택** 시스템 (1채널, 2채널, 3채널, 랭크 채널)
- **게임 시작** 동기화 (모든 플레이어 동시 시작)
- **역할 할당** 시스템 (17가지 역할)
- **게임 플레이** 화면 (역할별 능력 사용)

## 🛠 기술 스택

- **언어**: Java
- **GUI**: Swing
- **네트워크**: Socket Programming (ServerSocket, Socket)
- **멀티스레드**: Thread per client 모델
- **데이터베이스**: MySQL 8.0
- **JDBC 드라이버**: mysql-connector-j-9.5.0

## 📁 프로젝트 구조 및 파일 설명

```
mafia/
├── src/
│   ├── MainFrame.java              # 메인 GUI 프레임 (CardLayout으로 화면 전환 관리)
│   ├── LoginPanel.java             # 로그인/회원가입 화면
│   ├── LobbyPanel.java             # 로비 화면 (채널 선택, 방 목록 표시)
│   ├── GameRoomPanel.java          # 게임방 대기실 (플레이어 목록, 채팅, 게임 시작)
│   ├── GamePlayPanel.java          # 게임 플레이 화면 (역할 표시, 능력 사용, 게임 진행)
│   ├── TestConfig.java             # 테스트 모드 설정 (최소 인원 2명으로 테스트 가능)
│   │
│   ├── client/
│   │   └── GameClient.java         # 클라이언트 소켓 통신 (서버 연결, 메시지 송수신)
│   │
│   ├── server/
│   │   ├── MafiaServer.java        # 서버 메인 (클라이언트 연결 수락, 포트 9999)
│   │   ├── ClientHandler.java      # 클라이언트별 핸들러 (멀티스레드, 메시지 처리)
│   │   └── RoomManager.java        # 방 관리자 (방별 플레이어 목록, 메시지 브로드캐스트)
│   │
│   ├── protocol/
│   │   └── Message.java            # 메시지 프로토콜 (TYPE:data 형식, 직렬화/역직렬화)
│   │
│   └── database/
│       ├── DatabaseConnection.java # MySQL DB 연결 관리 (Connection Pool)
│       ├── UserDAO.java            # 사용자 데이터 액세스 (로그인, 회원가입)
│       └── RoomDAO.java            # 방 데이터 액세스 (방 생성, 조회, 입장/퇴장, 채널별 필터)
│
├── lib/
│   └── mysql-connector-j-9.5.0.jar # MySQL JDBC 드라이버
│
├── mafia.sql                        # 기본 DB 스키마 (user 테이블)
└── mafia_extended.sql               # 확장 DB 스키마 (rooms, room_players 테이블, 채널 정보)
```

## 🎭 역할 시스템

### 마피아 팀 (빨간색)
- **마피아 (MAFIA)** 🔪 - 밤에 한 명을 죽임
- **스파이 (SPY)** 🕶️ - 직업/팀 정보 확인
- **마담 (HOSTESS)** 💃 - 유혹하여 투표/능력 차단
- **도둑 (THIEF)** 🦹 - 능력 훔치기
- **짐승인간 (BEAST_MAN)** 🐺 - 마피아에게 길들여지면 팀 전환

### 시민 팀 (파란색/초록색)
- **경찰 (POLICE)** 🔍 - 마피아 여부 조사
- **의사 (DOCTOR)** 💊 - 마피아 공격으로부터 보호
- **군인 (SOLDIER)** 🎖️ - 1회 방탄
- **정치인 (POLITICIAN)** 📜 - 투표 2표
- **영매 (MEDIUM)** 🔮 - 죽은 사람 대화, 직업 확인 후 성불
- **연인 (LOVER)** 💕 - 2인 1세트, 밤 대화, 한 명 죽으면 다른 한 명도 죽음
- **기자 (REPORTER)** 📰 - 조사 후 다음 날 공개
- **사립탐정 (DETECTIVE)** 🕵️ - 밤 행동 추적
- **도굴꾼 (GHOUL)** 👻 - 죽은 사람 직업 훔치기
- **테러리스트 (MARTYR)** 💣 - 동반자살
- **성직자 (PRIEST)** ⛪ - 1회 부활
- **건달 (GANGSTER)** 🥊 - 협박하여 투표 차단
- **시민 (CITIZEN)** 👤 - 기본 역할

## ⚙️ 설치 및 설정

### 1. 데이터베이스 설정

```bash
# MySQL 실행 후
mysql -u root -p

# 데이터베이스 생성 및 스키마 적용
mysql -u root -p < mafia.sql
mysql -u root -p < mafia_extended.sql
```

### 2. 컴파일

```bash
javac -encoding UTF-8 -d bin -cp "lib/*" src/*.java src/**/*.java
```

## 🚀 실행 방법

### ⚠️ 중요: 반드시 순서대로 실행하세요!

### 1단계: 서버 실행 (필수)

```bash
# 터미널 1번에서 서버 실행
java -cp "bin:lib/*" server.MafiaServer
```

**출력 예시:**
```
========================================
🎮 마피아42 서버 시작
📡 포트: 9999
⏰ 시작 시각: ...
========================================
```

### 2단계: 클라이언트 실행 (여러 개 실행 가능)

**테스트 모드 (2명):**
```bash
# 터미널 2번
java -cp "bin:lib/*" MainFrame

# 터미널 3번
java -cp "bin:lib/*" MainFrame
```

**실제 게임 모드 (5-8명):**
```bash
# 터미널 2~9번에서 각각 실행
java -cp "bin:lib/*" MainFrame
```

## 🎯 사용 방법

### 1. 로그인
각 클라이언트에서 **다른 계정**으로 로그인:

**기존 계정:**
- 아이디: `123` / 비밀번호: `123`
- 아이디: `1234` / 비밀번호: `1234`
- 아이디: `2123` / 비밀번호: `123`

또는 "회원가입" 버튼으로 새 계정 생성

### 2. 채널 선택 및 방 입장
- 로그인 성공 시 자동으로 서버 연결
- 상단의 채널 레이블 클릭하여 채널 선택 (1채널, 2채널, 3채널, 랭크 채널)
- 선택한 채널의 방 목록 확인
- 원하는 방을 클릭하여 입장

### 3. 게임 시작
- 최소 인원 충족 시 "게임 시작" 버튼 활성화
  - 테스트 모드: 최소 2명
  - 실제 게임: 최소 5명, 최대 8명
- 한 플레이어가 게임 시작하면 **모든 플레이어 동시 시작**
- 각자 랜덤 역할 할당됨

### 4. 게임 플레이
- **왼쪽 패널**: 내 역할 정보 (아이콘, 이름, 설명)
- **중앙 패널**: 게임 로그 및 채팅
- **오른쪽 패널**: 플레이어 카드 (8명)
- 플레이어 카드 클릭으로 능력 사용

## 💬 멀티플레이어 게임 테스트

### 테스트 모드 (2명)
1. **서버 1개 + 클라이언트 2개** 실행
2. 각 클라이언트에서 **다른 계정**으로 로그인
3. 모두 **같은 방**에 입장
4. 한 클라이언트에서 "게임 시작" 클릭
5. **두 클라이언트 모두 게임 화면으로 전환** ✨
6. 각자 할당된 역할 확인

### 실제 게임 모드 (8명)
1. **서버 1개 + 클라이언트 8개** 실행
2. 각 클라이언트에서 다른 계정으로 로그인
3. 같은 방에 입장
4. 게임 시작
5. 역할 구성: 마피아2 + 의사1 + 경찰1 + 특직3 + 시민1

## 🔧 트러블슈팅

### 서버 연결 실패
```
❌ 서버에 연결할 수 없습니다
```
**해결방법:** 서버가 먼저 실행되어 있는지 확인하세요.

### DB 연결 실패
```
❌ DB 연결 실패
```
**해결방법:**
- MySQL이 실행 중인지 확인
- DB 사용자명/비밀번호 확인 (기본값: root/1234)
- mafia 데이터베이스가 생성되어 있는지 확인

### 방 입장 실패 (Duplicate entry)
```
❌ 방 입장 실패: Duplicate entry
```
**해결방법:** DB 정리 후 재시작
```bash
mysql -u root -p1234 -e "USE mafia; DELETE FROM room_players; UPDATE rooms SET current_players = 0;"
```

### 테스트 모드 변경
`src/TestConfig.java` 파일에서 `TEST_MODE` 값 변경:
- `true`: 최소 2명으로 테스트 가능
- `false`: 실제 게임 (최소 5명)

## 📊 네트워크 프로토콜

### 메시지 형식
```
TYPE:data
```

### 메시지 타입
- `LOGIN` - 로그인 (username|nickname|userId)
- `ROOM_JOIN` - 방 입장 (roomId)
- `ROOM_LEAVE` - 방 퇴장
- `CHAT_MESSAGE` - 채팅 메시지 (message)
- `PLAYER_JOINED` - 플레이어 입장 알림 (nickname)
- `PLAYER_LEFT` - 플레이어 퇴장 알림 (nickname)
- `PLAYER_LIST` - 플레이어 목록 (nick1|nick2|...)
- `GAME_START` - 게임 시작 (모든 클라이언트에게 브로드캐스트)
- `SYSTEM_MESSAGE` - 시스템 메시지

## 🎓 학습 포인트

이 프로젝트에서 다루는 개념:
- Java 소켓 프로그래밍 (ServerSocket, Socket)
- 멀티스레드 프로그래밍 (Thread per client)
- 동기화 (synchronized, ConcurrentHashMap, CopyOnWriteArrayList)
- JDBC를 활용한 데이터베이스 연동
- GUI 프로그래밍 (Swing, CardLayout, BorderLayout, GridLayout)
- 네트워크 프로토콜 설계 (직렬화/역직렬화)
- 클라이언트-서버 아키텍처
- Observer 패턴 (MessageListener)
- DAO 패턴 (Data Access Object)

## 📝 개발자 정보

- **프로젝트 목적**: 네트워크 프로그래밍 학습
- **개발 기간**: 2025년 11월
- **주요 기술**: Java Socket Programming, Multi-threading, MySQL, Swing GUI
