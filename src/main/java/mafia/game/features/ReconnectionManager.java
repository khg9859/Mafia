package mafia.game.features;

import java.util.*;
import java.util.concurrent.*;

/**
 * 재접속 시스템 관리자
 *
 * 이 클래스는 플레이어의 연결 끊김 및 재접속을 관리합니다.
 *
 * 설계 원칙:
 * - Singleton Pattern: 하나의 매니저만 존재
 * - Memento Pattern: 플레이어 상태 저장 및 복원
 * - Observer Pattern: 연결 상태 변화 통지
 *
 * 기능:
 * - 연결 끊김 감지
 * - 재접속 대기 시간 관리
 * - 게임 상태 복원
 * - AI 대체 플레이어
 *
 * @author Mafia Game Team
 * @version 2.0
 */
public class ReconnectionManager {

    // ========================================
    // Singleton 패턴
    // ========================================

    private static ReconnectionManager instance;

    /**
     * Singleton 인스턴스 조회
     *
     * @return ReconnectionManager 인스턴스
     */
    public static synchronized ReconnectionManager getInstance() {
        if (instance == null) {
            instance = new ReconnectionManager();
        }
        return instance;
    }

    // ========================================
    // 필드
    // ========================================

    /**
     * 연결 끊긴 플레이어 정보 (이름 -> 상태)
     */
    private final Map<String, DisconnectedPlayer> disconnectedPlayers;

    /**
     * 재접속 대기 타이머
     */
    private final ScheduledExecutorService scheduler;

    /**
     * 재접속 리스너
     */
    private final List<ReconnectionListener> listeners;

    /**
     * 기능 활성화 여부
     */
    private boolean enabled;

    // ========================================
    // 설정 상수
    // ========================================

    /**
     * 재접속 대기 시간 (초)
     */
    private static final int RECONNECTION_TIMEOUT_SECONDS = 30;

    /**
     * 연결 확인 간격 (초)
     */
    private static final int CONNECTION_CHECK_INTERVAL_SECONDS = 5;

    // ========================================
    // 생성자
    // ========================================

    /**
     * private 생성자 (Singleton)
     */
    private ReconnectionManager() {
        this.disconnectedPlayers = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.listeners = new ArrayList<>();
        this.enabled = true;
    }

    // ========================================
    // 연결 끊김 처리
    // ========================================

    /**
     * 플레이어 연결 끊김 처리
     *
     * @param playerName 플레이어 이름
     * @param gameState 현재 게임 상태
     */
    public void handleDisconnection(String playerName, PlayerGameState gameState) {
        if (!enabled) {
            return;
        }

        // 이미 재접속 대기 중인 경우 무시
        if (disconnectedPlayers.containsKey(playerName)) {
            return;
        }

        // 연결 끊긴 플레이어 등록
        DisconnectedPlayer disconnected = new DisconnectedPlayer(
            playerName,
            gameState,
            System.currentTimeMillis()
        );

        disconnectedPlayers.put(playerName, disconnected);

        // 리스너 통지
        notifyDisconnected(playerName);

        // 재접속 타임아웃 스케줄링
        scheduleTimeout(playerName);
    }

    /**
     * 재접속 타임아웃 스케줄링
     *
     * @param playerName 플레이어 이름
     */
    private void scheduleTimeout(String playerName) {
        scheduler.schedule(() -> {
            DisconnectedPlayer disconnected = disconnectedPlayers.get(playerName);

            if (disconnected != null && !disconnected.isReconnected()) {
                // 타임아웃 처리
                handleTimeout(playerName);
            }
        }, RECONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 재접속 타임아웃 처리
     *
     * @param playerName 플레이어 이름
     */
    private void handleTimeout(String playerName) {
        disconnectedPlayers.remove(playerName);

        // 리스너 통지
        notifyTimeout(playerName);
    }

    // ========================================
    // 재접속 처리
    // ========================================

    /**
     * 플레이어 재접속 시도
     *
     * @param playerName 플레이어 이름
     * @return 재접속 결과
     */
    public ReconnectionResult attemptReconnection(String playerName) {
        if (!enabled) {
            return ReconnectionResult.error("재접속 기능이 비활성화되어 있습니다.");
        }

        DisconnectedPlayer disconnected = disconnectedPlayers.get(playerName);

        if (disconnected == null) {
            return ReconnectionResult.error("재접속 대기 중인 플레이어가 아닙니다.");
        }

        // 타임아웃 체크
        long elapsed = System.currentTimeMillis() - disconnected.getDisconnectTime();
        if (elapsed > RECONNECTION_TIMEOUT_SECONDS * 1000) {
            disconnectedPlayers.remove(playerName);
            return ReconnectionResult.error("재접속 시간이 초과되었습니다.");
        }

        // 재접속 성공
        disconnected.markReconnected();
        PlayerGameState restoredState = disconnected.getGameState();

        // 리스너 통지
        notifyReconnected(playerName, restoredState);

        // 목록에서 제거
        disconnectedPlayers.remove(playerName);

        return ReconnectionResult.success(restoredState);
    }

    // ========================================
    // 상태 조회
    // ========================================

    /**
     * 재접속 대기 중인 플레이어 확인
     *
     * @param playerName 플레이어 이름
     * @return 대기 여부
     */
    public boolean isWaitingForReconnection(String playerName) {
        return disconnectedPlayers.containsKey(playerName);
    }

    /**
     * 남은 재접속 시간 조회 (초)
     *
     * @param playerName 플레이어 이름
     * @return 남은 시간 (초), 없으면 -1
     */
    public int getRemainingReconnectionTime(String playerName) {
        DisconnectedPlayer disconnected = disconnectedPlayers.get(playerName);

        if (disconnected == null) {
            return -1;
        }

        long elapsed = System.currentTimeMillis() - disconnected.getDisconnectTime();
        long remaining = RECONNECTION_TIMEOUT_SECONDS * 1000 - elapsed;

        return Math.max(0, (int) (remaining / 1000));
    }

    /**
     * 재접속 대기 중인 플레이어 목록
     *
     * @return 플레이어 이름 목록
     */
    public List<String> getDisconnectedPlayers() {
        return new ArrayList<>(disconnectedPlayers.keySet());
    }

    // ========================================
    // Observer 패턴 - 리스너 관리
    // ========================================

    /**
     * 재접속 리스너 추가
     *
     * @param listener 리스너
     */
    public void addReconnectionListener(ReconnectionListener listener) {
        listeners.add(listener);
    }

    /**
     * 재접속 리스너 제거
     *
     * @param listener 리스너
     */
    public void removeReconnectionListener(ReconnectionListener listener) {
        listeners.remove(listener);
    }

    /**
     * 연결 끊김 통지
     *
     * @param playerName 플레이어 이름
     */
    private void notifyDisconnected(String playerName) {
        for (ReconnectionListener listener : listeners) {
            listener.onPlayerDisconnected(playerName);
        }
    }

    /**
     * 재접속 통지
     *
     * @param playerName 플레이어 이름
     * @param state 복원된 상태
     */
    private void notifyReconnected(String playerName, PlayerGameState state) {
        for (ReconnectionListener listener : listeners) {
            listener.onPlayerReconnected(playerName, state);
        }
    }

    /**
     * 타임아웃 통지
     *
     * @param playerName 플레이어 이름
     */
    private void notifyTimeout(String playerName) {
        for (ReconnectionListener listener : listeners) {
            listener.onReconnectionTimeout(playerName);
        }
    }

    // ========================================
    // 설정 메소드
    // ========================================

    /**
     * 기능 활성화/비활성화
     *
     * @param enabled 활성화 여부
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 기능 활성화 상태 조회
     *
     * @return 활성화 여부
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 리셋 (게임 종료 시)
     */
    public void reset() {
        disconnectedPlayers.clear();
    }

    /**
     * 종료 (서버 종료 시)
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    // ========================================
    // 내부 클래스 - DisconnectedPlayer
    // ========================================

    /**
     * 연결 끊긴 플레이어 정보
     */
    private static class DisconnectedPlayer {
        private final String playerName;
        private final PlayerGameState gameState;
        private final long disconnectTime;
        private boolean reconnected;

        public DisconnectedPlayer(String playerName, PlayerGameState gameState, long disconnectTime) {
            this.playerName = playerName;
            this.gameState = gameState;
            this.disconnectTime = disconnectTime;
            this.reconnected = false;
        }

        public String getPlayerName() {
            return playerName;
        }

        public PlayerGameState getGameState() {
            return gameState;
        }

        public long getDisconnectTime() {
            return disconnectTime;
        }

        public boolean isReconnected() {
            return reconnected;
        }

        public void markReconnected() {
            this.reconnected = true;
        }
    }

    // ========================================
    // 내부 클래스 - PlayerGameState
    // ========================================

    /**
     * 플레이어 게임 상태 (Memento 패턴)
     */
    public static class PlayerGameState {
        private final String role;
        private final boolean alive;
        private final Map<String, String> knownInformation;
        private final List<String> chatHistory;

        public PlayerGameState(String role, boolean alive,
                              Map<String, String> knownInformation,
                              List<String> chatHistory) {
            this.role = role;
            this.alive = alive;
            this.knownInformation = new HashMap<>(knownInformation);
            this.chatHistory = new ArrayList<>(chatHistory);
        }

        public String getRole() {
            return role;
        }

        public boolean isAlive() {
            return alive;
        }

        public Map<String, String> getKnownInformation() {
            return new HashMap<>(knownInformation);
        }

        public List<String> getChatHistory() {
            return new ArrayList<>(chatHistory);
        }
    }

    // ========================================
    // 내부 클래스 - ReconnectionResult
    // ========================================

    /**
     * 재접속 결과
     */
    public static class ReconnectionResult {
        private final boolean success;
        private final String message;
        private final PlayerGameState state;

        private ReconnectionResult(boolean success, String message, PlayerGameState state) {
            this.success = success;
            this.message = message;
            this.state = state;
        }

        public static ReconnectionResult success(PlayerGameState state) {
            return new ReconnectionResult(true, "재접속에 성공했습니다.", state);
        }

        public static ReconnectionResult error(String message) {
            return new ReconnectionResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public PlayerGameState getState() {
            return state;
        }
    }

    // ========================================
    // 인터페이스 - ReconnectionListener
    // ========================================

    /**
     * 재접속 리스너 인터페이스
     */
    public interface ReconnectionListener {
        /**
         * 플레이어 연결 끊김 시 호출
         *
         * @param playerName 플레이어 이름
         */
        void onPlayerDisconnected(String playerName);

        /**
         * 플레이어 재접속 시 호출
         *
         * @param playerName 플레이어 이름
         * @param state 복원된 상태
         */
        void onPlayerReconnected(String playerName, PlayerGameState state);

        /**
         * 재접속 타임아웃 시 호출
         *
         * @param playerName 플레이어 이름
         */
        void onReconnectionTimeout(String playerName);
    }
}
