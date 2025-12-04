package mafia.game.features;

import mafia.game.models.Message;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 익명 쪽지 시스템 관리자
 *
 * 이 클래스는 게임 내 익명 쪽지 기능을 관리합니다.
 * 낮 시간에만 사용 가능하며, 전략적 정보 교환을 지원합니다.
 *
 * 설계 원칙:
 * - Singleton Pattern: 하나의 매니저만 존재
 * - Thread-Safe: ConcurrentHashMap 사용
 * - Strategy Pattern: 쪽지 전송 규칙 확장 가능
 *
 * 기능:
 * - 익명 쪽지 전송 및 수신
 * - 스팸 방지 (시간당 제한)
 * - 쪽지 히스토리 관리
 *
 * @author Mafia Game Team
 * @version 2.0
 */
public class WhisperManager {

    // ========================================
    // Singleton 패턴
    // ========================================

    private static WhisperManager instance;

    /**
     * Singleton 인스턴스 조회
     *
     * @return WhisperManager 인스턴스
     */
    public static synchronized WhisperManager getInstance() {
        if (instance == null) {
            instance = new WhisperManager();
        }
        return instance;
    }

    // ========================================
    // 필드
    // ========================================

    /**
     * 쪽지 히스토리 (수신자 -> 메시지 리스트)
     * Thread-safe를 위해 ConcurrentHashMap 사용
     */
    private final Map<String, List<Message>> whisperHistory;

    /**
     * 쪽지 전송 제한 (발신자 -> 전송 시간 리스트)
     * 스팸 방지용
     */
    private final Map<String, List<Long>> sendLimits;

    /**
     * 낮 페이즈 여부
     */
    private boolean isDayPhase;

    /**
     * 기능 활성화 여부
     */
    private boolean enabled;

    // ========================================
    // 설정 상수
    // ========================================

    /**
     * 시간당 최대 전송 가능 쪽지 수
     */
    private static final int MAX_WHISPERS_PER_HOUR = 10;

    /**
     * 제한 시간 (밀리초, 1시간)
     */
    private static final long LIMIT_WINDOW_MS = 60 * 60 * 1000;

    // ========================================
    // 생성자
    // ========================================

    /**
     * private 생성자 (Singleton)
     */
    private WhisperManager() {
        this.whisperHistory = new ConcurrentHashMap<>();
        this.sendLimits = new ConcurrentHashMap<>();
        this.isDayPhase = false;
        this.enabled = true;
    }

    // ========================================
    // 쪽지 전송 메소드
    // ========================================

    /**
     * 익명 쪽지 전송
     *
     * @param sender 발신자 (실제 이름, 하지만 수신자에게는 익명으로 표시)
     * @param receiver 수신자
     * @param content 내용
     * @return 전송 결과 메시지
     */
    public WhisperResult sendWhisper(String sender, String receiver, String content) {
        // 기능 비활성화 체크
        if (!enabled) {
            return WhisperResult.error("익명 쪽지 기능이 비활성화되어 있습니다.");
        }

        // 낮 페이즈 체크
        if (!isDayPhase) {
            return WhisperResult.error("익명 쪽지는 낮 시간에만 보낼 수 있습니다.");
        }

        // 자신에게 보내기 방지
        if (sender.equals(receiver)) {
            return WhisperResult.error("자신에게는 쪽지를 보낼 수 없습니다.");
        }

        // 전송 제한 체크
        if (!checkSendLimit(sender)) {
            return WhisperResult.error(
                String.format("시간당 최대 %d개의 쪽지만 보낼 수 있습니다.", MAX_WHISPERS_PER_HOUR)
            );
        }

        // 내용 검증
        if (content == null || content.trim().isEmpty()) {
            return WhisperResult.error("쪽지 내용을 입력해주세요.");
        }

        if (content.length() > 200) {
            return WhisperResult.error("쪽지는 최대 200자까지 작성할 수 있습니다.");
        }

        // 쪽지 생성
        Message whisper = Message.whisper(receiver, content);

        // 히스토리에 추가
        whisperHistory.computeIfAbsent(receiver, k -> new ArrayList<>()).add(whisper);

        // 전송 제한 기록
        recordSend(sender);

        return WhisperResult.success(whisper);
    }

    /**
     * 전송 제한 확인
     *
     * @param sender 발신자
     * @return 전송 가능 여부
     */
    private boolean checkSendLimit(String sender) {
        List<Long> sendTimes = sendLimits.computeIfAbsent(sender, k -> new ArrayList<>());

        // 현재 시간
        long now = System.currentTimeMillis();

        // 제한 시간 이전의 전송 기록 제거
        sendTimes.removeIf(time -> now - time > LIMIT_WINDOW_MS);

        // 제한 체크
        return sendTimes.size() < MAX_WHISPERS_PER_HOUR;
    }

    /**
     * 전송 기록
     *
     * @param sender 발신자
     */
    private void recordSend(String sender) {
        sendLimits.computeIfAbsent(sender, k -> new ArrayList<>())
                  .add(System.currentTimeMillis());
    }

    // ========================================
    // 쪽지 수신 메소드
    // ========================================

    /**
     * 특정 플레이어의 쪽지 조회
     *
     * @param playerName 플레이어 이름
     * @return 쪽지 리스트
     */
    public List<Message> getWhispers(String playerName) {
        return new ArrayList<>(
            whisperHistory.getOrDefault(playerName, Collections.emptyList())
        );
    }

    /**
     * 읽지 않은 쪽지 개수 조회
     *
     * @param playerName 플레이어 이름
     * @return 쪽지 개수
     */
    public int getUnreadCount(String playerName) {
        return whisperHistory.getOrDefault(playerName, Collections.emptyList()).size();
    }

    /**
     * 쪽지 읽음 처리 (히스토리 클리어)
     *
     * @param playerName 플레이어 이름
     */
    public void markAsRead(String playerName) {
        whisperHistory.remove(playerName);
    }

    // ========================================
    // 페이즈 관리
    // ========================================

    /**
     * 낮 페이즈 시작
     */
    public void startDayPhase() {
        this.isDayPhase = true;
    }

    /**
     * 낮 페이즈 종료
     */
    public void endDayPhase() {
        this.isDayPhase = false;
    }

    /**
     * 게임 리셋 (게임 종료 시 호출)
     */
    public void reset() {
        whisperHistory.clear();
        sendLimits.clear();
        isDayPhase = false;
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
     * 전체 쪽지 통계 조회
     *
     * @return 통계 문자열
     */
    public String getStatistics() {
        int totalWhispers = whisperHistory.values().stream()
                                          .mapToInt(List::size)
                                          .sum();

        int totalPlayers = whisperHistory.size();

        return String.format("총 쪽지: %d개 | 수신자: %d명", totalWhispers, totalPlayers);
    }

    // ========================================
    // 내부 클래스 - 쪽지 전송 결과
    // ========================================

    /**
     * 쪽지 전송 결과를 나타내는 클래스
     */
    public static class WhisperResult {
        private final boolean success;
        private final String message;
        private final Message whisper;

        private WhisperResult(boolean success, String message, Message whisper) {
            this.success = success;
            this.message = message;
            this.whisper = whisper;
        }

        public static WhisperResult success(Message whisper) {
            return new WhisperResult(true, "쪽지를 전송했습니다.", whisper);
        }

        public static WhisperResult error(String message) {
            return new WhisperResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Message getWhisper() {
            return whisper;
        }
    }
}
