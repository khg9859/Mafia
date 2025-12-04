# 마피아 게임 신규 기능 가이드

이 문서는 새롭게 추가된 9가지 주요 기능에 대한 상세 가이드입니다.

## 📋 목차

1. [익명 쪽지 시스템](#1-익명-쪽지-시스템)
2. [프리게임 로비 시스템](#2-프리게임-로비-시스템)
3. [업적/통계 시스템](#3-업적통계-시스템)
4. [라이브 투표 집계 표시](#4-라이브-투표-집계-표시)
5. [역할 가이드 & 튜토리얼](#5-역할-가이드--튜토리얼)
6. [감정 표현 시스템](#6-감정-표현-시스템)
7. [AI 플레이어 시스템](#7-ai-플레이어-시스템)
8. [재접속 기능](#8-재접속-기능)
9. [이벤트 모드 시스템](#9-이벤트-모드-시스템)

---

## 1. 익명 쪽지 시스템

### 📝 개요
낮 시간에 다른 플레이어에게 익명으로 쪽지를 보낼 수 있는 전략적 소통 기능입니다.

### 🎯 주요 기능
- **익명 전송**: 발신자의 신원이 숨겨집니다
- **스팸 방지**: 시간당 최대 10개의 쪽지 전송 제한
- **낮 시간 전용**: 낮 페이즈에만 사용 가능
- **200자 제한**: 간결한 메시지 권장

### 💻 사용 방법

**서버 측 통합:**
```java
import mafia.game.features.WhisperManager;

// 매니저 인스턴스 가져오기
WhisperManager whisperManager = WhisperManager.getInstance();

// 낮 페이즈 시작 시
whisperManager.startDayPhase();

// 쪽지 전송
WhisperManager.WhisperResult result = whisperManager.sendWhisper(
    senderName,      // 발신자 (실제 이름, 하지만 수신자에게는 익명)
    receiverName,    // 수신자
    content          // 내용
);

if (result.isSuccess()) {
    // 전송 성공
    Message whisper = result.getWhisper();
    // 수신자에게 전송
}

// 낮 페이즈 종료 시
whisperManager.endDayPhase();
```

**클라이언트 명령어:**
```
/whisper [대상] [내용]
예: /whisper 플레이어1 당신을 신뢰합니다
```

### 🔧 설정 가능 항목
```java
// 기능 활성화/비활성화
whisperManager.setEnabled(true);

// 상수 수정 (WhisperManager.java):
MAX_WHISPERS_PER_HOUR = 10;  // 시간당 최대 쪽지 수
LIMIT_WINDOW_MS = 3600000;    // 제한 시간 (밀리초)
```

### 📊 프로토콜
```
클라이언트 -> 서버: WHISPER:[대상]:[내용]
서버 -> 클라이언트: [익명 쪽지] [내용]
```

---

## 2. 프리게임 로비 시스템

### 📝 개요
게임 시작 전 30초간 플레이어들이 대화하고 준비 상태를 확인하는 대기실 기능입니다.

### 🎯 주요 기능
- **준비 상태 관리**: 각 플레이어가 준비 완료 표시
- **역할 선호도 설정**: 최대 3개의 선호 역할 선택 (참고용)
- **자동 게임 시작**: 모두 준비 시 5초 카운트다운 후 자동 시작
- **로비 채팅**: 자유로운 대화

### 💻 사용 방법

**서버 측 통합:**
```java
import mafia.game.features.LobbyManager;

// 매니저 인스턴스
LobbyManager lobbyManager = LobbyManager.getInstance();

// 로비 시작
lobbyManager.startLobby();

// 플레이어 추가
lobbyManager.addPlayer(playerName);

// 준비 상태 변경
lobbyManager.setReady(playerName, true);

// 역할 선호도 설정
List<String> preferences = Arrays.asList("MAFIA", "DOCTOR", "POLICE");
lobbyManager.setRolePreferences(playerName, preferences);

// 상태 리스너 등록
lobbyManager.addStateListener(new LobbyManager.LobbyStateListener() {
    @Override
    public void onStateChanged(LobbyManager.LobbyState newState) {
        if (newState == LobbyManager.LobbyState.READY) {
            // 게임 시작 준비 완료
            startGame();
        }
    }
});
```

**로비 상태:**
- `WAITING`: 플레이어 대기 중
- `READY`: 게임 시작 준비 완료
- `COUNTDOWN`: 카운트다운 중 (5초)
- `CLOSED`: 로비 종료

### 📊 프로토콜
```
서버 -> 클라이언트: LOBBY_STATE:[상태]
클라이언트 -> 서버: READY:true/false
클라이언트 -> 서버: ROLE_PREFERENCE:[역할1],[역할2],[역할3]
```

---

## 3. 업적/통계 시스템

### 📝 개요
플레이어의 게임 기록, 통계, 업적을 추적하고 리더보드를 제공하는 시스템입니다.

### 🎯 주요 기능
- **자동 통계 기록**: 게임 종료 시 자동 저장
- **영구 저장**: 파일 시스템에 직렬화하여 저장
- **업적 시스템**: 7가지 기본 업적
- **리더보드**: 승률, 게임 수, MVP 기준

### 🏆 기본 업적 목록
1. **첫 승리**: 첫 게임 승리
2. **완벽한 마피아**: 마피아로 완벽한 승리
3. **생존왕**: 10게임 연속 생존
4. **명탐정**: 경찰로 마피아 3명 이상 찾기
5. **불사조**: 한 게임에서 2번 부활
6. **베테랑**: 100게임 플레이
7. **챔피언**: 승률 70% 이상 (최소 20게임)

### 💻 사용 방법

**서버 측 통합:**
```java
import mafia.game.features.StatisticsManager;
import mafia.game.models.PlayerStatistics;

// 매니저 인스턴스
StatisticsManager statsManager = StatisticsManager.getInstance();

// 게임 시작 시
Map<String, String> players = new HashMap<>();
players.put("플레이어1", "MAFIA");
players.put("플레이어2", "DOCTOR");
statsManager.startGame(players);

// 게임 종료 시
String winningTeam = "CITIZEN"; // 또는 "MAFIA"
String mvpPlayer = "플레이어2";
statsManager.endGame(winningTeam, mvpPlayer);

// 통계 조회
PlayerStatistics stats = statsManager.getStatistics("플레이어1");
System.out.println(stats.getDetailedStats());

// 리더보드 조회
List<PlayerStatistics> leaderboard = statsManager.getLeaderboardByWinRate(10);
```

**클라이언트 명령어:**
```
/stats              - 내 통계 보기
/leaderboard        - 리더보드 보기
/achievements       - 업적 확인
```

### 📁 데이터 저장 위치
```
game_data/statistics/
├── 플레이어1.dat
├── 플레이어2.dat
└── ...
```

---

## 4. 라이브 투표 집계 표시

### 📝 개요
투표 진행 상황을 실시간으로 추적하고 시각화하는 시스템입니다.

### 🎯 주요 기능
- **실시간 집계**: 투표 즉시 반영
- **공개/익명 모드**: 설정 가능
- **진행 바 차트**: 득표수 시각화
- **투표율 계산**: 참여율 추적

### 💻 사용 방법

**서버 측 통합:**
```java
import mafia.game.features.VoteTracker;
import mafia.game.features.VoteTracker.VoteMode;

// 매니저 인스턴스
VoteTracker voteTracker = VoteTracker.getInstance();

// 투표 시작
Set<String> voters = new HashSet<>(Arrays.asList("P1", "P2", "P3", "P4"));
Set<String> candidates = new HashSet<>(Arrays.asList("P1", "P2", "P3", "P4"));
voteTracker.startVoting(voters, candidates, 20, VoteMode.PUBLIC);

// 투표 등록
VoteTracker.VoteCastResult result = voteTracker.castVote("P1", "P2");

// 실시간 정보 조회
double voteRate = voteTracker.calculateVoteRate();
List<String> barChart = voteTracker.getVoteBarChart(20);

// 투표 종료
VoteTracker.VoteResult voteResult = voteTracker.endVoting();
List<String> topVoted = voteResult.getTopVoted();

// 리스너 등록
voteTracker.addVoteChangeListener(new VoteTracker.VoteChangeListener() {
    @Override
    public void onVoteChanged(String voter, String target, boolean isRevote, double voteRate) {
        // 모든 클라이언트에게 업데이트 전송
        broadcastVoteUpdate(target, voteTracker.getVoteCount(target), voteRate);
    }
    // ... 다른 메소드 구현
});
```

### 📊 바 차트 예시
```
플레이어1: ████████░░░░░░░░░░░░ 8표
플레이어2: ███████░░░░░░░░░░░░░ 7표
플레이어3: ████░░░░░░░░░░░░░░░░ 4표
플레이어4: ██░░░░░░░░░░░░░░░░░░ 2표
```

### 📊 프로토콜
```
서버 -> 클라이언트: VOTE_UPDATE:[대상]:[득표수]:[투표율]
서버 -> 클라이언트: VOTE_CHART:[바차트 데이터]
```

---

## 5. 역할 가이드 & 튜토리얼

### 📝 개요
모든 역할의 상세 가이드, 전략, 팁을 제공하는 종합 도움말 시스템입니다.

### 🎯 주요 기능
- **13개 역할 가이드**: 모든 역할의 상세 설명
- **전략 & 팁**: 역할별 플레이 가이드
- **단축키 안내**: 능력 사용 명령어
- **기본 튜토리얼**: 신규 플레이어용

### 💻 사용 방법

**서버 측 통합:**
```java
import mafia.game.features.RoleGuideManager;
import mafia.game.features.RoleGuideManager.RoleGuide;

// 매니저 인스턴스
RoleGuideManager guideManager = RoleGuideManager.getInstance();

// 역할 가이드 조회
RoleGuide mafiaGuide = guideManager.getGuide("MAFIA");
String guideText = mafiaGuide.toString();

// 기본 튜토리얼
String tutorial = guideManager.getBasicTutorial();

// 튜토리얼 완료 체크
if (!guideManager.isTutorialCompleted(playerName)) {
    // 신규 플레이어에게 튜토리얼 표시
    sendTutorial(playerName);
    guideManager.completeTutorial(playerName);
}
```

**클라이언트 명령어:**
```
/guide [역할]       - 역할 가이드 보기
/guide MAFIA        - 마피아 가이드
/tutorial           - 기본 튜토리얼
/help               - 도움말
```

### 📖 가이드 구조
```
=== 마피아 가이드 ===

【소속】마피아 팀

【설명】
밤에 시민을 제거하는 핵심 악역입니다.

【능력】
밤마다 한 명의 플레이어를 제거할 수 있습니다.

【승리 조건】
마피아 팀이 시민 팀과 같거나 많아지면 승리합니다.

【전략】
1. 초반에는 조용히 행동하며 의사와 경찰을 찾아 제거하세요.
2. 낮에는 시민처럼 행동하며 다른 시민을 의심하도록 유도하세요.
...
```

---

## 6. 감정 표현 시스템

### 📝 개요
이모지로 감정을 표현하여 비언어적 소통을 지원하는 시스템입니다.

### 🎯 주요 기능
- **19개 기본 이모지**: 긍정, 부정, 중립, 게임 특화
- **쿨다운 시스템**: 3초 간격 제한
- **대상 지정**: 특정 플레이어에게 반응
- **사용 통계**: 인기 이모지 추적

### 😀 이모지 목록

**긍정:**
- 👍 좋아요
- 👏 박수
- ❤️ 하트
- 😄 웃음
- 🎉 축하

**부정:**
- 👎 싫어요
- 😡 화남
- 😢 슬픔
- 💔 실망

**중립:**
- 🤔 생각중
- 😐 무표정
- 🤷 모름

**게임 특화:**
- 😱 충격
- 🎯 타겟
- 🔍 조사
- ⚠️ 경고
- 🤐 침묵
- 🎭 가면
- 💀 죽음

### 💻 사용 방법

**서버 측 통합:**
```java
import mafia.game.features.EmotionManager;
import mafia.game.features.EmotionManager.EmotionResult;

// 매니저 인스턴스
EmotionManager emotionManager = EmotionManager.getInstance();

// 감정 표현
EmotionResult result = emotionManager.express(playerName, "THUMBS_UP");
// 또는 대상 지정
EmotionResult result = emotionManager.express(playerName, "THUMBS_UP", targetPlayer);

if (result.isSuccess()) {
    // 모든 플레이어에게 전송
    broadcastEmotion(playerName, result.getEmotion(), targetPlayer);
}

// 리스너 등록
emotionManager.addEmotionListener(new EmotionManager.EmotionListener() {
    @Override
    public void onEmotionExpressed(EmotionManager.EmotionEvent event) {
        String display = event.toDisplayString();
        // 클라이언트에게 전송
    }
});
```

**클라이언트 명령어:**
```
/emotion [ID]               - 감정 표현
/emotion THUMBS_UP          - 👍
/emotion SHOCKED Player1    - Player1에게 😱
/emotions                   - 이모지 목록
```

### 📊 프로토콜
```
클라이언트 -> 서버: EMOTION:[ID]:[대상(선택)]
서버 -> 클라이언트: EMOTION_DISPLAY:[발신자]:[이모지]:[대상]
```

---

## 7. AI 플레이어 시스템

### 📝 개요
인원 부족 시 AI 봇을 추가하여 게임을 진행할 수 있는 시스템입니다.

### 🎯 주요 기능
- **3가지 난이도**: 초급(30%), 중급(60%), 고급(90%) 최적 선택률
- **역할별 AI**: 각 역할에 맞는 행동 패턴
- **학습 능력**: 게임 진행 중 정보 축적
- **자연스러운 발언**: 간단한 발언 생성

### 🤖 난이도별 특징

**초급 (EASY):**
- 30% 확률로 최적 선택
- 주로 랜덤 행동
- 초보자 학습용

**중급 (MEDIUM):**
- 60% 확률로 최적 선택
- 기본 전략 수행
- 표준 게임 진행용

**고급 (HARD):**
- 90% 확률로 최적 선택
- 고급 전략 수행
- 숙련자 도전용

### 💻 사용 방법

**서버 측 통합:**
```java
import mafia.game.ai.AIPlayer;
import mafia.game.ai.BasicAIPlayer;

// AI 플레이어 생성
AIPlayer aiPlayer = new BasicAIPlayer(
    "AI_Bot_1",
    AIPlayer.Difficulty.MEDIUM
);

// 역할 배정
aiPlayer.setRole("CITIZEN");

// 밤 행동 결정
String target = aiPlayer.decideNightAction(alivePlayers);
if (target != null) {
    // AI 행동 처리
}

// 투표 결정
String voteTarget = aiPlayer.decideVote(alivePlayers);

// 발언 생성
AIPlayer.GameContext context = new AIPlayer.GameContext(
    dayCount, phase, alivePlayers, deadPlayers, voteCount
);
String statement = aiPlayer.generateStatement(context);

// 이벤트 관찰
AIPlayer.GameEvent event = new AIPlayer.GameEvent(
    AIPlayer.GameEvent.EventType.PLAYER_DIED,
    null, victimName, null
);
aiPlayer.observeEvent(event);
```

### 🎨 확장 방법
역할별 AI를 만들려면 `AIPlayer`를 상속:
```java
public class MafiaAIPlayer extends AIPlayer {
    @Override
    public String decideNightAction(List<String> alivePlayers) {
        // 마피아 전용 로직
        return findMostSuspicious(excludeSelf(alivePlayers));
    }
}
```

---

## 8. 재접속 기능

### 📝 개요
연결이 끊긴 플레이어가 30초 이내에 재접속하여 게임을 이어갈 수 있는 시스템입니다.

### 🎯 주요 기능
- **30초 대기 시간**: 재접속 시간 제공
- **상태 복원**: 역할, 생존 여부, 정보 복원
- **채팅 히스토리**: 놓친 메시지 제공
- **자동 타임아웃**: 시간 초과 시 자동 제거

### 💻 사용 방법

**서버 측 통합:**
```java
import mafia.game.features.ReconnectionManager;
import mafia.game.features.ReconnectionManager.PlayerGameState;
import mafia.game.features.ReconnectionManager.ReconnectionResult;

// 매니저 인스턴스
ReconnectionManager reconnectionManager = ReconnectionManager.getInstance();

// 연결 끊김 처리
PlayerGameState state = new PlayerGameState(
    playerRole,
    isAlive,
    knownInformation,
    chatHistory
);
reconnectionManager.handleDisconnection(playerName, state);

// 재접속 시도
ReconnectionResult result = reconnectionManager.attemptReconnection(playerName);
if (result.isSuccess()) {
    PlayerGameState restoredState = result.getState();
    // 상태 복원
    restorePlayerState(playerName, restoredState);
}

// 리스너 등록
reconnectionManager.addReconnectionListener(
    new ReconnectionManager.ReconnectionListener() {
        @Override
        public void onPlayerDisconnected(String playerName) {
            broadcastMessage("SYSTEM: " + playerName + "님의 연결이 끊겼습니다. (30초 대기)");
        }

        @Override
        public void onPlayerReconnected(String playerName, PlayerGameState state) {
            broadcastMessage("SYSTEM: " + playerName + "님이 재접속했습니다!");
        }

        @Override
        public void onReconnectionTimeout(String playerName) {
            broadcastMessage("SYSTEM: " + playerName + "님�� 타임아웃되었습니다.");
            // AI로 대체하거나 제거
        }
    }
);
```

### 🔄 재접속 프로세스
1. 플레이어 연결 끊김 감지
2. 게임 상태 저장 (Memento 패턴)
3. 30초 타이머 시작
4. 재접속 시 상태 복원
5. 타임아웃 시 자동 처리

### 📊 프로토콜
```
서버 -> 클라이언트: RECONNECT_WAIT:30
클라이언트 -> 서버: RECONNECT_ATTEMPT:[플레이어명]
서버 -> 클라이언트: RECONNECT_SUCCESS:[복원 데이터]
```

---

## 9. 이벤트 모드 시스템

### 📝 개요
시즌별 특별 이벤트와 테마를 제공하는 시스템입니다.

### 🎯 주요 기능
- **4가지 시즌 이벤트**: 할로윈, 크리스마스, 설날, 여름
- **특별 역할**: 이벤트 전용 역할
- **룰 변경**: 특수 규칙 적용
- **테마 적용**: 색상, 사운드 변경

### 🎃 이벤트 목록

**할로윈 (10월)**
- 뱀파이어: 플레이어를 뱀파이어로 변환
- 늑대인간: 보름달에 2명 공격
- 유령: 죽어도 능력 1회 사용

**🎄 크리스마스 (12월)**
- 산타: 아이템 선물
- 루돌프: 산타 보호
- 그린치: 마피아 팀

**🎊 설날 (1~2월)**
- 점쟁이: 운세 보기
- 조상님: 유언 남기기

**🏖️ 여름 (7~8월)**
- 라이프가드: 익사 방지
- 상어: 은밀한 공격

### 💻 사용 방법

**서버 측 통합:**
```java
import mafia.game.events.EventModeManager;
import mafia.game.events.EventModeManager.EventMode;

// 매니저 인스턴스
EventModeManager eventManager = EventModeManager.getInstance();

// 현재 활성 이벤트 확인
EventMode currentEvent = eventManager.getCurrentEvent();
if (currentEvent != null) {
    System.out.println("활성 이벤트: " + currentEvent.getName());
    System.out.println(currentEvent.toString());

    // 특별 역할 추가
    Map<String, EventModeManager.SpecialRole> specialRoles =
        currentEvent.getSpecialRoles();

    // 룰 변경 적용
    Map<String, String> ruleModifiers = currentEvent.getRuleModifiers();
    if (ruleModifiers.containsKey("NIGHT_DURATION")) {
        int nightDuration = Integer.parseInt(ruleModifiers.get("NIGHT_DURATION"));
        // 밤 시간 변경
    }

    // 테마 적용
    String primaryColor = currentEvent.getPrimaryColor();
    String soundPath = currentEvent.getThemeSoundPath();
}

// 수동 이벤트 활성화
eventManager.activateEvent("HALLOWEEN");

// 이벤트 비활성화
eventManager.deactivateEvent();
```

### 🎨 커스텀 이벤트 추가
```java
EventMode customEvent = new EventMode.Builder("CUSTOM", "커스텀 이벤트")
    .description("나만의 특별한 이벤트")
    .activePeriod(Month.JUNE, 1, Month.JUNE, 30)
    .addSpecialRole("CUSTOM_ROLE", "커스텀 역할", "특별한 능력")
    .addRuleModifier("CUSTOM_RULE", "true")
    .addThemeColor("#FF00FF", "#00FF00")
    .build();

eventManager.registerEvent(customEvent);
```

---

## 🔧 전역 설정

모든 기능은 개별적으로 활성화/비활성화할 수 있습니다:

```java
WhisperManager.getInstance().setEnabled(false);
LobbyManager.getInstance().setEnabled(false);
StatisticsManager.getInstance().setEnabled(false);
VoteTracker.getInstance().setMode(VoteMode.ANONYMOUS);
RoleGuideManager.getInstance().setEnabled(true);
EmotionManager.getInstance().setEnabled(true);
ReconnectionManager.getInstance().setEnabled(true);
EventModeManager.getInstance().setEnabled(true);
```

---

## 📚 아키텍처 패턴

모든 기능은 다음 소프트웨어 공학 원리를 따릅니다:

### 디자인 패턴
- **Singleton Pattern**: 모든 매니저 클래스
- **Observer Pattern**: 이벤트 리스너 시스템
- **Strategy Pattern**: 역할별/난이도별 전략
- **Builder Pattern**: 복잡한 객체 생성
- **Factory Pattern**: AI 플레이어 생성
- **Memento Pattern**: 재접속 상태 저장

### SOLID 원칙
- **Single Responsibility**: 각 클래스는 하나의 책임만
- **Open/Closed**: 확장에는 열려있고 수정에는 닫혀있음
- **Liskov Substitution**: 상속 계층 일관성
- **Interface Segregation**: 필요한 인터페이스만 구현
- **Dependency Inversion**: 추상화에 의존

### 확장성
- **모듈화**: 독립적인 기능 모듈
- **플러그인 구조**: 기존 코드 수정 없이 추가
- **설정 가능**: 모든 상수는 변경 가능

---

## 🚀 빠른 시작 예제

### 모든 기능 활성화
```java
public void initializeAllFeatures() {
    // 1. 익명 쪽지
    WhisperManager whisperManager = WhisperManager.getInstance();

    // 2. 로비
    LobbyManager lobbyManager = LobbyManager.getInstance();
    lobbyManager.startLobby();

    // 3. 통계
    StatisticsManager statsManager = StatisticsManager.getInstance();

    // 4. 투표 트래커
    VoteTracker voteTracker = VoteTracker.getInstance();

    // 5. 가이드
    RoleGuideManager guideManager = RoleGuideManager.getInstance();

    // 6. 감정 표현
    EmotionManager emotionManager = EmotionManager.getInstance();

    // 7. 재접속
    ReconnectionManager reconnectionManager = ReconnectionManager.getInstance();

    // 8. 이벤트 모드
    EventModeManager eventManager = EventModeManager.getInstance();
}
```

---

## 📞 지원

문제가 발생하거나 질문이 있으시면:
- GitHub Issues: https://github.com/khg9859/Mafia/issues
- 문서: README.md 참조

---

**버전**: 2.0
**최종 업데이트**: 2025-12-03
