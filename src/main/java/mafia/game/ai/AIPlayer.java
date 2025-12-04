package mafia.game.ai;

import java.util.*;

/**
 * AI 플레이어 추상 클래스
 *
 * 이 클래스는 AI 플레이어의 기본 동작을 정의합니다.
 * 각 난이도별 AI는 이 클래스를 상속받아 구현합니다.
 *
 * 설계 원칙:
 * - Strategy Pattern: 난이도별 전략 구현
 * - Template Method Pattern: 공통 동작 정의
 * - State Pattern: AI 상태 관리
 *
 * @author Mafia Game Team
 * @version 2.0
 */
public abstract class AIPlayer {

    // ========================================
    // 필드
    // ========================================

    /**
     * AI 플레이어 이름
     */
    protected String name;

    /**
     * AI 역할
     */
    protected String role;

    /**
     * AI 난이도
     */
    protected Difficulty difficulty;

    /**
     * 생존 여부
     */
    protected boolean alive;

    /**
     * 알고 있는 정보 (플레이어 -> 추측 역할)
     */
    protected Map<String, String> knownRoles;

    /**
     * 의심 레벨 (플레이어 -> 의심도 0.0~1.0)
     */
    protected Map<String, Double> suspicionLevels;

    /**
     * 신뢰 레벨 (플레이어 -> 신뢰도 0.0~1.0)
     */
    protected Map<String, Double> trustLevels;

    /**
     * 게임 기록 (관찰한 이벤트)
     */
    protected List<GameEvent> gameHistory;

    /**
     * 랜덤 생성기
     */
    protected Random random;

    // ========================================
    // 난이도 Enum
    // ========================================

    /**
     * AI 난이도 열거형
     */
    public enum Difficulty {
        EASY("초급", 0.3),      // 30% 최적 선택
        MEDIUM("중급", 0.6),    // 60% 최적 선택
        HARD("고급", 0.9);      // 90% 최적 선택

        private final String displayName;
        private final double optimalChoiceRate;

        Difficulty(String displayName, double optimalChoiceRate) {
            this.displayName = displayName;
            this.optimalChoiceRate = optimalChoiceRate;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getOptimalChoiceRate() {
            return optimalChoiceRate;
        }
    }

    // ========================================
    // 생성자
    // ========================================

    /**
     * AI 플레이어 생성자
     *
     * @param name 이름
     * @param difficulty 난이도
     */
    public AIPlayer(String name, Difficulty difficulty) {
        this.name = name;
        this.difficulty = difficulty;
        this.alive = true;
        this.knownRoles = new HashMap<>();
        this.suspicionLevels = new HashMap<>();
        this.trustLevels = new HashMap<>();
        this.gameHistory = new ArrayList<>();
        this.random = new Random();
    }

    // ========================================
    // 추상 메소드 (하위 클래스에서 구현)
    // ========================================

    /**
     * 밤 행동 결정
     *
     * @param alivePlayers 살아있는 플레이어 목록
     * @return 행동 대상 (null이면 행동 안 함)
     */
    public abstract String decideNightAction(List<String> alivePlayers);

    /**
     * 투표 대상 결정
     *
     * @param alivePlayers 살아있는 플레이어 목록
     * @return 투표 대상
     */
    public abstract String decideVote(List<String> alivePlayers);

    /**
     * 발언 생성 (낮 시간)
     *
     * @param context 게임 컨텍스트
     * @return 발언 내용 (null이면 발언 안 함)
     */
    public abstract String generateStatement(GameContext context);

    // ========================================
    // 템플릿 메소드 (공통 로직)
    // ========================================

    /**
     * 역할 설정
     *
     * @param role 역할
     */
    public void setRole(String role) {
        this.role = role;
        onRoleAssigned();
    }

    /**
     * 역할 배정 시 호출 (하위 클래스에서 오버라이드 가능)
     */
    protected void onRoleAssigned() {
        // 기본 구현: 아무것도 하지 않음
    }

    /**
     * 게임 이벤트 관찰
     *
     * @param event 이벤트
     */
    public void observeEvent(GameEvent event) {
        gameHistory.add(event);
        analyzeEvent(event);
    }

    /**
     * 이벤트 분석 (하위 클래스에서 오버라이드)
     *
     * @param event 이벤트
     */
    protected void analyzeEvent(GameEvent event) {
        // 기본 구현: 간단한 통계 업데이트
        switch (event.getType()) {
            case PLAYER_DIED:
                // 죽은 플레이어 정보 제거
                suspicionLevels.remove(event.getTarget());
                trustLevels.remove(event.getTarget());
                break;

            case VOTE_CAST:
                // 투표 패턴 분석
                updateSuspicionFromVote(event.getActor(), event.getTarget());
                break;

            case ROLE_REVEALED:
                // 역할 공개 시 정보 업데이트
                knownRoles.put(event.getTarget(), event.getData());
                break;
        }
    }

    /**
     * 투표 패턴으로 의심도 업데이트
     *
     * @param voter 투표자
     * @param target 투표 대상
     */
    protected void updateSuspicionFromVote(String voter, String target) {
        // 간단한 휴리스틱: 투표를 많이 받은 사람의 의심도 증가
        suspicionLevels.put(target,
            suspicionLevels.getOrDefault(target, 0.5) + 0.05
        );
    }

    // ========================================
    // 의사결정 헬퍼 메소드
    // ========================================

    /**
     * 난이도에 따른 선택
     * 확률적으로 최적 선택 vs 랜덤 선택
     *
     * @param optimalChoice 최적 선택
     * @param alternatives 대안 목록
     * @return 최종 선택
     */
    protected String makeChoice(String optimalChoice, List<String> alternatives) {
        if (random.nextDouble() < difficulty.getOptimalChoiceRate()) {
            return optimalChoice;
        } else {
            return alternatives.get(random.nextInt(alternatives.size()));
        }
    }

    /**
     * 가장 의심스러운 플레이어 찾기
     *
     * @param candidates 후보 목록
     * @return 가장 의심스러운 플레이어
     */
    protected String findMostSuspicious(List<String> candidates) {
        return candidates.stream()
            .max(Comparator.comparingDouble(p -> suspicionLevels.getOrDefault(p, 0.5)))
            .orElse(null);
    }

    /**
     * 가장 신뢰할 수 있는 플레이어 찾기
     *
     * @param candidates 후보 목록
     * @return 가장 신뢰할 수 있는 플레이어
     */
    protected String findMostTrusted(List<String> candidates) {
        return candidates.stream()
            .max(Comparator.comparingDouble(p -> trustLevels.getOrDefault(p, 0.5)))
            .orElse(null);
    }

    /**
     * 랜덤 선택
     *
     * @param candidates 후보 목록
     * @return 랜덤 선택된 플레이어
     */
    protected String chooseRandom(List<String> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * 자신을 제외한 플레이어 목록
     *
     * @param players 전체 플레이어 목록
     * @return 자신 제외 목록
     */
    protected List<String> excludeSelf(List<String> players) {
        return players.stream()
            .filter(p -> !p.equals(name))
            .toList();
    }

    // ========================================
    // Getter/Setter
    // ========================================

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public Map<String, String> getKnownRoles() {
        return new HashMap<>(knownRoles);
    }

    public Map<String, Double> getSuspicionLevels() {
        return new HashMap<>(suspicionLevels);
    }

    public Map<String, Double> getTrustLevels() {
        return new HashMap<>(trustLevels);
    }

    // ========================================
    // 내부 클래스 - GameEvent
    // ========================================

    /**
     * 게임 이벤트 데이터 클래스
     */
    public static class GameEvent {
        private final EventType type;
        private final String actor;
        private final String target;
        private final String data;

        public GameEvent(EventType type, String actor, String target, String data) {
            this.type = type;
            this.actor = actor;
            this.target = target;
            this.data = data;
        }

        public EventType getType() { return type; }
        public String getActor() { return actor; }
        public String getTarget() { return target; }
        public String getData() { return data; }

        /**
         * 이벤트 타입
         */
        public enum EventType {
            PLAYER_DIED,        // 플레이어 사망
            VOTE_CAST,          // 투표
            ROLE_REVEALED,      // 역할 공개
            NIGHT_ACTION,       // 밤 행동
            STATEMENT          // 발언
        }
    }

    // ========================================
    // 내부 클래스 - GameContext
    // ========================================

    /**
     * 게임 컨텍스트 (AI 의사결정에 필요한 정보)
     */
    public static class GameContext {
        private final int dayCount;
        private final String phase;
        private final List<String> alivePlayers;
        private final List<String> deadPlayers;
        private final Map<String, Integer> voteCount;

        public GameContext(int dayCount, String phase, List<String> alivePlayers,
                          List<String> deadPlayers, Map<String, Integer> voteCount) {
            this.dayCount = dayCount;
            this.phase = phase;
            this.alivePlayers = alivePlayers;
            this.deadPlayers = deadPlayers;
            this.voteCount = voteCount;
        }

        public int getDayCount() { return dayCount; }
        public String getPhase() { return phase; }
        public List<String> getAlivePlayers() { return alivePlayers; }
        public List<String> getDeadPlayers() { return deadPlayers; }
        public Map<String, Integer> getVoteCount() { return voteCount; }
    }
}
