# 마피아 게임 - 아키텍처 문서

## 📁 프로젝트 구조

```
마피아2/
├── src/main/java/mafia/game/
│   ├── MafiaGameServer.java          # 서버 메인 클래스 (2917줄)
│   ├── MafiaGameClientMain.java      # 클라이언트 로그인 화면
│   ├── MafiaGameClientView.java      # 클라이언트 게임 화면
│   │
│   ├── models/                        # 데이터 모델
│   │   ├── PlayerStatistics.java     # 플레이어 통계 데이터
│   │   └── Message.java               # 메시지 데이터 모델
│   │
│   ├── features/                      # 기능 모듈
│   │   ├── WhisperManager.java        # 익명 쪽지 시스템
│   │   ├── LobbyManager.java          # 프리게임 로비
│   │   ├── StatisticsManager.java     # 통계/업적 시스템
│   │   ├── VoteTracker.java           # 라이브 투표 집계
│   │   ├── RoleGuideManager.java      # 역할 가이드
│   │   ├── EmotionManager.java        # 감정 표현 시스템
│   │   └── ReconnectionManager.java   # 재접속 관리
│   │
│   ├── ai/                            # AI 시스템
│   │   ├── AIPlayer.java              # AI 추상 클래스
│   │   └── BasicAIPlayer.java         # 기본 AI 구현
│   │
│   └── events/                        # 이벤트 시스템
│       └── EventModeManager.java      # 시즌 이벤트 관리
│
├── src/main/resources/
│   ├── GameSound/                     # 게임 사운드
│   └── info/                          # 이미지 리소스
│
├── pom.xml                            # Maven 설정
├── README.md                          # 프로젝트 문서
├── NEW_FEATURES.md                    # 신규 기능 가이드
└── ARCHITECTURE.md                    # 이 파일
```

## 🏗️ 아키텍처 설계

### 레이어 구조

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│   (MafiaGameServer, ClientView)         │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│          Business Layer                 │
│     (Features, AI, Events)              │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│           Data Layer                    │
│          (Models, Persistence)          │
└─────────────────────────────────────────┘
```

### 주요 디자인 패턴

#### 1. Singleton Pattern
모든 매니저 클래스는 Singleton 패턴을 사용합니다.

```java
public class WhisperManager {
    private static WhisperManager instance;

    public static synchronized WhisperManager getInstance() {
        if (instance == null) {
            instance = new WhisperManager();
        }
        return instance;
    }

    private WhisperManager() { /* 초기화 */ }
}
```

**적용된 클래스:**
- WhisperManager
- LobbyManager
- StatisticsManager
- VoteTracker
- RoleGuideManager
- EmotionManager
- ReconnectionManager
- EventModeManager

#### 2. Observer Pattern
이벤트 기반 아키텍처를 위한 리스너 시스템

```java
public interface LobbyStateListener {
    void onStateChanged(LobbyState newState);
}

public class LobbyManager {
    private List<LobbyStateListener> listeners;

    public void addStateListener(LobbyStateListener listener) {
        listeners.add(listener);
    }

    private void notifyStateChange(LobbyState newState) {
        for (LobbyStateListener listener : listeners) {
            listener.onStateChanged(newState);
        }
    }
}
```

**적용된 클래스:**
- LobbyManager (상태 변화)
- VoteTracker (투표 변화)
- EmotionManager (감정 표현)
- ReconnectionManager (연결 상태)

#### 3. Strategy Pattern
역할별, 난이도별 다양한 전략 구현

```java
public abstract class AIPlayer {
    public abstract String decideNightAction(List<String> players);
    public abstract String decideVote(List<String> players);
}

public class MafiaAIPlayer extends AIPlayer {
    @Override
    public String decideNightAction(List<String> players) {
        // 마피아 전용 전략
    }
}
```

**적용된 클래스:**
- AIPlayer (난이도별 전략)
- EventMode (이벤트별 룰)

#### 4. Builder Pattern
복잡한 객체 생성을 위한 빌더

```java
RoleGuide guide = new RoleGuide.Builder("MAFIA", "마피아")
    .team("마피아")
    .description("설명")
    .ability("능력")
    .addStrategy("전략1")
    .addTip("팁1")
    .build();
```

**적용된 클래스:**
- RoleGuide
- Message
- EventMode

#### 5. Factory Pattern
객체 생성 추상화

```java
public class AIPlayerFactory {
    public static AIPlayer createAI(String role, Difficulty difficulty) {
        switch (role) {
            case "MAFIA": return new MafiaAIPlayer(difficulty);
            case "DOCTOR": return new DoctorAIPlayer(difficulty);
            default: return new BasicAIPlayer(difficulty);
        }
    }
}
```

#### 6. Memento Pattern
상태 저장 및 복원 (재접속)

```java
public class PlayerGameState {
    private final String role;
    private final boolean alive;
    private final Map<String, String> knownInfo;

    // Immutable state object
}

public class ReconnectionManager {
    public void handleDisconnection(String player, PlayerGameState state) {
        // 상태 저장
    }

    public PlayerGameState restore(String player) {
        // 상태 복원
        return savedState;
    }
}
```

## 🔌 모듈 간 통신

### 서버 통합 예제

```java
public class MafiaGameServer extends JFrame {
    // 매니저 인스턴스
    private WhisperManager whisperManager;
    private LobbyManager lobbyManager;
    private StatisticsManager statsManager;
    private VoteTracker voteTracker;
    private EmotionManager emotionManager;
    private ReconnectionManager reconnectionManager;
    private EventModeManager eventManager;

    public MafiaGameServer() {
        initializeManagers();
        setupListeners();
    }

    private void initializeManagers() {
        whisperManager = WhisperManager.getInstance();
        lobbyManager = LobbyManager.getInstance();
        statsManager = StatisticsManager.getInstance();
        voteTracker = VoteTracker.getInstance();
        emotionManager = EmotionManager.getInstance();
        reconnectionManager = ReconnectionManager.getInstance();
        eventManager = EventModeManager.getInstance();
    }

    private void setupListeners() {
        // 로비 상태 리스너
        lobbyManager.addStateListener(state -> {
            if (state == LobbyState.READY) {
                startGame();
            }
        });

        // 투표 변화 리스너
        voteTracker.addVoteChangeListener(new VoteChangeListener() {
            @Override
            public void onVoteChanged(String voter, String target,
                                     boolean isRevote, double voteRate) {
                broadcastVoteUpdate(target, voteRate);
            }
        });

        // 재접속 리스너
        reconnectionManager.addReconnectionListener(
            new ReconnectionListener() {
                @Override
                public void onPlayerReconnected(String player,
                                               PlayerGameState state) {
                    restorePlayerState(player, state);
                }
            }
        );
    }
}
```

## 📊 데이터 흐름

### 투표 시스템 예제

```
Client                  Server                  VoteTracker
  │                       │                         │
  │──VOTE:Player1──────>  │                         │
  │                       │──castVote()──────────>  │
  │                       │                         │
  │                       │  <──VoteCastResult───   │
  │                       │                         │
  │                       │  ──notifyListeners──>   │
  │  <──VOTE_UPDATE───────│                         │
  │                       │                         │
```

### 재접속 프로세스

```
Client              Server              ReconnectionManager
  │                    │                         │
  │──[연결끊김]──────>  │                         │
  │                    │──handleDisconnection──> │
  │                    │                         │
  │                    │    [상태저장]            │
  │                    │    [타이머시작:30초]      │
  │                    │                         │
  │──RECONNECT──────>  │                         │
  │                    │──attemptReconnection──> │
  │                    │  <──ReconnectionResult─ │
  │                    │    [상태복원]            │
  │  <──STATE_DATA─────│                         │
```

## 🔐 Thread Safety

모든 매니저 클래스는 Thread-Safe 설계:

```java
// ConcurrentHashMap 사용
private final Map<String, Message> messages = new ConcurrentHashMap<>();

// Collections.synchronizedList 사용
private final List<Event> history =
    Collections.synchronizedList(new ArrayList<>());

// synchronized 메소드
public static synchronized Manager getInstance() {
    if (instance == null) {
        instance = new Manager();
    }
    return instance;
}
```

## 📝 확장 가능성

### 새로운 기능 추가 방법

#### 1. 새 매니저 추가

```java
// 1. Singleton 패턴으로 매니저 생성
public class CustomFeatureManager {
    private static CustomFeatureManager instance;

    public static synchronized CustomFeatureManager getInstance() {
        if (instance == null) {
            instance = new CustomFeatureManager();
        }
        return instance;
    }

    private CustomFeatureManager() {
        // 초기화
    }

    // 기능 메소드
    public void doSomething() {
        // 구현
    }
}

// 2. 서버에 통합
public class MafiaGameServer {
    private CustomFeatureManager customManager;

    public void initialize() {
        customManager = CustomFeatureManager.getInstance();
    }
}
```

#### 2. 새 AI 역할 추가

```java
public class CustomRoleAI extends AIPlayer {
    public CustomRoleAI(String name, Difficulty difficulty) {
        super(name, difficulty);
    }

    @Override
    public String decideNightAction(List<String> alivePlayers) {
        // 커스텀 로직
        return chooseRandom(alivePlayers);
    }

    @Override
    public String decideVote(List<String> alivePlayers) {
        // 커스텀 로직
        return findMostSuspicious(alivePlayers);
    }

    @Override
    public String generateStatement(GameContext context) {
        // 커스텀 발언
        return "커스텀 메시지";
    }
}
```

#### 3. 새 이벤트 추가

```java
EventMode newEvent = new EventMode.Builder("NEW_EVENT", "새 이벤트")
    .description("설명")
    .activePeriod(Month.APRIL, 1, Month.APRIL, 30)
    .addSpecialRole("NEW_ROLE", "새 역할", "능력 설명")
    .addRuleModifier("NEW_RULE", "value")
    .addThemeColor("#ABCDEF", "#123456")
    .build();

EventModeManager.getInstance().registerEvent(newEvent);
```

## 🧪 테스트 전략

### 단위 테스트 예제

```java
@Test
public void testWhisperManager() {
    WhisperManager manager = WhisperManager.getInstance();
    manager.startDayPhase();

    // 정상 쪽지 전송
    WhisperResult result = manager.sendWhisper("P1", "P2", "Test");
    assertTrue(result.isSuccess());

    // 스팸 방지 테스트
    for (int i = 0; i < 11; i++) {
        manager.sendWhisper("P1", "P2", "Spam" + i);
    }
    WhisperResult spamResult = manager.sendWhisper("P1", "P2", "Spam11");
    assertFalse(spamResult.isSuccess());
}
```

### 통합 테스트 예제

```java
@Test
public void testGameFlow() {
    // 1. 로비 시작
    LobbyManager lobby = LobbyManager.getInstance();
    lobby.startLobby();
    lobby.addPlayer("P1");
    lobby.addPlayer("P2");
    lobby.setReady("P1", true);
    lobby.setReady("P2", true);

    // 2. 게임 시작
    Map<String, String> players = new HashMap<>();
    players.put("P1", "MAFIA");
    players.put("P2", "DOCTOR");
    StatisticsManager.getInstance().startGame(players);

    // 3. 투표
    VoteTracker tracker = VoteTracker.getInstance();
    tracker.startVoting(
        Set.of("P1", "P2"),
        Set.of("P1", "P2"),
        20,
        VoteMode.PUBLIC
    );

    tracker.castVote("P1", "P2");
    tracker.castVote("P2", "P1");

    VoteResult result = tracker.endVoting();
    assertTrue(result.isTie());

    // 4. 게임 종료
    StatisticsManager.getInstance().endGame("CITIZEN", "P2");
}
```

## 📈 성능 최적화

### 1. 메모리 관리
```java
// 히스토리 크기 제한
private static final int MAX_HISTORY_SIZE = 100;

private void addToHistory(Event event) {
    history.add(event);
    if (history.size() > MAX_HISTORY_SIZE) {
        history.remove(0);  // 오래된 항목 제거
    }
}
```

### 2. 비동기 처리
```java
// ScheduledExecutorService 사용
private final ScheduledExecutorService scheduler =
    Executors.newScheduledThreadPool(2);

scheduler.schedule(() -> {
    handleTimeout(playerName);
}, 30, TimeUnit.SECONDS);
```

### 3. 캐싱
```java
// 자주 조회되는 데이터 캐싱
private Map<String, PlayerStatistics> statisticsCache = new ConcurrentHashMap<>();

public PlayerStatistics getStatistics(String playerName) {
    return statisticsCache.computeIfAbsent(playerName, this::loadFromFile);
}
```

## 🔒 보안 고려사항

### 1. 입력 검증
```java
public WhisperResult sendWhisper(String sender, String receiver, String content) {
    // Null 체크
    if (sender == null || receiver == null || content == null) {
        return WhisperResult.error("Invalid input");
    }

    // 길이 제한
    if (content.length() > 200) {
        return WhisperResult.error("Message too long");
    }

    // XSS 방지
    content = sanitize(content);

    // ...
}
```

### 2. Rate Limiting
```java
// 쿨다운 시스템
private static final long COOLDOWN_MS = 3000;

private boolean checkCooldown(String player) {
    Long lastUsed = cooldowns.get(player);
    if (lastUsed != null) {
        return (System.currentTimeMillis() - lastUsed) >= COOLDOWN_MS;
    }
    return true;
}
```

### 3. 데이터 직렬화 보안
```java
// Serializable 버전 관리
private static final long serialVersionUID = 1L;

// 안전한 파일명
private String sanitizeFilename(String filename) {
    return filename.replaceAll("[^a-zA-Z0-9가-힣_-]", "_");
}
```

## 📚 참고 자료

### 디자인 패턴
- Gang of Four: Design Patterns
- Head First Design Patterns

### 소프트웨어 공학
- Clean Code by Robert C. Martin
- Effective Java by Joshua Bloch
- SOLID Principles

### 게임 개발
- Game Programming Patterns by Robert Nystrom

---

**작성자**: Mafia Game Team
**버전**: 2.0
**최종 업데이트**: 2025-12-03
