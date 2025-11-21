
// MafiaGameClientView.java
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.*;

public class MafiaGameClientView extends JFrame {
    private JPanel contentPane;
    private JTextField txtInput;
    private String UserName;
    private JButton btnSend;
    private JTextArea textArea;
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;

    // UI Components
    private JLabel lblPlayerCount;
    private JLabel lblPhaseInfo;
    private JPanel cardGridPanel;
    private PlayerCard[] playerCards;
    private Map<String, PlayerInfo> playerMap;
    private PlayerCard selectedCard;

    private String myRole = "";
    private String currentPhase = "WAITING";
    private boolean isDead = false;
    private int maxPlayers = 8;
    private boolean gameStartSoundPlayed = false;

    // Role image mapping
    private Map<String, String> roleImageMap;

    // Sound mappings
    private Map<String, String> roleSoundMap;
    private Map<String, String> phaseSoundMap;
    private Clip currentPhaseClip; // 현재 재생 중인 페이즈 사운드

    public MafiaGameClientView(String username, String ip_addr, String port_no) {
        initializeRoleImageMap();
        initializeRoleSoundMap();
        initializePhaseSoundMap();
        playerMap = new HashMap<>();

        setTitle("Mafia Game - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1200, 700);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(30, 30, 30));
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // Top status bar
        createTopStatusBar();

        // Left panel - Chat area
        createChatPanel();

        // Right panel - Player cards grid
        createPlayerCardGrid();

        setVisible(true);

        AppendText("Connecting to " + ip_addr + ":" + port_no + "...\n");
        UserName = username;

        try {
            socket = new Socket(ip_addr, Integer.parseInt(port_no));
            is = socket.getInputStream();
            dis = new DataInputStream(is);
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);

            SendMessage("/login " + UserName);
            ListenNetwork net = new ListenNetwork();
            net.start();

            Myaction action = new Myaction();
            btnSend.addActionListener(action);
            txtInput.addActionListener(action);
            txtInput.requestFocus();

        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
            AppendText("Connection error!\n");
        }
    }

    private void initializeRoleImageMap() {
        roleImageMap = new HashMap<>();
        roleImageMap.put("MAFIA", "mafia.png");
        roleImageMap.put("DOCTOR", "doctor.png");
        roleImageMap.put("POLICE", "police.png");
        roleImageMap.put("CITIZEN", "simin.png");
        roleImageMap.put("SOLDIER", "soldier.png");
        roleImageMap.put("POLITICIAN", "jeongchi.png");
        roleImageMap.put("GANGSTER", "geondal.png");
        roleImageMap.put("REPORTER", "gija.png");
        roleImageMap.put("SHAMAN", "yeongmae.png");
        roleImageMap.put("PRIEST", "seongzik.png");
        roleImageMap.put("MADAME", "dogul.png");
        roleImageMap.put("SPY", "tamjung.png");
        roleImageMap.put("DEFAULT", "default.png");
    }

    private void initializeRoleSoundMap() {
        roleSoundMap = new HashMap<>();

        // 마피아 진영
        roleSoundMap.put("MAFIA", "GameSound/Mafia_team/Mafia.wav");
        roleSoundMap.put("MADAME", "GameSound/Mafia_team/madam.wav");
        roleSoundMap.put("SPY", "GameSound/Mafia_team/spy_zupsun.wav");

        // 시민 진영
        roleSoundMap.put("DOCTOR", "GameSound/Citizen/doctor.wav");
        roleSoundMap.put("POLICE", "GameSound/Citizen/police.wav");
        roleSoundMap.put("POLITICIAN", "GameSound/Citizen/politician.wav");
        roleSoundMap.put("SOLDIER", "GameSound/Citizen/soldier.wav");
        roleSoundMap.put("SHAMAN", "GameSound/Citizen/SHAMAN.wav");
        roleSoundMap.put("REPORTER", "GameSound/Citizen/reporter.wav");
        roleSoundMap.put("GANGSTER", "GameSound/Citizen/gangster.wav");
        roleSoundMap.put("PRIEST", "GameSound/Citizen/priest.wav");
        roleSoundMap.put("GHOUL", "GameSound/Citizen/ghoul.wav");

        // 시민은 사운드 없음
        roleSoundMap.put("CITIZEN", null);
    }

    private void initializePhaseSoundMap() {
        phaseSoundMap = new HashMap<>();
        phaseSoundMap.put("NIGHT", "GameSound/night.wav");
        phaseSoundMap.put("DAY", "GameSound/morning.wav");
        phaseSoundMap.put("VOTE", "GameSound/vote.wav");
    }

    private void createTopStatusBar() {
        JPanel topBar = new JPanel();
        topBar.setBackground(new Color(40, 40, 40));
        topBar.setBounds(0, 0, 1200, 50);
        topBar.setLayout(null);
        contentPane.add(topBar);

        // Player count
        lblPlayerCount = new JLabel("0/8");
        lblPlayerCount.setFont(new Font("Arial", Font.BOLD, 18));
        lblPlayerCount.setForeground(Color.WHITE);
        lblPlayerCount.setBounds(20, 10, 100, 30);
        topBar.add(lblPlayerCount);

        // Phase info
        lblPhaseInfo = new JLabel("대기 중...");
        lblPhaseInfo.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        lblPhaseInfo.setForeground(new Color(255, 200, 100));
        lblPhaseInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblPhaseInfo.setBounds(400, 10, 400, 30);
        topBar.add(lblPhaseInfo);
    }

    private void createChatPanel() {
        JPanel chatPanel = new JPanel();
        chatPanel.setBackground(new Color(35, 35, 35));
        chatPanel.setBorder(new LineBorder(new Color(60, 60, 60), 1));
        chatPanel.setBounds(10, 60, 540, 590);
        chatPanel.setLayout(null);
        contentPane.add(chatPanel);

        // Chat title
        JLabel chatTitle = new JLabel("채팅");
        chatTitle.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        chatTitle.setForeground(new Color(200, 200, 200));
        chatTitle.setBounds(15, 10, 200, 25);
        chatPanel.add(chatTitle);

        // Chat area
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 40, 520, 490);
        scrollPane.setBorder(null);
        chatPanel.add(scrollPane);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        textArea.setBackground(new Color(25, 25, 25));
        textArea.setForeground(new Color(220, 220, 220));
        textArea.setCaretColor(Color.WHITE);
        scrollPane.setViewportView(textArea);

        // Input area
        txtInput = new JTextField();
        txtInput.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        txtInput.setBackground(new Color(45, 45, 45));
        txtInput.setForeground(Color.WHITE);
        txtInput.setCaretColor(Color.WHITE);
        txtInput.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(80, 80, 80), 1),
                new EmptyBorder(5, 10, 5, 10)));
        txtInput.setBounds(10, 540, 420, 40);
        chatPanel.add(txtInput);

        btnSend = new JButton("전송");
        btnSend.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnSend.setBackground(new Color(70, 130, 180));
        btnSend.setForeground(Color.WHITE);
        btnSend.setBorderPainted(false);
        btnSend.setFocusPainted(false);
        btnSend.setBounds(440, 540, 90, 40);
        chatPanel.add(btnSend);
    }

    private void createPlayerCardGrid() {
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(30, 30, 30));
        rightPanel.setBounds(560, 60, 620, 590);
        rightPanel.setLayout(null);
        contentPane.add(rightPanel);

        // Title
        JLabel gridTitle = new JLabel("플레이어");
        gridTitle.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        gridTitle.setForeground(new Color(200, 200, 200));
        gridTitle.setBounds(15, 10, 200, 25);
        rightPanel.add(gridTitle);

        // Card grid panel
        cardGridPanel = new JPanel();
        cardGridPanel.setBackground(new Color(30, 30, 30));
        cardGridPanel.setBounds(10, 45, 600, 535);
        cardGridPanel.setLayout(new GridLayout(2, 4, 15, 15));
        rightPanel.add(cardGridPanel);

        // Initialize player cards
        playerCards = new PlayerCard[maxPlayers];
        for (int i = 0; i < maxPlayers; i++) {
            playerCards[i] = new PlayerCard(i);
            cardGridPanel.add(playerCards[i]);
        }
    }

    // Player Card Component
    class PlayerCard extends JPanel {
        private int index;
        private JLabel imageLabel;
        private JLabel nameLabel;
        private JLabel statusIcon;
        private String playerName;
        private String role;
        private boolean isAlive = true;
        private boolean isEmpty = true;
        private Image roleImage;

        public PlayerCard(int index) {
            this.index = index;
            setLayout(null);
            setBackground(new Color(45, 45, 45));
            setBorder(new LineBorder(new Color(70, 70, 70), 2));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Image area
            imageLabel = new JLabel();
            imageLabel.setBounds(5, 5, 130, 180);
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setVerticalAlignment(SwingConstants.CENTER);
            add(imageLabel);

            // Status icon (top-right corner)
            statusIcon = new JLabel();
            statusIcon.setBounds(110, 10, 25, 25);
            statusIcon.setFont(new Font("Arial", Font.BOLD, 20));
            add(statusIcon);

            // Name label
            nameLabel = new JLabel("", SwingConstants.CENTER);
            nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setBounds(5, 190, 130, 25);
            add(nameLabel);

            // Empty state
            showEmptyState();

            // Click listener
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!isEmpty && isAlive && !isDead) {
                        handleCardClick();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isEmpty && isAlive && !isDead) {
                        setBorder(new LineBorder(new Color(100, 150, 200), 3));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedCard != PlayerCard.this) {
                        setBorder(new LineBorder(new Color(70, 70, 70), 2));
                    }
                }
            });
        }

        private void handleCardClick() {
            if (currentPhase.equals("NIGHT") || currentPhase.equals("VOTE")) {
                // Deselect previous card
                if (selectedCard != null && selectedCard != this) {
                    selectedCard.setBorder(new LineBorder(new Color(70, 70, 70), 2));
                }

                // Select this card
                selectedCard = this;
                setBorder(new LineBorder(new Color(255, 215, 0), 3));

                // Perform action
                performActionOnPlayer(playerName);
            }
        }

        public void setPlayer(String name, String role, boolean alive) {
            this.playerName = name;
            this.role = role;
            this.isAlive = alive;
            this.isEmpty = false;

            nameLabel.setText(name);

            // Load role image
            loadRoleImage(role);

            // Update status
            if (!alive) {
                statusIcon.setText("💀");
                statusIcon.setForeground(Color.RED);
                setBackground(new Color(60, 40, 40));
                applyGrayscale();
            } else {
                statusIcon.setText("");
                setBackground(new Color(45, 45, 45));
            }
        }

        public void clearPlayer() {
            this.isEmpty = true;
            this.playerName = null;
            this.role = null;
            this.isAlive = true;
            showEmptyState();
        }

        private void showEmptyState() {
            nameLabel.setText("");
            statusIcon.setText("");
            imageLabel.setIcon(null);
            imageLabel.setText("+");
            imageLabel.setFont(new Font("Arial", Font.PLAIN, 48));
            imageLabel.setForeground(new Color(100, 100, 100));
            setBackground(new Color(40, 40, 40));
            setBorder(new LineBorder(new Color(60, 60, 60), 2, true));
        }

        private void loadRoleImage(String role) {
            try {
                String imageName = roleImageMap.getOrDefault(role, "default.png");
                String imagePath = "info/" + imageName;

                BufferedImage img = ImageIO.read(new File(imagePath));
                Image scaledImg = img.getScaledInstance(130, 180, Image.SCALE_SMOOTH);
                roleImage = scaledImg;
                imageLabel.setIcon(new ImageIcon(scaledImg));
                imageLabel.setText("");
            } catch (IOException e) {
                imageLabel.setText("?");
                imageLabel.setFont(new Font("Arial", Font.BOLD, 48));
                imageLabel.setForeground(Color.GRAY);
            }
        }

        private void applyGrayscale() {
            if (roleImage != null) {
                BufferedImage buffered = new BufferedImage(
                        roleImage.getWidth(null),
                        roleImage.getHeight(null),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = buffered.createGraphics();
                g2d.drawImage(roleImage, 0, 0, null);
                g2d.dispose();

                for (int y = 0; y < buffered.getHeight(); y++) {
                    for (int x = 0; x < buffered.getWidth(); x++) {
                        int rgb = buffered.getRGB(x, y);
                        int alpha = (rgb >> 24) & 0xff;
                        int r = (rgb >> 16) & 0xff;
                        int g = (rgb >> 8) & 0xff;
                        int b = rgb & 0xff;

                        int gray = (r + g + b) / 3;
                        gray = (int) (gray * 0.5); // Darken

                        int newRgb = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                        buffered.setRGB(x, y, newRgb);
                    }
                }

                imageLabel.setIcon(new ImageIcon(buffered));
            }
        }
    }

    // Network listener
    class ListenNetwork extends Thread {
        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF();

                    if (msg.startsWith("ROLE:")) {
                        String role = msg.substring(5).trim();
                        myRole = role;
                        AppendText("당신의 역할: " + getRoleDisplayName(role) + "\n");

                        // Play game start sound effect (only once)
                        if (!gameStartSoundPlayed) {
                            playGameStartSound();
                            gameStartSoundPlayed = true;

                            // Play role sound after game start sound (with delay)
                            new Thread(() -> {
                                try {
                                    Thread.sleep(2000); // 2초 대기 후 역할 사운드 재생
                                    playRoleSound(role);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        } else {
                            // 게임 시작 사운드가 이미 재생된 경우 바로 역할 사운드 재생
                            playRoleSound(role);
                        }
                    } else if (msg.startsWith("PHASE:")) {
                        String phase = msg.substring(6).trim();
                        currentPhase = phase;
                        updatePhaseDisplay(phase);

                        // Play phase transition sound
                        playPhaseSound(phase);
                    } else if (msg.startsWith("PLAYERS:")) {
                        String players = msg.substring(8).trim();
                        updatePlayerCards(players);
                    } else if (msg.startsWith("DEAD:")) {
                        String status = msg.substring(5).trim();
                        if (status.equals("true")) {
                            isDead = true;
                            AppendText("=== 당신은 사망했습니다. ===\n");
                            // 사망 사운드는 제거 (마피아에게 죽었을 때만 재생)
                        } else if (status.equals("false")) {
                            isDead = false;
                            AppendText("=== 부활했습니다! ===\n");
                        }
                    } else {
                        AppendText(msg);

                        // 마피아에게 제거되었을 때만 Mafia.wav 재생
                        if (msg.contains("마피아에게 제거되었습니다")) {
                            playSound("GameSound/Mafia_team/Mafia.wav");
                        }
                    }

                } catch (IOException e) {
                    AppendText("연결이 끊어졌습니다!\n");
                    try {
                        dos.close();
                        dis.close();
                        socket.close();
                        break;
                    } catch (Exception ee) {
                        break;
                    }
                }
            }
        }
    }

    // Chat action
    class Myaction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnSend || e.getSource() == txtInput) {
                String msg = txtInput.getText().trim();
                if (msg.isEmpty())
                    return;

                String fullMsg = String.format("[%s] %s", UserName, msg);
                SendMessage(fullMsg);
                txtInput.setText("");
                txtInput.requestFocus();

                if (msg.contains("/exit")) {
                    System.exit(0);
                }
            }
        }
    }

    private void performActionOnPlayer(String targetPlayer) {
        if (isDead) {
            AppendText("사망한 플레이어는 행동할 수 없습니다.\n");
            return;
        }

        if (targetPlayer == null || targetPlayer.isEmpty()) {
            return;
        }

        // Remove [DEAD] prefix if exists
        if (targetPlayer.startsWith("[DEAD]")) {
            targetPlayer = targetPlayer.substring(6);
        }

        if (currentPhase.equals("NIGHT")) {
            String action = "";
            if (myRole.equals("MAFIA")) {
                action = "NIGHT_ACTION:MAFIA:" + targetPlayer;
                AppendText("선택: [" + targetPlayer + "] 제거\n");
            } else if (myRole.equals("SPY")) {
                action = "NIGHT_ACTION:SPY:" + targetPlayer;
                AppendText("선택: [" + targetPlayer + "] 역할 조사\n");
            } else if (myRole.equals("DOCTOR")) {
                action = "NIGHT_ACTION:DOCTOR:" + targetPlayer;
                AppendText("선택: [" + targetPlayer + "] 보호\n");
            } else if (myRole.equals("POLICE")) {
                action = "NIGHT_ACTION:POLICE:" + targetPlayer;
                AppendText("선택: [" + targetPlayer + "] 조사\n");
            } else if (myRole.equals("SHAMAN")) {
                action = "NIGHT_ACTION:SHAMAN:" + targetPlayer;
                AppendText("선택: [" + targetPlayer + "] 축복\n");
            } else if (myRole.equals("REPORTER")) {
                action = "NIGHT_ACTION:REPORTER:" + targetPlayer;
                AppendText("선택: [" + targetPlayer + "] 취재\n");
            } else if (myRole.equals("GANGSTER")) {
                action = "NIGHT_ACTION:GANGSTER:" + targetPlayer;
                AppendText("선택: [" + targetPlayer + "] 투표 금지\n");
            } else if (myRole.equals("PRIEST")) {
                action = "NIGHT_ACTION:PRIEST:" + targetPlayer;
                AppendText("선택: [" + targetPlayer + "] 부활\n");
            }
            if (!action.isEmpty()) {
                SendMessage(action);
            }
        } else if (currentPhase.equals("VOTE")) {
            SendMessage("VOTE:" + targetPlayer);
            AppendText("투표: [" + targetPlayer + "]\n");
        }
    }

    private void updatePhaseDisplay(String phase) {
        switch (phase) {
            case "WAITING":
                lblPhaseInfo.setText("게임 시작 대기 중...");
                lblPhaseInfo.setForeground(new Color(200, 200, 200));
                break;
            case "NIGHT":
                lblPhaseInfo.setText("🌙 밤 - 능력을 사용하세요");
                lblPhaseInfo.setForeground(new Color(100, 100, 255));
                break;
            case "DAY":
                lblPhaseInfo.setText("☀️ 낮 - 토론 시간");
                lblPhaseInfo.setForeground(new Color(255, 200, 100));
                break;
            case "VOTE":
                lblPhaseInfo.setText("🗳️ 투표 시간");
                lblPhaseInfo.setForeground(new Color(255, 100, 100));
                break;
            default:
                lblPhaseInfo.setText(phase);
                lblPhaseInfo.setForeground(Color.WHITE);
        }
    }

    private void updatePlayerCards(String players) {
        // Clear all cards first
        for (PlayerCard card : playerCards) {
            card.clearPlayer();
        }

        playerMap.clear();

        if (!players.isEmpty()) {
            String[] playerArray = players.split(",");
            lblPlayerCount.setText(playerArray.length + "/" + maxPlayers);

            for (int i = 0; i < playerArray.length && i < maxPlayers; i++) {
                String playerInfo = playerArray[i].trim();
                if (!playerInfo.isEmpty()) {
                    boolean isDead = playerInfo.startsWith("[DEAD]");
                    String playerName = isDead ? playerInfo.substring(6) : playerInfo;

                    // For now, we don't know other players' roles, so use default
                    String displayRole = playerName.equals(UserName) ? myRole : "DEFAULT";

                    playerCards[i].setPlayer(playerName, displayRole, !isDead);

                    PlayerInfo info = new PlayerInfo(playerName, displayRole, !isDead);
                    playerMap.put(playerName, info);
                }
            }
        } else {
            lblPlayerCount.setText("0/" + maxPlayers);
        }
    }

    private String getRoleDisplayName(String role) {
        switch (role) {
            case "MAFIA":
                return "마피아";
            case "MADAME":
                return "마담";
            case "SPY":
                return "스파이";
            case "DOCTOR":
                return "의사";
            case "POLICE":
                return "경찰";
            case "POLITICIAN":
                return "정치인";
            case "SOLDIER":
                return "군인";
            case "SHAMAN":
                return "영매";
            case "REPORTER":
                return "기자";
            case "GANGSTER":
                return "건달";
            case "PRIEST":
                return "성직자";
            case "CITIZEN":
                return "시민";
            default:
                return role;
        }
    }

    public void AppendText(String msg) {
        textArea.append(msg);
        textArea.setCaretPosition(textArea.getText().length());
    }

    public void SendMessage(String msg) {
        try {
            dos.writeUTF(msg);
        } catch (IOException e) {
            AppendText("메시지 전송 실패.\n");
            try {
                dos.close();
                dis.close();
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                System.exit(0);
            }
        }
    }

    // Play game start sound effect
    private void playGameStartSound() {
        // GameSound 폴더의 game_start.wav 사용
        playSound("GameSound/game_start.wav");
    }

    // Play role-specific sound
    private void playRoleSound(String role) {
        String soundPath = roleSoundMap.get(role);
        if (soundPath != null) {
            playSound(soundPath);
        }
    }

    // Play phase transition sound
    private void playPhaseSound(String phase) {
        // 이전 페이즈 사운드 중지
        stopCurrentPhaseSound();

        String soundPath = phaseSoundMap.get(phase);
        if (soundPath != null) {
            playPhaseClip(soundPath);
        }
    }

    // Stop current phase sound
    private void stopCurrentPhaseSound() {
        if (currentPhaseClip != null && currentPhaseClip.isRunning()) {
            currentPhaseClip.stop();
            currentPhaseClip.close();
            currentPhaseClip = null;
            System.out.println("Stopped previous phase sound");
        }
    }

    // Play phase sound with clip tracking
    private void playPhaseClip(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                File soundFile = new File(filePath);
                if (!soundFile.exists()) {
                    System.err.println("Sound file not found: " + filePath);
                    return;
                }

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                currentPhaseClip = AudioSystem.getClip();
                currentPhaseClip.open(audioInputStream);
                currentPhaseClip.start();

                System.out.println("Playing phase sound: " + soundFile.getName());

                // Wait for the clip to finish
                currentPhaseClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        currentPhaseClip.close();
                        try {
                            audioInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        currentPhaseClip = null;
                    }
                });
            } catch (UnsupportedAudioFileException e) {
                System.err.println("Audio format not supported: " + filePath);
            } catch (Exception e) {
                System.err.println("Error playing phase sound: " + filePath + " - " + e.getMessage());
            }
        }).start();
    }

    // Unified sound playback method
    private void playSound(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                File soundFile = new File(filePath);
                if (!soundFile.exists()) {
                    System.err.println("Sound file not found: " + filePath);
                    return;
                }

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();

                System.out.println("Playing sound: " + soundFile.getName());

                // Wait for the clip to finish
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                        try {
                            audioInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (UnsupportedAudioFileException e) {
                System.err.println("Audio format not supported: " + filePath);
            } catch (Exception e) {
                System.err.println("Error playing sound: " + filePath + " - " + e.getMessage());
            }
        }).start();
    }

    // Helper class
    class PlayerInfo {
        String name;
        String role;
        boolean alive;

        PlayerInfo(String name, String role, boolean alive) {
            this.name = name;
            this.role = role;
            this.alive = alive;
        }
    }
}
