package mafia.game;

/**
 * 마피아 게임 클라이언트 뷰 클래스
 *
 * 이 클래스는 마피아 게임의 메인 게임 화면을 담당합니다.
 * 서버와의 통신, 게임 진행 상태 표시, 플레이어 카드 표시, 채팅 기능을 제공합니다.
 *
 * 주요 기능:
 * - 서버와의 소켓 통신 (메시지 송수신)
 * - 게임 페이즈 표시 (대기, 밤, 낮, 투표)
 * - 플레이어 카드 그리드 (최대 8명)
 * - 실시간 채팅 시스템
 * - 역할별 행동 처리 (투표, 능력 사용)
 * - 사운드 재생 기능
 *
 * @author Mafia Game Team
 * @version 2.0
 */

// AWT 및 그래픽 관련 임포트
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

// IO 및 네트워크 관련 임포트
import java.io.*;
import java.net.Socket;

// 유틸리티 임포트
import java.util.*;

// 이미지 및 사운드 처리 임포트
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

// Swing UI 컴포넌트 임포트
import javax.swing.*;
import javax.swing.border.*;

/**
 * MafiaGameClientView 메인 클래스
 * JFrame을 상속받아 게임 화면 GUI를 제공합니다.
 */
public class MafiaGameClientView extends JFrame {

    // ========================================
    // UI 컴포넌트
    // ========================================

    /**
     * 메인 컨텐츠 패널
     */
    private JPanel contentPane;

    /**
     * 채팅 입력 필드
     */
    private JTextField txtInput;

    /**
     * 전송 버튼
     */
    private JButton btnSend;

    /**
     * 찬성 버튼
     */
    private JButton btnAgree;

    /**
     * 반대 버튼
     */
    private JButton btnDisagree;

    /**
     * 찬반 투표 버튼 패널
     */
    private JPanel agreeDisagreePanel;

    /**
     * 채팅 로그 텍스트 영역
     */
    private JTextArea textArea;

    /**
     * 플레이어 수 표시 레이블
     */
    private JLabel lblPlayerCount;

    /**
     * 게임 페이즈 정보 레이블
     */
    private JLabel lblPhaseInfo;

    /**
     * 플레이어 카드 그리드 패널
     */
    private JPanel cardGridPanel;

    /**
     * 플레이어 카드 배열 (최대 8명)
     */
    private PlayerCard[] playerCards;

    // ========================================
    // 네트워크 관련 변수
    // ========================================

    /**
     * 사용자 이름
     */
    private String UserName;

    /**
     * 서버 소켓
     */
    private Socket socket;

    /**
     * 입력 스트림
     */
    private InputStream is;

    /**
     * 출력 스트림
     */
    private OutputStream os;

    /**
     * 데이터 입력 스트림
     */
    private DataInputStream dis;

    /**
     * 데이터 출력 스트림
     */
    private DataOutputStream dos;

    // ========================================
    // 게임 상태 변수
    // ========================================

    /**
     * 현재 플레이어의 역할
     */
    private String myRole = "";

    /**
     * 현재 게임 페이즈 (WAITING, NIGHT, DAY, VOTE)
     */
    private String currentPhase = "WAITING";

    /**
     * 플레이어 사망 여부
     */
    private boolean isDead = false;

    /**
     * 최대 플레이어 수
     */
    private int maxPlayers = 8;

    /**
     * 게임 시작 사운드 재생 여부
     */
    private boolean gameStartSoundPlayed = false;

    /**
     * 현재 선택된 플레이어 카드
     */
    private PlayerCard selectedCard;

    // ========================================
    // 데이터 구조
    // ========================================

    /**
     * 플레이어 정보 맵 (이름 -> 정보)
     */
    private Map<String, PlayerInfo> playerMap;

    /**
     * 역할 이미지 매핑 (역할 -> 이미지 파일명)
     */
    private Map<String, String> roleImageMap;

    // ========================================
    // 사운드 관련 변수
    // ========================================

    /**
     * 현재 재생 중인 사운드 클립
     */
    private Clip currentClip;

    // ========================================
    // 생성자 및 초기화
    // ========================================

    /**
     * 게임 뷰 생성자
     * 서버에 연결하고 UI를 초기화합니다.
     *
     * @param username 사용자 이름
     * @param ip_addr  서버 IP 주소
     * @param port_no  서버 포트 번호
     */
    public MafiaGameClientView(String username, String ip_addr, String port_no) {
        // 초기화
        initializeDataStructures();

        // UI 생성
        initializeUI(username);

        // 네트워크 연결
        connectToServer(username, ip_addr, port_no);
    }

    /**
     * 데이터 구조 초기화
     */
    private void initializeDataStructures() {
        roleImageMap = new HashMap<>();
        playerMap = new HashMap<>();
        initializeRoleImageMap();
    }

    /**
     * 역할 이미지 매핑 초기화
     */
    private void initializeRoleImageMap() {
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
        roleImageMap.put("MADAME", "madame.png");
        roleImageMap.put("GHOUL", "dogul.png");
        roleImageMap.put("SPY", "spy.png");
        roleImageMap.put("DEFAULT", "default.png");
    }

    /**
     * UI 초기화
     *
     * @param username 사용자 이름
     */
    private void initializeUI(String username) {
        setTitle("Mafia Game - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1200, 700);

        contentPane = new GameBackgroundPanel();
        contentPane.setBackground(new Color(30, 30, 30));
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // UI 컴포넌트 생성
        createTopStatusBar();
        createChatPanel();
        createPlayerCardGrid();

        setVisible(true);
    }

    /**
     * 서버에 연결
     *
     * @param username 사용자 이름
     * @param ip_addr  서버 IP 주소
     * @param port_no  서버 포트 번호
     */
    private void connectToServer(String username, String ip_addr, String port_no) {
        AppendText("Connecting to " + ip_addr + ":" + port_no + "...\n");
        UserName = username;

        try {
            // 소켓 연결
            socket = new Socket(ip_addr, Integer.parseInt(port_no));
            is = socket.getInputStream();
            dis = new DataInputStream(is);
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);

            // 로그인 메시지 전송
            SendMessage("/login " + UserName);

            // 네트워크 리스너 시작
            ListenNetwork net = new ListenNetwork();
            net.start();

            // 액션 리스너 설정
            setupActionListeners();

            txtInput.requestFocus();

        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
            AppendText("Connection error!\n");
        }
    }

    /**
     * 액션 리스너 설정
     */
    private void setupActionListeners() {
        Myaction action = new Myaction();
        btnSend.addActionListener(action);
        txtInput.addActionListener(action);
    }

    // ========================================
    // UI 컴포넌트 생성 메소드
    // ========================================

    /**
     * 상단 상태바 생성
     * 플레이어 수와 현재 페이즈를 표시합니다.
     */
    private void createTopStatusBar() {
        JPanel topBar = new JPanel();
        topBar.setBackground(new Color(40, 40, 40));
        topBar.setBounds(0, 0, 1200, 50);
        topBar.setLayout(null);
        contentPane.add(topBar);

        // 플레이어 수 표시
        lblPlayerCount = new JLabel("0/8");
        lblPlayerCount.setFont(new Font("Arial", Font.BOLD, 18));
        lblPlayerCount.setForeground(Color.WHITE);
        lblPlayerCount.setBounds(20, 10, 100, 30);
        topBar.add(lblPlayerCount);

        // 페이즈 정보 표시
        lblPhaseInfo = new JLabel("대기 중...");
        lblPhaseInfo.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        lblPhaseInfo.setForeground(new Color(255, 200, 100));
        lblPhaseInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblPhaseInfo.setBounds(400, 10, 400, 30);
        topBar.add(lblPhaseInfo);
    }

    /**
     * 채팅 패널 생성
     * 채팅 로그와 입력 필드를 포함합니다.
     */
    private void createChatPanel() {
        JPanel chatPanel = new JPanel();
        chatPanel.setBackground(new Color(35, 35, 35));
        chatPanel.setBorder(new LineBorder(new Color(60, 60, 60), 1));
        chatPanel.setBounds(10, 60, 540, 590);
        chatPanel.setLayout(null);
        contentPane.add(chatPanel);

        // 채팅 제목
        JLabel chatTitle = new JLabel("채팅");
        chatTitle.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        chatTitle.setForeground(new Color(200, 200, 200));
        chatTitle.setBounds(15, 10, 200, 25);
        chatPanel.add(chatTitle);

        // 채팅 로그 영역
        createChatArea(chatPanel);

        // 입력 영역
        createInputArea(chatPanel);
    }

    /**
     * 채팅 로그 영역 생성
     *
     * @param chatPanel 채팅 패널
     */
    private void createChatArea(JPanel chatPanel) {
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
    }

    /**
     * 입력 영역 생성
     *
     * @param chatPanel 채팅 패널
     */
    private void createInputArea(JPanel chatPanel) {
        // 입력 필드
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

        // 전송 버튼
        btnSend = new JButton("전송");
        btnSend.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnSend.setBackground(new Color(70, 130, 180));
        btnSend.setForeground(Color.WHITE);
        btnSend.setBorderPainted(false);
        btnSend.setFocusPainted(false);
        btnSend.setBounds(440, 540, 90, 40);
        chatPanel.add(btnSend);
    }

    /**
     * 플레이어 카드 그리드 생성
     * 최대 8명의 플레이어를 2x4 그리드로 표시합니다.
     */
    private void createPlayerCardGrid() {
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(30, 30, 30));
        rightPanel.setBounds(560, 60, 620, 590);
        rightPanel.setLayout(null);
        contentPane.add(rightPanel);

        // 제목
        JLabel gridTitle = new JLabel("플레이어");
        gridTitle.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        gridTitle.setForeground(new Color(200, 200, 200));
        gridTitle.setBounds(15, 10, 200, 25);
        rightPanel.add(gridTitle);

        // 카드 그리드 패널
        cardGridPanel = new JPanel();
        cardGridPanel.setBackground(new Color(30, 30, 30));
        cardGridPanel.setBounds(10, 45, 600, 535);
        cardGridPanel.setLayout(new GridLayout(2, 4, 15, 15));
        rightPanel.add(cardGridPanel);

        // 플레이어 카드 초기화
        playerCards = new PlayerCard[maxPlayers];
        for (int i = 0; i < maxPlayers; i++) {
            playerCards[i] = new PlayerCard(i);
            cardGridPanel.add(playerCards[i]);
        }
    }

    // ========================================
    // 게임 로직 메소드
    // ========================================

    /**
     * 플레이어 선택 시 행동 수행
     *
     * @param targetPlayer 선택된 플레이어 이름
     */
    private void performActionOnPlayer(String targetPlayer) {
        if (isDead) {
            AppendText("사망한 플레이어는 행동할 수 없습니다.\n");
            return;
        }

        if (targetPlayer == null || targetPlayer.isEmpty()) {
            return;
        }

        // [DEAD] 접두사 제거
        if (targetPlayer.startsWith("[DEAD]")) {
            targetPlayer = targetPlayer.substring(6);
        }

        if (currentPhase.equals("NIGHT")) {
            performNightAction(targetPlayer);
        } else if (currentPhase.equals("VOTE")) {
            performVote(targetPlayer);
        }
    }

    /**
     * 밤 행동 수행
     *
     * @param target 대상 플레이어
     */
    private void performNightAction(String target) {
        String action = "";

        switch (myRole) {
            case "MAFIA":
                action = "NIGHT_ACTION:MAFIA:" + target;
                AppendText("선택: [" + target + "] 제거\n");
                break;
            case "SPY":
                action = "NIGHT_ACTION:SPY:" + target;
                AppendText("선택: [" + target + "] 역할 조사\n");
                break;
            case "DOCTOR":
                action = "NIGHT_ACTION:DOCTOR:" + target;
                AppendText("선택: [" + target + "] 보호\n");
                break;
            case "POLICE":
                action = "NIGHT_ACTION:POLICE:" + target;
                AppendText("선택: [" + target + "] 조사\n");
                break;
            case "SHAMAN":
                action = "NIGHT_ACTION:SHAMAN:" + target;
                AppendText("선택: [" + target + "] 축복\n");
                break;
            case "REPORTER":
                action = "NIGHT_ACTION:REPORTER:" + target;
                AppendText("선택: [" + target + "] 취재\n");
                break;
            case "GANGSTER":
                action = "NIGHT_ACTION:GANGSTER:" + target;
                AppendText("선택: [" + target + "] 투표 금지\n");
                break;
            case "PRIEST":
                action = "NIGHT_ACTION:PRIEST:" + target;
                AppendText("선택: [" + target + "] 부활\n");
                break;
            // MADAME은 투표로만 능력 사용 (밤 행동 없음)
        }

        if (!action.isEmpty()) {
            SendMessage(action);
        }
    }

    /**
     * 투표 수행
     *
     * @param target 대상 플레이어
     */
    private void performVote(String target) {
        SendMessage("VOTE:" + target);
        AppendText("투표: [" + target + "]\n");
    }

    /**
     * 페이즈 표시 업데이트
     *
     * @param phase 현재 페이즈
     */
    private void updatePhaseDisplay(String phase) {
        // FINAL_DEFENSE:플레이어명 형식 처리
        if (phase.startsWith("FINAL_DEFENSE:")) {
            String[] parts = phase.split(":");
            String targetPlayer = parts.length > 1 ? parts[1] : "";
            lblPhaseInfo.setText("최후의 반론 - " + targetPlayer);
            lblPhaseInfo.setForeground(new Color(255, 165, 0));
            hideAgreeDisagreeButtons();
            return;
        }

        switch (phase) {
            case "WAITING":
                lblPhaseInfo.setText("게임 시작 대기 중...");
                lblPhaseInfo.setForeground(new Color(200, 200, 200));
                hideAgreeDisagreeButtons();
                break;
            case "NIGHT":
                lblPhaseInfo.setText("밤 - 능력을 사용하세요");
                lblPhaseInfo.setForeground(new Color(100, 100, 255));
                hideAgreeDisagreeButtons();
                break;
            case "DAY":
                lblPhaseInfo.setText("낮 - 토론 시간");
                lblPhaseInfo.setForeground(new Color(255, 200, 100));
                hideAgreeDisagreeButtons();
                break;
            case "VOTE":
                lblPhaseInfo.setText("투표 시간");
                lblPhaseInfo.setForeground(new Color(255, 100, 100));
                hideAgreeDisagreeButtons();
                break;
            case "AGREE_DISAGREE":
                lblPhaseInfo.setText("찬반 투표");
                lblPhaseInfo.setForeground(new Color(220, 20, 60));
                showAgreeDisagreeButtons();
                break;
            default:
                lblPhaseInfo.setText(phase);
                lblPhaseInfo.setForeground(Color.WHITE);
                hideAgreeDisagreeButtons();
        }
    }

    /**
     * 찬반 투표 버튼 표시 (고급 애니메이션 디자인)
     */
    private void showAgreeDisagreeButtons() {
        if (agreeDisagreePanel == null) {
            // 메인 패널 생성 (그라데이션 효과)
            agreeDisagreePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // 그라데이션 배경
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(45, 45, 45),
                        0, getHeight(), new Color(30, 30, 30)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                    // 테두리 효과
                    g2d.setColor(new Color(100, 100, 100, 150));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
                }
            };
            agreeDisagreePanel.setOpaque(false);
            agreeDisagreePanel.setBounds(100, 450, 360, 130);
            agreeDisagreePanel.setLayout(null);
            contentPane.add(agreeDisagreePanel);
            contentPane.setComponentZOrder(agreeDisagreePanel, 0);

            // 제목 레이블 (빛나는 효과)
            JLabel titleLabel = new JLabel("⚖️ 처형 여부를 선택하세요 ⚖️", SwingConstants.CENTER);
            titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
            titleLabel.setForeground(new Color(255, 215, 0));
            titleLabel.setBounds(10, 15, 340, 30);

            // 제목 그림자 효과
            titleLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 215, 0, 100)),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)
            ));
            agreeDisagreePanel.add(titleLabel);

            // 찬성 버튼 (고급 디자인)
            btnAgree = createStyledButton("✓ 찬성", new Color(46, 204, 113), new Color(39, 174, 96));
            btnAgree.setBounds(30, 60, 145, 55);
            btnAgree.addActionListener(e -> {
                animateButtonClick(btnAgree);
                new Thread(() -> {
                    try {
                        Thread.sleep(200);
                        SendMessage("AGREE_DISAGREE:AGREE");
                        hideAgreeDisagreeButtons();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            });
            agreeDisagreePanel.add(btnAgree);

            // 반대 버튼 (고급 디자인)
            btnDisagree = createStyledButton("✗ 반대", new Color(231, 76, 60), new Color(192, 57, 43));
            btnDisagree.setBounds(185, 60, 145, 55);
            btnDisagree.addActionListener(e -> {
                animateButtonClick(btnDisagree);
                new Thread(() -> {
                    try {
                        Thread.sleep(200);
                        SendMessage("AGREE_DISAGREE:DISAGREE");
                        hideAgreeDisagreeButtons();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            });
            agreeDisagreePanel.add(btnDisagree);
        }

        // 페이드 인 애니메이션
        agreeDisagreePanel.setVisible(true);
        fadeInPanel(agreeDisagreePanel);
    }

    /**
     * 스타일이 적용된 버튼 생성
     */
    private JButton createStyledButton(String text, Color normalColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 그라데이션 배경
                GradientPaint gradient = new GradientPaint(
                    0, 0, getBackground(),
                    0, getHeight(), getBackground().darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // 하이라이트 효과
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() / 2 - 5, 10, 10);

                // 텍스트
                g2d.setColor(getForeground());
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                // 텍스트 그림자
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(getText(), textX + 1, textY + 1);

                // 실제 텍스트
                g2d.setColor(getForeground());
                g2d.drawString(getText(), textX, textY);
            }
        };

        button.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        button.setBackground(normalColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 호버 애니메이션
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animateColorChange(button, button.getBackground(), hoverColor, 200);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                animateColorChange(button, button.getBackground(), normalColor, 200);
            }
        });

        return button;
    }

    /**
     * 색상 변화 애니메이션
     */
    private void animateColorChange(JButton button, Color from, Color to, int duration) {
        new Thread(() -> {
            int steps = 20;
            int delay = duration / steps;

            for (int i = 0; i <= steps; i++) {
                float ratio = (float) i / steps;
                int r = (int) (from.getRed() + (to.getRed() - from.getRed()) * ratio);
                int g = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * ratio);
                int b = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * ratio);

                Color newColor = new Color(r, g, b);
                SwingUtilities.invokeLater(() -> {
                    button.setBackground(newColor);
                    button.repaint();
                });

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    /**
     * 버튼 클릭 애니메이션
     */
    private void animateButtonClick(JButton button) {
        new Thread(() -> {
            try {
                // 축소
                for (int i = 0; i < 5; i++) {
                    int offset = i * 2;
                    int finalOffset = offset;
                    SwingUtilities.invokeLater(() -> {
                        button.setBounds(
                            button.getX() + finalOffset,
                            button.getY() + finalOffset,
                            button.getWidth() - finalOffset * 2,
                            button.getHeight() - finalOffset * 2
                        );
                    });
                    Thread.sleep(20);
                }

                // 확대
                for (int i = 5; i >= 0; i--) {
                    int offset = i * 2;
                    int finalOffset = offset;
                    SwingUtilities.invokeLater(() -> {
                        button.setBounds(
                            button.getX() - finalOffset,
                            button.getY() - finalOffset,
                            button.getWidth() + finalOffset * 2,
                            button.getHeight() + finalOffset * 2
                        );
                    });
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 패널 페이드 인 애니메이션
     */
    private void fadeInPanel(JPanel panel) {
        panel.setOpaque(false);
        new Thread(() -> {
            for (int i = 0; i <= 10; i++) {
                try {
                    Thread.sleep(30);
                    panel.repaint();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    /**
     * 찬반 투표 버튼 숨기기
     */
    private void hideAgreeDisagreeButtons() {
        if (agreeDisagreePanel != null) {
            agreeDisagreePanel.setVisible(false);
            contentPane.repaint();
        }
    }

    /**
     * 플레이어 카드 업데이트
     *
     * @param players 플레이어 목록 문자열
     */
    private void updatePlayerCards(String players) {
        // 모든 카드 초기화
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

                    // 다른 플레이어의 역할은 알 수 없으므로 DEFAULT 사용
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

    /**
     * 역할 표시 이름 반환
     *
     * @param role 역할 코드
     * @return 한글 역할 이름
     */
    private String getRoleDisplayName(String role) {
        switch (role) {
            case "MAFIA":
                return "마피아";
            case "MADAME":
                return "마담";
            case "GHOUL":
                return "도굴꾼";
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

    // ========================================
    // 네트워크 관련 메소드
    // ========================================

    /**
     * 채팅 메시지 추가
     *
     * @param msg 메시지
     */
    public void AppendText(String msg) {
        textArea.append(msg);
        textArea.setCaretPosition(textArea.getText().length());
    }

    /**
     * 서버로 메시지 전송
     *
     * @param msg 전송할 메시지
     */
    public void SendMessage(String msg) {
        try {
            dos.writeUTF(msg);
        } catch (IOException e) {
            AppendText("메시지 전송 실패.\n");
            closeConnection();
        }
    }

    /**
     * 연결 종료
     */
    private void closeConnection() {
        try {
            dos.close();
            dis.close();
            socket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(0);
        }
    }

    // ========================================
    // 사운드 관련 메소드
    // ========================================

    /**
     * 서버로부터 받은 사운드 재생
     *
     * @param filePath 사운드 파일 경로
     */
    private void playServerSound(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        // 이전 사운드 중지
        stopCurrentSound();

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

                // 사운드 종료 리스너
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

    /**
     * 현재 사운드 중지
     */
    private void stopCurrentSound() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }

    // ========================================
    // 내부 클래스 - 플레이어 카드
    // ========================================

    /**
     * 개별 플레이어를 표시하는 카드 컴포넌트
     */
    class PlayerCard extends JPanel {
        /**
         * 카드 인덱스
         */
        private int index;

        /**
         * 이미지 레이블
         */
        private JLabel imageLabel;

        /**
         * 이름 레이블
         */
        private JLabel nameLabel;

        /**
         * 상태 아이콘 레이블
         */
        private JLabel statusIcon;

        /**
         * 플레이어 이름
         */
        private String playerName;

        /**
         * 역할
         */
        private String role;

        /**
         * 생존 여부
         */
        private boolean isAlive = true;

        /**
         * 빈 카드 여부
         */
        private boolean isEmpty = true;

        /**
         * 역할 이미지
         */
        private Image roleImage;

        /**
         * 플레이어 카드 생성자
         *
         * @param index 카드 인덱스
         */
        public PlayerCard(int index) {
            this.index = index;
            initializeCard();
            setupMouseListener();
        }

        /**
         * 카드 초기화
         */
        private void initializeCard() {
            setLayout(null);
            setBackground(new Color(45, 45, 45));
            setBorder(new LineBorder(new Color(70, 70, 70), 2));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // 이미지 영역
            imageLabel = new JLabel();
            imageLabel.setBounds(5, 5, 130, 180);
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setVerticalAlignment(SwingConstants.CENTER);
            add(imageLabel);

            // 상태 아이콘 (우측 상단)
            statusIcon = new JLabel();
            statusIcon.setBounds(110, 10, 25, 25);
            statusIcon.setFont(new Font("Arial", Font.BOLD, 20));
            add(statusIcon);

            // 이름 레이블
            nameLabel = new JLabel("", SwingConstants.CENTER);
            nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setBounds(5, 190, 130, 25);
            add(nameLabel);

            // 빈 상태 표시
            showEmptyState();
        }

        /**
         * 마우스 리스너 설정
         */
        private void setupMouseListener() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // 영매와 성직자는 죽은 사람도 선택 가능
                    boolean canSelectDead = myRole.equals("SHAMAN") || myRole.equals("PRIEST");
                    boolean canClick = !isEmpty && (isAlive || canSelectDead);

                    if (canClick) {
                        handleCardClick();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    // 영매와 성직자는 죽은 사람도 선택 가능
                    boolean canSelectDead = myRole.equals("SHAMAN") || myRole.equals("PRIEST");
                    boolean canHover = !isEmpty && (isAlive || canSelectDead);

                    if (canHover) {
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

        /**
         * 카드 클릭 처리
         */
        private void handleCardClick() {
            if (currentPhase.equals("NIGHT") || currentPhase.equals("VOTE")) {
                // 이전 카드 선택 해제
                if (selectedCard != null && selectedCard != this) {
                    selectedCard.setBorder(new LineBorder(new Color(70, 70, 70), 2));
                }

                // 현재 카드 선택
                selectedCard = this;
                setBorder(new LineBorder(new Color(255, 215, 0), 3));

                // 행동 수행
                performActionOnPlayer(playerName);
            }
        }

        /**
         * 플레이어 이름 반환
         *
         * @return 플레이어 이름
         */
        public String getPlayerName() {
            return playerName;
        }

        /**
         * 플레이어 설정
         *
         * @param name  이름
         * @param role  역할
         * @param alive 생존 여부
         */
        public void setPlayer(String name, String role, boolean alive) {
            this.playerName = name;
            this.role = role;
            this.isAlive = alive;
            this.isEmpty = false;

            nameLabel.setText(name);

            // 역할 이미지 로드
            loadRoleImage(role);

            // 상태 업데이트
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

        /**
         * 플레이어 제거
         */
        public void clearPlayer() {
            this.isEmpty = true;
            this.playerName = null;
            this.role = null;
            this.isAlive = true;
            showEmptyState();
        }

        /**
         * 빈 상태 표시
         */
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

        /**
         * 역할 이미지 로드
         *
         * @param role 역할
         */
        private void loadRoleImage(String role) {
            try {
                String imageName = roleImageMap.getOrDefault(role, "default.png");
                String imagePath = "/info/" + imageName;

                InputStream imgStream = getClass().getResourceAsStream(imagePath);
                if (imgStream != null) {
                    BufferedImage img = ImageIO.read(imgStream);
                    Image scaledImg = img.getScaledInstance(130, 180, Image.SCALE_SMOOTH);
                    roleImage = scaledImg;
                    imageLabel.setIcon(new ImageIcon(scaledImg));
                    imageLabel.setText("");
                    imgStream.close();
                } else {
                    imageLabel.setText("?");
                    imageLabel.setFont(new Font("Arial", Font.BOLD, 48));
                    imageLabel.setForeground(Color.GRAY);
                }
            } catch (IOException e) {
                imageLabel.setText("?");
                imageLabel.setFont(new Font("Arial", Font.BOLD, 48));
                imageLabel.setForeground(Color.GRAY);
            }
        }

        /**
         * 그레이스케일 효과 적용 (사망 시)
         */
        private void applyGrayscale() {
            if (roleImage != null) {
                BufferedImage buffered = new BufferedImage(
                        roleImage.getWidth(null),
                        roleImage.getHeight(null),
                        BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2d = buffered.createGraphics();
                g2d.drawImage(roleImage, 0, 0, null);
                g2d.dispose();

                // 그레이스케일 변환
                for (int y = 0; y < buffered.getHeight(); y++) {
                    for (int x = 0; x < buffered.getWidth(); x++) {
                        int rgb = buffered.getRGB(x, y);
                        int alpha = (rgb >> 24) & 0xff;
                        int r = (rgb >> 16) & 0xff;
                        int g = (rgb >> 8) & 0xff;
                        int b = rgb & 0xff;

                        int gray = (r + g + b) / 3;
                        gray = (int) (gray * 0.5); // 어둡게

                        int newRgb = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                        buffered.setRGB(x, y, newRgb);
                    }
                }

                imageLabel.setIcon(new ImageIcon(buffered));
            }
        }
    }

    // ========================================
    // 내부 클래스 - 네트워크 리스너
    // ========================================

    /**
     * 서버로부터 메시지를 수신하는 스레드
     */
    class ListenNetwork extends Thread {
        /**
         * 스레드 실행
         * 서버로부터 메시지를 지속적으로 수신합니다.
         */
        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF();

                    // 메시지 타입별 처리
                    if (msg.startsWith("ROLE:")) {
                        handleRoleMessage(msg);
                    } else if (msg.startsWith("PHASE:")) {
                        handlePhaseMessage(msg);
                    } else if (msg.startsWith("PLAYERS:")) {
                        handlePlayersMessage(msg);
                    } else if (msg.startsWith("DEAD:")) {
                        handleDeadMessage(msg);
                    } else if (msg.startsWith("SOUND:")) {
                        handleSoundMessage(msg);
                    } else if (msg.startsWith("REVEAL:")) {
                        handleRevealMessage(msg);
                    } else {
                        AppendText(msg);
                    }

                } catch (IOException e) {
                    AppendText("연결이 끊어졌습니다!\n");
                    closeConnection();
                    break;
                }
            }
        }

        /**
         * 역할 메시지 처리
         *
         * @param msg 메시지
         */
        private void handleRoleMessage(String msg) {
            String role = msg.substring(5).trim();
            myRole = role;
            AppendText("당신의 역할: " + getRoleDisplayName(role) + "\n");
        }

        /**
         * 페이즈 메시지 처리
         *
         * @param msg 메시지
         */
        private void handlePhaseMessage(String msg) {
            String phase = msg.substring(6).trim();
            currentPhase = phase;
            updatePhaseDisplay(phase);
        }

        /**
         * 플레이어 목록 메시지 처리
         *
         * @param msg 메시지
         */
        private void handlePlayersMessage(String msg) {
            String players = msg.substring(8).trim();
            updatePlayerCards(players);
        }

        /**
         * 사망 메시지 처리
         *
         * @param msg 메시지
         */
        private void handleDeadMessage(String msg) {
            String status = msg.substring(5).trim();
            if (status.equals("true")) {
                isDead = true;
                AppendText("=== 당신은 사망했습니다. ===\n");
            } else if (status.equals("false")) {
                isDead = false;
                AppendText("=== 부활했습니다! ===\n");
            }
        }

        /**
         * 사운드 메시지 처리
         *
         * @param msg 메시지
         */
        private void handleSoundMessage(String msg) {
            String soundPath = msg.substring(6).trim();
            playServerSound(soundPath);
        }

        /**
         * REVEAL 메시지 처리 (접선 시 상대방 역할 이미지 표시)
         * 형식: REVEAL:플레이어이름:역할
         *
         * @param msg 메시지
         */
        private void handleRevealMessage(String msg) {
            // REVEAL:이름:역할
            String[] parts = msg.substring(7).split(":");
            if (parts.length == 2) {
                String playerName = parts[0];
                String role = parts[1].trim();

                // 해당 플레이어 카드 찾아서 이미지 업데이트
                for (PlayerCard card : playerCards) {
                    if (playerName.equals(card.getPlayerName())) {
                        card.setPlayer(playerName, role, true);
                        break;
                    }
                }
            }
        }
    }

    // ========================================
    // 내부 클래스 - 액션 리스너
    // ========================================

    /**
     * 채팅 입력 및 전송 버튼 액션 리스너
     */
    class Myaction implements ActionListener {
        /**
         * 액션 이벤트 처리
         *
         * @param e 액션 이벤트
         */
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

    // ========================================
    // 내부 클래스 - 플레이어 정보
    // ========================================

    /**
     * 플레이어 정보를 저장하는 헬퍼 클래스
     */
    class PlayerInfo {
        /**
         * 플레이어 이름
         */
        String name;

        /**
         * 역할
         */
        String role;

        /**
         * 생존 여부
         */
        boolean alive;

        /**
         * 플레이어 정보 생성자
         *
         * @param name  이름
         * @param role  역할
         * @param alive 생존 여부
         */
        PlayerInfo(String name, String role, boolean alive) {
            this.name = name;
            this.role = role;
            this.alive = alive;
        }
    }

    /**
     * 게임 화면 배경 패널
     *
     * 게임 배경 이미지를 표시하는 커스텀 패널입니다.
     */
    class GameBackgroundPanel extends JPanel {
        private Image backgroundImage;

        /**
         * 게임 배경 패널 생성자
         *
         * /info/game_background.png 또는 .jpg 파일을 로드합니다.
         * 파일이 없으면 기본 배경색으로 표시됩니다.
         */
        public GameBackgroundPanel() {
            try {
                // 게임 배경 이미지 우선 시도
                java.net.URL bgURL = getClass().getResource("/info/game_background.png");

                // PNG가 없으면 JPG 시도
                if (bgURL == null) {
                    bgURL = getClass().getResource("/info/game_background.jpg");
                }

                // 게임 배경이 없으면 기본 배경 사용
                if (bgURL == null) {
                    bgURL = getClass().getResource("/info/background.png");
                }

                if (bgURL != null) {
                    backgroundImage = new javax.swing.ImageIcon(bgURL).getImage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // 배경 이미지를 패널 크기에 맞게 그리기
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
