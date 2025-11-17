import client.TestGameClient;
import protocol.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * 테스트용 게임방 패널 - TestGameClient 사용
 */
public class TestGameRoomPanel extends JPanel implements TestGameClient.MessageListener {
    private TestMainFrame frame;
    private TestGameClient client;
    private int roomId;
    private String roomName;

    private JTextArea chatArea;
    private JTextField chatInput;
    private JPanel playerSlotsPanel;
    private List<PlayerSlot> playerSlots;
    private JButton startGameBtn;
    private String myNickname;

    private class PlayerSlot extends JPanel {
        private JLabel iconLabel;
        private JLabel nicknameLabel;
        private String playerName;

        public PlayerSlot(String fontName) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
            setBackground(new Color(40, 40, 40));
            setPreferredSize(new Dimension(200, 80));
            setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2));

            iconLabel = new JLabel("?");
            iconLabel.setFont(new Font(fontName, Font.BOLD, 40));
            iconLabel.setForeground(Color.GRAY);
            iconLabel.setPreferredSize(new Dimension(60, 60));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

            nicknameLabel = new JLabel("빈 슬롯");
            nicknameLabel.setFont(new Font(fontName, Font.BOLD, 14));
            nicknameLabel.setForeground(Color.GRAY);

            add(iconLabel);
            add(nicknameLabel);
        }

        public void setPlayer(String nickname) {
            this.playerName = nickname;
            iconLabel.setText("👤");
            iconLabel.setForeground(Color.WHITE);
            nicknameLabel.setText(nickname);
            nicknameLabel.setForeground(Color.WHITE);
            setBackground(new Color(50, 80, 50));
        }

        public void clearPlayer() {
            this.playerName = null;
            iconLabel.setText("?");
            iconLabel.setForeground(Color.GRAY);
            nicknameLabel.setText("빈 슬롯");
            nicknameLabel.setForeground(Color.GRAY);
            setBackground(new Color(40, 40, 40));
        }

        public boolean isEmpty() {
            return playerName == null;
        }
    }

    public TestGameRoomPanel(TestMainFrame frame, int roomId, String roomName, TestGameClient client, String myNickname) {
        this.frame = frame;
        this.roomId = roomId;
        this.roomName = roomName;
        this.client = client;
        this.myNickname = myNickname;
        this.playerSlots = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 20));

        String fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        JPanel header = createHeader(fontName);
        add(header, BorderLayout.NORTH);

        JPanel centerPanel = createCenterPanel(fontName);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = createChatPanel(fontName);
        add(bottomPanel, BorderLayout.SOUTH);

        client.addMessageListener(this);
    }

    private JPanel createHeader(String fontName) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 30));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("🧪 " + roomName + " [테스트 모드]");
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setFont(new Font(fontName, Font.BOLD, 20));

        JButton leaveBtn = new JButton("방 나가기");
        leaveBtn.setBackground(new Color(180, 70, 70));
        leaveBtn.setForeground(Color.WHITE);
        leaveBtn.setFont(new Font(fontName, Font.BOLD, 14));
        leaveBtn.setFocusPainted(false);
        leaveBtn.setOpaque(true);
        leaveBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        leaveBtn.addActionListener(e -> leaveRoom());

        header.add(titleLabel, BorderLayout.WEST);
        header.add(leaveBtn, BorderLayout.EAST);

        return header;
    }

    private JPanel createCenterPanel(String fontName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(25, 25, 25));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        // 테스트용: 2개 슬롯만 표시
        playerSlotsPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        playerSlotsPanel.setBackground(new Color(25, 25, 25));

        for (int i = 0; i < 2; i++) {
            PlayerSlot slot = new PlayerSlot(fontName);
            playerSlots.add(slot);
            playerSlotsPanel.add(slot);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(25, 25, 25));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        startGameBtn = new JButton("게임 시작 (테스트)");
        startGameBtn.setFont(new Font(fontName, Font.BOLD, 18));
        startGameBtn.setBackground(new Color(220, 180, 80));
        startGameBtn.setForeground(Color.BLACK);
        startGameBtn.setFocusPainted(false);
        startGameBtn.setOpaque(true);
        startGameBtn.setPreferredSize(new Dimension(250, 50));
        startGameBtn.setBorder(BorderFactory.createEmptyBorder());

        startGameBtn.addActionListener(e -> startGame());

        buttonPanel.add(startGameBtn);

        panel.add(playerSlotsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createChatPanel(String fontName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 30));
        panel.setPreferredSize(new Dimension(0, 180));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(40, 40, 40));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font(fontName, Font.PLAIN, 13));
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(new Color(30, 30, 30));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        chatInput = new JTextField();
        chatInput.setBackground(new Color(50, 50, 50));
        chatInput.setForeground(Color.WHITE);
        chatInput.setFont(new Font(fontName, Font.PLAIN, 14));
        chatInput.setCaretColor(Color.WHITE);
        chatInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JButton sendBtn = new JButton("▶");
        sendBtn.setBackground(new Color(80, 150, 200));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFont(new Font(fontName, Font.BOLD, 16));
        sendBtn.setFocusPainted(false);
        sendBtn.setOpaque(true);
        sendBtn.setPreferredSize(new Dimension(60, 35));
        sendBtn.setBorder(BorderFactory.createEmptyBorder());

        sendBtn.addActionListener(e -> sendChat());

        chatInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendChat();
                }
            }
        });

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        panel.add(chatScroll, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void sendChat() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            client.sendChatMessage(message);
            chatInput.setText("");
        }
    }

    private void startGame() {
        client.requestGameStart();
    }

    private void leaveRoom() {
        client.leaveRoom();
        client.removeMessageListener(this);
        frame.switchTo("lobby");
    }

    @Override
    public void onMessageReceived(Message msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {
                case PLAYER_LIST:
                    updatePlayerList(msg.getData());
                    break;

                case PLAYER_JOINED:
                    addSystemMessage(msg.getData() + "님이 입장했습니다.");
                    break;

                case PLAYER_LEFT:
                    addSystemMessage(msg.getData() + "님이 퇴장했습니다.");
                    break;

                case CHAT_MESSAGE:
                    handleChatMessage(msg.getData());
                    break;

                case SYSTEM_MESSAGE:
                    addSystemMessage(msg.getData());
                    break;

                case GAME_START:
                    handleGameStart();
                    break;

                default:
                    break;
            }
        });
    }

    private void updatePlayerList(String playerData) {
        for (PlayerSlot slot : playerSlots) {
            slot.clearPlayer();
        }

        if (playerData != null && !playerData.isEmpty()) {
            String[] players = playerData.split("\\|");
            for (int i = 0; i < players.length && i < playerSlots.size(); i++) {
                playerSlots.get(i).setPlayer(players[i]);
            }
        }
    }

    private void handleChatMessage(String data) {
        String[] parts = data.split("\\|", 2);
        if (parts.length == 2) {
            String nickname = parts[0];
            String message = parts[1];
            addChatMessage(nickname, message);
        }
    }

    private void addChatMessage(String nickname, String message) {
        chatArea.append("[" + nickname + "] " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void addSystemMessage(String message) {
        chatArea.append("📢 " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void handleGameStart() {
        int playerCount = 0;
        for (PlayerSlot slot : playerSlots) {
            if (!slot.isEmpty()) {
                playerCount++;
            }
        }

        String myRole = assignRole(playerCount);

        client.removeMessageListener(this);
        frame.showGamePlay(roomId, roomName, myNickname, myRole);
    }

    private String assignRole(int playerCount) {
        String[] roles = {"MAFIA", "CITIZEN"};
        return roles[(int)(Math.random() * roles.length)];
    }
}
