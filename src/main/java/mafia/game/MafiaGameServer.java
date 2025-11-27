package mafia.game;

// MafiaGameServer.java
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.io.File;
import javax.sound.sampled.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class MafiaGameServer extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    JTextArea textArea;
    private JTextField txtPortNumber;
    private JButton btnGameStart;

    private ServerSocket socket;
    private Socket client_socket;
    private Vector<UserService> UserVec = new Vector<>();

    // 게임 상태 변수
    private boolean gameStarted = false;
    private String gamePhase = "WAITING"; // WAITING, NIGHT, DAY, VOTE, RESULT
    private int dayCount = 0;
    private Map<String, Boolean> aliveStatus = new HashMap<>();
    private Map<String, Integer> voteCount = new HashMap<>();
    private Map<String, String> nightActions = new HashMap<>(); // 밤 행동 저장
    private Map<String, Boolean> soldierShield = new HashMap<>(); // 군인의 방어막 상태 (true = 방어막 있음)
    private Map<String, Boolean> blessedStatus = new HashMap<>(); // 성불 상태 (true = 성불됨)
    private boolean spyContactedMafia = false; // 스파이가 마피아와 접선했는지 여부
    private String mafiaName = ""; // 마피아 이름
    private String spyName = ""; // 스파이 이름
    private String shamanName = ""; // 영매 이름
    private String reporterTarget = ""; // 기자가 선택한 타겟
    private String reporterTargetRole = ""; // 기자 타겟의 직업
    private int nightCount = 0; // 밤 카운트 (기자 능력 사용 제한용)
    private boolean reporterUsed = false; // 기자가 능력을 사용했는지 여부
    private String ghoulName = ""; // 도굴꾼 이름
    private boolean ghoulTransformed = false; // 도굴꾼이 변신했는지 여부
    private String ghoulVictim = ""; // 도굴꾼이 직업을 가져간 사람 (부활 시 시민이 됨)
    private Map<String, Boolean> voteBanned = new HashMap<>(); // 건달에 의해 투표 금지된 플레이어 (true = 투표 불가)
    private boolean priestUsed = false; // 성직자가 소생 능력을 사용했는지 여부
    private String priestTarget = ""; // 성직자가 선택한 부활 대상
    private String madameName = ""; // 마담 이름
    private Map<String, Boolean> seduced = new HashMap<>(); // 마담에게 유혹당한 플레이어 (true = 능력 사용 불가)
    private boolean madameContactedMafia = false; // 마담이 마피아와 접선했는지 여부

    private Clip currentClip; // Server-side sound clip

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MafiaGameServer frame = new MafiaGameServer();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public MafiaGameServer() {
        // 메인 프레임 설정
        setTitle("Mafia Game Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 600, 500); // 크기 약간 키움

        // 전체 테마 색상 정의
        java.awt.Color backgroundColor = new java.awt.Color(18, 18, 18); // Very Dark Gray (Almost Black)
        java.awt.Color panelColor = new java.awt.Color(30, 30, 30, 220); // Dark Gray with Transparency
        java.awt.Color textColor = new java.awt.Color(240, 240, 240); // White
        java.awt.Color accentColor = new java.awt.Color(192, 57, 43); // Deep Red (Mafia Red)
        // java.awt.Color buttonColor = new java.awt.Color(41, 128, 185); // Darker Blue
        // (Unused)

        // Custom Background Panel
        contentPane = new BackgroundPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentPane.setLayout(new java.awt.BorderLayout());
        setContentPane(contentPane);

        // 1. 헤더 패널
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new java.awt.Color(25, 25, 25, 220)); // Slightly lighter than background, transparent
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        headerPanel.setLayout(new java.awt.BorderLayout());

        JLabel titleLabel = new JLabel("마피아 게임 서버");
        titleLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
        titleLabel.setForeground(textColor);

        // 아이콘 추가
        try {
            java.net.URL iconURL = getClass().getResource("/info/ServerImg.png");
            if (iconURL != null) {
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(iconURL);
                java.awt.Image img = icon.getImage();
                java.awt.Image newImg = img.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
                titleLabel.setIcon(new javax.swing.ImageIcon(newImg));
                titleLabel.setIconTextGap(15); // 아이콘과 텍스트 사이 간격
            }
        } catch (Exception e) {
            System.out.println("Icon load failed: " + e.getMessage());
        }

        headerPanel.add(titleLabel, java.awt.BorderLayout.WEST);

        JLabel statusLabel = new JLabel("● Offline");
        statusLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        statusLabel.setForeground(new java.awt.Color(255, 0, 0)); // Red
        headerPanel.add(statusLabel, java.awt.BorderLayout.EAST);

        contentPane.add(headerPanel, java.awt.BorderLayout.NORTH);

        // 2. 로그 영역 (중앙)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new java.awt.BorderLayout());
        centerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        centerPanel.setOpaque(false); // Make transparent to show background

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
        textArea.setBackground(panelColor);
        textArea.setForeground(textColor);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(panelColor));
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = accentColor;
                this.trackColor = panelColor;
            }
        });
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        centerPanel.add(scrollPane, java.awt.BorderLayout.CENTER);

        // 로그 영역 제목
        JLabel logLabel = new JLabel("Server Logs");
        logLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        logLabel.setForeground(textColor);
        logLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        centerPanel.add(logLabel, java.awt.BorderLayout.NORTH);

        contentPane.add(centerPanel, java.awt.BorderLayout.CENTER);

        // 3. 컨트롤 패널 (하단)
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new java.awt.GridLayout(2, 1, 10, 10));
        controlPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        controlPanel.setOpaque(false); // Transparent

        // 포트 설정 패널
        JPanel portPanel = new JPanel();
        portPanel.setLayout(new java.awt.BorderLayout(10, 0));
        portPanel.setOpaque(false);

        JLabel lblPort = new JLabel("Port Number:");
        lblPort.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        lblPort.setForeground(textColor);
        portPanel.add(lblPort, java.awt.BorderLayout.WEST);

        txtPortNumber = new JTextField();
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setText("30000");
        txtPortNumber.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 14));
        txtPortNumber.setBackground(panelColor);
        txtPortNumber.setForeground(textColor);
        txtPortNumber.setCaretColor(textColor);
        txtPortNumber.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(accentColor),
                javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        portPanel.add(txtPortNumber, java.awt.BorderLayout.CENTER);

        JButton btnServerStart = new JButton("Start Server");
        styleButton(btnServerStart, accentColor, textColor);
        btnServerStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
                } catch (NumberFormatException | IOException e1) {
                    e1.printStackTrace();
                    AppendText("[Error] Port binding failed.");
                    return;
                }
                AppendText("Mafia Game Server Running...");
                btnServerStart.setText("Server Running");
                btnServerStart.setEnabled(false);
                btnServerStart.setBackground(new java.awt.Color(39, 174, 96)); // Green
                txtPortNumber.setEnabled(false);
                btnGameStart.setEnabled(true);
                statusLabel.setForeground(new java.awt.Color(39, 174, 96)); // Green
                statusLabel.setText("● Running");

                AcceptServer accept_server = new AcceptServer();
                accept_server.start();
            }
        });
        portPanel.add(btnServerStart, java.awt.BorderLayout.EAST);
        btnServerStart.setPreferredSize(new java.awt.Dimension(150, 40));

        controlPanel.add(portPanel);

        // 게임 시작 버튼
        btnGameStart = new JButton("Start Game (Need 4+ players)");
        styleButton(btnGameStart, new java.awt.Color(149, 165, 166), textColor); // Disabled color initially
        btnGameStart.setEnabled(false);
        btnGameStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (UserVec.size() < 4) {
                    AppendText("최소 4명 이상 필요합니다!");
                    WriteAll("SYSTEM: 최소 4명 이상 필요합니다.\n");
                    return;
                }
                startGame();
            }
        });
        controlPanel.add(btnGameStart);

        contentPane.add(controlPanel, java.awt.BorderLayout.SOUTH);
    }

    // Custom Panel for Background Image
    class BackgroundPanel extends JPanel {
        private java.awt.Image backgroundImage;

        public BackgroundPanel() {
            try {
                java.net.URL bgURL = getClass().getResource("/info/server_background.jpg");
                if (bgURL != null) {
                    backgroundImage = new javax.swing.ImageIcon(bgURL).getImage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(new java.awt.Color(18, 18, 18));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private void styleButton(JButton button, java.awt.Color bg, java.awt.Color fg) {
        button.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        // Hover effect could be added here with MouseListener if needed
    }

    // 게임 시작
    private void startGame() {
        if (gameStarted) {
            AppendText("게임이 이미 시작되었습니다!");
            return;
        }

        gameStarted = true;
        dayCount = 0;
        btnGameStart.setEnabled(false);

        AppendText("===== 게임 시작! =====");
        AppendText("참가자 수: " + UserVec.size());

        // 역할 배정
        assignRoles();

        // 모든 플레이어를 살아있는 상태로 초기화
        for (UserService user : UserVec) {
            aliveStatus.put(user.UserName, true);
        }

        WriteAll("SYSTEM: ===== 마피아 게임이 시작되었습니다! =====\n");
        WriteAll("SYSTEM: 참가자 수: " + UserVec.size() + "명\n");
        playSound("/GameSound/game_start.wav"); // Play locally on server

        // 게임 시작 후 밤 페이즈로 전환
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                startNightPhase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 역할 배정
    private void assignRoles() {
        List<String> roles = new ArrayList<>();
        int playerCount = UserVec.size();

        // 역할 구성 (최대 8명)
        // 4명: 마피아1, 의사1, 경찰1, 시민1
        // 5명: 마피아2, 의사1, 경찰1, 정치인 또는 군인 1 (랜덤)
        // 6명: 마피아1, 스파이1, 의사1, 경찰1, 정치인1, 군인1
        // 7명: 마피아1, 스파이1, 의사1, 경찰1, 정치인1, 군인1, 영매1
        // 8명: 마피아2, 스파이1, 의사1, 경찰1, 특직 3명 (정치인, 기자, 군인, 영매, 도굴꾼 중 랜덤)

        if (playerCount > 8) {
            AppendText("최대 8명까지만 게임 가능합니다!");
            WriteAll("SYSTEM: 최대 8명까지만 게임 가능합니다.\n");
            gameStarted = false;
            btnGameStart.setEnabled(true);
            return;
        }

        if (playerCount == 4) {
            roles.add("MAFIA");
            roles.add("DOCTOR");
            roles.add("POLICE");
            roles.add("CITIZEN");
        } else if (playerCount == 5) {
            roles.add("MAFIA");
            roles.add("MAFIA");
            roles.add("DOCTOR");
            roles.add("POLICE");
            // 정치인 또는 군인 중 랜덤 선택
            if (Math.random() < 0.5) {
                roles.add("POLITICIAN");
            } else {
                roles.add("SOLDIER");
            }
        } else if (playerCount == 6) {
            // 6명: 마피아1, 보조1(스파이 또는 마담), 의사1, 경찰1, 특직 2명
            roles.add("MAFIA");

            // 마피아 보조 직업 중 하나 랜덤 선택
            if (Math.random() < 0.5) {
                roles.add("SPY");
            } else {
                roles.add("MADAME");
            }

            roles.add("DOCTOR");
            roles.add("POLICE");
            roles.add("POLITICIAN");
            roles.add("SOLDIER");
        } else if (playerCount == 7) {
            // 7명: 마피아1, 보조1(스파이 또는 마담), 의사1, 경찰1, 특직 3명
            roles.add("MAFIA");

            // 마피아 보조 직업 중 하나 랜덤 선택
            if (Math.random() < 0.5) {
                roles.add("SPY");
            } else {
                roles.add("MADAME");
            }

            roles.add("DOCTOR");
            roles.add("POLICE");
            roles.add("POLITICIAN");
            roles.add("SOLDIER");
            roles.add("SHAMAN");
        } else if (playerCount == 8) {
            // 8명: 마피아2, 보조1(스파이 또는 마담 중 랜덤), 의사1, 경찰1, 특직 3명
            roles.add("MAFIA");
            roles.add("MAFIA");

            // 마피아 보조 직업 중 하나 랜덤 선택 (50% 확률)
            if (Math.random() < 0.5) {
                roles.add("SPY");
            } else {
                roles.add("MADAME");
            }

            roles.add("DOCTOR");
            roles.add("POLICE");

            // 특수 직업 7개 중 3개 랜덤 선택
            List<String> specialRoles = new ArrayList<>();
            specialRoles.add("POLITICIAN");
            specialRoles.add("REPORTER");
            specialRoles.add("SOLDIER");
            specialRoles.add("SHAMAN");
            specialRoles.add("GHOUL");
            specialRoles.add("GANGSTER");
            specialRoles.add("PRIEST");
            Collections.shuffle(specialRoles);

            // 앞의 3개만 추가
            for (int i = 0; i < 3; i++) {
                roles.add(specialRoles.get(i));
            }
        }

        // 역할 섞기
        Collections.shuffle(roles);

        // 역할 배정 및 전송
        for (int i = 0; i < UserVec.size(); i++) {
            UserService user = UserVec.get(i);
            String role = roles.get(i);
            user.setRole(role);

            // 군인이면 방어막 초기화
            if (role.equals("SOLDIER")) {
                soldierShield.put(user.UserName, true);
            }

            // 마피아, 스파이, 영매, 도굴꾼 이름 저장
            if (role.equals("MAFIA")) {
                mafiaName = user.UserName;
            } else if (role.equals("MADAME")) {
                madameName = user.UserName;
            } else if (role.equals("SPY")) {
                spyName = user.UserName;
            } else if (role.equals("SHAMAN")) {
                shamanName = user.UserName;
            } else if (role.equals("GHOUL")) {
                ghoulName = user.UserName;
            }

            String roleMsg = getRoleDescription(role);
            user.WriteOne("ROLE:" + role + "\n");
            user.WriteOne("SYSTEM: " + roleMsg + "\n");

            // Send game start sound (only once, maybe handled by client but good to be
            // explicit if we want server control)
            // Let's NOT send game_start.wav here, but in startGame().

            // Play role specific sound locally on server?
            // User asked for "Server plays sound".
            // But role sounds are private... "You are Mafia".
            // If server plays "You are Mafia", everyone hears it.
            // This contradicts the game logic if the server is a public speaker.
            // However, the user asked "Server plays sound".
            // Let's assume they want the ATMOSPHERE sounds (Phase, Death) on server.
            // But what about role sounds?
            // If I play role sound on server, it reveals the role.
            // I should probably NOT play role sounds on server, or play them only if they
            // are generic.
            // But the previous code sent role sounds to individuals.
            // If I can't send to individuals (because client doesn't play), I can't do
            // private audio.
            // I will Comment out role sounds for now as they are private info.
            // OR, maybe the user implies the server is the "Game Master" and just plays
            // public sounds.
            // I'll comment out role sounds to be safe, or ask user?
            // User said "Server plays sound".
            // Let's just remove the SOUND command for roles.

            // user.WriteOne("SOUND:" + roleSound + "\n");
            AppendText(user.UserName + " -> " + role);
        }
    }

    private String getRoleSoundPath(String role) {
        switch (role) {
            case "MAFIA":
                return null; // Mafia.wav is for death only
            case "MADAME":
                return "/GameSound/Mafia_team/madam.wav";
            case "SPY":
                return "/GameSound/Mafia_team/spy_zupsun.wav";
            case "DOCTOR":
                return "/GameSound/Citizen/doctor.wav";
            case "POLICE":
                return "/GameSound/Citizen/police.wav";
            case "POLITICIAN":
                return "/GameSound/Citizen/politician.wav";
            case "SOLDIER":
                return "/GameSound/Citizen/soldier.wav";
            case "SHAMAN":
                return "/GameSound/Citizen/SHAMAN.wav";
            case "REPORTER":
                return "/GameSound/Citizen/reporter.wav";
            case "GANGSTER":
                return "/GameSound/Citizen/gangster.wav";
            case "PRIEST":
                return "/GameSound/Citizen/priest.wav";
            case "GHOUL":
                return "/GameSound/Citizen/ghoul.wav";
            default:
                return null;
        }
    }

    private String getRoleDescription(String role) {
        switch (role) {
            case "MAFIA":
                return "당신은 [마피아]입니다. 밤에 시민을 제거하세요!";
            case "MADAME":
                return "당신은 [마담]입니다. 마피아 팀이며 낮 투표로 플레이어를 유혹하여 밤에 능력을 사용하지 못하게 만듭니다!";
            case "SPY":
                return "당신은 [스파이]입니다. 마피아 팀이며 밤에 한 명의 직업을 알아낼 수 있습니다!";
            case "DOCTOR":
                return "당신은 [의사]입니다. 밤에 한 명을 지정하여 보호하세요!";
            case "POLICE":
                return "당신은 [경찰]입니다. 밤에 한 명을 조사하여 마피아인지 확인하세요!";
            case "POLITICIAN":
                return "당신은 [정치인]입니다. 투표로 죽지 않으며 2표를 행사합니다!";
            case "SOLDIER":
                return "당신은 [군인]입니다. 마피아의 공격을 한 차례 버틸 수 있습니다!";
            case "SHAMAN":
                return "당신은 [영매]입니다. 죽은 자들의 대화를 보고 밤에 한 명을 성불시켜 직업을 알아낼 수 있습니다!";
            case "REPORTER":
                return "당신은 [기자]입니다. 2일차 밤부터 8일차 밤까지 한 명을 선택하여 다음 날 아침에 직업을 공개할 수 있습니다!";
            case "GHOUL":
                return "당신은 [도굴꾼]입니다. 첫날 밤 마피아에게 살해당한 사람의 직업을 얻습니다. 사망자가 없으면 시민이 됩니다!";
            case "GANGSTER":
                return "당신은 [건달]입니다. 밤마다 한 명을 선택하여 다음 날 투표를 못하게 만들 수 있습니다!";
            case "PRIEST":
                return "당신은 [성직자]입니다. 게임 중 단 한 번, 죽은 플레이어 한 명을 부활시킬 수 있습니다! (성불된 사람은 부활 불가)";
            case "CITIZEN":
                return "당신은 [시민]입니다. 낮 투표로 마피아를 찾아내세요!";
            default:
                return "역할이 배정되었습니다.";
        }
    }

    // 밤 페이즈
    private void startNightPhase() {
        dayCount++;
        nightCount++;
        gamePhase = "NIGHT";
        nightActions.clear();
        voteBanned.clear(); // 건달 투표 금지 초기화
        // seduced는 초기화하지 않음 (낮에 유혹, 밤에 적용)
        reporterTarget = ""; // 기자 타겟 초기화
        reporterTargetRole = ""; // 기자 타겟 역할 초기화

        AppendText("===== " + dayCount + "일차 밤 =====");
        WriteAll("PHASE:NIGHT\n");
        playSound("/GameSound/night.wav"); // Play locally
        WriteAll("SYSTEM: ===== " + dayCount + "일차 밤이 되었습니다 =====\n");
        WriteAll("SYSTEM: 마피아는 제거할 대상을, 의사는 보호할 대상을, 경찰은 조사할 대상을 선택하세요.\n");

        // 살아있는 플레이어 목록 전송
        sendAlivePlayerList();

        // 30초 대기 후 낮 페이즈로 전환
        new Thread(() -> {
            try {
                Thread.sleep(30000);
                processNightActions();
                Thread.sleep(1000); // Wait for death sound/text to be perceived
                startDayPhase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 밤 행동 처리
    private void processNightActions() {
        String mafiaTarget = nightActions.get("MAFIA");
        String doctorTarget = nightActions.get("DOCTOR");
        String policeTarget = nightActions.get("POLICE");
        String spyTarget = nightActions.get("SPY");
        String gangsterTarget = nightActions.get("GANGSTER");

        AppendText("=== 밤 행동 결과 ===");
        AppendText("마피아 타겟: " + (mafiaTarget != null ? mafiaTarget : "없음"));
        AppendText("의사 보호: " + (doctorTarget != null ? doctorTarget : "없음"));
        AppendText("경찰 조사: " + (policeTarget != null ? policeTarget : "없음"));
        AppendText("스파이 조사: " + (spyTarget != null ? spyTarget : "없음"));
        AppendText("건달 타겟: " + (gangsterTarget != null ? gangsterTarget : "없음"));

        // 건달의 투표 금지 처리
        if (gangsterTarget != null) {
            voteBanned.put(gangsterTarget, true);
            AppendText(gangsterTarget + " 다음 투표 금지됨");
        }

        // 마피아의 공격 처리
        if (mafiaTarget != null) {
            boolean savedByDoctor = mafiaTarget.equals(doctorTarget);
            boolean savedBySoldier = false;

            // 군인의 방어막 체크 (유혹당하지 않은 경우에만)
            boolean soldierSeduced = seduced.get(mafiaTarget) != null && seduced.get(mafiaTarget);
            if (soldierShield.containsKey(mafiaTarget) && soldierShield.get(mafiaTarget) && !soldierSeduced) {
                savedBySoldier = true;
                soldierShield.put(mafiaTarget, false); // 방어막 사용됨
            } else if (soldierShield.containsKey(mafiaTarget) && soldierShield.get(mafiaTarget) && soldierSeduced) {
                // 군인이 유혹당한 경우 방어막 무효화
                soldierShield.put(mafiaTarget, false);
                AppendText(mafiaTarget + " 군인이지만 유혹당해 방어막 무효화");
            }

            if (savedByDoctor) {
                WriteAll("SYSTEM: 의사가 누군가를 구했습니다!\n");
                AppendText(mafiaTarget + " 의사가 구함");
            } else if (savedBySoldier) {
                WriteAll("SYSTEM: [" + mafiaTarget + "] 군인이 마피아의 공격을 막아냈습니다!\n");
                AppendText(mafiaTarget + " 군인이 방어막으로 생존");
            } else {
                aliveStatus.put(mafiaTarget, false);
                WriteAll("SYSTEM: [" + mafiaTarget + "]님이 마피아에게 제거되었습니다.\n");
                playSound("/GameSound/Mafia_team/Mafia.wav"); // Play locally
                AppendText(mafiaTarget + " 사망");

                // 해당 플레이어에게 사망 알림
                for (UserService user : UserVec) {
                    if (user.UserName.equals(mafiaTarget)) {
                        user.WriteOne("DEAD:true\n");

                        // 도굴꾼 능력: 첫날 밤 사망자의 직업 획득
                        // 단, 군인은 첫날 밤에 죽지 않으므로 도굴꾼이 얻을 수 없음
                        if (dayCount == 1 && !ghoulTransformed && !ghoulName.isEmpty()) {
                            String victimRole = user.role;
                            // 도굴꾼 찾기
                            for (UserService ghoulUser : UserVec) {
                                if (ghoulUser.UserName.equals(ghoulName)) {
                                    ghoulUser.setRole(victimRole);
                                    ghoulVictim = mafiaTarget; // 도굴 희생자 기록
                                    // 클라이언트에 역할 변경 알림
                                    ghoulUser.WriteOne("ROLE:" + victimRole + "\n");
                                    ghoulUser.WriteOne("SYSTEM: 첫날 밤 사망자 [" + mafiaTarget + "]의 직업 [" + victimRole
                                            + "]을 얻었습니다!\n");
                                    ghoulUser.WriteOne("SYSTEM: " + getRoleDescription(victimRole) + "\n");
                                    AppendText(
                                            "도굴꾼 " + ghoulName + "이 " + victimRole + "로 변신 (희생자: " + mafiaTarget + ")");
                                    ghoulTransformed = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // 도굴꾼 능력: 첫날 밤 사망자가 없으면 시민이 됨
        if (dayCount == 1 && !ghoulTransformed && !ghoulName.isEmpty()) {
            for (UserService user : UserVec) {
                if (user.UserName.equals(ghoulName)) {
                    user.setRole("CITIZEN");
                    // 클라이언트에 역할 변경 알림
                    user.WriteOne("ROLE:CITIZEN\n");
                    user.WriteOne("SYSTEM: 첫날 밤 사망자가 없어 [시민]이 되었습니다.\n");
                    user.WriteOne("SYSTEM: " + getRoleDescription("CITIZEN") + "\n");
                    AppendText("도굴꾼 " + ghoulName + "이 시민으로 변신");
                    ghoulTransformed = true;
                    break;
                }
            }
        }

        // 경찰과 스파이 조사 결과는 즉시 전송되므로 여기서는 처리하지 않음
    }

    // 낮 페이즈
    private void startDayPhase() {
        gamePhase = "DAY";

        // 게임 종료 체크
        if (checkGameEnd()) {
            return;
        }

        AppendText("===== " + dayCount + "일차 낮 =====");
        WriteAll("PHASE:DAY\n");
        playSound("/GameSound/morning.wav"); // Play locally
        WriteAll("SYSTEM: ===== " + dayCount + "일차 낮이 되었습니다 =====\n");

        // 유혹 초기화 (새로운 낮이 시작되면 이전 유혹 해제)
        seduced.clear();

        // 성직자의 부활 처리
        if (!priestTarget.isEmpty()) {
            aliveStatus.put(priestTarget, true);
            WriteAll("SYSTEM: 🌟 [" + priestTarget + "]님이 성직자에 의해 부활했습니다! 🌟\n");
            AppendText("성직자가 " + priestTarget + " 부활 성공");

            // 부활한 플레이어에게 알림
            for (UserService targetUser : UserVec) {
                if (targetUser.UserName.equals(priestTarget)) {
                    // 도굴꾼의 희생자인 경우 시민으로 변경
                    if (priestTarget.equals(ghoulVictim)) {
                        targetUser.setRole("CITIZEN");
                        targetUser.WriteOne("ROLE:CITIZEN\n");
                        targetUser.WriteOne("SYSTEM: 🌟 성직자에 의해 부활했습니다! 🌟\n");
                        targetUser.WriteOne("SYSTEM: 당신의 직업은 도굴꾼에게 빼앗겨 [시민]이 되었습니다.\n");
                        targetUser.WriteOne("SYSTEM: " + getRoleDescription("CITIZEN") + "\n");
                        AppendText(priestTarget + " 부활 (도굴 희생자 -> 시민)");
                    } else {
                        targetUser.WriteOne("SYSTEM: 🌟 성직자에 의해 부활했습니다! 🌟\n");
                    }
                    targetUser.WriteOne("DEAD:false\n");
                    break;
                }
            }

            priestTarget = ""; // 초기화
        }

        // 기자의 특종 발표
        if (!reporterTarget.isEmpty() && !reporterTargetRole.isEmpty()) {
            WriteAll("SYSTEM: 🔥 특종입니다. [" + reporterTarget + "]의 직업은 [" + reporterTargetRole + "]입니다! 🔥\n");
            AppendText("기자 특종: " + reporterTarget + " -> " + reporterTargetRole);
        }

        WriteAll("SYSTEM: 자유롭게 대화하고 의심되는 사람을 찾으세요.\n");
        WriteAll("SYSTEM: 30초 후 투표가 시작됩니다.\n");

        sendAlivePlayerList();

        // 30초 대기 후 투표 페이즈로 전환
        new Thread(() -> {
            try {
                Thread.sleep(30000);
                startVotePhase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 투표 페이즈
    private void startVotePhase() {
        gamePhase = "VOTE";
        voteCount.clear();

        // 살아있는 모든 플레이어를 투표 대상으로 초기화
        for (String player : aliveStatus.keySet()) {
            if (aliveStatus.get(player)) {
                voteCount.put(player, 0);
            }
        }

        AppendText("===== 투표 시작 =====");
        WriteAll("PHASE:VOTE\n");
        playSound("/GameSound/vote.wav"); // Play locally
        WriteAll("SYSTEM: ===== 투표 시작 =====\n");
        WriteAll("SYSTEM: 제거할 플레이어를 투표하세요! (20초)\n");

        sendAlivePlayerList();

        // 20초 대기 후 투표 결과 처리
        new Thread(() -> {
            try {
                Thread.sleep(20000);
                processVoteResult();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 투표 결과 처리
    private void processVoteResult() {
        AppendText("=== 투표 결과 ===");

        String maxVotedPlayer = null;
        int maxVotes = 0;
        boolean tie = false;

        for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
            AppendText(entry.getKey() + ": " + entry.getValue() + "표");
            WriteAll("SYSTEM: [" + entry.getKey() + "] " + entry.getValue() + "표\n");

            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                maxVotedPlayer = entry.getKey();
                tie = false;
            } else if (entry.getValue() == maxVotes && maxVotes > 0) {
                tie = true;
            }
        }

        if (tie || maxVotes == 0) {
            WriteAll("SYSTEM: 동점 또는 투표 없음! 아무도 제거되지 않았습니다.\n");
            AppendText("투표 무효");
        } else {
            // 정치인인지 확인
            String eliminatedRole = "";
            boolean isPolitician = false;
            for (UserService user : UserVec) {
                if (user.UserName.equals(maxVotedPlayer)) {
                    eliminatedRole = user.role;
                    if (eliminatedRole.equals("POLITICIAN")) {
                        isPolitician = true;
                    }
                }
            }

            // 정치인이 유혹당했는지 확인
            boolean politicianSeduced = seduced.get(maxVotedPlayer) != null && seduced.get(maxVotedPlayer);

            if (isPolitician && !politicianSeduced) {
                // 정치인은 투표로 죽지 않음 (유혹당하지 않은 경우)
                WriteAll("SYSTEM: [" + maxVotedPlayer + "]님은 정치인이므로 투표로 제거되지 않습니다!\n");
                AppendText(maxVotedPlayer + " 투표 1위 (정치인 - 생존)");
            } else if (isPolitician && politicianSeduced) {
                // 정치인이 유혹당한 경우 능력 무효화
                aliveStatus.put(maxVotedPlayer, false);
                for (UserService user : UserVec) {
                    if (user.UserName.equals(maxVotedPlayer)) {
                        user.WriteOne("DEAD:true\n");
                    }
                }
                WriteAll("SYSTEM: [" + maxVotedPlayer + "]님은 정치인이지만 마담에게 유혹당해 투표로 제거되었습니다!\n");
                AppendText(maxVotedPlayer + " 제거됨 (정치인 - 유혹당함)");
            } else {
                aliveStatus.put(maxVotedPlayer, false);

                // 사망 알림
                for (UserService user : UserVec) {
                    if (user.UserName.equals(maxVotedPlayer)) {
                        user.WriteOne("DEAD:true\n");
                    }
                }

                WriteAll("SYSTEM: [" + maxVotedPlayer + "]님이 투표로 제거되었습니다.\n");
                AppendText(maxVotedPlayer + " 제거됨 (역할: " + eliminatedRole + ")");
            }
        }

        // 게임 종료 체크
        if (checkGameEnd()) {
            return;
        }

        // 다음 밤으로
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                startNightPhase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Play sound locally on server
    private void playSound(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        // Stop previous sound
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }

        new Thread(() -> {
            try {
                InputStream soundStream = getClass().getResourceAsStream(filePath);
                if (soundStream == null) {
                    System.err.println("Sound file not found: " + filePath);
                    return;
                }

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundStream);
                currentClip = AudioSystem.getClip();
                currentClip.open(audioInputStream);
                currentClip.start();

                System.out.println("Playing sound: " + filePath);

                // Wait for the clip to finish
                currentClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        currentClip.close();
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

    // Stop sound locally on server
    private void stopSound() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }

    // 게임 종료 체크
    private boolean checkGameEnd() {
        int aliveCount = 0;
        int mafiaCount = 0;
        int citizenPower = 0; // 시민 팀의 실질적인 힘 (정치인은 2로 계산)

        for (UserService user : UserVec) {
            if (aliveStatus.get(user.UserName)) {
                aliveCount++;
                // 마피아 팀: 마피아, 스파이, 마담(접선 후)
                if (user.role.equals("MAFIA") || user.role.equals("SPY")
                        || (user.role.equals("MADAME") && madameContactedMafia)) {
                    mafiaCount++;
                } else {
                    // 시민 팀 (정치인은 2명으로 계산)
                    if (user.role.equals("POLITICIAN")) {
                        citizenPower += 2;
                    } else {
                        citizenPower += 1;
                    }
                }
            }
        }

        AppendText("생존자: " + aliveCount + "명, 마피아: " + mafiaCount + "명, 시민팀 파워: " + citizenPower);

        if (mafiaCount == 0) {
            // 시민 승리
            WriteAll("PHASE:END\n");
            WriteAll("SYSTEM: ===== 게임 종료 =====\n");
            WriteAll("SYSTEM: 승리 팀: 시민 팀\n");
            stopSound(); // Stop sound on game end
            AppendText("===== 게임 종료: 시민 승리 =====");
            revealAllRoles();
            gameStarted = false;
            btnGameStart.setEnabled(true);
            return true;
        } else if (mafiaCount >= citizenPower) {
            // 마피아 승리 - 마피아 수가 시민팀 파워와 같거나 많을 때
            WriteAll("PHASE:END\n");
            WriteAll("SYSTEM: ===== 게임 종료! 마피아 팀 승리! =====\n");
            WriteAll("SYSTEM: 마피아가 시민 팀과 같거나 많아졌습니다!\n");
            AppendText("===== 게임 종료: 마피아 승리 =====");
            revealAllRoles();
            gameStarted = false;
            btnGameStart.setEnabled(true);
            return true;
        }

        return false;
    }

    // 모든 역할 공개
    private void revealAllRoles() {
        WriteAll("SYSTEM: ===== 역할 공개 =====\n");
        for (UserService user : UserVec) {
            WriteAll("SYSTEM: [" + user.UserName + "] - " + user.role + "\n");
        }
    }

    // 모든 플레이어 목록 전송 (살아있는지 죽었는지 표시)
    private void sendAlivePlayerList() {
        StringBuilder playerList = new StringBuilder("PLAYERS:");
        for (String player : aliveStatus.keySet()) {
            if (aliveStatus.get(player)) {
                playerList.append(player).append(",");
            } else {
                // 죽은 플레이어는 [DEAD] 접두사 추가
                playerList.append("[DEAD]").append(player).append(",");
            }
        }
        WriteAll(playerList.toString() + "\n");
    }

    // 접속 중인 모든 플레이어 목록 전송 (대기실용)
    private void broadcastPlayerList() {
        StringBuilder playerList = new StringBuilder("PLAYERS:");
        for (UserService user : UserVec) {
            playerList.append(user.UserName).append(",");
        }
        WriteAll(playerList.toString() + "\n");
    }

    class AcceptServer extends Thread {
        public void run() {
            while (true) {
                try {
                    AppendText("Waiting for players...");
                    client_socket = socket.accept();
                    AppendText("새로운 플레이어 from " + client_socket);

                    UserService new_user = new UserService(client_socket);
                    UserVec.add(new_user);
                    broadcastPlayerList(); // Update player list for everyone (now includes new user)
                    AppendText("플레이어 입장. 현재 플레이어 수: " + UserVec.size());
                    new_user.start();
                } catch (IOException e) {
                    AppendText("accept 에러 발생");
                }
            }
        }
    }

    public void AppendText(String str) {
        textArea.append(str + "\n");
        textArea.setCaretPosition(textArea.getText().length());
    }

    public void WriteAll(String str) {
        for (int i = 0; i < UserVec.size(); i++) {
            UserService user = UserVec.get(i);
            user.WriteOne(str);
        }
    }

    class UserService extends Thread {
        private InputStream is;
        private OutputStream os;
        private DataInputStream dis;
        private DataOutputStream dos;
        private Socket client_socket;
        private Vector<UserService> user_vc;
        private String UserName = "";
        String role = ""; // 플레이어 역할

        public UserService(Socket client_socket) {
            this.client_socket = client_socket;
            this.user_vc = UserVec;
            try {
                is = client_socket.getInputStream();
                dis = new DataInputStream(is);
                os = client_socket.getOutputStream();
                dos = new DataOutputStream(os);

                String line1 = dis.readUTF();
                String[] msg = line1.split(" ");
                UserName = msg[1].trim();

                AppendText("새로운 플레이어: " + UserName);
                WriteOne("SYSTEM: 마피아 게임 서버에 오신 것을 환영합니다!\n");
                WriteOne("SYSTEM: [" + UserName + "]님 환영합니다.\n");

                String br_msg = "SYSTEM: [" + UserName + "]님이 입장하였습니다.\n";
                WriteAll(br_msg);

            } catch (Exception e) {
                AppendText("UserService 생성 오류");
            }
        }

        public void setRole(String role) {
            this.role = role;
        }

        public void logout() {
            user_vc.removeElement(this);
            String br_msg = "SYSTEM: [" + UserName + "]님이 퇴장하였습니다.\n";
            WriteAll(br_msg);
            broadcastPlayerList(); // Update player list for everyone
            AppendText("플레이어 퇴장: " + UserName + " (현재 " + user_vc.size() + "명)");
        }

        public void WriteOne(String msg) {
            try {
                dos.writeUTF(msg);
            } catch (IOException e) {
                AppendText("전송 오류: " + UserName);
                try {
                    dos.close();
                    dis.close();
                    client_socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                logout();
            }
        }

        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF();
                    msg = msg.trim();
                    AppendText(msg);

                    // 게임 명령어 처리
                    if (msg.startsWith("NIGHT_ACTION:")) {
                        // NIGHT_ACTION:ROLE:TARGET 형식
                        String[] parts = msg.split(":");
                        if (parts.length == 3) {
                            String actionRole = parts[1];
                            String target = parts[2];

                            // 영매와 성직자를 제외한 모든 직업은 죽은 사람에게 스킬을 쓸 수 없음
                            if (!actionRole.equals("SHAMAN") && !actionRole.equals("PRIEST")
                                    && aliveStatus.get(target) != null && !aliveStatus.get(target)) {
                                WriteOne("SYSTEM: 죽은 사람에게는 능력을 사용할 수 없습니다!\n");
                                return;
                            }

                            // 마담에게 유혹당한 경우 능력 사용 불가
                            if (seduced.get(UserName) != null && seduced.get(UserName)) {
                                WriteOne("SYSTEM: 마담에게 유혹당해 능력을 사용할 수 없습니다!\n");
                                AppendText(UserName + " 유혹당해 능력 사용 불가");
                                return;
                            }

                            nightActions.put(actionRole, target);
                            AppendText(UserName + "(" + role + ") -> " + target);

                            // 마피아가 타겟을 선택하면 모든 마피아에게 동기화
                            if (actionRole.equals("MAFIA")) {
                                // 모든 마피아에게 선택 결과 알림
                                for (UserService mafiaUser : UserVec) {
                                    if (mafiaUser.role.equals("MAFIA")) {
                                        mafiaUser.WriteOne("SYSTEM: 마피아 팀이 [" + target + "]님을 타겟으로 선택했습니다.\n");
                                    }
                                }
                                WriteOne("SYSTEM: [" + target + "]님을 타겟으로 선택했습니다.\n");
                            }
                            // 경찰과 스파이는 즉시 조사 결과 전송
                            else if (actionRole.equals("POLICE")) {
                                for (UserService targetUser : UserVec) {
                                    if (targetUser.UserName.equals(target)) {
                                        String result = targetUser.role.equals("MAFIA") || targetUser.role.equals("SPY")
                                                ? "마피아입니다!"
                                                : "마피아가 아닙니다.";
                                        WriteOne("SYSTEM: [" + target + "]님은 " + result + "\n");
                                        AppendText("경찰 " + UserName + "이 " + target + " 조사 -> " + result);
                                        break;
                                    }
                                }
                            } else if (actionRole.equals("SPY")) {
                                for (UserService targetUser : UserVec) {
                                    if (targetUser.UserName.equals(target)) {
                                        String targetRole = targetUser.role;
                                        WriteOne("SYSTEM: [" + target + "]님의 직업은 [" + targetRole + "]입니다!\n");
                                        AppendText("스파이 " + UserName + "이 " + target + " 조사 -> " + targetRole);

                                        // 마피아를 조사했다면 접선
                                        if (targetRole.equals("MAFIA") && !spyContactedMafia) {
                                            spyContactedMafia = true;
                                            // 마피아에게 스파이 정보 알림
                                            for (UserService mafiaUser : UserVec) {
                                                if (mafiaUser.role.equals("MAFIA")) {
                                                    mafiaUser.WriteOne(
                                                            "SYSTEM: [" + spyName + "]님이 스파이로 접선했습니다! 이제 동료입니다.\n");
                                                    AppendText("마피아와 스파이 접선 완료");
                                                }
                                            }
                                            WriteOne("SYSTEM: 마피아 [" + mafiaName + "]님과 접선했습니다! 이제 서로를 알 수 있습니다.\n");
                                        }

                                        // 군인을 조사했다면 군인도 스파이를 알게 됨
                                        if (targetRole.equals("SOLDIER")) {
                                            targetUser.WriteOne("SYSTEM: 당신을 조사한 [" + UserName + "]님이 스파이임을 알아냈습니다!\n");
                                            AppendText("군인 " + target + "이 스파이 " + UserName + " 정체 파악");
                                        }
                                        break;
                                    }
                                }
                            } else if (actionRole.equals("SHAMAN")) {
                                // 영매의 성불 능력
                                for (UserService targetUser : UserVec) {
                                    if (targetUser.UserName.equals(target)) {
                                        // 죽은 사람만 성불 가능
                                        if (aliveStatus.get(target) != null && !aliveStatus.get(target)) {
                                            String targetRole = targetUser.role;
                                            WriteOne("SYSTEM: [" + target + "]님을 성불시켰습니다. 직업은 [" + targetRole
                                                    + "]였습니다!\n");
                                            AppendText("영매 " + UserName + "이 " + target + " 성불 -> " + targetRole);
                                            // 성불 상태 설정
                                            blessedStatus.put(target, true);
                                        } else {
                                            WriteOne("SYSTEM: [" + target + "]님은 아직 살아있습니다!\n");
                                        }
                                        break;
                                    }
                                }
                            } else if (actionRole.equals("REPORTER")) {
                                // 기자의 특종 능력
                                if (nightCount == 1) {
                                    WriteOne("SYSTEM: 첫 번째 밤에는 기자 능력을 사용할 수 없습니다!\n");
                                } else if (nightCount > 8) {
                                    WriteOne("SYSTEM: 8일차 이후에는 기자 능력을 사용할 수 없습니다!\n");
                                } else {
                                    // 2일차~8일차 밤에만 사용 가능
                                    for (UserService targetUser : UserVec) {
                                        if (targetUser.UserName.equals(target)) {
                                            reporterTarget = target;
                                            reporterTargetRole = targetUser.role;
                                            WriteOne("SYSTEM: [" + target + "]님을 취재했습니다. 내일 아침에 특종이 발표됩니다!\n");
                                            AppendText("기자 " + UserName + "이 " + target + " 취재 -> 다음 낮에 공개");
                                            break;
                                        }
                                    }
                                }
                            } else if (actionRole.equals("GANGSTER")) {
                                // 건달의 투표 금지 능력
                                WriteOne("SYSTEM: [" + target + "]님을 선택했습니다. 다음 투표에서 투표하지 못합니다!\n");
                                AppendText("건달 " + UserName + "이 " + target + " 선택 -> 다음 투표 금지");

                                // 타겟에게 협박 메시지 전송
                                for (UserService targetUser : UserVec) {
                                    if (targetUser.UserName.equals(target)) {
                                        targetUser.WriteOne("SYSTEM: 협박을 받았습니다! 다음 투표에 참여할 수 없습니다.\n");
                                        break;
                                    }
                                }
                            } else if (actionRole.equals("PRIEST")) {
                                // 성직자의 소생 능력 (밤에 선택, 낮에 부활)
                                if (priestUsed) {
                                    WriteOne("SYSTEM: 이미 소생 능력을 사용했습니다!\n");
                                } else if (aliveStatus.get(target) == null) {
                                    WriteOne("SYSTEM: 해당 플레이어를 찾을 수 없습니다!\n");
                                } else if (aliveStatus.get(target)) {
                                    WriteOne("SYSTEM: [" + target + "]님은 살아있습니다! 죽은 사람만 부활시킬 수 있습니다.\n");
                                } else if (blessedStatus.get(target) != null && blessedStatus.get(target)) {
                                    WriteOne("SYSTEM: [" + target + "]님은 성불되어 부활할 수 없습니다!\n");
                                } else {
                                    // 부활 대상 저장 (낮에 실제 부활 처리)
                                    priestTarget = target;
                                    priestUsed = true;
                                    WriteOne("SYSTEM: [" + target + "]님을 부활 대상으로 선택했습니다. 다음 낮에 부활합니다!\n");
                                    AppendText("성직자 " + UserName + "이 " + target + " 부활 예약");
                                }
                            } else {
                                WriteOne("SYSTEM: 선택이 완료되었습니다.\n");
                            }
                        }
                    } else if (msg.startsWith("VOTE:")) {
                        // VOTE:TARGET 형식
                        String[] parts = msg.split(":");
                        if (parts.length == 2) {
                            String target = parts[1];

                            // 건달에 의해 투표가 금지된 경우
                            if (voteBanned.get(UserName) != null && voteBanned.get(UserName)) {
                                WriteOne("SYSTEM: 건달에 의해 투표가 금지되었습니다!\n");
                            }
                            // 죽은 사람은 투표할 수 없음
                            else if (aliveStatus.get(UserName) != null && !aliveStatus.get(UserName)) {
                                WriteOne("SYSTEM: 죽은 사람은 투표할 수 없습니다!\n");
                            }
                            // 죽은 사람에게 투표할 수 없음
                            else if (aliveStatus.get(target) != null && !aliveStatus.get(target)) {
                                WriteOne("SYSTEM: 죽은 사람에게는 투표할 수 없습니다!\n");
                            } else if (voteCount.containsKey(target)) {
                                // 정치인이면 2표, 그 외는 1표
                                int votes = role.equals("POLITICIAN") ? 2 : 1;
                                voteCount.put(target, voteCount.get(target) + votes);
                                AppendText(UserName + "(" + role + ") -> " + target + " 투표 (" + votes + "표)");
                                WriteOne("SYSTEM: [" + target + "]님에게 투표했습니다." + (votes == 2 ? " (2표)" : "") + "\n");

                                // 마담의 유혹 능력
                                if (role.equals("MADAME")) {
                                    seduced.put(target, true);
                                    AppendText("마담 " + UserName + "이 " + target + " 유혹 -> 밤 능력 사용 불가");

                                    // 유혹당한 사람에게 알림
                                    for (UserService targetUser : UserVec) {
                                        if (targetUser.UserName.equals(target)) {
                                            // 마피아를 유혹한 경우 접선
                                            if (targetUser.role.equals("MAFIA")) {
                                                madameContactedMafia = true;
                                                WriteOne("SYSTEM: [" + target
                                                        + "]님은 마피아입니다! 접선했습니다. 이제 밤에 대화할 수 있습니다.\n");
                                                targetUser.WriteOne(
                                                        "SYSTEM: [" + UserName + "]님이 마담으로 접선했습니다! 이제 동료입니다.\n");
                                                AppendText("마담과 마피아 접선 완료");

                                                // 접선 후 즉시 게임 종료 조건 체크
                                                new Thread(() -> {
                                                    try {
                                                        Thread.sleep(1000); // 1초 대기 후 체크
                                                        if (checkGameEnd()) {
                                                            AppendText("마담 접선 후 게임 종료 조건 충족");
                                                        }
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }).start();
                                            } else {
                                                // 일반 플레이어 유혹
                                                targetUser.WriteOne("SYSTEM: 💋 마담에게 유혹당했습니다! 밤에 능력을 사용할 수 없습니다.\n");
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else if (msg.contains("/exit")) {
                        logout();
                        return;
                    } else {
                        // 일반 채팅 메시지
                        if (gamePhase.equals("NIGHT")) {
                            // 밤에는 마피아 팀과 죽은 플레이어만 채팅 가능
                            if (aliveStatus.get(UserName) != null && !aliveStatus.get(UserName)) {
                                // 죽은 플레이어의 채팅 (밤에도 가능, 성불당하지 않은 경우에만)
                                if (blessedStatus.get(UserName) != null && blessedStatus.get(UserName)) {
                                    WriteOne("SYSTEM: 성불당해서 채팅할 수 없습니다.\n");
                                } else {
                                    // 죽은 플레이어끼리 채팅 + 영매도 볼 수 있음
                                    for (UserService user : UserVec) {
                                        // 죽은 플레이어들에게 전송
                                        if (aliveStatus.get(user.UserName) != null && !aliveStatus.get(user.UserName)) {
                                            user.WriteOne("[DEAD CHAT] " + msg + "\n");
                                        }
                                        // 영매에게도 전송 (살아있는 영매만)
                                        if (user.role.equals("SHAMAN") && (aliveStatus.get(user.UserName) == null
                                                || aliveStatus.get(user.UserName))) {
                                            user.WriteOne("[DEAD CHAT] " + msg + "\n");
                                        }
                                    }
                                    AppendText("[DEAD CHAT] " + msg);
                                }
                            } else if (role.equals("MAFIA")) {
                                // 마피아는 항상 채팅 가능
                                for (UserService user : UserVec) {
                                    if (user.role.equals("MAFIA")) {
                                        user.WriteOne("[MAFIA TEAM] " + msg + "\n");
                                    }
                                    // 스파이가 접선했다면 스파이에게도 전송
                                    if (user.role.equals("SPY") && spyContactedMafia) {
                                        user.WriteOne("[MAFIA TEAM] " + msg + "\n");
                                    }
                                    // 마담이 접선했다면 마담에게도 전송
                                    if (user.role.equals("MADAME") && madameContactedMafia) {
                                        user.WriteOne("[MAFIA TEAM] " + msg + "\n");
                                    }
                                }
                                AppendText("[MAFIA TEAM] " + msg);
                            } else if (role.equals("MADAME")) {
                                // 마담은 접선 후에만 채팅 가능
                                if (madameContactedMafia) {
                                    for (UserService user : UserVec) {
                                        if (user.role.equals("MAFIA")) {
                                            user.WriteOne("[MAFIA TEAM] " + msg + "\n");
                                        }
                                        if (user.role.equals("SPY") && spyContactedMafia) {
                                            user.WriteOne("[MAFIA TEAM] " + msg + "\n");
                                        }
                                        if (user.role.equals("MADAME")) {
                                            user.WriteOne("[MAFIA TEAM] " + msg + "\n");
                                        }
                                    }
                                    AppendText("[MAFIA TEAM] " + msg);
                                } else {
                                    WriteOne("SYSTEM: 마피아와 접선하기 전에는 채팅할 수 없습니다.\n");
                                }
                            } else if (role.equals("SPY")) {
                                // 스파이는 접선 후에만 채팅 가능
                                if (spyContactedMafia) {
                                    for (UserService user : UserVec) {
                                        if (user.role.equals("MAFIA")) {
                                            user.WriteOne("[MAFIA TEAM] " + msg + "\n");
                                        }
                                        if (user.role.equals("SPY")) {
                                            user.WriteOne("[MAFIA TEAM] " + msg + "\n");
                                        }
                                        if (user.role.equals("MADAME") && madameContactedMafia) {
                                            user.WriteOne("[MAFIA TEAM] " + msg + "\n");
                                        }
                                    }
                                    AppendText("[MAFIA TEAM] " + msg);
                                } else {
                                    WriteOne("SYSTEM: 마피아와 접선하기 전에는 채팅할 수 없습니다.\n");
                                }
                            } else {
                                WriteOne("SYSTEM: 밤에는 채팅할 수 없습니다.\n");
                            }
                        } else {
                            // 낮이나 투표 시간
                            // 마담에게 유혹당한 경우 채팅 불가 (마피아 제외, 투표 시간에만)
                            if (gamePhase.equals("VOTE") && seduced.get(UserName) != null && seduced.get(UserName)
                                    && !role.equals("MAFIA")) {
                                WriteOne("SYSTEM: 마담에게 유혹당해 채팅할 수 없습니다!\n");
                            }
                            // 살아있는 플레이어와 죽은 플레이어의 채팅 분리
                            else if (aliveStatus.get(UserName) != null && !aliveStatus.get(UserName)) {
                                // 죽은 플레이어의 채팅 (성불당하지 않은 경우에만)
                                if (blessedStatus.get(UserName) != null && blessedStatus.get(UserName)) {
                                    WriteOne("SYSTEM: 성불당해서 채팅할 수 없습니다.\n");
                                } else {
                                    // 죽은 플레이어끼리 채팅 + 영매도 볼 수 있음
                                    for (UserService user : UserVec) {
                                        // 죽은 플레이어들에게 전송
                                        if (aliveStatus.get(user.UserName) != null && !aliveStatus.get(user.UserName)) {
                                            user.WriteOne("[DEAD CHAT] " + msg + "\n");
                                        }
                                        // 영매에게도 전송 (살아있는 영매만)
                                        if (user.role.equals("SHAMAN") && (aliveStatus.get(user.UserName) == null
                                                || aliveStatus.get(user.UserName))) {
                                            user.WriteOne("[DEAD CHAT] " + msg + "\n");
                                        }
                                    }
                                    AppendText("[DEAD CHAT] " + msg);
                                }
                            } else {
                                // 살아있는 플레이어의 채팅은 모두에게 전송
                                WriteAll(msg + "\n");
                            }
                        }
                    }

                } catch (IOException e) {
                    AppendText("연결 오류: " + UserName);
                    try {
                        dos.close();
                        dis.close();
                        client_socket.close();
                        logout();
                        break;
                    } catch (Exception ee) {
                        break;
                    }
                }
            }
        }
    }
}
