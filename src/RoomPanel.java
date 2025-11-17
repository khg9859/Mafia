import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class RoomPanel extends JPanel {
    private JTextArea chatArea;
    private JTextField txtInput;
    private String nickname;
    private int roomId;
    private String roomName;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private MainFrame frame;
    private String fontName;
    private JPanel playerListPanel;
    private JPanel playerCardsPanel;
    private ArrayList<String> players;
    private int maxPlayers;
    private JLabel lblRoomInfo;

    public RoomPanel(MainFrame frame, String nickname, int roomId, String roomName, int maxPlayers,
                     Socket socket, DataInputStream dis, DataOutputStream dos) {
        this.frame = frame;
        this.nickname = nickname;
        this.roomId = roomId;
        this.roomName = roomName;
        this.maxPlayers = maxPlayers;
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
        this.players = new ArrayList<>();
        this.players.add(nickname); // 자신을 먼저 추가

        fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 20));

        // 상단 패널 (방 제목 + 나가기 버튼)
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 메인 컨텐츠 (왼쪽: 참여자, 중앙: 채팅, 오른쪽: 플레이어 카드)
        JPanel mainContent = createMainContent();
        add(mainContent, BorderLayout.CENTER);

        // 서버 메시지 수신 시작
        new RoomListener().start();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(30, 30, 30));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblRoomName = new JLabel("마피아 42 / " + maxPlayers);
        lblRoomName.setForeground(Color.WHITE);
        lblRoomName.setFont(new Font(fontName, Font.BOLD, 20));

        lblRoomInfo = new JLabel(players.size() + " / " + maxPlayers);
        lblRoomInfo.setForeground(Color.LIGHT_GRAY);
        lblRoomInfo.setFont(new Font(fontName, Font.PLAIN, 16));

        JButton btnLeave = new JButton("방 나가기");
        btnLeave.setBackground(new Color(200, 50, 50));
        btnLeave.setForeground(Color.WHITE);
        btnLeave.setFont(new Font(fontName, Font.BOLD, 14));
        btnLeave.setFocusPainted(false);
        btnLeave.addActionListener(e -> leaveRoom());

        topPanel.add(lblRoomName, BorderLayout.WEST);
        topPanel.add(lblRoomInfo, BorderLayout.CENTER);
        topPanel.add(btnLeave, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createMainContent() {
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(20, 20, 20));

        // 왼쪽: 참여자 목록
        JPanel leftPanel = createLeftPanel();
        mainContent.add(leftPanel, BorderLayout.WEST);

        // 중앙: 낮/밤 시간 + 채팅
        JPanel centerPanel = createCenterPanel();
        mainContent.add(centerPanel, BorderLayout.CENTER);

        // 오른쪽: 플레이어 카드
        JPanel rightPanel = createRightPanel();
        mainContent.add(rightPanel, BorderLayout.EAST);

        return mainContent;
    }

    // 왼쪽: 참여자 목록
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(180, 0));
        leftPanel.setBackground(new Color(25, 25, 25));
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("참여자");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font(fontName, Font.BOLD, 16));
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        playerListPanel = new JPanel();
        playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
        playerListPanel.setBackground(new Color(25, 25, 25));

        updatePlayerList();

        JScrollPane scrollPane = new JScrollPane(playerListPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(25, 25, 25));

        leftPanel.add(lblTitle, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        return leftPanel;
    }

    // 중앙: 낮/밤 시간 + 채팅
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(35, 35, 35));
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 낮/밤 시간 표시
        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.setBackground(new Color(40, 40, 40));
        timePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblTime = new JLabel("낮 시간", SwingConstants.CENTER);
        lblTime.setForeground(new Color(255, 200, 100));
        lblTime.setFont(new Font(fontName, Font.BOLD, 18));

        JTextArea timeDescription = new JTextArea();
        timeDescription.setEditable(false);
        timeDescription.setBackground(new Color(40, 40, 40));
        timeDescription.setForeground(Color.LIGHT_GRAY);
        timeDescription.setFont(new Font(fontName, Font.PLAIN, 12));
        timeDescription.setText("게임이 시작되었습니다!\n게임이 시작되었습니다.\n밤이 되었습니다...\n마피아가 플레이어를 제거했습니다.\n낮이 되었습니다. 투표를 시작합니다.");
        timeDescription.setLineWrap(true);
        timeDescription.setWrapStyleWord(true);

        timePanel.add(lblTime, BorderLayout.NORTH);
        timePanel.add(timeDescription, BorderLayout.CENTER);

        // 채팅 영역
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(30, 30, 30));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font(fontName, Font.PLAIN, 13));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(new LineBorder(new Color(50, 50, 50)));

        // 입력 영역
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBackground(new Color(35, 35, 35));
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel lblInputTitle = new JLabel("투표");
        lblInputTitle.setForeground(Color.WHITE);
        lblInputTitle.setFont(new Font(fontName, Font.PLAIN, 12));

        txtInput = new JTextField();
        txtInput.setFont(new Font(fontName, Font.PLAIN, 13));
        txtInput.setBackground(new Color(50, 50, 50));
        txtInput.setForeground(Color.WHITE);
        txtInput.setCaretColor(Color.WHITE);
        txtInput.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(100, 150, 255), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton btnSend = new JButton("전송");
        btnSend.setBackground(new Color(60, 120, 200));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font(fontName, Font.BOLD, 13));
        btnSend.setFocusPainted(false);
        btnSend.setPreferredSize(new Dimension(70, 35));

        txtInput.addActionListener(e -> sendMessage());
        btnSend.addActionListener(e -> sendMessage());

        JPanel inputRow = new JPanel(new BorderLayout(5, 0));
        inputRow.setBackground(new Color(35, 35, 35));
        inputRow.add(txtInput, BorderLayout.CENTER);
        inputRow.add(btnSend, BorderLayout.EAST);

        inputPanel.add(lblInputTitle, BorderLayout.NORTH);
        inputPanel.add(inputRow, BorderLayout.CENTER);

        centerPanel.add(timePanel, BorderLayout.NORTH);
        centerPanel.add(chatScroll, BorderLayout.CENTER);
        centerPanel.add(inputPanel, BorderLayout.SOUTH);

        return centerPanel;
    }

    // 오른쪽: 플레이어 카드
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(550, 0));
        rightPanel.setBackground(new Color(25, 25, 25));
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("내 역할: 시민");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font(fontName, Font.BOLD, 16));
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        playerCardsPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        playerCardsPanel.setBackground(new Color(25, 25, 25));

        updatePlayerCards();

        rightPanel.add(lblTitle, BorderLayout.NORTH);
        rightPanel.add(playerCardsPanel, BorderLayout.CENTER);

        return rightPanel;
    }

    private void updatePlayerList() {
        playerListPanel.removeAll();

        for (int i = 0; i < maxPlayers; i++) {
            JLabel playerLabel = new JLabel();
            playerLabel.setFont(new Font(fontName, Font.PLAIN, 14));
            playerLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

            if (i < players.size()) {
                playerLabel.setText("● 플레이어" + (i + 1));
                playerLabel.setForeground(Color.WHITE);
            } else {
                playerLabel.setText("○ 플레이어" + (i + 1));
                playerLabel.setForeground(new Color(100, 100, 100));
            }

            playerListPanel.add(playerLabel);
        }

        playerListPanel.revalidate();
        playerListPanel.repaint();
    }

    private void updatePlayerCards() {
        playerCardsPanel.removeAll();
        
        // 그리드 레이아웃 재설정 (3열 고정, 행은 인원수에 따라)
        int cols = 3;
        int rows = (int) Math.ceil(maxPlayers / 3.0);
        playerCardsPanel.setLayout(new GridLayout(rows, cols, 10, 10));

        for (int i = 0; i < maxPlayers; i++) {
            JPanel card = createPlayerCard(i + 1, i < players.size());
            playerCardsPanel.add(card);
        }

        playerCardsPanel.revalidate();
        playerCardsPanel.repaint();
    }

    private JPanel createPlayerCard(int playerNum, boolean isActive) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(40, 40, 40));
        card.setBorder(new LineBorder(isActive ? new Color(100, 150, 255) : new Color(60, 60, 60), 2));

        JLabel lblPlayerNum = new JLabel("P" + playerNum, SwingConstants.CENTER);
        lblPlayerNum.setForeground(Color.LIGHT_GRAY);
        lblPlayerNum.setFont(new Font(fontName, Font.BOLD, 12));
        lblPlayerNum.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel lblCard = new JLabel("?", SwingConstants.CENTER);
        lblCard.setForeground(new Color(150, 150, 150));
        lblCard.setFont(new Font(fontName, Font.BOLD, 80));

        JLabel lblStatus = new JLabel(isActive ? "대기중" : "대기중", SwingConstants.CENTER);
        lblStatus.setForeground(isActive ? Color.GREEN : new Color(100, 100, 100));
        lblStatus.setFont(new Font(fontName, Font.PLAIN, 11));
        lblStatus.setBorder(new EmptyBorder(5, 0, 5, 0));

        card.add(lblPlayerNum, BorderLayout.NORTH);
        card.add(lblCard, BorderLayout.CENTER);
        card.add(lblStatus, BorderLayout.SOUTH);

        return card;
    }

    private void sendMessage() {
        String message = txtInput.getText().trim();
        if (!message.isEmpty()) {
            try {
                // 서버로 메시지 전송
                dos.writeUTF(message);
                txtInput.setText("");
                txtInput.requestFocus();
            } catch (IOException e) {
                appendChat("메시지 전송 실패");
                e.printStackTrace();
            }
        }
    }

    private void leaveRoom() {
        try {
            if (dos != null) {
                dos.writeUTF("/leave_room");
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 연결이 끊어진 경우에도 로비로 돌아가기
            SwingUtilities.invokeLater(() -> {
                LobbyPanel lobbyPanel = new LobbyPanel(frame, nickname);
                frame.switchPanel(lobbyPanel);
            });
        }
    }

    private void appendChat(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getText().length());
        });
    }

    class RoomListener extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    String message = dis.readUTF();
                    
                    if (message.startsWith("/leave_success")) {
                        // 로비로 돌아가기
                        SwingUtilities.invokeLater(() -> {
                            LobbyPanel lobbyPanel = new LobbyPanel(frame, nickname);
                            frame.switchPanel(lobbyPanel);
                        });
                        break;
                    } else if (message.startsWith("/player_joined ")) {
                        String playerName = message.substring(15);
                        players.add(playerName);
                        SwingUtilities.invokeLater(() -> {
                            updatePlayerList();
                            updatePlayerCards();
                            if (lblRoomInfo != null) {
                                lblRoomInfo.setText(players.size() + " / " + maxPlayers);
                            }
                        });
                        appendChat(">>> " + playerName + "님이 입장하였습니다.");
                    } else if (message.startsWith("/player_left ")) {
                        String playerName = message.substring(13);
                        players.remove(playerName);
                        SwingUtilities.invokeLater(() -> {
                            updatePlayerList();
                            updatePlayerCards();
                            if (lblRoomInfo != null) {
                                lblRoomInfo.setText(players.size() + " / " + maxPlayers);
                            }
                        });
                        appendChat("<<< " + playerName + "님이 퇴장하였습니다.");
                    } else {
                        // 일반 채팅 메시지 표시
                        appendChat(message);
                    }
                }
            } catch (IOException e) {
                appendChat("서버 연결이 끊어졌습니다.");
            }
        }
    }
}
