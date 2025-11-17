import client.TestGameClient;
import database.RoomDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 테스트용 로비 패널 - TestGameClient 사용
 */
public class TestLobbyPanel extends JPanel {
    private TestMainFrame frame;
    private TestGameClient client;

    public TestLobbyPanel(TestMainFrame frame, String nickname, TestGameClient client) {
        this.frame = frame;
        this.client = client;

        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 20));

        String fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        JPanel navBar = createNavBar(fontName);
        add(navBar, BorderLayout.NORTH);

        JPanel leftPanel = createLeftPanel(nickname, fontName);
        add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = createRightPanel(fontName);
        add(rightPanel, BorderLayout.CENTER);
    }

    private JPanel createNavBar(String fontName) {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        navBar.setBackground(new Color(30, 30, 30));

        JLabel titleLabel = new JLabel("🧪 테스트 로비 (포트 9998)");
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setFont(new Font(fontName, Font.BOLD, 18));
        navBar.add(titleLabel);

        return navBar;
    }

    private JPanel createLeftPanel(String nickname, String fontName) {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBackground(new Color(25, 25, 25));

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setBackground(new Color(25, 25, 25));
        userInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel nameLabel = new JLabel(nickname, SwingConstants.CENTER);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font(fontName, Font.BOLD, 18));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel testLabel = new JLabel("테스트 모드", SwingConstants.CENTER);
        testLabel.setForeground(Color.YELLOW);
        testLabel.setFont(new Font(fontName, Font.BOLD, 14));
        testLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        userInfo.add(nameLabel);
        userInfo.add(Box.createRigidArea(new Dimension(0, 10)));
        userInfo.add(testLabel);

        leftPanel.add(userInfo, BorderLayout.CENTER);

        return leftPanel;
    }

    private JPanel createRightPanel(String fontName) {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(35, 35, 35));

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topButtons.setBackground(new Color(40, 40, 40));

        JLabel infoLabel = new JLabel("테스트 모드: 2명으로 게임 시작 가능");
        infoLabel.setForeground(Color.YELLOW);
        infoLabel.setFont(new Font(fontName, Font.BOLD, 14));
        topButtons.add(infoLabel);

        JPanel roomList = new JPanel();
        roomList.setLayout(new BoxLayout(roomList, BoxLayout.Y_AXIS));
        roomList.setBackground(new Color(35, 35, 35));

        List<RoomDAO.Room> rooms = RoomDAO.getAllRooms();
        for (RoomDAO.Room roomData : rooms) {
            JPanel room = new JPanel(new BorderLayout());
            room.setMaximumSize(new Dimension(700, 60));
            room.setBackground(new Color(45, 45, 45));
            room.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            room.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel roomTitle = new JLabel(roomData.roomId + ". " + roomData.roomName + " [테스트]");
            roomTitle.setForeground(Color.WHITE);
            roomTitle.setFont(new Font(fontName, Font.PLAIN, 14));

            JLabel roomCount = new JLabel(roomData.currentPlayers + "/2", SwingConstants.CENTER);
            roomCount.setForeground(Color.GREEN);
            roomCount.setFont(new Font(fontName, Font.PLAIN, 13));

            room.add(roomTitle, BorderLayout.WEST);
            room.add(roomCount, BorderLayout.EAST);

            final int roomId = roomData.roomId;
            final String roomName = roomData.roomName;
            room.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    joinRoom(roomId, roomName);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    room.setBackground(new Color(60, 60, 60));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    room.setBackground(new Color(45, 45, 45));
                }
            });

            roomList.add(room);
            roomList.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JScrollPane scrollPane = new JScrollPane(roomList);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(35, 35, 35));

        rightPanel.add(topButtons, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        return rightPanel;
    }

    private void joinRoom(int roomId, String roomName) {
        System.out.println("🧪 [테스트] 방 입장 시도: " + roomName + " (ID: " + roomId + ")");
        client.joinRoom(roomId);
        frame.showGameRoom(roomId, roomName);
    }
}
