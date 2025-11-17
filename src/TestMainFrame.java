import client.TestGameClient;

import javax.swing.*;
import java.awt.*;

/**
 * 테스트용 메인 프레임 - TestGameClient 사용 (포트 9998)
 */
public class TestMainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private TestGameClient gameClient;
    private String currentNickname;

    public TestMainFrame() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            String fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                    ? "Apple SD Gothic Neo" : "맑은 고딕";
            UIManager.put("defaultFont", new Font(fontName, Font.PLAIN, 14));
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Mafia42 - 테스트 클라이언트 (포트 9998)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 테스트용 간단한 로그인 패널
        String fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";
        JPanel testLoginPanel = createTestLoginPanel(fontName);
        mainPanel.add(testLoginPanel, "login");

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createTestLoginPanel(String fontName) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(20, 20, 20));

        JPanel loginBox = new JPanel();
        loginBox.setLayout(new BoxLayout(loginBox, BoxLayout.Y_AXIS));
        loginBox.setBackground(new Color(40, 40, 40));
        loginBox.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("🧪 테스트 클라이언트");
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setFont(new Font(fontName, Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel portLabel = new JLabel("포트: 9998");
        portLabel.setForeground(Color.LIGHT_GRAY);
        portLabel.setFont(new Font(fontName, Font.PLAIN, 14));
        portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField nicknameField = new JTextField(15);
        nicknameField.setMaximumSize(new Dimension(200, 30));
        nicknameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = new JButton("접속");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setBackground(new Color(220, 180, 80));
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setFont(new Font(fontName, Font.BOLD, 14));
        loginBtn.setFocusPainted(false);

        loginBtn.addActionListener(e -> {
            String nickname = nicknameField.getText().trim();
            if (!nickname.isEmpty()) {
                showLobby(nickname, (int)(Math.random() * 10000), "test_" + nickname);
            }
        });

        loginBox.add(titleLabel);
        loginBox.add(Box.createRigidArea(new Dimension(0, 10)));
        loginBox.add(portLabel);
        loginBox.add(Box.createRigidArea(new Dimension(0, 20)));
        loginBox.add(nicknameField);
        loginBox.add(Box.createRigidArea(new Dimension(0, 15)));
        loginBox.add(loginBtn);

        panel.add(loginBox);
        return panel;
    }

    public void showLobby(String nickname, int userId, String username) {
        System.out.println("➡ [테스트] showLobby 호출됨: " + nickname);
        this.currentNickname = nickname;

        gameClient = new TestGameClient();
        if (gameClient.connect(username, nickname, userId)) {
            System.out.println("✅ 테스트 서버 연결 성공 (포트 9998)");
            TestLobbyPanel lobbyPanel = new TestLobbyPanel(this, nickname, gameClient);
            mainPanel.add(lobbyPanel, "lobby");
            cardLayout.show(mainPanel, "lobby");
            mainPanel.revalidate();
            mainPanel.repaint();
        } else {
            System.out.println("❌ 테스트 서버 연결 실패");
            JOptionPane.showMessageDialog(this,
                "테스트 서버에 연결할 수 없습니다.\n테스트 서버(포트 9998)가 실행 중인지 확인하세요.",
                "연결 실패",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showGameRoom(int roomId, String roomName) {
        System.out.println("➡ [테스트] showGameRoom 호출됨: " + roomName);
        TestGameRoomPanel gameRoomPanel = new TestGameRoomPanel(this, roomId, roomName, gameClient, currentNickname);
        mainPanel.add(gameRoomPanel, "gameroom");
        cardLayout.show(mainPanel, "gameroom");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showGamePlay(int roomId, String roomName, String myNickname, String myRole) {
        System.out.println("➡ [테스트] showGamePlay 호출됨: " + roomName + ", 역할: " + myRole);
        JPanel gamePlayPanel = createTestGamePlayPanel(roomName, myRole);
        mainPanel.add(gamePlayPanel, "gameplay");
        cardLayout.show(mainPanel, "gameplay");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private JPanel createTestGamePlayPanel(String roomName, String myRole) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 20, 20));

        JLabel label = new JLabel("🎮 게임 시작! 방: " + roomName + " | 역할: " + myRole, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Apple SD Gothic Neo", Font.BOLD, 24));

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public void switchTo(String name) {
        cardLayout.show(mainPanel, name);
    }

    public TestGameClient getGameClient() {
        return gameClient;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TestMainFrame());
    }
}
