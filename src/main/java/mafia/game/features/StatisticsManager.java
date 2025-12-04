package mafia.game.features;

import mafia.game.models.PlayerStatistics;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 플레이어 통계 및 업적 시스템 관리자
 *
 * 이 클래스는 플레이어의 게임 통계와 업적을 관리합니다.
 * 파일 시스템에 데이터를 영구 저장합니다.
 *
 * 설계 원칙:
 * - Singleton Pattern: 하나의 매니저만 존재
 * - Repository Pattern: 데이터 저장/조회 추상화
 * - Persistence: 파일 시스템에 직렬화
 *
 * 기능:
 * - 플레이어 통계 기록 및 조회
 * - 업적 관리
 * - 리더보드 생성
 * - 데이터 영구 저장
 *
 * @author Mafia Game Team
 * @version 2.0
 */
public class StatisticsManager {

    // ========================================
    // Singleton 패턴
    // ========================================

    private static StatisticsManager instance;

    /**
     * Singleton 인스턴스 조회
     *
     * @return StatisticsManager 인스턴스
     */
    public static synchronized StatisticsManager getInstance() {
        if (instance == null) {
            instance = new StatisticsManager();
        }
        return instance;
    }

    // ========================================
    // 필드
    // ========================================

    /**
     * 플레이어 통계 맵 (이름 -> 통계)
     */
    private final Map<String, PlayerStatistics> statistics;

    /**
     * 현재 게임 참가자 (게임 종료 시 통계 업데이트용)
     */
    private final Map<String, String> currentGamePlayers; // 이름 -> 역할

    /**
     * 현재 게임 승리 팀
     */
    private String winningTeam;

    /**
     * 데이터 저장 경로
     */
    private final String dataDirectory;

    /**
     * 기능 활성화 여부
     */
    private boolean enabled;

    // ========================================
    // 생성자
    // ========================================

    /**
     * private 생성자 (Singleton)
     */
    private StatisticsManager() {
        this.statistics = new ConcurrentHashMap<>();
        this.currentGamePlayers = new ConcurrentHashMap<>();
        this.dataDirectory = "game_data/statistics/";
        this.enabled = true;

        // 데이터 디렉토리 생성
        createDataDirectory();

        // 저장된 통계 로드
        loadAllStatistics();
    }

    // ========================================
    // 디렉토리 관리
    // ========================================

    /**
     * 데이터 디렉토리 생성
     */
    private void createDataDirectory() {
        File dir = new File(dataDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // ========================================
    // 게임 시작/종료
    // ========================================

    /**
     * 게임 시작 기록
     *
     * @param players 플레이어 리스트 (이름 -> 역할)
     */
    public void startGame(Map<String, String> players) {
        currentGamePlayers.clear();
        currentGamePlayers.putAll(players);
        winningTeam = null;

        // 플레이어 통계 객체 생성 (없는 경우)
        for (String playerName : players.keySet()) {
            statistics.computeIfAbsent(playerName, PlayerStatistics::new);
        }
    }

    /**
     * 게임 종료 기록
     *
     * @param winningTeam 승리 팀 ("CITIZEN" 또는 "MAFIA")
     * @param mvpPlayer MVP 플레이어 이름
     */
    public void endGame(String winningTeam, String mvpPlayer) {
        if (!enabled) {
            return;
        }

        this.winningTeam = winningTeam;

        // 각 플레이어 통계 업데이트
        for (Map.Entry<String, String> entry : currentGamePlayers.entrySet()) {
            String playerName = entry.getKey();
            String role = entry.getValue();

            // 플레이어 팀 판별
            String playerTeam = getTeamByRole(role);

            // 승리 여부
            boolean isWin = playerTeam.equals(winningTeam);

            // MVP 여부
            boolean isMvp = playerName.equals(mvpPlayer);

            // 통계 기록
            PlayerStatistics stats = statistics.get(playerName);
            if (stats != null) {
                stats.recordGame(role, isWin, isMvp);

                // 통계 저장
                saveStatistics(playerName, stats);
            }
        }

        // 게임 데이터 클리어
        currentGamePlayers.clear();
    }

    /**
     * 역할로 팀 판별
     *
     * @param role 역할
     * @return 팀 ("CITIZEN" 또는 "MAFIA")
     */
    private String getTeamByRole(String role) {
        switch (role) {
            case "MAFIA":
            case "SPY":
            case "MADAME":
                return "MAFIA";
            default:
                return "CITIZEN";
        }
    }

    // ========================================
    // 통계 조회
    // ========================================

    /**
     * 플레이어 통계 조회
     *
     * @param playerName 플레이어 이름
     * @return 통계 객체 (없으면 새로 생성)
     */
    public PlayerStatistics getStatistics(String playerName) {
        return statistics.computeIfAbsent(playerName, PlayerStatistics::new);
    }

    /**
     * 모든 플레이어 통계 조회
     *
     * @return 통계 맵 (읽기 전용)
     */
    public Map<String, PlayerStatistics> getAllStatistics() {
        return new HashMap<>(statistics);
    }

    // ========================================
    // 리더보드
    // ========================================

    /**
     * 승률 기준 리더보드 생성
     *
     * @param limit 상위 N명
     * @return 리더보드 리스트
     */
    public List<PlayerStatistics> getLeaderboardByWinRate(int limit) {
        return statistics.values().stream()
                .filter(s -> s.getTotalGames() >= 5) // 최소 5게임 이상
                .sorted((a, b) -> Double.compare(b.getWinRate(), a.getWinRate()))
                .limit(limit)
                .toList();
    }

    /**
     * 총 게임 수 기준 리더보드 생성
     *
     * @param limit 상위 N명
     * @return 리더보드 리스트
     */
    public List<PlayerStatistics> getLeaderboardByGames(int limit) {
        return statistics.values().stream()
                .sorted((a, b) -> Integer.compare(b.getTotalGames(), a.getTotalGames()))
                .limit(limit)
                .toList();
    }

    /**
     * MVP 기준 리더보드 생성
     *
     * @param limit 상위 N명
     * @return 리더보드 리스트
     */
    public List<PlayerStatistics> getLeaderboardByMVP(int limit) {
        return statistics.values().stream()
                .sorted((a, b) -> Integer.compare(b.getMvpCount(), a.getMvpCount()))
                .limit(limit)
                .toList();
    }

    /**
     * 리더보드 문자열 생성
     *
     * @param type 리더보드 타입 ("WINRATE", "GAMES", "MVP")
     * @param limit 상위 N명
     * @return 리더보드 문자열
     */
    public String getLeaderboardString(String type, int limit) {
        List<PlayerStatistics> leaderboard;

        switch (type.toUpperCase()) {
            case "WINRATE":
                leaderboard = getLeaderboardByWinRate(limit);
                break;
            case "GAMES":
                leaderboard = getLeaderboardByGames(limit);
                break;
            case "MVP":
                leaderboard = getLeaderboardByMVP(limit);
                break;
            default:
                return "알 수 없는 리더보드 타입입니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(type).append(" 리더보드 TOP ").append(limit).append(" ===\n");

        int rank = 1;
        for (PlayerStatistics stats : leaderboard) {
            sb.append(String.format("%d. %s\n", rank++, stats.toString()));
        }

        return sb.toString();
    }

    // ========================================
    // 업적 관리
    // ========================================

    /**
     * 플레이어 업적 해제
     *
     * @param playerName 플레이어 이름
     * @param achievementId 업적 ID
     */
    public void unlockAchievement(String playerName, String achievementId) {
        PlayerStatistics stats = getStatistics(playerName);
        stats.unlockAchievement(achievementId);
        saveStatistics(playerName, stats);
    }

    /**
     * 플레이어의 모든 업적 조회
     *
     * @param playerName 플레이어 이름
     * @return 업적 맵
     */
    public Map<String, Boolean> getAchievements(String playerName) {
        PlayerStatistics stats = getStatistics(playerName);
        return stats.getAllAchievements();
    }

    // ========================================
    // 데이터 영속성 (파일 저장/로드)
    // ========================================

    /**
     * 플레이어 통계 저장
     *
     * @param playerName 플레이어 이름
     * @param stats 통계 객체
     */
    private void saveStatistics(String playerName, PlayerStatistics stats) {
        try {
            String filename = dataDirectory + sanitizeFilename(playerName) + ".dat";
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(stats);
            oos.close();
            fos.close();
        } catch (IOException e) {
            System.err.println("통계 저장 실패: " + playerName);
            e.printStackTrace();
        }
    }

    /**
     * 플레이어 통계 로드
     *
     * @param playerName 플레이어 이름
     * @return 통계 객체 (실패 시 null)
     */
    private PlayerStatistics loadStatistics(String playerName) {
        try {
            String filename = dataDirectory + sanitizeFilename(playerName) + ".dat";
            File file = new File(filename);

            if (!file.exists()) {
                return null;
            }

            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            PlayerStatistics stats = (PlayerStatistics) ois.readObject();
            ois.close();
            fis.close();

            return stats;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("통계 로드 실패: " + playerName);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 모든 플레이어 통계 로드
     */
    private void loadAllStatistics() {
        File dir = new File(dataDirectory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".dat"));

        if (files == null) {
            return;
        }

        for (File file : files) {
            String filename = file.getName();
            String playerName = filename.substring(0, filename.length() - 4);

            PlayerStatistics stats = loadStatistics(playerName);
            if (stats != null) {
                statistics.put(playerName, stats);
            }
        }

        System.out.println("통계 로드 완료: " + statistics.size() + "명");
    }

    /**
     * 파일명 정제 (특수문자 제거)
     *
     * @param filename 원본 파일명
     * @return 정제된 파일명
     */
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9가-힣_-]", "_");
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
    // 유틸리티
    // ========================================

    /**
     * 플레이어 통계 초기화 (주의: 영구 삭제)
     *
     * @param playerName 플레이어 이름
     */
    public void resetStatistics(String playerName) {
        statistics.remove(playerName);

        String filename = dataDirectory + sanitizeFilename(playerName) + ".dat";
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 모든 통계 초기화 (주의: 영구 삭제)
     */
    public void resetAllStatistics() {
        statistics.clear();

        File dir = new File(dataDirectory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".dat"));

        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }
}
