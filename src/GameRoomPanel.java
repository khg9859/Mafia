import client.GameClient;
import protocol.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 게임방 내부 화면 - 플레이어 목록 + 채팅
 */
public class GameRoomPanel extends JPanel implements GameClient.MessageListener {
    private MainFrame frame;
    private GameClient client;
    private int roomId;
    private String roomName;

    private JTextArea chatArea;
    private JTextField chatInput;
    private JPanel playerListPanel;
    private DefaultListModel<String> playerListModel;
    private JList<String> playerList;

    public GameRoomPanel(MainFrame frame, int roomId, String roomName, GameClient client) {
        this.frame = frame;
        this.roomId = roomId;
        this.roomName = roomName;
        this.client = client;

        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 20));

        String fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        // 상단 헤더
        JPanel header = createHeader(fontName);
        add(header, BorderLayout.NORTH);

        // 왼쪽 플레이어 목록
        JPanel leftPanel = createPlayerListPanel(fontName);
        add(leftPanel, BorderLayout.WEST);

        // 오른쪽 채팅 영역
        JPanel rightPanel = createChatPanel(fontName);
        add(rightPanel, BorderLayout.CENTER);

        // 메시지 리스너 등록
        client.addMessageListener(this);
    }

    // 상단 헤더
    private JPanel createHeader(String fontName) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 30));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("🎮 " + roomName);
        titleLabel.setForeground(Color.WHITE);
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

    // 왼쪽 플레이어 목록
    private JPanel createPlayerListPanel(String fontName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(250, 0));
        panel.setBackground(new Color(25, 25, 25));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("👥 플레이어 목록");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(fontName, Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        playerListModel = new DefaultListModel<>();
        playerList = new JList<>(playerListModel);
        playerList.setBackground(new Color(35, 35, 35));
        playerList.setForeground(Color.WHITE);
        playerList.setFont(new Font(fontName, Font.PLAIN, 14));
        playerList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(playerList);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(35, 35, 35));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // 오른쪽 채팅 영역
    private JPanel createChatPanel(String fontName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 채팅 히스토리
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(30, 30, 30));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font(fontName, Font.PLAIN, 14));
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(null);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // 입력 영역
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(new Color(20, 20, 20));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        chatInput = new JTextField();
        chatInput.setBackground(new Color(40, 40, 40));
        chatInput.setForeground(Color.WHITE);
        chatInput.setFont(new Font(fontName, Font.PLAIN, 14));
        chatInput.setCaretColor(Color.WHITE);
        chatInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JButton sendBtn = new JButton("전송");
        sendBtn.setBackground(new Color(70, 130, 180));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFont(new Font(fontName, Font.BOLD, 14));
        sendBtn.setFocusPainted(false);
        sendBtn.setOpaque(true);
        sendBtn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        // 전송 버튼 이벤트
        sendBtn.addActionListener(e -> sendChat());

        // Enter 키로 전송
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

    /**
     * 채팅 메시지 전송
     */
    private void sendChat() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            client.sendChatMessage(message);
            chatInput.setText("");
        }
    }

    /**
     * 방 나가기
     */
    private void leaveRoom() {
        client.leaveRoom();
        client.removeMessageListener(this);
        frame.switchTo("lobby");
    }

    /**
     * 서버로부터 메시지 수신 (콜백)
     */
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

                default:
                    break;
            }
        });
    }

    /**
     * 플레이어 목록 업데이트
     */
    private void updatePlayerList(String playerData) {
        playerListModel.clear();
        if (playerData != null && !playerData.isEmpty()) {
            String[] players = playerData.split("\\|");
            for (String player : players) {
                playerListModel.addElement("🟢 " + player);
            }
        }
    }

    /**
     * 채팅 메시지 처리
     */
    private void handleChatMessage(String data) {
        String[] parts = data.split("\\|", 2);
        if (parts.length == 2) {
            String nickname = parts[0];
            String message = parts[1];
            addChatMessage(nickname, message);
        }
    }

    /**
     * 채팅 메시지 추가
     */
    private void addChatMessage(String nickname, String message) {
        chatArea.append("[" + nickname + "] " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    /**
     * 시스템 메시지 추가
     */
    private void addSystemMessage(String message) {
        chatArea.append("📢 " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
