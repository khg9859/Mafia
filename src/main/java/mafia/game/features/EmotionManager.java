package mafia.game.features;

import mafia.game.models.Message;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 감정 표현 시스템 관리자
 *
 * 이 클래스는 플레이어의 감정 표현(이모지, 이모티콘)을 관리합니다.
 *
 * 설계 원칙:
 * - Singleton Pattern: 하나의 매니저만 존재
 * - Observer Pattern: 감정 표현 이벤트 통지
 * - Rate Limiting: 스팸 방지
 *
 * 기능:
 * - 이모지 반응
 * - 쿨다운 시스템
 * - 감정 통계
 *
 * @author Mafia Game Team
 * @version 2.0
 */
public class EmotionManager {

    // ========================================
    // Singleton 패턴
    // ========================================

    private static EmotionManager instance;

    /**
     * Singleton 인스턴스 조회
     *
     * @return EmotionManager 인스턴스
     */
    public static synchronized EmotionManager getInstance() {
        if (instance == null) {
            instance = new EmotionManager();
        }
        return instance;
    }

    // ========================================
    // 필드
    // ========================================

    /**
     * 사용 가능한 이모지 목록
     */
    private final Map<String, Emotion> emotions;

    /**
     * 감정 표현 쿨다운 (플레이어 -> 마지막 사용 시간)
     */
    private final Map<String, Long> cooldowns;

    /**
     * 감정 표현 히스토리
     */
    private final List<EmotionEvent> history;

    /**
     * 감정 표현 리스너
     */
    private final List<EmotionListener> listeners;

    /**
     * 기능 활성화 여부
     */
    private boolean enabled;

    // ========================================
    // 설정 상수
    // ========================================

    /**
     * 쿨다운 시간 (밀리초, 3초)
     */
    private static final long COOLDOWN_MS = 3000;

    /**
     * 히스토리 최대 크기
     */
    private static final int MAX_HISTORY_SIZE = 100;

    // ========================================
    // 생성자
    // ========================================

    /**
     * private 생성자 (Singleton)
     */
    private EmotionManager() {
        this.emotions = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.history = Collections.synchronizedList(new ArrayList<>());
        this.listeners = new ArrayList<>();
        this.enabled = true;

        // 기본 이모지 초기화
        initializeEmotions();
    }

    // ========================================
    // 이모지 초기화
    // ========================================

    /**
     * 기본 이모지 목록 초기화
     */
    private void initializeEmotions() {
        // 긍정 감정
        registerEmotion(new Emotion("👍", "THUMBS_UP", "좋아요", EmotionCategory.POSITIVE));
        registerEmotion(new Emotion("👏", "CLAP", "박수", EmotionCategory.POSITIVE));
        registerEmotion(new Emotion("❤️", "HEART", "하트", EmotionCategory.POSITIVE));
        registerEmotion(new Emotion("😄", "SMILE", "웃음", EmotionCategory.POSITIVE));
        registerEmotion(new Emotion("🎉", "PARTY", "축하", EmotionCategory.POSITIVE));

        // 부정 감정
        registerEmotion(new Emotion("👎", "THUMBS_DOWN", "싫어요", EmotionCategory.NEGATIVE));
        registerEmotion(new Emotion("😡", "ANGRY", "화남", EmotionCategory.NEGATIVE));
        registerEmotion(new Emotion("😢", "SAD", "슬픔", EmotionCategory.NEGATIVE));
        registerEmotion(new Emotion("💔", "BROKEN_HEART", "실망", EmotionCategory.NEGATIVE));

        // 중립 감정
        registerEmotion(new Emotion("🤔", "THINKING", "생각중", EmotionCategory.NEUTRAL));
        registerEmotion(new Emotion("😐", "NEUTRAL", "무표정", EmotionCategory.NEUTRAL));
        registerEmotion(new Emotion("🤷", "SHRUG", "모름", EmotionCategory.NEUTRAL));

        // 게임 특화 감정
        registerEmotion(new Emotion("😱", "SHOCKED", "충격", EmotionCategory.GAME));
        registerEmotion(new Emotion("🎯", "TARGET", "타겟", EmotionCategory.GAME));
        registerEmotion(new Emotion("🔍", "INVESTIGATE", "조사", EmotionCategory.GAME));
        registerEmotion(new Emotion("⚠️", "WARNING", "경고", EmotionCategory.GAME));
        registerEmotion(new Emotion("🤐", "SILENCE", "침묵", EmotionCategory.GAME));
        registerEmotion(new Emotion("🎭", "MASK", "가면", EmotionCategory.GAME));
        registerEmotion(new Emotion("💀", "SKULL", "죽음", EmotionCategory.GAME));
    }

    /**
     * 이모지 등록
     *
     * @param emotion 감정 객체
     */
    private void registerEmotion(Emotion emotion) {
        emotions.put(emotion.getId(), emotion);
    }

    // ========================================
    // 감정 표현
    // ========================================

    /**
     * 감정 표현
     *
     * @param playerName 플레이어 이름
     * @param emotionId 감정 ID
     * @return 표현 결과
     */
    public EmotionResult express(String playerName, String emotionId) {
        return express(playerName, emotionId, null);
    }

    /**
     * 감정 표현 (대상 지정)
     *
     * @param playerName 플레이어 이름
     * @param emotionId 감정 ID
     * @param targetPlayer 대상 플레이어 (null이면 전체)
     * @return 표현 결과
     */
    public EmotionResult express(String playerName, String emotionId, String targetPlayer) {
        // 기능 비활성화 체크
        if (!enabled) {
            return EmotionResult.error("감정 표현 기능이 비활성화되어 있습니다.");
        }

        // 이모지 존재 확인
        Emotion emotion = emotions.get(emotionId);
        if (emotion == null) {
            return EmotionResult.error("존재하지 않는 감정입니다.");
        }

        // 쿨다운 체크
        if (!checkCooldown(playerName)) {
            long remaining = getRemainingCooldown(playerName);
            return EmotionResult.error(
                String.format("%.1f초 후에 다시 사용할 수 있습니다.", remaining / 1000.0)
            );
        }

        // 감정 표현 이벤트 생성
        EmotionEvent event = new EmotionEvent(playerName, emotion, targetPlayer);

        // 히스토리에 추가
        addToHistory(event);

        // 쿨다운 기록
        recordCooldown(playerName);

        // 통계 업데이트
        emotion.incrementUsageCount();

        // 리스너 통지
        notifyEmotionExpressed(event);

        return EmotionResult.success(emotion);
    }

    /**
     * 쿨다운 체크
     *
     * @param playerName 플레이어 이름
     * @return 사용 가능 여부
     */
    private boolean checkCooldown(String playerName) {
        Long lastUsed = cooldowns.get(playerName);
        if (lastUsed == null) {
            return true;
        }

        long now = System.currentTimeMillis();
        return (now - lastUsed) >= COOLDOWN_MS;
    }

    /**
     * 남은 쿨다운 시간 조회
     *
     * @param playerName 플레이어 이름
     * @return 남은 시간 (밀리초)
     */
    private long getRemainingCooldown(String playerName) {
        Long lastUsed = cooldowns.get(playerName);
        if (lastUsed == null) {
            return 0;
        }

        long now = System.currentTimeMillis();
        long elapsed = now - lastUsed;
        return Math.max(0, COOLDOWN_MS - elapsed);
    }

    /**
     * 쿨다�� 기록
     *
     * @param playerName 플레이어 이름
     */
    private void recordCooldown(String playerName) {
        cooldowns.put(playerName, System.currentTimeMillis());
    }

    /**
     * 히스토리에 추가
     *
     * @param event 이벤트
     */
    private void addToHistory(EmotionEvent event) {
        history.add(event);

        // 최대 크기 제한
        while (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }

    // ========================================
    // 감정 조회
    // ========================================

    /**
     * 모든 이모지 조회
     *
     * @return 이모지 리스트
     */
    public List<Emotion> getAllEmotions() {
        return new ArrayList<>(emotions.values());
    }

    /**
     * 카테고리별 이모지 조회
     *
     * @param category 카테고리
     * @return 이모지 리스트
     */
    public List<Emotion> getEmotionsByCategory(EmotionCategory category) {
        return emotions.values().stream()
                      .filter(e -> e.getCategory() == category)
                      .toList();
    }

    /**
     * 이모지 상세 정보 조회
     *
     * @param emotionId 감정 ID
     * @return 이모지 객체
     */
    public Emotion getEmotion(String emotionId) {
        return emotions.get(emotionId);
    }

    /**
     * 이모지 목록 문자열
     *
     * @return 목록 문자열
     */
    public String getEmotionListString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 사용 가능한 감정 표현 ===\n\n");

        for (EmotionCategory category : EmotionCategory.values()) {
            List<Emotion> categoryEmotions = getEmotionsByCategory(category);
            if (!categoryEmotions.isEmpty()) {
                sb.append("【").append(category.getDisplayName()).append("】\n");
                for (Emotion emotion : categoryEmotions) {
                    sb.append(String.format("%s %s (/emotion %s)\n",
                        emotion.getIcon(),
                        emotion.getName(),
                        emotion.getId()
                    ));
                }
                sb.append("\n");
            }
        }

        sb.append("사용법: /emotion [감정ID] 또는 /emotion [감정ID] [대상]\n");

        return sb.toString();
    }

    // ========================================
    // 히스토리 조회
    // ========================================

    /**
     * 최근 감정 표현 히스토리
     *
     * @param limit 조회 개수
     * @return 이벤트 리스트
     */
    public List<EmotionEvent> getRecentHistory(int limit) {
        int size = history.size();
        int fromIndex = Math.max(0, size - limit);
        return new ArrayList<>(history.subList(fromIndex, size));
    }

    /**
     * 특정 플레이어의 감정 표현 히스토리
     *
     * @param playerName 플레이어 이름
     * @return 이벤트 리스트
     */
    public List<EmotionEvent> getPlayerHistory(String playerName) {
        return history.stream()
                     .filter(e -> e.getPlayerName().equals(playerName))
                     .toList();
    }

    // ========================================
    // 통계
    // ========================================

    /**
     * 가장 많이 사용된 이모지 조회
     *
     * @param limit 상위 N개
     * @return 이모지 리스트
     */
    public List<Emotion> getMostUsedEmotions(int limit) {
        return emotions.values().stream()
                      .sorted((a, b) -> Integer.compare(b.getUsageCount(), a.getUsageCount()))
                      .limit(limit)
                      .toList();
    }

    /**
     * 감정 표현 통계 문자열
     *
     * @return 통계 문자열
     */
    public String getStatistics() {
        int totalExpressions = history.size();
        List<Emotion> topEmotions = getMostUsedEmotions(5);

        StringBuilder sb = new StringBuilder();
        sb.append("=== 감정 표현 통계 ===\n");
        sb.append("총 표현 횟수: ").append(totalExpressions).append("회\n\n");

        sb.append("인기 감정 TOP 5:\n");
        int rank = 1;
        for (Emotion emotion : topEmotions) {
            sb.append(String.format("%d. %s %s (%d회)\n",
                rank++,
                emotion.getIcon(),
                emotion.getName(),
                emotion.getUsageCount()
            ));
        }

        return sb.toString();
    }

    // ========================================
    // Observer 패턴 - 리스너 관리
    // ========================================

    /**
     * 감정 표현 리스너 추가
     *
     * @param listener 리스너
     */
    public void addEmotionListener(EmotionListener listener) {
        listeners.add(listener);
    }

    /**
     * 감정 표현 리스너 제거
     *
     * @param listener 리스너
     */
    public void removeEmotionListener(EmotionListener listener) {
        listeners.remove(listener);
    }

    /**
     * 감정 표현 통지
     *
     * @param event 이벤트
     */
    private void notifyEmotionExpressed(EmotionEvent event) {
        for (EmotionListener listener : listeners) {
            listener.onEmotionExpressed(event);
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
        cooldowns.clear();
        history.clear();

        // 사용 횟수 초기화
        for (Emotion emotion : emotions.values()) {
            emotion.resetUsageCount();
        }
    }

    // ========================================
    // 내부 클래스 - Emotion
    // ========================================

    /**
     * 감정 데이터 클래스
     */
    public static class Emotion {
        private final String icon;
        private final String id;
        private final String name;
        private final EmotionCategory category;
        private int usageCount;

        public Emotion(String icon, String id, String name, EmotionCategory category) {
            this.icon = icon;
            this.id = id;
            this.name = name;
            this.category = category;
            this.usageCount = 0;
        }

        public String getIcon() { return icon; }
        public String getId() { return id; }
        public String getName() { return name; }
        public EmotionCategory getCategory() { return category; }
        public int getUsageCount() { return usageCount; }

        public void incrementUsageCount() {
            usageCount++;
        }

        public void resetUsageCount() {
            usageCount = 0;
        }

        @Override
        public String toString() {
            return icon + " " + name;
        }
    }

    // ========================================
    // Enum - EmotionCategory
    // ========================================

    /**
     * 감정 카테고리
     */
    public enum EmotionCategory {
        POSITIVE("긍정"),
        NEGATIVE("부정"),
        NEUTRAL("중립"),
        GAME("게임");

        private final String displayName;

        EmotionCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ========================================
    // 내부 클래스 - EmotionEvent
    // ========================================

    /**
     * 감정 표현 이벤트
     */
    public static class EmotionEvent {
        private final String playerName;
        private final Emotion emotion;
        private final String targetPlayer;
        private final long timestamp;

        public EmotionEvent(String playerName, Emotion emotion, String targetPlayer) {
            this.playerName = playerName;
            this.emotion = emotion;
            this.targetPlayer = targetPlayer;
            this.timestamp = System.currentTimeMillis();
        }

        public String getPlayerName() { return playerName; }
        public Emotion getEmotion() { return emotion; }
        public String getTargetPlayer() { return targetPlayer; }
        public long getTimestamp() { return timestamp; }

        public boolean hasTarget() {
            return targetPlayer != null;
        }

        public String toDisplayString() {
            if (hasTarget()) {
                return String.format("%s → %s %s",
                    playerName,
                    targetPlayer,
                    emotion.getIcon()
                );
            } else {
                return String.format("%s %s",
                    playerName,
                    emotion.getIcon()
                );
            }
        }
    }

    // ========================================
    // 내부 클래스 - EmotionResult
    // ========================================

    /**
     * 감정 표현 결과
     */
    public static class EmotionResult {
        private final boolean success;
        private final String message;
        private final Emotion emotion;

        private EmotionResult(boolean success, String message, Emotion emotion) {
            this.success = success;
            this.message = message;
            this.emotion = emotion;
        }

        public static EmotionResult success(Emotion emotion) {
            return new EmotionResult(true, "감정을 표현했습니다.", emotion);
        }

        public static EmotionResult error(String message) {
            return new EmotionResult(false, message, null);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Emotion getEmotion() { return emotion; }
    }

    // ========================================
    // 인터페이스 - EmotionListener
    // ========================================

    /**
     * 감정 표현 리스너 인터페이스
     */
    public interface EmotionListener {
        /**
         * 감정 표현 시 호출
         *
         * @param event 이벤트
         */
        void onEmotionExpressed(EmotionEvent event);
    }
}
