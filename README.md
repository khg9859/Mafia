# 마피아 게임 (Mafia Game)

자바 소켓 프로그래밍 기반의 멀티플레이어 마피아 게임입니다.

## ✨ 목차

1. [빠른 시작](#-빠른-시작)
2. [게임 방법](#-게임-방법)
3. [역할 설명](#-역할-설명)
4. [주요 기능](#-주요-기능)
5. [프로젝트 구조](#-프로젝트-구조)
6. [개발 가이드](#-개발-가이드)
7. [기술 스택](#-기술-스택)

---

## ✨ 빠른 시작

### 필요 조건

- Java 17 이상
- Maven 3.6 이상

### 설치 방법

```bash
# 저장소 클론
git clone https://github.com/khg9859/Mafia.git
cd Mafia

# Maven 빌드
mvn clean package
```

### 실행 방법

**1. 서버 실행**

```bash
java -jar target/mafia-game-1.0.0-server-executable.jar
```

1. 서버 GUI가 열립니다
2. Port Number 확인 (기본값: 30000)
3. "Start Server" 버튼 클릭
4. 플레이어 접속 대기

**2. 클라이언트 실행 (여러 개 가능)**

```bash
java -jar target/mafia-game-1.0.0-client-executable.jar
```

1. User Name 입력
2. IP Address 입력 (로컬: 127.0.0.1)
3. Port Number 입력 (서버와 동일)
4. "Connect to Game" 버튼 클릭

**3. 게임 시작**

- 최소 4명 이상 접속 필요
- 서버에서 "Start Game" 버튼 클릭

### 테스트 모드

개발 및 테스트를 위한 자동화 모드입니다.

**활성화 방법:**

`MafiaGameServer.java` 74번 줄:
```java
private static final boolean TEST_MODE = true;
```

`MafiaGameClientMain.java` 52번 줄:
```java
private static final boolean TEST_MODE = true;
```

**테스트 모드 기능:**
- 클라이언트: 자동 로그인 (랜덤 이름 생성)
- 서버: 8명 접속 시 자동 게임 시작

**빠른 테스트:**
```bash
# 컴파일
mvn clean compile

# 서버 실행
java -cp target/classes mafia.game.MafiaGameServer &

# 클라이언트 8개 실행
for i in {1..8}; do
  java -cp target/classes mafia.game.MafiaGameClientMain &
  sleep 0.5
done
```

---

## ✨ 게임 방법

### 게임 진행 순서

**1단계: 대기 (WAITING)**
- 최소 4명 이상 접속
- 서버 관리자가 게임 시작

**2단계: 밤 (NIGHT) - 30초**
- 마피아: 제거할 대상 선택
- 의사: 보호할 대상 선택
- 경찰: 조사할 대상 선택
- 기타 특수 직업: 능력 사용
- 시민: 대기

**3단계: 밤 결과 발표**
- 마피아에게 제거된 사람 공개
- 의사 보호 성공 시 생존
- 군인 방어막으로 생존 가능

**4단계: 낮 (DAY) - 30초**
- 자유 토론
- 의심되는 사람 찾기
- 기자 특종 발표 (해당 시)
- 성직자 부활 (해당 시)

**5단계: 투표 (VOTE) - 20초**
- 의심되는 사람에게 투표
- 최다 득표자 제거
- 동점 시 아무도 제거 안 됨

**6단계: 최후의 반론 - 15초**
- 제거 대상자의 변론
- 찬반 투표로 최종 결정

**7단계: 승리 조건 확인**
- 시민 팀 승리: 모든 마피아 제거
- 마피아 팀 승리: 마피아 수 ≥ 시민 수

조건 미충족 시 2단계로 반복

### 플레이 팁

**시민 팀 전략:**
- 경찰 정보 신중히 판단
- 투표 패턴 분석
- 행동 패턴 관찰
- 정보 공유

**마피아 팀 전략:**
- 시민처럼 자연스럽게 행동
- 능력자(의사/경찰) 우선 제거
- 다른 플레이어에게 의심 유도
- 동료와 투표 분산

---

## ✨ 역할 설명

### 마피아 팀

**마피아 (MAFIA)**
- 밤에 시민 한 명 제거
- 마피아 수 ≥ 시민 수 시 승리
- 4명: 1명 / 5명 이상: 2명

**스파이 (SPY)**
- 밤에 한 명의 직업 조사
- 마피아 조사 시 접선하여 마피아 팀 합류
- 군인에게 정체 노출

**마담 (MADAME)**
- 낮 투표로 플레이어 유혹
- 유혹당한 플레이어는 밤 능력 사용 불가
- 마피아 유혹 시 접선하여 마피아 팀 합류

### 시민 팀

**의사 (DOCTOR)**
- 밤에 한 명 보호
- 마피아 공격 방어

**경찰 (POLICE)**
- 밤에 한 명 조사
- 마피아 여부 확인

**정치인 (POLITICIAN)**
- 투표로 죽지 않음
- 2표 행사

**군인 (SOLDIER)**
- 마피아 공격 1회 방어
- 스파이 조사 시 스파이 정체 파악

**영매 (SHAMAN)**
- 죽은 자 대화 확인
- 한 명 성불시켜 직업 확인

**기자 (REPORTER)**
- 2~8일차 밤에 한 명 취재
- 다음 날 직업 공개

**도굴꾼 (GHOUL)**
- 첫날 밤 마피아 희생자의 직업 획득

**건달 (GANGSTER)**
- 밤마다 한 명 선택
- 다음 날 투표 금지

**성직자 (PRIEST)**
- 게임 중 1회 부활 능력
- 성불된 플레이어는 부활 불가

**시민 (CITIZEN)**
- 특별 능력 없음
- 투표로 마피아 찾기

---

## ✨ 주요 기능

### 1. 쪽지 시스템

낮 시간에 익명 쪽지 전송 가능

**사용법:**
```
/whisper [대상] [내용]
```

**특징:**
- 익명 전송
- 시간당 최대 10개 제한
- 200자 제한

### 2. 로비 시스템

게임 시작 전 30초 대기실

**기능:**
- 준비 상태 확인
- 역할 선호도 설정 (최대 3개)
- 자유 채팅
- 모두 준비 시 자동 시작

### 3. 통계 시스템

플레이어 기록 추적 및 업적

**명령어:**
```
/stats              - 내 통계
/leaderboard        - 리더보드
/achievements       - 업적 확인
```

**업적 목록:**
- 첫 승리
- 완벽한 마피아
- 생존왕 (10게임 연속)
- 명탐정 (마피아 3명 이상 찾기)
- 불사조 (2번 부활)
- 베테랑 (100게임)
- 챔피언 (승률 70% 이상)

### 4. 실시간 투표 집계

투표 진행 상황 실시간 표시

**기능:**
- 득표수 바 차트
- 투표율 계산
- 공개/익명 모드

**예시:**
```
플레이어1: ████████░░ 8표
플레이어2: ███████░░░ 7표
플레이어3: ████░░░░░░ 4표
```

### 5. 역할 가이드

모든 역할의 상세 설명

**명령어:**
```
/guide              - 내 역할 가이드
/guide MAFIA        - 마피아 가이드
/tutorial           - 기본 튜토리얼
/help               - 도움말
```

### 6. 감정 표현

이모지로 감정 표현

**명령어:**
```
/emotion THUMBS_UP          - 👍
/emotion SHOCKED Player1    - Player1에게 😱
/emotions                   - 이모지 목록
```

**이모지 종류:**
- 긍정: 👍 👏 ❤️ 😄 🎉
- 부정: 👎 😡 😢 💔
- 중립: 🤔 😐 🤷
- 게임: 😱 🎯 🔍 ⚠️ 🤐 🎭 💀

### 7. 재접속 기능

연결 끊김 시 30초 내 재접속 가능

**기능:**
- 게임 상태 자동 저장
- 역할, 정보 복원
- 채팅 히스토리 제공
- 타임아웃 시 자동 처리

### 8. 이벤트 모드

시즌별 특별 이벤트

**크리스마스 이벤트 (12월):**
- 산타가 랜덤 플레이어에게 2표 스킬 선물
- 크리스마스 테마 메시지

**향후 추가 예정:**
- 할로윈 (10월): 뱀파이어, 늑대인간
- 설날 (1-2월): 점쟁이, 조상님
- 여름 (7-8월): 라이프가드, 상어

---

## ✨ 프로젝트 구조

```
마피아2/
├── src/main/java/mafia/game/
│   ├── MafiaGameServer.java          # 서버 메인
│   ├── MafiaGameClientMain.java      # 클라이언트 로그인
│   ├── MafiaGameClientView.java      # 클라이언트 게임 화면
│   │
│   ├── models/                        # 데이터 모델
│   │   ├── PlayerStatistics.java
│   │   └── Message.java
│   │
│   ├── features/                      # 기능 모듈
│   │   ├── WhisperManager.java        # 쪽지 시스템
│   │   ├── LobbyManager.java          # 로비 시스템
│   │   ├── StatisticsManager.java     # 통계 시스템
│   │   ├── VoteTracker.java           # 투표 집계
│   │   ├── RoleGuideManager.java      # 역할 가이드
│   │   ├── EmotionManager.java        # 감정 표현
│   │   └── ReconnectionManager.java   # 재접속 관리
│   │
│   └── events/                        # 이벤트 시스템
│       └── EventModeManager.java
│
├── src/main/resources/
│   ├── GameSound/                     # 게임 사운드
│   └── info/                          # 이미지 리소스
│
├── game_data/statistics/              # 플레이어 통계 저장
├── pom.xml                            # Maven 설정
└── README.md                          # 이 파일
```

---

## ✨ 개발 가이드

### 빌드 명령어

```bash
# 컴파일만
mvn compile

# 테스트 및 패키징
mvn clean package

# 테스트 건너뛰고 빌드
mvn clean package -DskipTests
```

### 디자인 패턴

**Singleton Pattern**
- 모든 매니저 클래스

**Observer Pattern**
- 이벤트 리스너 시스템

**Strategy Pattern**
- 역할별 전략

**Builder Pattern**
- 복잡한 객체 생성

**Memento Pattern**
- 재접속 상태 저장

### 새 기능 추가 방법

**1. 새 매니저 생성**

```java
public class CustomManager {
    private static CustomManager instance;
    
    public static synchronized CustomManager getInstance() {
        if (instance == null) {
            instance = new CustomManager();
        }
        return instance;
    }
    
    private CustomManager() {
        // 초기화
    }
}
```

**2. 서버에 통합**

```java
public class MafiaGameServer {
    private CustomManager customManager;
    
    private void initializeManagers() {
        customManager = CustomManager.getInstance();
    }
}
```

### 새 이벤트 추가

```java
EventMode newEvent = new EventMode.Builder("NEW_EVENT", "새 이벤트")
    .description("설명")
    .activePeriod(Month.APRIL, 1, Month.APRIL, 30)
    .addSpecialRole("NEW_ROLE", "새 역할", "능력")
    .build();

EventModeManager.getInstance().registerEvent(newEvent);
```

### CI/CD

GitHub Actions 자동 빌드 및 릴리스

**릴리스 방법:**
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

---

## ✨ 기술 스택

- **언어**: Java 17
- **빌드**: Maven 3.x
- **GUI**: Java Swing
- **네트워크**: Java Socket
- **사운드**: Java Sound API
- **CI/CD**: GitHub Actions

### 프로토콜

**서버 → 클라이언트:**
```
ROLE:역할명                    - 역할 배정
PHASE:단계명                   - 게임 단계 변경
PLAYERS:이름1,이름2,...        - 플레이어 목록
DEAD:true/false                - 사망/부활
SYSTEM: 메시지                 - 시스템 메시지
```

**클라이언트 → 서버:**
```
/login 이름                    - 로그인
NIGHT_ACTION:역할:대상         - 밤 행동
VOTE:대상                      - 투표
[이름] 메시지                  - 채팅
/exit                          - 종료
```

---

## ✨ 라이선스

이 프로젝트는 교육 목적으로 작성되었습니다.

## ✨ 기여

이슈와 PR은 언제나 환영합니다!

## ✨ 문의

GitHub Issues: https://github.com/khg9859/Mafia/issues

---

**버전**: 2.0  
**최종 업데이트**: 2025-12-04
