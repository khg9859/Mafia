package mafia.game.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 플레이어 통계 데이터 모델
 *
 * 이 클래스는 플레이어의 게임 통계를 관리합니다.
 * Serializable을 구현하여 파일 저장/불러오기를 지원합니다.
 *
 * 설계 원칙:
 * - Single Responsibility Principle: 통계 데이터만 관리
 * - Encapsulation: private 필드와 getter/setter
 * - Immutability: 통계는 증가만 가능 (감소 불가)
 *
 * @author Mafia Game Team
 * @version 2.0
 */
public class PlayerStatistics implements Serializable {
    private static final long serialVersionUID = 1L;

    // ========================================
    // 플레이어 식별 정보
    // ========================================

    /**
     * 플레이어 이름
     */
    private String playerName;

    // ========================================
    // 기본 통계
    // ========================================

    /**
     * 총 게임 수
     */
    private int totalGames;

    /**
     * 승리 횟수
     */
    private int wins;

    /**
     * 패배 횟수
     */
    private int losses;

    /**
     * MVP 횟수 (게임에서 가장 활약한 플레이어)
     */
    private int mvpCount;

    // ========================================
    // 역할별 통계 (역할 이름 -> [플레이 수, 승리 수])
    // ========================================

    /**
     * 역할별 플레이 횟수
     */
    private Map<String, Integer> rolePlays;

    /**
     * 역할별 승리 횟수
     */
    private Map<String, Integer> roleWins;

    // ========================================
    // 업적 데이터
    // ========================================

    /**
     * 획득한 업적 목록
     */
    private Map<String, Boolean> achievements;

    // ========================================
    // 생성자
    // ========================================

    /**
     * 기본 생성자
     * 모든 통계를 0으로 초기화합니다.
     *
     * @param playerName 플레이어 이름
     */
    public PlayerStatistics(String playerName) {
        this.playerName = playerName;
        this.totalGames = 0;
        this.wins = 0;
        this.losses = 0;
        this.mvpCount = 0;
        this.rolePlays = new HashMap<>();
        this.roleWins = new HashMap<>();
        this.achievements = new HashMap<>();

        // 기본 업적 초기화
        initializeAchievements();
    }

    /**
     * 업적 목록 초기화
     * 확장성을 위해 별도 메소드로 분리
     */
    private void initializeAchievements() {
        achievements.put("FIRST_WIN", false);           // 첫 승리
        achievements.put("PERFECT_MAFIA", false);       // 마피아로 완벽한 승리
        achievements.put("SURVIVOR", false);            // 10게임 연속 생존
        achievements.put("DETECTIVE_MASTER", false);    // 경찰로 마피아 3명 이상 찾기
        achievements.put("IMMORTAL", false);            // 한 게임에서 2번 부활
        achievements.put("VETERAN", false);             // 100게임 플레이
        achievements.put("CHAMPION", false);            // 승률 70% 이상 (최소 20게임)
    }

    // ========================================
    // 통계 업데이트 메소드
    // ========================================

    /**
     * 게임 결과 기록
     *
     * @param role 플레이한 역할
     * @param isWin 승리 여부
     * @param isMvp MVP 여부
     */
    public void recordGame(String role, boolean isWin, boolean isMvp) {
        totalGames++;

        if (isWin) {
            wins++;

            // 첫 승리 업적
            if (wins == 1) {
                unlockAchievement("FIRST_WIN");
            }
        } else {
            losses++;
        }

        if (isMvp) {
            mvpCount++;
        }

        // 역할별 통계 업데이트
        rolePlays.put(role, rolePlays.getOrDefault(role, 0) + 1);
        if (isWin) {
            roleWins.put(role, roleWins.getOrDefault(role, 0) + 1);
        }

        // 100게임 업적
        if (totalGames >= 100) {
            unlockAchievement("VETERAN");
        }

        // 챔피언 업적 체크
        checkChampionAchievement();
    }

    /**
     * 업적 해제
     *
     * @param achievementId 업적 ID
     */
    public void unlockAchievement(String achievementId) {
        if (achievements.containsKey(achievementId)) {
            achievements.put(achievementId, true);
        }
    }

    /**
     * 챔피언 업적 확인
     * 승률 70% 이상, 최소 20게임 플레이
     */
    private void checkChampionAchievement() {
        if (totalGames >= 20) {
            double winRate = (double) wins / totalGames;
            if (winRate >= 0.70) {
                unlockAchievement("CHAMPION");
            }
        }
    }

    // ========================================
    // Getter 메소드
    // ========================================

    public String getPlayerName() {
        return playerName;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getMvpCount() {
        return mvpCount;
    }

    /**
     * 승률 계산
     *
     * @return 승률 (0.0 ~ 1.0)
     */
    public double getWinRate() {
        if (totalGames == 0) {
            return 0.0;
        }
        return (double) wins / totalGames;
    }

    /**
     * 특정 역할의 플레이 횟수 조회
     *
     * @param role 역할 이름
     * @return 플레이 횟수
     */
    public int getRolePlays(String role) {
        return rolePlays.getOrDefault(role, 0);
    }

    /**
     * 특정 역할의 승리 횟수 조회
     *
     * @param role 역할 이름
     * @return 승리 횟수
     */
    public int getRoleWins(String role) {
        return roleWins.getOrDefault(role, 0);
    }

    /**
     * 특정 역할의 승률 조회
     *
     * @param role 역할 이름
     * @return 승률 (0.0 ~ 1.0)
     */
    public double getRoleWinRate(String role) {
        int plays = getRolePlays(role);
        if (plays == 0) {
            return 0.0;
        }
        return (double) getRoleWins(role) / plays;
    }

    /**
     * 업적 획득 여부 확인
     *
     * @param achievementId 업적 ID
     * @return 획득 여부
     */
    public boolean hasAchievement(String achievementId) {
        return achievements.getOrDefault(achievementId, false);
    }

    /**
     * 모든 업적 목록 조회
     *
     * @return 업적 맵 (읽기 전용)
     */
    public Map<String, Boolean> getAllAchievements() {
        return new HashMap<>(achievements);
    }

    // ========================================
    // 유틸리티 메소드
    // ========================================

    /**
     * 통계 정보를 문자열로 반환
     *
     * @return 통계 요약 문자열
     */
    @Override
    public String toString() {
        return String.format(
            "Player: %s | Games: %d | W/L: %d/%d (%.1f%%) | MVP: %d",
            playerName, totalGames, wins, losses, getWinRate() * 100, mvpCount
        );
    }

    /**
     * 상세 통계 정보 반환
     *
     * @return 상세 통계 문자열
     */
    public String getDetailedStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(playerName).append(" 통계 ===\n");
        sb.append("총 게임: ").append(totalGames).append("\n");
        sb.append("승리: ").append(wins).append(" | 패배: ").append(losses).append("\n");
        sb.append("승률: ").append(String.format("%.1f%%", getWinRate() * 100)).append("\n");
        sb.append("MVP: ").append(mvpCount).append("회\n");

        sb.append("\n=== 역할별 통계 ===\n");
        for (String role : rolePlays.keySet()) {
            int plays = rolePlays.get(role);
            int wins = roleWins.getOrDefault(role, 0);
            double winRate = plays > 0 ? (double) wins / plays * 100 : 0.0;
            sb.append(String.format("%s: %d게임, %d승 (%.1f%%)\n",
                role, plays, wins, winRate));
        }

        sb.append("\n=== 업적 ===\n");
        int unlockedCount = 0;
        for (Map.Entry<String, Boolean> entry : achievements.entrySet()) {
            if (entry.getValue()) {
                sb.append("✓ ").append(getAchievementName(entry.getKey())).append("\n");
                unlockedCount++;
            }
        }
        sb.append(String.format("달성: %d/%d\n", unlockedCount, achievements.size()));

        return sb.toString();
    }

    /**
     * 업적 ID를 한글 이름으로 변환
     *
     * @param achievementId 업적 ID
     * @return 업적 한글 이름
     */
    private String getAchievementName(String achievementId) {
        switch (achievementId) {
            case "FIRST_WIN": return "첫 승리";
            case "PERFECT_MAFIA": return "완벽한 마피아";
            case "SURVIVOR": return "생존왕";
            case "DETECTIVE_MASTER": return "명탐정";
            case "IMMORTAL": return "불사조";
            case "VETERAN": return "베테랑";
            case "CHAMPION": return "챔피언";
            default: return achievementId;
        }
    }
}
