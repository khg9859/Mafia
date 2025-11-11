import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private ChannelSelectPanel channelPanel;
    private LoginPanel loginPanel;
    private LobbyPanel lobbyPanel;

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

    // ✅ 로그인 성공 시 호출되는 메서드
    public void showLobby(String nickname) {
        System.out.println("➡ showLobby 호출됨: " + nickname);
        lobbyPanel = new LobbyPanel(this, nickname);
        mainPanel.add(lobbyPanel, "lobby");
        cardLayout.show(mainPanel, "lobby");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void switchTo(String name) {
        cardLayout.show(mainPanel, name);
    }

    public static void main(String[] args) {
        // ✅ 메인 스레드에서 실행 (Nimbus 적용 후)
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}