package mafia.game.features;

import mafia.game.models.Message;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 프리게임 로비 시스템 관리자
 *
 * 이 클래스는 게임 시작 전 로비 기능을 관리합니다.
 * 플레이어들이 자유롭게 대화하고 준비 상태를 관리합니다.
 *
 * 설계 원칙:
 * - Singleton Pattern: 하나의 로비 매니저만 존재
 * - Observer Pattern: 로비 상태 변화를 리스너에게 통지
 * - State Pattern: 로비 상태 관리
 *
 * 기능:
 * - 로비 채팅
 * - 플레이어 준비 상태 관리
 * - 역할 선호도 설정
 * - 자동 게임 시작
 *
 * @author Mafia Game Team
 * @version 2.0
 */
public class LobbyManager {

    // ========================================
    // Singleton 패턴
    // ========================================

    private static LobbyManager instance;

    /**
     * Singleton 인스턴스 조회
     *
     * @return LobbyManager 인스턴스
     */
    public static synchronized LobbyManager getInstance() {
        if (instance == null) {
            instance = new LobbyManager();
        }
        return instance;
    }

    // ========================================
    // 필드
    // ========================================

    /**
     * 로비 상태
     */
    private LobbyState state;

    /**
     * 플레이어 준비 상태 (이름 -> 준비 여부)
     */
    private final Map<String, Boolean> readyStatus;

    /**
     * 플레이어 역할 선호도 (이름 -> 선호 역할 리스트)
     */
    private final Map<String, List<String>> rolePreferences;

    /**
     * 로비 채팅 히스토리
     */
    private final List<Message> chatHistory;

    /**
     * 로비 시작 시간
     */
    private long lobbyStartTime;

    /**
     * 로비 상태 변화 리스너
     */
    private final List<LobbyStateListener> listeners;

    /**
     * 기능 활성화 여부
     */
    private boolean enabled;

    // ========================================
    // 설정 상수
    // ========================================

    /**
     * 로비 대기 시간 (초)
     */
    private static final int LOBBY_DURATION_SECONDS = 30;

    /**
     * 최소 플레이어 수
     */
    private static final int MIN_PLAYERS = 4;

    // ========================================
    // 로비 상태 Enum
    // ========================================

    /**
     * 로비 상태 열거형
     */
    public enum LobbyState {
        WAITING,        // 플레이어 대기 중
        READY,          // 게임 시작 준비 완료
        COUNTDOWN,      // 카운트다운 중
        CLOSED          // 로비 종료 (게임 시작)
    }

    // ========================================
    // 생성자
    // ========================================

    /**
     * private 생성자 (Singleton)
     */
    private LobbyManager() {
        this.state = LobbyState.WAITING;
        this.readyStatus = new ConcurrentHashMap<>();
        this.rolePreferences = new ConcurrentHashMap<>();
        this.chatHistory = Collections.synchronizedList(new ArrayList<>());
        this.listeners = new ArrayList<>();
        this.enabled = true;
    }

    // ========================================
    // 로비 시작/종료
    // ========================================

    /**
     * 로비 시작
     */
    public void startLobby() {
        if (!enabled) {
            return;
        }

        this.state = LobbyState.WAITING;
        this.lobbyStartTime = System.currentTimeMillis();
        this.readyStatus.clear();
        this.rolePreferences.clear();
        this.chatHistory.clear();

        notifyStateChange(state);
    }

    /**
     * 로비 종료
     */
    public void closeLobby() {
        this.state = LobbyState.CLOSED;
        notifyStateChange(state);
    }

    /**
     * 로비 리셋
     */
    public void reset() {
        this.state = LobbyState.WAITING;
        this.readyStatus.clear();
        this.rolePreferences.clear();
        this.chatHistory.clear();
    }

    // ========================================
    // 플레이어 관리
    // ========================================

    /**
     * 플레이어 추가
     *
     * @param playerName 플레이어 이름
     */
    public void addPlayer(String playerName) {
        readyStatus.put(playerName, false);
        checkReadyState();
    }

    /**
     * 플레이어 제거
     *
     * @param playerName 플레이어 이름
     */
    public void removePlayer(String playerName) {
        readyStatus.remove(playerName);
        rolePreferences.remove(playerName);
        checkReadyState();
    }

    /**
     * 플레이어 준비 상태 설정
     *
     * @param playerName 플레이어 이름
     * @param ready 준비 여부
     */
    public void setReady(String playerName, boolean ready) {
        if (readyStatus.containsKey(playerName)) {
            readyStatus.put(playerName, ready);
            checkReadyState();
        }
    }

    /**
     * 플레이어 준비 상태 조회
     *
     * @param playerName 플레이어 이름
     * @return 준비 여부
     */
    public boolean isReady(String playerName) {
        return readyStatus.getOrDefault(playerName, false);
    }

    /**
     * 모든 플레이어가 준비되었는지 확인
     *
     * @return 모두 준비 여부
     */
    public boolean isAllReady() {
        if (readyStatus.size() < MIN_PLAYERS) {
            return false;
        }

        return readyStatus.values().stream().allMatch(ready -> ready);
    }

    /**
     * 준비 상태 체크 및 상태 전환
     */
    private void checkReadyState() {
        if (state == LobbyState.WAITING && isAllReady()) {
            // 모두 준비 완료 -> 카운트다운 시작
            startCountdown();
        } else if (state == LobbyState.COUNTDOWN && !isAllReady()) {
            // 준비 취소된 플레이어 있음 -> 대기 상태로 복귀
            cancelCountdown();
        }
    }

    /**
     * 카운트다운 시작
     */
    private void startCountdown() {
        this.state = LobbyState.COUNTDOWN;
        notifyStateChange(state);

        // 5초 후 게임 시작
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                if (state == LobbyState.COUNTDOWN) {
                    this.state = LobbyState.READY;
                    notifyStateChange(state);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 카운트다운 취소
     */
    private void cancelCountdown() {
        this.state = LobbyState.WAITING;
        notifyStateChange(state);
    }

    // ========================================
    // 역할 선호도 관리
    // ========================================

    /**
     * 역할 선호도 설정
     *
     * @param playerName 플레이어 이름
     * @param preferredRoles 선호 역할 리스트 (최대 3개)
     */
    public void setRolePreferences(String playerName, List<String> preferredRoles) {
        if (preferredRoles.size() > 3) {
            preferredRoles = preferredRoles.subList(0, 3);
        }
        rolePreferences.put(playerName, new ArrayList<>(preferredRoles));
    }

    /**
     * 역할 선호도 조회
     *
     * @param playerName 플레이어 이름
     * @return 선호 역할 리스트
     */
    public List<String> getRolePreferences(String playerName) {
        return new ArrayList<>(
            rolePreferences.getOrDefault(playerName, Collections.emptyList())
        );
    }

    /**
     * 모든 플레이어의 역할 선호도 조회
     *
     * @return 역할 선호도 맵 (읽기 전용)
     */
    public Map<String, List<String>> getAllRolePreferences() {
        return new HashMap<>(rolePreferences);
    }

    // ========================================
    // 채팅 관리
    // ========================================

    /**
     * 로비 채팅 메시지 추가
     *
     * @param sender 발신자
     * @param content 내용
     */
    public void addChatMessage(String sender, String content) {
        Message message = Message.chat(sender, content);
        chatHistory.add(message);

        // 최대 100개 메시지만 유지
        if (chatHistory.size() > 100) {
            chatHistory.remove(0);
        }
    }

    /**
     * 로비 채팅 히스토리 조회
     *
     * @return 채팅 히스토리
     */
    public List<Message> getChatHistory() {
        return new ArrayList<>(chatHistory);
    }

    // ========================================
    // 상태 조회
    // ========================================

    /**
     * 현재 로비 상태 조회
     *
     * @return 로비 상태
     */
    public LobbyState getState() {
        return state;
    }

    /**
     * 로비 진행 시간 조회 (초)
     *
     * @return 경과 시간 (초)
     */
    public int getElapsedSeconds() {
        long elapsed = System.currentTimeMillis() - lobbyStartTime;
        return (int) (elapsed / 1000);
    }

    /**
     * 로비 남은 시간 조회 (초)
     *
     * @return 남은 시간 (초)
     */
    public int getRemainingSeconds() {
        int elapsed = getElapsedSeconds();
        int remaining = LOBBY_DURATION_SECONDS - elapsed;
        return Math.max(0, remaining);
    }

    /**
     * 플레이어 수 조회
     *
     * @return 플레이어 수
     */
    public int getPlayerCount() {
        return readyStatus.size();
    }

    /**
     * 준비 완료 플레이어 수 조회
     *
     * @return 준비 완료 수
     */
    public int getReadyCount() {
        return (int) readyStatus.values().stream().filter(ready -> ready).count();
    }

    // ========================================
    // Observer 패턴 - 리스너 관리
    // ========================================

    /**
     * 상태 변화 리스너 추가
     *
     * @param listener 리스너
     */
    public void addStateListener(LobbyStateListener listener) {
        listeners.add(listener);
    }

    /**
     * 상태 변화 리스너 제거
     *
     * @param listener 리스너
     */
    public void removeStateListener(LobbyStateListener listener) {
        listeners.remove(listener);
    }

    /**
     * 상태 변화 통지
     *
     * @param newState 새로운 상태
     */
    private void notifyStateChange(LobbyState newState) {
        for (LobbyStateListener listener : listeners) {
            listener.onStateChanged(newState);
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

    // ========================================
    // 통계 메소드
    // ========================================

    /**
     * 로비 정보 문자열
     *
     * @return 로비 정보
     */
    public String getLobbyInfo() {
        return String.format(
            "로비 상태: %s | 플레이어: %d/%d 준비 | 경과: %d초",
            state,
            getReadyCount(),
            getPlayerCount(),
            getElapsedSeconds()
        );
    }

    // ========================================
    // 인터페이스 - 상태 변화 리스너
    // ========================================

    /**
     * 로비 상태 변화 리스너 인터페이스
     */
    public interface LobbyStateListener {
        /**
         * 상태 변화 시 호출
         *
         * @param newState 새로운 상태
         */
        void onStateChanged(LobbyState newState);
    }
}
