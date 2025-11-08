import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private ChannelSelectPanel channelPanel;
    private LoginPanel loginPanel;
    private LobbyPanel lobbyPanel;

    public MainFrame() {
        setTitle("Mafia42 - Java GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

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
        cardLayout.show(mainPanel, "lobby");  // ✅ 실제 전환
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void switchTo(String name) {
        cardLayout.show(mainPanel, name);
    }

    public static void main(String[] args) {
        new MainFrame();
    }
}