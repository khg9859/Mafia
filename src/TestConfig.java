public class TestConfig {
    // 테스트 모드: true로 설정하면 2명만으로도 게임 시작 가능
    public static final boolean TEST_MODE = true;

    // 테스트 모드일 때 최소 플레이어 수
    public static final int TEST_MIN_PLAYERS = 2;

    // 실제 게임 최소 플레이어 수
    public static final int REAL_MIN_PLAYERS = 5;

    // 최대 플레이어 수
    public static final int MAX_PLAYERS = 8;

    // 현재 설정에 따른 최소 플레이어 수 반환
    public static int getMinPlayers() {
        return TEST_MODE ? TEST_MIN_PLAYERS : REAL_MIN_PLAYERS;
    }
}
