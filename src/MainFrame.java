import client.GameClient;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private ChannelSelectPanel channelPanel;
    private LoginPanel loginPanel;
    private LobbyPanel lobbyPanel;
    private GameRoomPanel gameRoomPanel;
    private GamePlayPanel gamePlayPanel;

    private GameClient gameClient;
    private String currentNickname;
    private String currentUsername;
    private int currentUserId;

    public MainFrame() {
        // ✅ 1. 맥/윈도우 공통 UI 스타일 적용
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            String fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                    ? "Apple SD Gothic Neo" : "맑은 고딕";
            UIManager.put("defaultFont", new Font(fontName, Font.PLAIN, 14));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ✅ 2. 프레임 설정
        setTitle("Mafia42 - Java GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        // ✅ 3. 카드 레이아웃 초기화
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // ✅ 4. 각 패널 등록
        channelPanel = new ChannelSelectPanel(this);
        loginPanel = new LoginPanel(this);

        mainPanel.add(channelPanel, "channel");
        mainPanel.add(loginPanel, "login");

        add(mainPanel);
        setVisible(true);
    }

    // ✅ 로그인 성공 시 호출되는 메서드 (서버 연결 포함)
    public void showLobby(String nickname, int userId, String username) {
        System.out.println("➡ showLobby 호출됨: " + nickname);
        this.currentNickname = nickname;
        this.currentUsername = username;
        this.currentUserId = userId;

        // 서버에 연결
        gameClient = new GameClient();
        if (gameClient.connect(username, nickname, userId)) {
            System.out.println("✅ 서버 연결 성공");
            lobbyPanel = new LobbyPanel(this, nickname, gameClient);
            mainPanel.add(lobbyPanel, "lobby");
            cardLayout.show(mainPanel, "lobby");
            mainPanel.revalidate();
            mainPanel.repaint();
        } else {
            System.out.println("❌ 서버 연결 실패");
            JOptionPane.showMessageDialog(this,
                "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.",
                "연결 실패",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // ✅ 방 입장 시 호출되는 메서드
    public void showGameRoom(int roomId, String roomName) {
        System.out.println("➡ showGameRoom 호출됨: " + roomName);
        gameRoomPanel = new GameRoomPanel(this, roomId, roomName, gameClient, currentNickname);
        mainPanel.add(gameRoomPanel, "gameroom");
        cardLayout.show(mainPanel, "gameroom");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // ✅ 게임 시작 시 호출되는 메서드
    public void showGamePlay(int roomId, String roomName, String myNickname, String myRole) {
        System.out.println("➡ showGamePlay 호출됨: " + roomName + ", 역할: " + myRole);
        gamePlayPanel = new GamePlayPanel(this, roomId, roomName, gameClient, myNickname, myRole);
        mainPanel.add(gamePlayPanel, "gameplay");
        cardLayout.show(mainPanel, "gameplay");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // ✅ 내 정보 화면으로 이동
    public void showMyInfo(String nickname, int userId) {
        System.out.println("➡ showMyInfo 호출됨: " + nickname);
        MyInfoPanel myInfoPanel = new MyInfoPanel(this, nickname, userId);
        mainPanel.add(myInfoPanel, "myinfo");
        cardLayout.show(mainPanel, "myinfo");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // ✅ 상점 화면으로 이동
    public void showShop(String nickname, int userId) {
        System.out.println("➡ showShop 호출됨: " + nickname);
        ShopPanel shopPanel = new ShopPanel(this, nickname, userId, currentUsername);
        mainPanel.add(shopPanel, "shop");
        cardLayout.show(mainPanel, "shop");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // ✅ 길드 화면으로 이동
    public void showGuild(String nickname, int userId) {
        System.out.println("➡ showGuild 호출됨: " + nickname);
        GuildPanel guildPanel = new GuildPanel(this, nickname, userId, currentUsername);
        mainPanel.add(guildPanel, "guild");
        cardLayout.show(mainPanel, "guild");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void switchTo(String name) {
        cardLayout.show(mainPanel, name);
    }

    public GameClient getGameClient() {
        return gameClient;
    }

    public static void main(String[] args) {
        // ✅ 메인 스레드에서 실행 (Nimbus 적용 후)
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}