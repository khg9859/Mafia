package mafia.game.features;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 라이브 투표 집계 시스템
 *
 * 이 클래스는 투표 진행 상황을 실시간으로 추적하고 표시합니다.
 *
 * 설계 원칙:
 * - Observer Pattern: 투표 변화를 리스너에게 실시간 통지
 * - Thread-Safe: 동시 투표 처리
 * - Strategy Pattern: 공개/익명 투표 모드 전환
 *
 * 기능:
 * - 실시간 투표 집계
 * - 투표율 계산
 * - 공개/익명 모드 지원
 * - 투표 진행 바 데이터 제공
 *
 * @author Mafia Game Team
 * @version 2.0
 */
public class VoteTracker {

    // ========================================
    // Singleton 패턴
    // ========================================

    private static VoteTracker instance;

    /**
     * Singleton 인스턴스 조회
     *
     * @return VoteTracker 인스턴스
     */
    public static synchronized VoteTracker getInstance() {
        if (instance == null) {
            instance = new VoteTracker();
        }
        return instance;
    }

    // ========================================
    // 필드
    // ========================================

    /**
     * 투표 집계 (대상 -> 득표수)
     */
    private final Map<String, Integer> voteCount;

    /**
     * 투표자 추적 (투표자 -> 투표 대상)
     */
    private final Map<String, String> voterToTarget;

    /**
     * 투표 가능한 플레이어 목록
     */
    private final Set<String> eligibleVoters;

    /**
     * 투표 후보 목록
     */
    private final Set<String> candidates;

    /**
     * 투표 변화 리스너
     */
    private final List<VoteChangeListener> listeners;

    /**
     * 투표 모드 (PUBLIC: 공개, ANONYMOUS: 익명)
     */
    private VoteMode mode;

    /**
     * 투표 시작 시간
     */
    private long voteStartTime;

    /**
     * 투표 제한 시간 (초)
     */
    private int voteDurationSeconds;

    /**
     * 투표 진행 중 여부
     */
    private boolean active;

    // ========================================
    // 투표 모드 Enum
    // ========================================

    /**
     * 투표 모드 열거형
     */
    public enum VoteMode {
        PUBLIC,     // 공개 투표 (누가 누구에게 투표했는지 공개)
        ANONYMOUS   // 익명 투표 (득표수만 공개)
    }

    // ========================================
    // 생성자
    // ========================================

    /**
     * private 생성자 (Singleton)
     */
    private VoteTracker() {
        this.voteCount = new ConcurrentHashMap<>();
        this.voterToTarget = new ConcurrentHashMap<>();
        this.eligibleVoters = ConcurrentHashMap.newKeySet();
        this.candidates = ConcurrentHashMap.newKeySet();
        this.listeners = new ArrayList<>();
        this.mode = VoteMode.PUBLIC;
        this.active = false;
    }

    // ========================================
    // 투표 시작/종료
    // ========================================

    /**
     * 투표 시작
     *
     * @param voters 투표 가능한 플레이어 목록
     * @param candidates 투표 후보 목록
     * @param durationSeconds 투표 제한 시간 (초)
     * @param mode 투표 모드
     */
    public void startVoting(Set<String> voters, Set<String> candidates,
                           int durationSeconds, VoteMode mode) {
        // 초기화
        this.voteCount.clear();
        this.voterToTarget.clear();
        this.eligibleVoters.clear();
        this.candidates.clear();

        // 설정
        this.eligibleVoters.addAll(voters);
        this.candidates.addAll(candidates);
        this.voteDurationSeconds = durationSeconds;
        this.mode = mode;
        this.voteStartTime = System.currentTimeMillis();
        this.active = true;

        // 후보자 득표수 초기화
        for (String candidate : candidates) {
            voteCount.put(candidate, 0);
        }

        // 리스너 통지
        notifyVoteStarted();
    }

    /**
     * 투표 종료
     *
     * @return 투표 결과
     */
    public VoteResult endVoting() {
        this.active = false;

        VoteResult result = new VoteResult(
            new HashMap<>(voteCount),
            new HashMap<>(voterToTarget),
            calculateVoteRate(),
            findTopVoted()
        );

        notifyVoteEnded(result);

        return result;
    }

    /**
     * 투표 리셋
     */
    public void reset() {
        this.voteCount.clear();
        this.voterToTarget.clear();
        this.eligibleVoters.clear();
        this.candidates.clear();
        this.active = false;
    }

    // ========================================
    // 투표 처리
    // ========================================

    /**
     * 투표 등록
     *
     * @param voter 투표자
     * @param target 투표 대상
     * @return 투표 성공 여부
     */
    public VoteCastResult castVote(String voter, String target) {
        // 활성 상태 체크
        if (!active) {
            return VoteCastResult.error("현재 투표 시간이 아닙니다.");
        }

        // 투표 자격 체크
        if (!eligibleVoters.contains(voter)) {
            return VoteCastResult.error("투표 권한이 없습니다.");
        }

        // 후보 체크
        if (!candidates.contains(target)) {
            return VoteCastResult.error("올바른 투표 대상이 아닙니다.");
        }

        // 이전 투표 취소 (재투표)
        String previousTarget = voterToTarget.get(voter);
        if (previousTarget != null) {
            voteCount.put(previousTarget, voteCount.get(previousTarget) - 1);
        }

        // 새로운 투표 등록
        voterToTarget.put(voter, target);
        voteCount.put(target, voteCount.get(target) + 1);

        // 리스너 통지
        notifyVoteChanged(voter, target, previousTarget != null);

        return VoteCastResult.success(target);
    }

    /**
     * 투표 취소
     *
     * @param voter 투표자
     * @return 취소 성공 여부
     */
    public boolean cancelVote(String voter) {
        String target = voterToTarget.remove(voter);

        if (target != null) {
            voteCount.put(target, voteCount.get(target) - 1);
            notifyVoteChanged(voter, null, true);
            return true;
        }

        return false;
    }

    // ========================================
    // 투표 조회
    // ========================================

    /**
     * 특정 후보의 득표수 조회
     *
     * @param candidate 후보자
     * @return 득표수
     */
    public int getVoteCount(String candidate) {
        return voteCount.getOrDefault(candidate, 0);
    }

    /**
     * 전체 득표 현황 조회
     *
     * @return 득표 맵 (읽기 전용)
     */
    public Map<String, Integer> getAllVoteCounts() {
        return new HashMap<>(voteCount);
    }

    /**
     * 투표자가 누구에게 투표했는지 조회
     *
     * @param voter 투표자
     * @return 투표 대상 (없으면 null)
     */
    public String getVoterTarget(String voter) {
        return voterToTarget.get(voter);
    }

    /**
     * 투표 여부 확인
     *
     * @param voter 투표자
     * @return 투표 완료 여부
     */
    public boolean hasVoted(String voter) {
        return voterToTarget.containsKey(voter);
    }

    /**
     * 투표율 계산
     *
     * @return 투표율 (0.0 ~ 1.0)
     */
    public double calculateVoteRate() {
        if (eligibleVoters.isEmpty()) {
            return 0.0;
        }
        return (double) voterToTarget.size() / eligibleVoters.size();
    }

    /**
     * 최다 득표자 찾기
     *
     * @return 최다 득표자 리스트 (동점자 포함)
     */
    public List<String> findTopVoted() {
        if (voteCount.isEmpty()) {
            return Collections.emptyList();
        }

        int maxVotes = voteCount.values().stream()
                               .max(Integer::compare)
                               .orElse(0);

        if (maxVotes == 0) {
            return Collections.emptyList();
        }

        List<String> topVoted = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
            if (entry.getValue() == maxVotes) {
                topVoted.add(entry.getKey());
            }
        }

        return topVoted;
    }

    // ========================================
    // 투표 진행 정보
    // ========================================

    /**
     * 경과 시간 조회 (초)
     *
     * @return 경과 시간
     */
    public int getElapsedSeconds() {
        if (!active) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - voteStartTime;
        return (int) (elapsed / 1000);
    }

    /**
     * 남은 시간 조회 (초)
     *
     * @return 남은 시간
     */
    public int getRemainingSeconds() {
        int elapsed = getElapsedSeconds();
        int remaining = voteDurationSeconds - elapsed;
        return Math.max(0, remaining);
    }

    /**
     * 투표 진행률 (0.0 ~ 1.0)
     *
     * @return 진행률
     */
    public double getProgress() {
        if (voteDurationSeconds <= 0) {
            return 1.0;
        }
        return Math.min(1.0, (double) getElapsedSeconds() / voteDurationSeconds);
    }

    // ========================================
    // 투표 통계
    // ========================================

    /**
     * 투표 통계 문자열 생성
     *
     * @return 통계 문자열
     */
    public String getStatistics() {
        return String.format(
            "투표 진행률: %.1f%% | 참여: %d/%d | 남은 시간: %d초",
            calculateVoteRate() * 100,
            voterToTarget.size(),
            eligibleVoters.size(),
            getRemainingSeconds()
        );
    }

    /**
     * 투표 결과 바 차트 데이터 생성
     *
     * @param maxWidth 최대 바 너비 (문자 수)
     * @return 바 차트 문자열 리스트
     */
    public List<String> getVoteBarChart(int maxWidth) {
        List<String> chart = new ArrayList<>();

        // 최대 득표수 찾기
        int maxVotes = voteCount.values().stream()
                               .max(Integer::compare)
                               .orElse(1);

        // 각 후보별 바 생성
        for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
            String candidate = entry.getKey();
            int votes = entry.getValue();

            // 바 길이 계산
            int barLength = maxVotes > 0 ? (votes * maxWidth / maxVotes) : 0;

            // 바 생성
            String bar = "█".repeat(barLength);
            String empty = "░".repeat(maxWidth - barLength);

            // 모드에 따라 표시
            String line;
            if (mode == VoteMode.PUBLIC) {
                line = String.format("%s: %s%s %d표", candidate, bar, empty, votes);
            } else {
                line = String.format("%s: %s%s", candidate, bar, empty);
            }

            chart.add(line);
        }

        return chart;
    }

    // ========================================
    // Observer 패턴 - 리스너 관리
    // ========================================

    /**
     * 투표 변화 리스너 추가
     *
     * @param listener 리스너
     */
    public void addVoteChangeListener(VoteChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * 투표 변화 리스너 제거
     *
     * @param listener 리스너
     */
    public void removeVoteChangeListener(VoteChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * 투표 시작 통지
     */
    private void notifyVoteStarted() {
        for (VoteChangeListener listener : listeners) {
            listener.onVoteStarted(voteDurationSeconds, mode);
        }
    }

    /**
     * 투표 변화 통지
     *
     * @param voter 투표자
     * @param target 투표 대상
     * @param isRevote 재투표 여부
     */
    private void notifyVoteChanged(String voter, String target, boolean isRevote) {
        for (VoteChangeListener listener : listeners) {
            listener.onVoteChanged(voter, target, isRevote, calculateVoteRate());
        }
    }

    /**
     * 투표 종료 통지
     *
     * @param result 투표 결과
     */
    private void notifyVoteEnded(VoteResult result) {
        for (VoteChangeListener listener : listeners) {
            listener.onVoteEnded(result);
        }
    }

    // ========================================
    // Getter
    // ========================================

    public boolean isActive() {
        return active;
    }

    public VoteMode getMode() {
        return mode;
    }

    public void setMode(VoteMode mode) {
        this.mode = mode;
    }

    // ========================================
    // 내부 클래스 - 투표 결과
    // ========================================

    /**
     * 투표 결과 데이터 클래스
     */
    public static class VoteResult {
        private final Map<String, Integer> voteCounts;
        private final Map<String, String> voterToTarget;
        private final double voteRate;
        private final List<String> topVoted;

        public VoteResult(Map<String, Integer> voteCounts,
                         Map<String, String> voterToTarget,
                         double voteRate,
                         List<String> topVoted) {
            this.voteCounts = voteCounts;
            this.voterToTarget = voterToTarget;
            this.voteRate = voteRate;
            this.topVoted = topVoted;
        }

        public Map<String, Integer> getVoteCounts() {
            return voteCounts;
        }

        public Map<String, String> getVoterToTarget() {
            return voterToTarget;
        }

        public double getVoteRate() {
            return voteRate;
        }

        public List<String> getTopVoted() {
            return topVoted;
        }

        public boolean isTie() {
            return topVoted.size() > 1;
        }

        public String getWinner() {
            return topVoted.isEmpty() ? null : topVoted.get(0);
        }
    }

    // ========================================
    // 내부 클래스 - 투표 등록 결과
    // ========================================

    /**
     * 투표 등록 결과 클래스
     */
    public static class VoteCastResult {
        private final boolean success;
        private final String message;
        private final String target;

        private VoteCastResult(boolean success, String message, String target) {
            this.success = success;
            this.message = message;
            this.target = target;
        }

        public static VoteCastResult success(String target) {
            return new VoteCastResult(true, "투표가 등록되었습니다.", target);
        }

        public static VoteCastResult error(String message) {
            return new VoteCastResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getTarget() {
            return target;
        }
    }

    // ========================================
    // 인���페이스 - 투표 변화 리스너
    // ========================================

    /**
     * 투표 변화 리스너 인터페이스
     */
    public interface VoteChangeListener {
        /**
         * 투표 시작 시 호출
         *
         * @param durationSeconds 투표 제한 시간
         * @param mode 투표 모드
         */
        void onVoteStarted(int durationSeconds, VoteMode mode);

        /**
         * 투표 변화 시 호출
         *
         * @param voter 투표자
         * @param target 투표 대상
         * @param isRevote 재투표 여부
         * @param voteRate 현재 투표율
         */
        void onVoteChanged(String voter, String target, boolean isRevote, double voteRate);

        /**
         * 투표 종료 시 호출
         *
         * @param result 투표 결과
         */
        void onVoteEnded(VoteResult result);
    }
}
