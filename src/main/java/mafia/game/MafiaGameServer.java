package mafia.game;

/**
 * 마피아 게임 서버 클래스
 *
 * 이 클래스는 마피아 게임의 서버 역할을 담당합니다.
 * 클라이언트 연결 관리, 게임 진행 로직, 역할 배정, 페이즈 관리 등의 기능을 제공합니다.
 *
 * 주요 기능:
 * - 서버 소켓을 통한 클라이언트 연결 관리
 * - 게임 시작 및 역할 배정
 * - 밤/낮 페이즈 관리 및 전환
 * - 투표 시스템 및 게임 종료 조건 처리
 * - 사운드 재생 기능
 *
 * @author Mafia Game Team
 * @version 2.0
 */

// 표준 라이브러리 임포트
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

// 사운드 관련 라이브러리
import javax.sound.sampled.*;

// Swing UI 관련 라이브러리
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

// 신규 기능 매니저 임포트
import mafia.game.features.*;
import mafia.game.models.*;
import mafia.game.events.*;
import mafia.game.features.WhisperManager.WhisperResult;
import mafia.game.features.EmotionManager.EmotionResult;
import mafia.game.features.VoteTracker.VoteMode;
import mafia.game.features.ReconnectionManager.PlayerGameState;
import mafia.game.features.ReconnectionManager.ReconnectionResult;

/**
 * MafiaGameServer 메인 클래스
 * JFrame을 상속받아 GUI 서버 인터페이스를 제공합니다.
 */
public class MafiaGameServer extends JFrame {

    // ========================================
    // 상수 정의
    // ========================================

    /**
     * 직렬화 버전 UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 테스트 모드 활성화 여부
     * true: 8명 접속 시 자동 게임 시작
     * false: 수동으로 게임 시작 버튼 클릭 필요
     *
     * 배포 전에는 반드시 false로 변경할 것!
     */
    private static final boolean TEST_MODE = true;

    /**
     * 테스트 모드 자동 시작 인원수
     */
    private static final int AUTO_START_PLAYER_COUNT = 8;

    // ========================================
    // UI 컴포넌트
    // ========================================

    /**
     * 메인 컨텐츠 패널
     */
    private JPanel contentPane;

    /**
     * 서버 로그를 표시하는 텍스트 영역
     */
    JTextArea textArea;

    /**
     * 포트 번호 입력 필드
     */
    private JTextField txtPortNumber;

    /**
     * 게임 시작 버튼
     */
    private JButton btnGameStart;

    /**
     * 서버 상태 표시 레이블
     */
    private JLabel statusLabel;

    // ========================================
    // 네트워크 관련 변수
    // ========================================

    /**
     * 서버 소켓
     */
    private ServerSocket socket;

    /**
     * 클라이언트 소켓
     */
    private Socket client_socket;

    /**
     * 접속한 사용자들의 서비스 객체를 저장하는 벡터
     */
    private Vector<UserService> UserVec = new Vector<>();

    // ========================================
    // 게임 상태 변수
    // ========================================

    /**
     * 게임 시작 여부
     */
    private boolean gameStarted = false;

    /**
     * 현재 게임 페이즈 (WAITING, NIGHT, DAY, VOTE, RESULT)
     */
    private String gamePhase = "WAITING";

    /**
     * 현재 날짜 카운트
     */
    private int dayCount = 0;

    /**
     * 각 플레이어의 생존 상태 (이름 -> 생존여부)
     */
    private Map<String, Boolean> aliveStatus = new HashMap<>();

    /**
     * 투표 집계 (이름 -> 투표수)
     */
    private Map<String, Integer> voteCount = new HashMap<>();

    /**
     * 밤 행동 저장 (역할 -> 대상)
     */
    private Map<String, String> nightActions = new HashMap<>();

    // ========================================
    // 역할별 특수 상태 변수
    // ========================================

    /**
     * 군인의 방어막 상태 (이름 -> 방어막 존재 여부)
     */
    private Map<String, Boolean> soldierShield = new HashMap<>();

    /**
     * 성불 상태 (이름 -> 성불 여부)
     */
    private Map<String, Boolean> blessedStatus = new HashMap<>();

    /**
     * 건달에 의한 투표 금지 상태 (이름 -> 투표 금지 여부)
     */
    private Map<String, Boolean> voteBanned = new HashMap<>();

    /**
     * 이번 라운드에 이미 투표한 플레이어 추적 (중복 투표 방지)
     */
    private Set<String> hasVotedThisRound = new HashSet<>();

    /**
     * 크리스마스 이벤트: 산타의 선물을 받은 플레이어 (2표 스킬)
     */
    private String santaGiftReceiver = null;

    /**
     * 크리스마스 이벤트 활성화 여부
     */
    private boolean christmasEventActive = false;

    /**
     * 마담에게 유혹당한 플레이어 (이름 -> 유혹 여부)
     */
    private Map<String, Boolean> seduced = new HashMap<>();

    /**
     * 스파이가 마피아와 접선했는지 여부
     */
    private boolean spyContactedMafia = false;

    /**
     * 마담이 마피아와 접선했는지 여부
     */
    private boolean madameContactedMafia = false;
    private boolean spyUsedThisNight = false;
    private boolean doctorUsedThisNight = false;

    // ========================================
    // 역할 이름 저장 변수
    // ========================================

    /**
     * 마피아 플레이어 이름
     */
    private String mafiaName = "";

    /**
     * 스파이 플레이어 이름
     */
    private String spyName = "";

    /**
     * 영매 플레이어 이름
     */
    private String shamanName = "";

    /**
     * 도굴꾼 플레이어 이름
     */
    private String ghoulName = "";

    /**
     * 마담 플레이어 이름
     */
    private String madameName = "";

    // ========================================
    // 특수 능력 관련 변수
    // ========================================

    /**
     * 기자가 선택한 타겟
     */
    private String reporterTarget = "";

    /**
     * 기자 타겟의 직업
     */
    private String reporterTargetRole = "";

    /**
     * 밤 카운트 (기자 능력 사용 제한용)
     */
    private int nightCount = 0;

    /**
     * 기자가 능력을 사용했는지 여부
     */
    private boolean reporterUsedAbility = false;

    // ========================================
    // 최후의 반론 관련 변수
    // ========================================

    /**
     * 찬반 투표 집계 (찬성/반대)
     */
    private int agreeVotes = 0;
    private int disagreeVotes = 0;

    /**
     * 이번 찬반 투표에 참여한 플레이어 추적
     */
    private Set<String> hasVotedFinalDecision = new HashSet<>();

    /**
     * 최후의 반론 대상 플레이어
     */
    private String finalDefensePlayer = "";

    /**
     * 도굴꾼이 변신했는지 여부
     */
    private boolean ghoulTransformed = false;

    /**
     * 도굴꾼이 직업을 가져간 사람 (부활 시 시민이 됨)
     */
    private String ghoulVictim = "";

    /**
     * 성직자가 소생 능력을 사용했는지 여부
     */
    private boolean priestUsed = false;

    /**
     * 이번 밤에 경찰 능력을 사용했는지 여부
     */
    private boolean policeUsedThisNight = false;

    /**
     * 이번 밤에 건달 능력을 사용했는지 여부
     */
    private boolean gangsterUsedThisNight = false;

    /**
     * 이번 밤에 영매 능력을 사용했는지 여부
     */
    private boolean shamanUsedThisNight = false;

    /**
     * 성직자가 선택한 부활 대상
     */
    private String priestTarget = "";

    // ========================================
    // 사운드 관련 변수
    // ========================================

    /**
     * 현재 재생 중인 사운드 클립
     */
    private Clip currentClip;

    // ========================================
    // 신규 기능 매니저 인스턴스
    // ========================================

    /**
     * 쪽지 시스템 매니저
     */
    private WhisperManager whisperManager;

    /**
     * 로비 시스템 매니저
     */
    private LobbyManager lobbyManager;

    /**
     * 통계 및 업적 매니저
     */
    private StatisticsManager statisticsManager;

    /**
     * 투표 추적 매니저
     */
    private VoteTracker voteTracker;

    /**
     * 역할 가이드 매니저
     */
    private RoleGuideManager roleGuideManager;

    /**
     * 감정 표현 매니저
     */
    private EmotionManager emotionManager;

    /**
     * 재접속 매니저
     */
    private ReconnectionManager reconnectionManager;

    /**
     * 이벤트 모드 매니저
     */
    private EventModeManager eventModeManager;

    // ========================================
    // 메인 메소드
    // ========================================

    /**
     * 프로그램 진입점
     *
     * @param args 커맨드 라인 인자
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // 서버 프레임 생성 및 표시
                    MafiaGameServer frame = new MafiaGameServer();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ========================================
    // 생성자 및 UI 초기화
    // ========================================

    /**
     * 서버 GUI를 초기화하는 생성자
     */
    public MafiaGameServer() {
        initializeFrame();
        initializeTheme();
        createHeaderPanel();
        createCenterPanel();
        createControlPanel();
        initializeManagers();
    }

    /**
     * 신규 기능 매니저들을 초기화합니다.
     * Singleton 인스턴스를 가져와 이벤트 리스너를 설정합니다.
     */
    private void initializeManagers() {
        // 매니저 인스턴스 초기화
        whisperManager = WhisperManager.getInstance();
        lobbyManager = LobbyManager.getInstance();
        statisticsManager = StatisticsManager.getInstance();
        voteTracker = VoteTracker.getInstance();
        roleGuideManager = RoleGuideManager.getInstance();
        emotionManager = EmotionManager.getInstance();
        reconnectionManager = ReconnectionManager.getInstance();
        eventModeManager = EventModeManager.getInstance();

        AppendText("[System] 신규 기능 매니저 초기화 완료\n");
        AppendText("[System] - 쪽지 시스템\n");
        AppendText("[System] - 로비 시스템\n");
        AppendText("[System] - 통계 및 업적\n");
        AppendText("[System] - 실시간 투표 추적\n");
        AppendText("[System] - 역할 가이드\n");
        AppendText("[System] - 감정 표현\n");
        AppendText("[System] - 재접속 지원\n");
        AppendText("[System] - 이벤트 모드\n");
    }

    /**
     * 프레임 기본 설정 초기화
     */
    private void initializeFrame() {
        setTitle("Mafia Game Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 600, 500);
    }

    /**
     * 테마 색상 및 배경 패널 초기화
     */
    private void initializeTheme() {
        // 커스텀 배경 패널 설정
        contentPane = new BackgroundPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentPane.setLayout(new java.awt.BorderLayout());
        setContentPane(contentPane);
    }

    /**
     * 헤더 패널 생성
     * 서버 제목과 상태 표시
     */
    private void createHeaderPanel() {
        // 테마 색상 정의
        java.awt.Color textColor = new java.awt.Color(240, 240, 240);

        // 헤더 패널 생성
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new java.awt.Color(25, 25, 25, 220));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        headerPanel.setLayout(new java.awt.BorderLayout());

        // 제목 레이블
        JLabel titleLabel = new JLabel("마피아 게임 서버");
        titleLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
        titleLabel.setForeground(textColor);

        // 서버 아이콘 로드 시도
        loadServerIcon(titleLabel);

        headerPanel.add(titleLabel, java.awt.BorderLayout.WEST);

        // 상태 레이블
        statusLabel = new JLabel("● Offline");
        statusLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        statusLabel.setForeground(new java.awt.Color(255, 0, 0));
        headerPanel.add(statusLabel, java.awt.BorderLayout.EAST);

        contentPane.add(headerPanel, java.awt.BorderLayout.NORTH);
    }

    /**
     * 서버 아이콘 로드
     *
     * @param titleLabel 아이콘을 표시할 레이블
     */
    private void loadServerIcon(JLabel titleLabel) {
        try {
            java.net.URL iconURL = getClass().getResource("/info/ServerImg.png");
            if (iconURL != null) {
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(iconURL);
                java.awt.Image img = icon.getImage();
                java.awt.Image newImg = img.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
                titleLabel.setIcon(new javax.swing.ImageIcon(newImg));
                titleLabel.setIconTextGap(15);
            }
        } catch (Exception e) {
            System.out.println("Icon load failed: " + e.getMessage());
        }
    }

    /**
     * 중앙 패널 생성 (로그 영역)
     */
    private void createCenterPanel() {
        // 테마 색상
        java.awt.Color panelColor = new java.awt.Color(30, 30, 30, 220);
        java.awt.Color textColor = new java.awt.Color(240, 240, 240);
        java.awt.Color accentColor = new java.awt.Color(192, 57, 43);

        // 중앙 패널
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new java.awt.BorderLayout());
        centerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        centerPanel.setOpaque(false);

        // 로그 텍스트 영역
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
        textArea.setBackground(panelColor);
        textArea.setForeground(textColor);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 스크롤 패널
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

        // 로그 제목
        JLabel logLabel = new JLabel("Server Logs");
        logLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        logLabel.setForeground(textColor);
        logLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        centerPanel.add(logLabel, java.awt.BorderLayout.NORTH);

        contentPane.add(centerPanel, java.awt.BorderLayout.CENTER);
    }

    /**
     * 컨트롤 패널 생성 (포트 설정 및 게임 시작)
     */
    private void createControlPanel() {
        // 테마 색상
        java.awt.Color panelColor = new java.awt.Color(30, 30, 30, 220);
        java.awt.Color textColor = new java.awt.Color(240, 240, 240);
        java.awt.Color accentColor = new java.awt.Color(192, 57, 43);

        // 컨트롤 패널
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new java.awt.GridLayout(2, 1, 10, 10));
        controlPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        controlPanel.setOpaque(false);

        // 포트 설정 패널
        JPanel portPanel = createPortPanel(panelColor, textColor, accentColor);
        controlPanel.add(portPanel);

        // 게임 시작 버튼
        btnGameStart = createGameStartButton(textColor);
        controlPanel.add(btnGameStart);

        contentPane.add(controlPanel, java.awt.BorderLayout.SOUTH);
    }

    /**
     * 포트 설정 패널 생성
     *
     * @param panelColor  패널 배경색
     * @param textColor   텍스트 색상
     * @param accentColor 강조 색상
     * @return 생성된 패널
     */
    private JPanel createPortPanel(java.awt.Color panelColor, java.awt.Color textColor, java.awt.Color accentColor) {
        JPanel portPanel = new JPanel();
        portPanel.setLayout(new java.awt.BorderLayout(10, 0));
        portPanel.setOpaque(false);

        // 포트 레이블
        JLabel lblPort = new JLabel("Port Number:");
        lblPort.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        lblPort.setForeground(textColor);
        portPanel.add(lblPort, java.awt.BorderLayout.WEST);

        // 포트 입력 필드
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

        // 서버 시작 버튼
        JButton btnServerStart = createServerStartButton(textColor, accentColor);
        portPanel.add(btnServerStart, java.awt.BorderLayout.EAST);
        btnServerStart.setPreferredSize(new java.awt.Dimension(150, 40));

        return portPanel;
    }

    /**
     * 서버 시작 버튼 생성
     *
     * @param textColor   텍스트 색상
     * @param accentColor 버튼 배경색
     * @return 생성된 버튼
     */
    private JButton createServerStartButton(java.awt.Color textColor, java.awt.Color accentColor) {
        JButton btnServerStart = new JButton("Start Server");
        styleButton(btnServerStart, accentColor, textColor);

        btnServerStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startServer(btnServerStart);
            }
        });

        return btnServerStart;
    }

    /**
     * 서버 시작 로직
     *
     * @param btnServerStart 서버 시작 버튼
     */
    private void startServer(JButton btnServerStart) {
        try {
            // 포트 번호로 서버 소켓 생성
            socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
        } catch (NumberFormatException | IOException e1) {
            e1.printStackTrace();
            AppendText("[Error] Port binding failed.");
            return;
        }

        // UI 업데이트
        AppendText("Mafia Game Server Running...");
        btnServerStart.setText("Server Running");
        btnServerStart.setEnabled(false);
        btnServerStart.setBackground(new java.awt.Color(39, 174, 96));
        txtPortNumber.setEnabled(false);
        btnGameStart.setEnabled(true);

        // 서버 상태를 Online으로 변경
        statusLabel.setText("● Online");
        statusLabel.setForeground(new java.awt.Color(0, 255, 0));

        // 클라이언트 수락 스레드 시작
        AcceptServer accept_server = new AcceptServer();
        accept_server.start();
    }

    /**
     * 게임 시작 버튼 생성
     *
     * @param textColor 텍스트 색상
     * @return 생성된 버튼
     */
    private JButton createGameStartButton(java.awt.Color textColor) {
        btnGameStart = new JButton("Start Game (Need 4+ players)");
        styleButton(btnGameStart, new java.awt.Color(149, 165, 166), textColor);
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

        return btnGameStart;
    }

    // ========================================
    // UI 헬퍼 클래스
    // ========================================

    /**
     * 배경 이미지를 표시하는 커스텀 패널
     */
    class BackgroundPanel extends JPanel {
        /**
         * 배경 이미지
         */
        private java.awt.Image backgroundImage;

        /**
         * 배경 패널 생성자
         * 배경 이미지를 로드합니다.
         */
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

        /**
         * 패널 그리기
         * 배경 이미지를 패널 크기에 맞게 그립니다.
         */
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

    /**
     * 버튼 스타일 적용
     *
     * @param button 스타일을 적용할 버튼
     * @param bg     배경색
     * @param fg     전경색 (텍스트 색상)
     */
    private void styleButton(JButton button, java.awt.Color bg, java.awt.Color fg) {
        button.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
    }

    // ========================================
    // 게임 로직 - 게임 시작 및 역할 배정
    // ========================================

    /**
     * 게임 시작 메소드
     * 역할을 배정하고 첫 번째 밤 페이즈를 시작합니다.
     */
    private void startGame() {
        // 중복 시작 방지
        if (gameStarted) {
            AppendText("게임이 이미 시작되었습니다!");
            return;
        }

        // 게임 상태 초기화
        gameStarted = true;
        dayCount = 0;
        btnGameStart.setEnabled(false);

        AppendText("===== 게임 시작! =====");
        AppendText("참가자 수: " + UserVec.size());

        // 이벤트 모드 확인 및 적용
        checkAndApplyEventMode();

        // 역할 배정
        assignRoles();

        // 모든 플레이어를 살아있는 상태로 초기화
        initializePlayerStatus();

        // 클라이언트에게 게임 시작 알림
        WriteAll("SYSTEM: ===== 마피아 게임이 시작되었습니다! =====\n");
        WriteAll("SYSTEM: 참가자 수: " + UserVec.size() + "명\n");

        // 게임 시작 사운드 재생
        playSound("/GameSound/game_start.wav");

        // 2초 후 밤 페이즈 시작
        scheduleNightPhaseStart();
    }

    /**
     * 이벤트 모드 확인 및 적용
     * 
     * 현재 구현: 크리스마스 이벤트 (12월)
     * 
     * 확장 가능한 구조:
     * - 할로윈 이벤트 (10월): 뱀파이어, 늑대인간 등 특수 역할 추가
     * - 설날 이벤트 (1-2월): 점쟁이, 조상님 등 한국 전통 테마
     * - 여름 이벤트 (7-8월): 라이프가드, 상어 등 해변 테마
     * 
     * 새 이벤트 추가 방법:
     * 1. EventModeManager에 이벤트 등록
     * 2. 이 메소드에 월별 조건 추가
     * 3. 이벤트별 특수 로직 구현 (giveSantaGift 참고)
     */
    private void checkAndApplyEventMode() {
        java.time.LocalDate now = java.time.LocalDate.now();
        int currentMonth = now.getMonthValue();
        
        // 크리스마스 이벤트 (12월)
        if (currentMonth == 12) {
            activateChristmasEvent();
        }
        
        // TODO: 다른 이벤트 추가 예시
        // else if (currentMonth == 10) {
        //     activateHalloweenEvent();
        // }
        // else if (currentMonth >= 1 && currentMonth <= 2) {
        //     activateLunarNewYearEvent();
        // }
        // else if (currentMonth >= 7 && currentMonth <= 8) {
        //     activateSummerEvent();
        // }
    }
    
    /**
     * 크리스마스 이벤트 활성화
     * 산타가 랜덤 플레이어에게 2표 스킬을 선물합니다.
     */
    private void activateChristmasEvent() {
        christmasEventActive = true;
        AppendText("크리스마스 이벤트가 활성화되었습니다!");
        
        WriteAll("SYSTEM: \n");
        WriteAll("SYSTEM: 🎄🎅✨ 메리 크리스마스! ✨🎅🎄\n");
        WriteAll("SYSTEM: \n");
        WriteAll("SYSTEM: 곧 크리스마스입니다, 여러분!\n");
        WriteAll("SYSTEM: 산타가 특별한 선물을 가져왔습니다...\n");
        WriteAll("SYSTEM: \n");

        // 3초 후 산타의 선물 발표
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                giveSantaGift();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 산타의 선물 - 랜덤 플레이어에게 2표 스킬 부여
     */
    private void giveSantaGift() {
        if (!christmasEventActive || UserVec.isEmpty()) {
            return;
        }

        // 랜덤 플레이어 선택
        Random rand = new Random();
        UserService luckyPlayer = UserVec.get(rand.nextInt(UserVec.size()));
        santaGiftReceiver = luckyPlayer.UserName;

        AppendText("🎁 산타가 " + santaGiftReceiver + "님에게 선물을 주었습니다! (2표 스킬)");

        // 모든 플레이어에게 알림
        WriteAll("SYSTEM: \n");
        WriteAll("SYSTEM: 🎅 호호호! 산타입니다! 🎅\n");
        WriteAll("SYSTEM: 🎁 [" + santaGiftReceiver + "]님에게 특별한 선물을 드립니다!\n");
        WriteAll("SYSTEM: \n");
        WriteAll("SYSTEM: 💫 선물 내용: 투표 시 2표로 인정됩니다! 💫\n");
        WriteAll("SYSTEM: \n");

        // 선물받은 플레이어에게 특별 메시지
        luckyPlayer.WriteOne("SYSTEM: \n");
        luckyPlayer.WriteOne("SYSTEM: 🎄🎁 축하합니다! 🎁🎄\n");
        luckyPlayer.WriteOne("SYSTEM: 산타가 당신을 선택했습니다!\n");
        luckyPlayer.WriteOne("SYSTEM: 당신의 투표는 2표로 카운트됩니다!\n");
        luckyPlayer.WriteOne("SYSTEM: 이 선물을 현명하게 사용하세요...\n");
        luckyPlayer.WriteOne("SYSTEM: \n");
    }

    /**
     * 모든 플레이어의 생존 상태 초기화
     */
    private void initializePlayerStatus() {
        for (UserService user : UserVec) {
            aliveStatus.put(user.UserName, true);
        }
    }

    /**
     * 밤 페이즈 시작 예약
     */
    private void scheduleNightPhaseStart() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                startNightPhase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 역할 배정 메소드
     * 플레이어 수에 따라 적절한 역할 구성을 생성하고 배정합니다.
     */
    private void assignRoles() {
        int playerCount = UserVec.size();

        // 플레이어 수 제한 확인
        if (playerCount > 8) {
            handleTooManyPlayers();
            return;
        }

        // 역할 리스트 생성
        List<String> roles = createRoleList(playerCount);

        // 역할 섞기
        Collections.shuffle(roles);

        // 역할 배정 및 전송
        distributeRoles(roles);
    }

    /**
     * 플레이어 수 초과 처리
     */
    private void handleTooManyPlayers() {
        AppendText("최대 8명까지만 게임 가능합니다!");
        WriteAll("SYSTEM: 최대 8명까지만 게임 가능합니다.\n");
        gameStarted = false;
        btnGameStart.setEnabled(true);
    }

    /**
     * 플레이어 수에 따른 역할 리스트 생성
     *
     * @param playerCount 플레이어 수
     * @return 역할 리스트
     */
    private List<String> createRoleList(int playerCount) {
        List<String> roles = new ArrayList<>();

        switch (playerCount) {
            case 4:
                roles = createRolesFor4Players();
                break;
            case 5:
                roles = createRolesFor5Players();
                break;
            case 6:
                roles = createRolesFor6Players();
                break;
            case 7:
                roles = createRolesFor7Players();
                break;
            case 8:
                roles = createRolesFor8Players();
                break;
        }

        return roles;
    }

    /**
     * 4명용 역할 구성
     * 마피아1, 의사1, 경찰1, 시민1
     *
     * @return 역할 리스트
     */
    private List<String> createRolesFor4Players() {
        List<String> roles = new ArrayList<>();
        roles.add("MAFIA");
        roles.add("DOCTOR");
        roles.add("POLICE");
        roles.add("CITIZEN");
        return roles;
    }

    /**
     * 5명용 역할 구성
     * 마피아2, 의사1, 경찰1, 정치인 또는 군인 1 (랜덤)
     *
     * @return 역할 리스트
     */
    private List<String> createRolesFor5Players() {
        List<String> roles = new ArrayList<>();
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

        return roles;
    }

    /**
     * 6명용 역할 구성
     * 마피아1, 보조1(스파이 또는 마담), 의사1, 경찰1, 특�� 2명
     *
     * @return 역할 리스트
     */
    private List<String> createRolesFor6Players() {
        List<String> roles = new ArrayList<>();
        roles.add("MAFIA");

        // 마피아 보조 직업 중 하나 랜덤 선택
        addRandomMafiaSupporter(roles);

        roles.add("DOCTOR");
        roles.add("POLICE");
        roles.add("POLITICIAN");
        roles.add("SOLDIER");

        return roles;
    }

    /**
     * 7명용 역할 구성
     * 마피아1, 보조1(스파이 또는 마담), 의사1, 경찰1, 특직 3명
     *
     * @return 역할 리스트
     */
    private List<String> createRolesFor7Players() {
        List<String> roles = new ArrayList<>();
        roles.add("MAFIA");

        // 마피아 보조 직업 중 하나 랜덤 선택
        addRandomMafiaSupporter(roles);

        roles.add("DOCTOR");
        roles.add("POLICE");
        roles.add("POLITICIAN");
        roles.add("SOLDIER");
        roles.add("SHAMAN");

        return roles;
    }

    /**
     * 8명용 역할 구성
     * 마피아2, 보조1(스파이 또는 마담), 의사1, 경찰1, 특직 3명
     *
     * @return 역할 리스트
     */
    private List<String> createRolesFor8Players() {
        List<String> roles = new ArrayList<>();
        roles.add("MAFIA");
        roles.add("MAFIA");

        // 마피아 보조 직업 중 하나 랜덤 선택
        addRandomMafiaSupporter(roles);

        roles.add("DOCTOR");
        roles.add("POLICE");

        // 특수 직업 7개 중 3개 랜덤 선택
        addRandomSpecialRoles(roles, 3);

        return roles;
    }

    /**
     * 마피아 보조 직업 랜덤 추가 (스파이 또는 마담)
     *
     * @param roles 역할 리스트
     */
    private void addRandomMafiaSupporter(List<String> roles) {
        if (Math.random() < 0.5) {
            roles.add("SPY");
        } else {
            roles.add("MADAME");
        }
    }

    /**
     * 특수 직업 랜덤 추가
     *
     * @param roles 역할 리스트
     * @param count 추가할 개수
     */
    private void addRandomSpecialRoles(List<String> roles, int count) {
        List<String> specialRoles = new ArrayList<>();
        specialRoles.add("POLITICIAN");
        specialRoles.add("REPORTER");
        specialRoles.add("SOLDIER");
        specialRoles.add("SHAMAN");
        specialRoles.add("GHOUL");
        specialRoles.add("GANGSTER");
        specialRoles.add("PRIEST");

        Collections.shuffle(specialRoles);

        for (int i = 0; i < count && i < specialRoles.size(); i++) {
            roles.add(specialRoles.get(i));
        }
    }

    /**
     * 역할 배정 및 전송
     *
     * @param roles 역할 리스트
     */
    private void distributeRoles(List<String> roles) {
        for (int i = 0; i < UserVec.size(); i++) {
            UserService user = UserVec.get(i);
            String role = roles.get(i);

            // 역할 설정
            user.setRole(role);

            // 역할별 초기화 처리
            initializeRoleSpecificData(user, role);

            // 역할 정보 전송
            sendRoleInfo(user, role);

            // 로그 출력
            AppendText(user.UserName + " -> " + role);
        }
    }

    /**
     * 역할별 특수 데이터 초기화
     *
     * @param user 사용자 서비스 객체
     * @param role 역할
     */
    private void initializeRoleSpecificData(UserService user, String role) {
        switch (role) {
            case "SOLDIER":
                soldierShield.put(user.UserName, true);
                break;
            case "MAFIA":
                mafiaName = user.UserName;
                break;
            case "MADAME":
                madameName = user.UserName;
                break;
            case "SPY":
                spyName = user.UserName;
                break;
            case "SHAMAN":
                shamanName = user.UserName;
                break;
            case "GHOUL":
                ghoulName = user.UserName;
                break;
        }
    }

    /**
     * 역할 정보 전송
     *
     * @param user 사용자 서비스 객체
     * @param role 역할
     */
    private void sendRoleInfo(UserService user, String role) {
        String roleMsg = getRoleDescription(role);
        user.WriteOne("ROLE:" + role + "\n");
        user.WriteOne("SYSTEM: " + roleMsg + "\n");
    }

    /**
     * 역할별 사운드 경로 반환
     *
     * @param role 역할
     * @return 사운드 파일 경로
     */
    private String getRoleSoundPath(String role) {
        switch (role) {
            case "MAFIA":
                return null;
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

    /**
     * 역할 설명 반환
     *
     * @param role 역할
     * @return 역할 설명 문자열
     */
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

    // ========================================
    // 게임 로직 - 페이즈 관리
    // ========================================

    /**
     * 밤 페이즈 시작
     * 플레이어들이 밤 행동을 선택하는 시간입니다.
     */
    private void startNightPhase() {
        // 날짜 및 밤 카운트 증가
        dayCount++;
        nightCount++;
        gamePhase = "NIGHT";

        // 상태 초기화
        resetNightPhaseStatus();

        // 로그 및 클라이언트 알림
        AppendText("===== " + dayCount + "일차 밤 =====");
        WriteAll("PHASE:NIGHT\n");
        playSound("/GameSound/night.wav");
        WriteAll("SYSTEM: ===== " + dayCount + "일차 밤이 되었습니다 =====\n");
        WriteAll("SYSTEM: 마피아는 제거할 대상을, 의사는 보호할 대상을, 경찰은 조사할 대상을 선택하세요.\n");

        // 살아있는 플레이어 목록 전송
        sendAlivePlayerList();

        // 마피아 팀 정보 공유 (마피아 상호 인식 및 접선 정보 유지)
        broadcastMafiaTeamInfo();

        // 30초 후 밤 행동 처리 및 낮 페이즈 전환
        scheduleNightPhaseEnd();
    }

    /**
     * 밤 페이즈 상태 초기화
     */
    private void resetNightPhaseStatus() {
        nightActions.clear();
        voteBanned.clear();
        reporterTarget = "";
        reporterTargetRole = "";
        policeUsedThisNight = false;
        gangsterUsedThisNight = false;
        shamanUsedThisNight = false;
        spyUsedThisNight = false;
        doctorUsedThisNight = false;
    }

    /**
     * 밤 페이즈 종료 예약
     */
    private void scheduleNightPhaseEnd() {
        new Thread(() -> {
            try {
                // 30초 대기
                Thread.sleep(30000);
                processNightActions();
                Thread.sleep(1000);
                startDayPhase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }



    /**
     * 밤 행동 처리
     * 마피아의 공격, 의사의 보호, 경찰의 조사 등을 처리합니다.
     */
    private void processNightActions() {
        // 행동 데이터 수집
        String mafiaTarget = nightActions.get("MAFIA");
        String doctorTarget = nightActions.get("DOCTOR");
        String policeTarget = nightActions.get("POLICE");
        String spyTarget = nightActions.get("SPY");
        String gangsterTarget = nightActions.get("GANGSTER");

        // 로그 출력
        logNightActions(mafiaTarget, doctorTarget, policeTarget, spyTarget, gangsterTarget);

        // 건달의 투표 금지 처리
        processGangsterAction(gangsterTarget);

        // 마피아의 공격 처리
        processMafiaAttack(mafiaTarget, doctorTarget);

        // 도굴꾼 능력 처리 (첫날 밤)
        processGhoulAbility();
    }

    /**
     * 밤 행동 로그 출력
     */
    private void logNightActions(String mafiaTarget, String doctorTarget, String policeTarget,
            String spyTarget, String gangsterTarget) {
        AppendText("=== 밤 행동 결과 ===");
        AppendText("마피아 타겟: " + (mafiaTarget != null ? mafiaTarget : "없음"));
        AppendText("의사 보호: " + (doctorTarget != null ? doctorTarget : "없음"));
        AppendText("경찰 조사: " + (policeTarget != null ? policeTarget : "없음"));
        AppendText("스파이 조사: " + (spyTarget != null ? spyTarget : "없음"));
        AppendText("건달 타겟: " + (gangsterTarget != null ? gangsterTarget : "없음"));
    }

    /**
     * 건달의 투표 금지 처리
     *
     * @param gangsterTarget 건달의 타겟
     */
    private void processGangsterAction(String gangsterTarget) {
        if (gangsterTarget != null) {
            voteBanned.put(gangsterTarget, true);
            AppendText(gangsterTarget + " 다음 투표 금지됨");
        }
    }

    /**
     * 마피아의 공격 처리
     *
     * @param mafiaTarget  마피아의 타겟
     * @param doctorTarget 의사가 보호한 타겟
     */
    private void processMafiaAttack(String mafiaTarget, String doctorTarget) {
        if (mafiaTarget == null) {
            return;
        }

        // 의사의 보호 확인
        boolean savedByDoctor = mafiaTarget.equals(doctorTarget);

        // 군인의 방어막 확인
        boolean savedBySoldier = checkSoldierDefense(mafiaTarget);

        if (savedByDoctor) {
            handleDoctorSave(mafiaTarget);
        } else if (savedBySoldier) {
            handleSoldierDefense(mafiaTarget);
        } else {
            handlePlayerDeath(mafiaTarget);
        }
    }

    /**
     * 군인의 방어막 확인
     *
     * @param target 대상 플레이어
     * @return 방어막으로 보호되었는지 여부
     */
    private boolean checkSoldierDefense(String target) {
        // 유혹당한 경우 확인
        boolean soldierSeduced = seduced.get(target) != null && seduced.get(target);

        if (soldierShield.containsKey(target) && soldierShield.get(target) && !soldierSeduced) {
            soldierShield.put(target, false);
            return true;
        } else if (soldierShield.containsKey(target) && soldierShield.get(target) && soldierSeduced) {
            soldierShield.put(target, false);
            AppendText(target + " 군인이지만 유혹당해 방어막 무효화");
        }

        return false;
    }

    /**
     * 의사의 구조 처리
     *
     * @param target 구조된 플레이어
     */
    private void handleDoctorSave(String target) {
        WriteAll("SYSTEM: 의사가 누군가를 구했습니다!\n");
        AppendText(target + " 의사가 구함");
    }

    /**
     * 군인의 방어 처리
     *
     * @param target 방어한 플레이어
     */
    private void handleSoldierDefense(String target) {
        WriteAll("SYSTEM: [" + target + "] 군인이 마피아의 공격을 막아냈습니다!\n");
        AppendText(target + " 군인이 방어막으로 생존");
    }

    /**
     * 플레이어 사망 처리
     *
     * @param target 사망한 플레이어
     */
    private void handlePlayerDeath(String target) {
        aliveStatus.put(target, false);
        WriteAll("SYSTEM: [" + target + "]님이 마피아에게 제거되었습니다.\n");
        playSound("/GameSound/Mafia_team/Mafia.wav");
        AppendText(target + " 사망");

        // 사망 알림 전송
        notifyPlayerDeath(target);

        // 도굴꾼 능력 처리 (아직 변신하지 않은 경우 언제든 가능)
        if (!ghoulTransformed && !ghoulName.isEmpty()) {
            processGhoulTransformation(target);
        }
    }

    /**
     * 플레이어에게 사망 알림 전송
     *
     * @param target 사망한 플레이어
     */
    private void notifyPlayerDeath(String target) {
        for (UserService user : UserVec) {
            if (user.UserName.equals(target)) {
                user.WriteOne("DEAD:true\n");
            }
        }
    }

    /**
     * 도굴꾼 변신 처리
     *
     * @param victim 희생자
     */
    private void processGhoulTransformation(String victim) {
        for (UserService user : UserVec) {
            if (user.UserName.equals(victim)) {
                String victimRole = user.role;

                for (UserService ghoulUser : UserVec) {
                    if (ghoulUser.UserName.equals(ghoulName)) {
                        ghoulUser.setRole(victimRole);
                        ghoulVictim = victim;

                        // 클라이언트에 역할 변경 알림
                        ghoulUser.WriteOne("ROLE:" + victimRole + "\n");
                        ghoulUser.WriteOne("SYSTEM: 첫날 밤 사망자 [" + victim + "]의 직업 [" + victimRole + "]을 얻었습니다!\n");
                        ghoulUser.WriteOne("SYSTEM: " + getRoleDescription(victimRole) + "\n");

                        AppendText("도굴꾼 " + ghoulName + "이 " + victimRole + "로 변신 (희생자: " + victim + ")");
                        ghoulTransformed = true;
                        break;
                    }
                }
            }
        }
    }

    /**
     * 도굴꾼 능력 처리 (첫날 밤 사망자가 없는 경우)
     */
    private void processGhoulAbility() {
        // 첫날 밤 사망자가 없어도 도굴꾼은 직업을 얻을 때까지 유지됨
        // 따라서 강제로 시민으로 만드는 로직 제거
    }

    /**
     * 낮 페이즈 시작
     * 플레이어들이 토론하고 의견을 나누는 시간입니다.
     */
    private void startDayPhase() {
        gamePhase = "DAY";

        // 게임 종료 체크
        if (checkGameEnd()) {
            return;
        }

        // 로그 및 클라이언트 알림
        AppendText("===== " + dayCount + "일차 낮 =====");
        WriteAll("PHASE:DAY\n");
        playSound("/GameSound/morning.wav");
        WriteAll("SYSTEM: ===== " + dayCount + "일차 낮이 되었습니다 =====\n");

        // 유혹 초기화
        seduced.clear();

        // 성직자의 부활 처리
        processPriestRevival();

        // 기자의 특종 발표
        processReporterScoop();

        // 안내 메시지
        WriteAll("SYSTEM: 자유롭게 대화하고 의심되는 사람을 찾으세요.\n");
        WriteAll("SYSTEM: 30초 후 투표가 시작됩니다.\n");

        sendAlivePlayerList();

        // 30초 후 투표 페이즈 시작
        scheduleDayPhaseEnd();
    }

    /**
     * 성직자의 부활 처리
     */
    private void processPriestRevival() {
        if (priestTarget.isEmpty()) {
            return;
        }

        aliveStatus.put(priestTarget, true);
        WriteAll("SYSTEM: [" + priestTarget + "]님이 성직자에 의해 부활했습니다!\n");
        AppendText("성직자가 " + priestTarget + " 부활 성공");

        // 부활한 플레이어에게 알림
        for (UserService targetUser : UserVec) {
            if (targetUser.UserName.equals(priestTarget)) {
                // 도굴꾼의 희생자인 경우 시민으로 변경 (도굴꾼이 실제로 직업을 가져간 경우만)
                if (priestTarget.equals(ghoulVictim) && ghoulTransformed) {
                    reviveAsGhoulVictim(targetUser);
                } else {
                    reviveAsOriginalRole(targetUser);
                }
                break;
            }
        }

        priestTarget = "";
    }

    /**
     * 도굴꾼 희생자로 부활
     *
     * @param targetUser 대상 사용자
     */
    private void reviveAsGhoulVictim(UserService targetUser) {
        targetUser.setRole("CITIZEN");
        targetUser.WriteOne("ROLE:CITIZEN\n");
        targetUser.WriteOne("SYSTEM: 성직자에 의해 부활했습니다!\n");
        targetUser.WriteOne("SYSTEM: 당신의 직업은 도굴꾼에게 빼앗겨 [시민]이 되었습니다.\n");
        targetUser.WriteOne("SYSTEM: " + getRoleDescription("CITIZEN") + "\n");
        targetUser.WriteOne("DEAD:false\n");
        AppendText(priestTarget + " 부활 (도굴 희생자 -> 시민)");
    }

    /**
     * 원래 역할로 부활
     *
     * @param targetUser 대상 사용자
     */
    private void reviveAsOriginalRole(UserService targetUser) {
        targetUser.WriteOne("SYSTEM: 성직자에 의해 부활했습니다!\n");
        targetUser.WriteOne("DEAD:false\n");
    }

    /**
     * 기자의 특종 발표
     */
    private void processReporterScoop() {
        if (!reporterTarget.isEmpty() && !reporterTargetRole.isEmpty()) {
            WriteAll("SYSTEM: 특종입니다. [" + reporterTarget + "]의 직업은 [" + reporterTargetRole + "]입니다!\n");
            AppendText("기자 특종: " + reporterTarget + " -> " + reporterTargetRole);
        }
    }

    /**
     * 낮 페이즈 종료 예약
     */
    private void scheduleDayPhaseEnd() {
        new Thread(() -> {
            try {
                Thread.sleep(30000);
                startVotePhase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 투표 페이즈 시작
     * 플레이어들이 제거할 대상을 투표하는 시간입니다.
     */
    private void startVotePhase() {
        gamePhase = "VOTE";
        voteCount.clear();
        hasVotedThisRound.clear();

        // 살아있는 모든 플레이어를 투표 대상으로 초기화
        initializeVoteCounts();

        // VoteTracker 시작 - 실시간 투표 추적
        Set<String> aliveVoters = new HashSet<>();
        Set<String> aliveCandidates = new HashSet<>();
        for (Map.Entry<String, Boolean> entry : aliveStatus.entrySet()) {
            if (entry.getValue()) {
                aliveVoters.add(entry.getKey());
                aliveCandidates.add(entry.getKey());
            }
        }
        voteTracker.startVoting(aliveVoters, aliveCandidates, 20,
                               mafia.game.features.VoteTracker.VoteMode.PUBLIC);

        // 로그 및 클라이언트 알림
        AppendText("===== 투표 시작 =====");
        WriteAll("PHASE:VOTE\n");
        playSound("/GameSound/vote.wav");
        WriteAll("SYSTEM: ===== 투표 시작 =====\n");
        WriteAll("SYSTEM: 제거할 플레이어를 투표하세요! (20초)\n");

        sendAlivePlayerList();

        // 투표 진행 상황 업데이트 스레드 시작
        startVoteProgressUpdates();

        // 20초 후 투표 결과 처리
        scheduleVotePhaseEnd();
    }

    /**
     * 투표 카운트 초기화
     */
    private void initializeVoteCounts() {
        for (String player : aliveStatus.keySet()) {
            if (aliveStatus.get(player)) {
                voteCount.put(player, 0);
            }
        }
    }

    /**
     * 투표 진행 상황 실시간 업데이트
     */
    private void startVoteProgressUpdates() {
        new Thread(() -> {
            try {
                while (voteTracker.isActive() && gamePhase.equals("VOTE")) {
                    Thread.sleep(5000); // 5초마다 업데이트

                    // 투표 현황 브로드캐스트
                    String stats = voteTracker.getStatistics();

                    // 투표 바 차트 전송
                    List<String> barChart = voteTracker.getVoteBarChart(20);
                    StringBuilder chartMsg = new StringBuilder();
                    chartMsg.append(stats).append("\n");
                    chartMsg.append("실시간 투표 현황:\n");
                    for (String bar : barChart) {
                        chartMsg.append("  ").append(bar).append("\n");
                    }
                    WriteAll("SYSTEM:" + chartMsg.toString());
                }
            } catch (InterruptedException e) {
                // 투표 종료
            }
        }).start();
    }

    /**
     * 투표 페이즈 종료 예약
     */
    private void scheduleVotePhaseEnd() {
        new Thread(() -> {
            try {
                // 20초 대기
                Thread.sleep(20000);
                processVoteResult();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }



    /**
     * 투표 결과 처리
     * 가장 많은 표를 받은 플레이어를 제거합니다.
     */
    private void processVoteResult() {
        AppendText("=== 투표 결과 ===");

        // VoteTracker 종료 및 최종 결과 브로드캐스트
        mafia.game.features.VoteTracker.VoteResult trackerResult = voteTracker.endVoting();

        // 최종 투표 현황 표시
        List<String> finalChart = voteTracker.getVoteBarChart(30);
        StringBuilder finalMsg = new StringBuilder();
        finalMsg.append("\n" + "=".repeat(50) + "\n");
        finalMsg.append("📊 최종 투표 결과\n");
        finalMsg.append("=".repeat(50) + "\n");
        for (String bar : finalChart) {
            finalMsg.append(bar).append("\n");
        }
        finalMsg.append("=".repeat(50) + "\n");
        WriteAll("SYSTEM:" + finalMsg.toString());

        // 최다 득표자 찾기
        VoteResult result = findMaxVotedPlayer();

        // 투표 결과 출력
        displayVoteResults();

        // 동점 또는 투표 없음 처리
        if (result.isTie || result.maxVotes == 0) {
            handleNoElimination();
            // 게임 종료 체크
            if (checkGameEnd()) {
                return;
            }
            // 다음 밤으로
            scheduleNextNightPhase();
        } else {
            // 최후의 반론 페이즈 시작
            startFinalDefensePhase(result.maxVotedPlayer);
        }
    }

    /**
     * 투표 결과 데이터 클래스
     */
    private class VoteResult {
        String maxVotedPlayer;
        int maxVotes;
        boolean isTie;

        VoteResult(String player, int votes, boolean tie) {
            this.maxVotedPlayer = player;
            this.maxVotes = votes;
            this.isTie = tie;
        }
    }

    /**
     * 최다 득표자 찾기
     *
     * @return 투표 결과
     */
    private VoteResult findMaxVotedPlayer() {
        String maxVotedPlayer = null;
        int maxVotes = 0;
        boolean tie = false;

        for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                maxVotedPlayer = entry.getKey();
                tie = false;
            } else if (entry.getValue() == maxVotes && maxVotes > 0) {
                tie = true;
            }
        }

        return new VoteResult(maxVotedPlayer, maxVotes, tie);
    }

    /**
     * 투표 결과 출력
     */
    private void displayVoteResults() {
        for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
            AppendText(entry.getKey() + ": " + entry.getValue() + "표");
            WriteAll("SYSTEM: [" + entry.getKey() + "] " + entry.getValue() + "표\n");
        }
    }

    /**
     * 제거 없음 처리
     */
    private void handleNoElimination() {
        WriteAll("SYSTEM: 동점 또는 투표 없음! 아무도 제거되지 않았습니다.\n");
        AppendText("투표 무효");
    }

    /**
     * 플레이어 제거 처리
     *
     * @param eliminatedPlayer 제거될 플레이어
     */
    private void handleElimination(String eliminatedPlayer) {
        // 역할 확인
        String eliminatedRole = getPlayerRole(eliminatedPlayer);
        boolean isPolitician = eliminatedRole.equals("POLITICIAN");
        boolean politicianSeduced = seduced.get(eliminatedPlayer) != null && seduced.get(eliminatedPlayer);

        if (isPolitician && !politicianSeduced) {
            handlePoliticianSurvival(eliminatedPlayer);
        } else if (isPolitician && politicianSeduced) {
            handlePoliticianElimination(eliminatedPlayer);
        } else {
            handleNormalElimination(eliminatedPlayer, eliminatedRole);
        }
    }

    /**
     * 플레이어 역할 조회
     *
     * @param playerName 플레이어 이름
     * @return 역할
     */
    private String getPlayerRole(String playerName) {
        for (UserService user : UserVec) {
            if (user.UserName.equals(playerName)) {
                return user.role;
            }
        }
        return "";
    }

    /**
     * 정치인 생존 처리
     *
     * @param playerName 플레이어 이름
     */
    private void handlePoliticianSurvival(String playerName) {
        WriteAll("SYSTEM: [" + playerName + "]님은 정치인이므로 투표로 제거되지 않습니다!\n");
        AppendText(playerName + " 투표 1위 (정치인 - 생존)");
    }

    /**
     * 정치인 제거 처리 (유혹당한 경우)
     *
     * @param playerName 플레이어 이름
     */
    private void handlePoliticianElimination(String playerName) {
        aliveStatus.put(playerName, false);

        for (UserService user : UserVec) {
            if (user.UserName.equals(playerName)) {
                user.WriteOne("DEAD:true\n");
            }
        }

        WriteAll("SYSTEM: [" + playerName + "]님은 정치인이지만 마담에게 유혹당해 투표로 제거되었습니다!\n");
        AppendText(playerName + " 제거됨 (정치인 - 유혹당함)");
    }

    /**
     * 일반 플레이어 제거 처리
     *
     * @param playerName 플레이어 이름
     * @param role       역할
     */
    private void handleNormalElimination(String playerName, String role) {
        aliveStatus.put(playerName, false);

        // 사망 알림
        for (UserService user : UserVec) {
            if (user.UserName.equals(playerName)) {
                user.WriteOne("DEAD:true\n");
            }
        }

        WriteAll("SYSTEM: [" + playerName + "]님이 투표로 제거되었습니다.\n");
        AppendText(playerName + " 제거됨 (역할: " + role + ")");
    }

    // ========================================
    // 최후의 반론 시스템
    // ========================================

    /**
     * 최후의 반론 페이즈 시작
     *
     * @param targetPlayer 투표로 선택된 플레이어
     */
    private void startFinalDefensePhase(String targetPlayer) {
        gamePhase = "FINAL_DEFENSE";
        finalDefensePlayer = targetPlayer;

        AppendText("===== 최후의 반론 시작 =====");
        WriteAll("PHASE:FINAL_DEFENSE:" + targetPlayer + "\n");
        WriteAll("SYSTEM: ===== 최후의 반론 시작 =====\n");
        WriteAll("SYSTEM: [" + targetPlayer + "]님의 최후의 반론 시간입니다. (15초)\n");

        // 15초 후 찬반 투표 시작
        new Thread(() -> {
            try {
                Thread.sleep(15000);
                startAgreeDisagreeVote();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 찬반 투표 시작
     */
    private void startAgreeDisagreeVote() {
        gamePhase = "AGREE_DISAGREE";
        agreeVotes = 0;
        disagreeVotes = 0;
        hasVotedFinalDecision.clear();

        AppendText("===== 찬반 투표 시작 =====");
        WriteAll("PHASE:AGREE_DISAGREE\n");
        WriteAll("SYSTEM: ===== 찬반 투표 시작 =====\n");
        WriteAll("SYSTEM: [" + finalDefensePlayer + "]님의 처형에 찬성/반대 투표를 진행합니다. (5초)\n");
        WriteAll("SYSTEM: 아무것도 선택하지 않으면 자동으로 반대로 처리됩니다.\n");

        // 마담의 유혹을 받은 대상에게만 알림
        if (seduced.containsKey(finalDefensePlayer) && seduced.get(finalDefensePlayer)) {
            for (UserService user : UserVec) {
                if (user.UserName.equals(finalDefensePlayer)) {
                    user.WriteOne("SYSTEM: 마담의 유혹을 받았습니다. 말을 할 수 없습니다.\n");
                    break;
                }
            }
        }

        // 5초 후 찬반 투표 결과 처리
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                processFinalDecision();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 찬반 투표 결과 처리
     */
    private void processFinalDecision() {
        // 투표하지 않은 살아있는 플레이어는 자동으로 반대 (최후의 변론 대상자 제외)
        for (UserService user : UserVec) {
            if (aliveStatus.get(user.UserName) != null && aliveStatus.get(user.UserName)) {
                // 최후의 변론 대상자는 투표권이 없으므로 제외
                if (user.UserName.equals(finalDefensePlayer)) {
                    continue;
                }
                if (!hasVotedFinalDecision.contains(user.UserName)) {
                    disagreeVotes++;
                }
            }
        }

        AppendText("=== 찬반 투표 결과 ===");
        AppendText("찬성: " + agreeVotes + "표, 반대: " + disagreeVotes + "표");
        WriteAll("SYSTEM: 찬성 " + agreeVotes + "표, 반대 " + disagreeVotes + "표\n");

        // 찬성이 반대보다 많거나 동점이면 처형
        if (agreeVotes >= disagreeVotes) {
            WriteAll("SYSTEM: 찬성이 반대와 동점 이상입니다. [" + finalDefensePlayer + "]님이 처형됩니다.\n");
            AppendText(finalDefensePlayer + " 처형됨");
            handleElimination(finalDefensePlayer);
        } else {
            WriteAll("SYSTEM: 반대가 더 많습니다. [" + finalDefensePlayer + "]님이 살아남습니다.\n");
            AppendText(finalDefensePlayer + " 생존");
        }

        // 게임 종료 체크
        if (checkGameEnd()) {
            return;
        }

        // 다음 밤으로
        scheduleNextNightPhase();
    }



    /**
     * 다음 밤 페이즈 예약
     */
    private void scheduleNextNightPhase() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                startNightPhase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ========================================
    // 게임 로직 - 게임 종료 및 승리 조건
    // ========================================

    /**
     * 게임 종료 조건 체크
     *
     * @return 게임이 종료되었는지 여부
     */
    private boolean checkGameEnd() {
        int aliveCount = 0;
        int mafiaCount = 0;
        int citizenPower = 0;

        // 생존자 집계
        for (UserService user : UserVec) {
            if (aliveStatus.get(user.UserName)) {
                aliveCount++;

                if (isMafiaTeam(user)) {
                    mafiaCount++;
                } else {
                    citizenPower += getPoliticianVotePower(user);
                }
            }
        }

        AppendText("생존자: " + aliveCount + "명, 마피아: " + mafiaCount + "명, 시민팀 파워: " + citizenPower);

        // 승리 조건 체크
        if (mafiaCount == 0) {
            return handleCitizenVictory();
        } else if (mafiaCount >= citizenPower) {
            return handleMafiaVictory();
        }

        return false;
    }

    /**
     * 마피아 팀 여부 확인
     *
     * @param user 사용자
     * @return 마피아 팀 여부
     */
    private boolean isMafiaTeam(UserService user) {
        return user.role.equals("MAFIA") || user.role.equals("SPY")
                || (user.role.equals("MADAME") && madameContactedMafia);
    }

    /**
     * 정치인 투표 파워 계산
     *
     * @param user 사용자
     * @return 투표 파워
     */
    private int getPoliticianVotePower(UserService user) {
        return user.role.equals("POLITICIAN") ? 2 : 1;
    }

    /**
     * 시민 승리 처리
     *
     * @return true (게임 종료)
     */
    private boolean handleCitizenVictory() {
        WriteAll("PHASE:END\n");
        WriteAll("SYSTEM: ===== 게임 종료 =====\n");
        WriteAll("SYSTEM: 승리 팀: 시민 팀\n");
        stopSound();
        AppendText("===== 게임 종료: 시민 승리 =====");
        revealAllRoles();
        resetGameState();
        return true;
    }

    /**
     * 마피아 승리 처리
     *
     * @return true (게임 종료)
     */
    private boolean handleMafiaVictory() {
        WriteAll("PHASE:END\n");
        WriteAll("SYSTEM: ===== 게임 종료! 마피아 팀 승리! =====\n");
        WriteAll("SYSTEM: 마피아가 시민 팀과 같거나 많아졌습니다!\n");
        AppendText("===== 게임 종료: 마피아 승리 =====");
        revealAllRoles();
        resetGameState();
        return true;
    }

    /**
     * 게임 상태 리셋
     */
    private void resetGameState() {
        gameStarted = false;
        btnGameStart.setEnabled(true);
    }

    /**
     * 모든 역할 공개
     */
    private void revealAllRoles() {
        WriteAll("SYSTEM: ===== 역할 공개 =====\n");
        for (UserService user : UserVec) {
            WriteAll("SYSTEM: [" + user.UserName + "] - " + user.role + "\n");
        }
    }

    // ========================================
    // 사운드 관리
    // ========================================

    /**
     * 사운드 재생
     *
     * @param filePath 사운드 파일 경로
     */
    private void playSound(String filePath) {
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
                InputStream bufferedStream = new BufferedInputStream(soundStream);

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedStream);
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

    /**
     * 사운드 중지 (public 메소드)
     */
    private void stopSound() {
        stopCurrentSound();
    }

    // ========================================
    // 플레이어 목록 관리
    // ========================================

    /**
     * 살아있는 플레이어 목록 전송
     */
    private void sendAlivePlayerList() {
        StringBuilder playerList = new StringBuilder("PLAYERS:");

        for (String player : aliveStatus.keySet()) {
            if (aliveStatus.get(player)) {
                playerList.append(player).append(",");
            } else {
                playerList.append("[DEAD]").append(player).append(",");
            }
        }

        WriteAll(playerList.toString() + "\n");
    }

    /**
     * 접속 중인 모든 플레이어 목록 전송 (대기실용)
     */
    private void broadcastPlayerList() {
        StringBuilder playerList = new StringBuilder("PLAYERS:");

        for (UserService user : UserVec) {
            playerList.append(user.UserName).append(",");
        }

        WriteAll(playerList.toString() + "\n");
    }

    // ========================================
    // 네트워크 - 클라이언트 수락 스레드
    // ========================================

    /**
     * 클라이언트 연결을 수락하는 스레드
     */
    class AcceptServer extends Thread {
        /**
         * 스레드 실행
         */
        public void run() {
            while (true) {
                try {
                    AppendText("Waiting for players...");

                    // 클라이언트 연결 대기
                    client_socket = socket.accept();
                    AppendText("새로운 플레이어 from " + client_socket);

                    // 사용자 서비스 생성 및 추가
                    UserService new_user = new UserService(client_socket);
                    UserVec.add(new_user);

                    // 플레이어 목록 업데이트
                    broadcastPlayerList();
                    AppendText("플레이어 입장. 현재 플레이어 수: " + UserVec.size());

                    // 사용자 서비스 스레드 시작
                    new_user.start();

                    // 테스트 모드: 자동 게임 시작
                    if (TEST_MODE && UserVec.size() == AUTO_START_PLAYER_COUNT && !gameStarted) {
                        AppendText("[TEST MODE] " + AUTO_START_PLAYER_COUNT + "명 접속 완료! 자동으로 게임을 시작합니다.");
                        // 약간의 딜레이 후 게임 시작 (플레이어들이 연결을 완료할 시간)
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                startGame();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                } catch (IOException e) {
                    AppendText("accept 에러 발생");
                }
            }
        }
    }

    // ========================================
    // 유틸리티 메소드
    // ========================================

    /**
     * 로그 텍스트 추가
     *
     * @param str 로그 문자열
     */
    public void AppendText(String str) {
        textArea.append(str + "\n");
        textArea.setCaretPosition(textArea.getText().length());
    }

    /**
     * 모든 클라이언트에게 메시지 전송
     *
     * @param str 전송할 메시지
     */
    public void WriteAll(String str) {
        for (int i = 0; i < UserVec.size(); i++) {
            UserService user = UserVec.get(i);
            user.WriteOne(str);
        }
    }

    // ========================================
    // 내부 클래스 - UserService
    // ========================================

    /**
     * 개별 사용자와의 통신을 담당하는 서비스 클래스
     */
    class UserService extends Thread {
        // 네트워크 스트림
        private InputStream is;
        private OutputStream os;
        private DataInputStream dis;
        private DataOutputStream dos;

        // 소켓 및 사용자 정보
        private Socket client_socket;
        protected Vector<UserService> user_vc;
        protected String UserName = "";

        /**
         * 사용자 역할
         */
        String role = "";

        /**
         * UserService 생성자
         *
         * @param client_socket 클라이언트 소켓
         */
        public UserService(Socket client_socket) {
            this.client_socket = client_socket;
            this.user_vc = UserVec;

            try {
                // 입출력 스트림 초기화
                is = client_socket.getInputStream();
                dis = new DataInputStream(is);
                os = client_socket.getOutputStream();
                dos = new DataOutputStream(os);

                // 사용자 이름 수신
                String line1 = dis.readUTF();
                String[] msg = line1.split(" ");
                UserName = msg[1].trim();

                // 환영 메시지
                AppendText("새로운 플레이어: " + UserName);
                WriteOne("SYSTEM: 마피아 게임 서버에 오신 것을 환영합니다!\n");
                WriteOne("SYSTEM: [" + UserName + "]님 환영합니다.\n");

                // 입장 알림
                String br_msg = "SYSTEM: [" + UserName + "]님이 입장하였습니다.\n";
                WriteAll(br_msg);

            } catch (Exception e) {
                AppendText("UserService 생성 오류");
            }
        }

        /**
         * 역할 설정
         *
         * @param role 역할
         */
        public void setRole(String role) {
            this.role = role;
        }

        /**
         * 로그아웃 처리
         */
        public void logout() {
            user_vc.removeElement(this);
            String br_msg = "SYSTEM: [" + UserName + "]님이 퇴장하였습니다.\n";
            WriteAll(br_msg);
            broadcastPlayerList();
            AppendText("플레이어 퇴장: " + UserName + " (현재 " + user_vc.size() + "명)");
        }

        /**
         * 개별 메시지 전송
         *
         * @param msg 전송할 메시지
         */
        public void WriteOne(String msg) {
            try {
                dos.writeUTF(msg);
            } catch (IOException e) {
                AppendText("전송 오류: " + UserName);
                closeConnection();
                logout();
            }
        }

        /**
         * 연결 종료
         */
        private void closeConnection() {
            try {
                dos.close();
                dis.close();
                client_socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        /**
         * 스레드 실행 (메시지 수신 처리)
         */
        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF();
                    msg = msg.trim();
                    AppendText(msg);

                    // 메시지 타입별 처리
                    if (msg.startsWith("NIGHT_ACTION:")) {
                        handleNightAction(msg);
                    } else if (msg.startsWith("VOTE:")) {
                        handleVote(msg);
                    } else if (msg.startsWith("AGREE_DISAGREE:")) {
                        handleAgreeDisagreeVote(msg);
                    } else if (msg.startsWith("WHISPER:")) {
                        handleWhisper(msg);
                    } else if (msg.startsWith("EMOTION:")) {
                        handleEmotion(msg);
                    } else if (msg.startsWith("/guide")) {
                        handleGuideCommand(msg);
                    } else if (msg.startsWith("/stats")) {
                        handleStatsCommand(msg);
                    } else if (msg.contains("/exit")) {
                        logout();
                        return;
                    } else {
                        handleChatMessage(msg);
                    }

                } catch (IOException e) {
                    AppendText("연결 오류: " + UserName);
                    closeConnection();
                    logout();
                    break;
                }
            }
        }

        /**
         * 밤 행동 처리
         *
         * @param msg 메시지
         */
        private void handleNightAction(String msg) {
            // NIGHT_ACTION:ROLE:TARGET 형식
            String[] parts = msg.split(":");
            if (parts.length != 3) {
                return;
            }

            String actionRole = parts[1];
            String target = parts[2];

            // 죽은 사람 대상 능력 사용 제한 (영매, 성직자 제외)
            if (!actionRole.equals("SHAMAN") && !actionRole.equals("PRIEST")
                    && aliveStatus.get(target) != null && !aliveStatus.get(target)) {
                WriteOne("SYSTEM: 죽은 사람에게는 능력을 사용할 수 없습니다!\n");
                return;
            }

            // 유혹당한 경우 능력 사용 불가
            if (seduced.get(UserName) != null && seduced.get(UserName)) {
                WriteOne("SYSTEM: 마담에게 유혹당해 능력을 사용할 수 없습니다!\n");
                AppendText(UserName + " 유혹당해 능력 사용 불가");
                return;
            }

            // 행동 저장
            nightActions.put(actionRole, target);
            AppendText(UserName + "(" + role + ") -> " + target);

            // 역할별 특수 처리
            processRoleSpecificAction(actionRole, target);
        }

        /**
         * 역할별 특수 행동 처리
         *
         * @param actionRole 행동 역할
         * @param target     대상
         */
        private void processRoleSpecificAction(String actionRole, String target) {
            switch (actionRole) {
                case "MAFIA":
                    handleMafiaAction(target);
                    break;
                case "DOCTOR": // 추가: 의사 행동 처리
                    handleDoctorAction(target);
                    break;
                case "POLICE":
                    handlePoliceAction(target);
                    break;
                case "SPY":
                    handleSpyAction(target);
                    break;
                case "SHAMAN":
                    handleShamanAction(target);
                    break;
                case "REPORTER":
                    handleReporterAction(target);
                    break;
                case "GANGSTER":
                    handleGangsterAction(target);
                    break;
                case "PRIEST":
                    handlePriestAction(target);
                    break;
                // MADAME은 투표로만 능력 사용 (밤 행동 없음)
            }
        }

        /**
         * 마피아 행동 처리
         *
         * @param target 대상
         */
        private void handleMafiaAction(String target) {
            // 마담에게 유혹당한 경우
            if (seduced.get(UserName) != null && seduced.get(UserName)) {
                WriteOne("SYSTEM: 마담에게 유혹당해 능력을 사용할 수 없습니다!\n");
                return;
            }

            // 모든 마피아에게 선택 결과 알림
            for (UserService mafiaUser : UserVec) {
                if (mafiaUser.role.equals("MAFIA")) {
                    mafiaUser.WriteOne("SYSTEM: 마피아 팀이 [" + target + "]님을 타겟으로 선택했습니다.\n");
                }
            }
            WriteOne("SYSTEM: [" + target + "]님을 타겟으로 선택했습니다.\n");
        }

        /**
         * 의사 행동 처리
         *
         * @param target 대상
         */
        private void handleDoctorAction(String target) {
            // 마담에게 유혹당한 경우
            if (seduced.get(UserName) != null && seduced.get(UserName)) {
                WriteOne("SYSTEM: 마담에게 유혹당해 능력을 사용할 수 없습니다!\n");
                return;
            }

            if (doctorUsedThisNight) {
                WriteOne("SYSTEM: 이미 이번 밤에 치료 능력을 사용했습니다.\n");
                return;
            }

            WriteOne("SYSTEM: [" + target + "]님을 보호 대상으로 선택했습니다.\n");
            AppendText("의사 " + UserName + "이 " + target + " 보호 선택");
            doctorUsedThisNight = true;
        }

        /**
         * 경찰 행동 처리
         *
         * @param target 대상
         */
        private void handlePoliceAction(String target) {
            // 마담에게 유혹당한 경우
            if (seduced.get(UserName) != null && seduced.get(UserName)) {
                WriteOne("SYSTEM: 마담에게 유혹당해 능력을 사용할 수 없습니다!\n");
                return;
            }

            if (policeUsedThisNight) {
                WriteOne("SYSTEM: 이미 이번 밤에 조사를 완료했습니다.\n");
                return;
            }

            for (UserService targetUser : UserVec) {
                if (targetUser.UserName.equals(target)) {
                    String targetRole = targetUser.role;
                    boolean isMafia = targetRole.equals("MAFIA") || targetRole.equals("SPY");
                    String result = isMafia ? "마피아입니다!" : "마피아가 아닙니다.";

                    WriteOne("SYSTEM: [" + target + "]님은 " + result + "\n");

                    // 마피아인 경우 이미지 전송
                    if (isMafia) {
                        WriteOne("REVEAL:" + target + ":" + targetRole);
                    }

                    AppendText("경찰 " + UserName + "이 " + target + " 조사 -> " + result);
                    policeUsedThisNight = true;
                    break;
                }
            }
        }

        /**
         * 스파이 행동 처리
         *
         * @param target 대상
         */
        private void handleSpyAction(String target) {
            // 마담에게 유혹당한 경우
            if (seduced.get(UserName) != null && seduced.get(UserName)) {
                WriteOne("SYSTEM: 마담에게 유혹당해 능력을 사용할 수 없습니다!\n");
                return;
            }

            if (spyUsedThisNight) {
                WriteOne("SYSTEM: 이미 이번 밤에 조사를 완료했습니다.\n");
                return;
            }
            for (UserService targetUser : UserVec) {
                if (targetUser.UserName.equals(target)) {
                    String targetRole = targetUser.role;

                    // 스파이 조사 사운드 재생
                    WriteOne("SOUND:/GameSound/simin/police_choose.wav");

                    WriteOne("SYSTEM: [" + target + "]님의 직업은 [" + targetRole + "]입니다!\n");
                    AppendText("스파이 " + UserName + "이 " + target + " 조사 -> " + targetRole);

                    // 마피아 접선
                    if (targetRole.equals("MAFIA") && !spyContactedMafia) {
                        handleSpyMafiaContact(targetUser);
                    }

                    // 군인 상호 인식
                    if (targetRole.equals("SOLDIER")) {
                        targetUser.WriteOne("SYSTEM: 당신을 조사한 [" + UserName + "]님이 스파이임을 알아냈습니다!\n");
                        AppendText("군인 " + target + "이 스파이 " + UserName + " 정체 파악");
                    }
                    break;
                }
            }
            spyUsedThisNight = true;
        }

        /**
         * 스파이-마피아 접선 처리
         *
         * @param mafiaUser 마피아 사용자
         */
        private void handleSpyMafiaContact(UserService mafiaUser) {
            spyContactedMafia = true;

            // 모든 플레이어를 확인하여 마피아 팀원들과 정보 교환
            for (UserService user : UserVec) {
                // 마피아인 경우
                if (user.role.equals("MAFIA")) {
                    // 스파이에게 마피아 정보 알림 및 이미지 전송
                    WriteOne("SYSTEM: [" + user.UserName + "]님은 마피아입니다! 접선했습니다. 이제 밤에 대화할 수 있습니다.\n");
                    WriteOne("REVEAL:" + user.UserName + ":MAFIA");

                    // 마피아에게 스파이 정보 알림 및 이미지 전송
                    user.WriteOne("SYSTEM: 스파이 [" + UserName + "]님이 접선했습니다! 이제 동료입니다.\n");
                    user.WriteOne("REVEAL:" + UserName + ":SPY");
                }
                // 이미 접선한 마담인 경우
                else if (user.role.equals("MADAME") && madameContactedMafia) {
                    // 스파이에게 마담 정보 알림 및 이미지 전송
                    WriteOne("SYSTEM: [" + user.UserName + "]님은 마담입니다! 접선했습니다. 이제 밤에 대화할 수 있습니다.\n");
                    WriteOne("REVEAL:" + user.UserName + ":MADAME");

                    // 마담에게 스파이 정보 알림 및 이미지 전송
                    user.WriteOne("SYSTEM: [" + UserName + "]님이 스파이로 접선했습니다! 이제 동료입니다.\n");
                    user.WriteOne("REVEAL:" + UserName + ":SPY");
                }
            }

            AppendText("마피아와 스파이 접선 완료");
        }

        /**
         * 영매 행동 처리
         *
         * @param target 대상
         */
        private void handleShamanAction(String target) {
            // 마담에게 유혹당한 경우
            if (seduced.get(UserName) != null && seduced.get(UserName)) {
                WriteOne("SYSTEM: 마담에게 유혹당해 능력을 사용할 수 없습니다!\n");
                return;
            }

            if (shamanUsedThisNight) {
                WriteOne("SYSTEM: 이미 이번 밤에 성불 능력을 사용했습니다.\n");
                return;
            }

            for (UserService targetUser : UserVec) {
                if (targetUser.UserName.equals(target)) {
                    // 죽은 사람만 성불 가능
                    if (aliveStatus.get(target) != null && !aliveStatus.get(target)) {
                        String targetRole = targetUser.role;
                        WriteOne("SYSTEM: [" + target + "]님을 성불시켰습니다. 직업은 [" + targetRole + "]였습니다!\n");
                        AppendText("영매 " + UserName + "이 " + target + " 성불 -> " + targetRole);
                        blessedStatus.put(target, true);
                        shamanUsedThisNight = true;
                    } else {
                        WriteOne("SYSTEM: [" + target + "]님은 아직 살아있습니다!\n");
                    }
                    break;
                }
            }
        }

        /**
         * 기자 행동 처리
         *
         * @param target 대상
         */
        private void handleReporterAction(String target) {
            // 마담에게 유혹당한 경우
            if (seduced.get(UserName) != null && seduced.get(UserName)) {
                WriteOne("SYSTEM: 마담에게 유혹당해 능력을 사용할 수 없습니다!\n");
                return;
            }

            if (nightCount == 1) {
                WriteOne("SYSTEM: 첫 번째 밤에는 기자 능력을 사용할 수 없습니다!\n");
            } else if (nightCount > 8) {
                WriteOne("SYSTEM: 8일차 이후에는 기자 능력을 사용할 수 없습니다!\n");
            } else {
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
        }

        /**
         * 건달 행동 처리
         *
         * @param target 대상
         */
        private void handleGangsterAction(String target) {
            // 마담에게 유혹당한 경우
            if (seduced.get(UserName) != null && seduced.get(UserName)) {
                WriteOne("SYSTEM: 마담에게 유혹당해 능력을 사용할 수 없습니다!\n");
                return;
            }

            if (gangsterUsedThisNight) {
                WriteOne("SYSTEM: 이미 이번 밤에 능력을 사용했습니다.\n");
                return;
            }

            WriteOne("SYSTEM: [" + target + "]님을 선택했습니다. 다음 투표에서 투표하지 못합니다!\n");
            AppendText("건달 " + UserName + "이 " + target + " 선택 -> 다음 투표 금지");
            gangsterUsedThisNight = true;

            // 타겟에게 협박 메시지 전송
            for (UserService targetUser : UserVec) {
                if (targetUser.UserName.equals(target)) {
                    targetUser.WriteOne("SYSTEM: 협박을 받았습니다! 다음 투표에 참여할 수 없습니다.\n");
                    break;
                }
            }
        }

        /**
         * 성직자 행동 처리
         *
         * @param target 대상
         */
        private void handlePriestAction(String target) {
            // 마담에게 유혹당한 경우
            if (seduced.get(UserName) != null && seduced.get(UserName)) {
                WriteOne("SYSTEM: 마담에게 유혹당해 능력을 사용할 수 없습니다!\n");
                return;
            }

            if (priestUsed) { // priestUsedAbility 대신 priestUsed 사용
                WriteOne("SYSTEM: 이미 소생 능력을 사용했습니다!\n");
            } else if (aliveStatus.get(target) == null) {
                WriteOne("SYSTEM: 해당 플레이어를 찾을 수 없습니다!\n");
            } else if (aliveStatus.get(target)) {
                WriteOne("SYSTEM: [" + target + "]님은 살아있습니다! 죽은 사람만 부활시킬 수 있습니다.\n");
            } else if (blessedStatus.get(target) != null && blessedStatus.get(target)) {
                WriteOne("SYSTEM: [" + target + "]님은 성불되어 부활할 수 없습니다!\n");
            } else {
                priestTarget = target;
                priestUsed = true;
                WriteOne("SYSTEM: [" + target + "]님을 부활 대상으로 선택했습니다. 다음 낮에 부활합니다!\n");
                AppendText("성직자 " + UserName + "이 " + target + " 부활 예약");
            }
        }

        /**
         * 쪽지 전송 처리 - 제거됨 (게임 밸런스를 위해)
         * 프로토콜: WHISPER:RECEIVER:CONTENT
         *
         * @param msg 메시지
         */
        @Deprecated
        private void handleWhisper(String msg) {
            // WHISPER:RECEIVER:CONTENT 형식
            String[] parts = msg.split(":", 3);
            if (parts.length != 3) {
                WriteOne("SYSTEM: 잘못된 쪽지 형식입니다.\n");
                return;
            }

            String receiver = parts[1];
            String content = parts[2];

            // 낮 시간에만 쪽지 가능
            if (!gamePhase.equals("DAY")) {
                WriteOne("SYSTEM: 낮 시간에만 쪽지를 보낼 수 있습니다.\n");
                return;
            }

            // 죽은 플레이어는 쪽지 불가
            if (aliveStatus.get(UserName) != null && !aliveStatus.get(UserName)) {
                WriteOne("SYSTEM: 죽은 플레이어는 쪽지를 보낼 수 없습니다.\n");
                return;
            }

            // 쪽지 전송
            WhisperResult result = whisperManager.sendWhisper(UserName, receiver, content);

            if (result.isSuccess()) {
                WriteOne("SYSTEM: " + receiver + "님에게 익명 쪽지를 보냈습니다.\n");

                // 수신자에게 전송
                for (UserService user : UserVec) {
                    if (user.UserName.equals(receiver)) {
                        user.WriteOne("WHISPER: 익명의 누군가: " + content + "\n");
                        break;
                    }
                }

                AppendText("[쪽지] " + UserName + " -> " + receiver + ": " + content);
            } else {
                WriteOne("SYSTEM: " + result.getMessage() + "\n");
            }
        }

        /**
         * 감정 표현 처리
         * 프로토콜: EMOTION:EMOTION_ID:TARGET (TARGET은 선택사항)
         *
         * @param msg 메시지
         */
        private void handleEmotion(String msg) {
            // EMOTION:EMOTION_ID:TARGET 형식
            String[] parts = msg.split(":", 3);
            if (parts.length < 2) {
                WriteOne("SYSTEM: 잘못된 감정 표현 형식입니다.\n");
                return;
            }

            String emotionId = parts[1];
            String target = parts.length == 3 ? parts[2] : null;

            // 죽은 플레이어는 감정 표현 불가
            if (aliveStatus.get(UserName) != null && !aliveStatus.get(UserName)) {
                WriteOne("SYSTEM: 죽은 플레이어는 감정 표현을 할 수 없습니다.\n");
                return;
            }

            // 감정 표현
            EmotionResult result = emotionManager.express(UserName, emotionId, target);

            if (result.isSuccess()) {
                String emotionEmoji = result.getEmotion().getIcon();
                String emotionName = result.getEmotion().getName();

                // 모든 살아있는 플레이어에게 브로드캐스트
                String broadcastMsg = target != null
                    ? "EMOTION: " + UserName + "님이 " + target + "님에게 " + emotionEmoji + " " + emotionName + "\n"
                    : "EMOTION: " + UserName + "님이 " + emotionEmoji + " " + emotionName + "\n";

                for (UserService user : UserVec) {
                    if (aliveStatus.get(user.UserName) != null && aliveStatus.get(user.UserName)) {
                        user.WriteOne(broadcastMsg);
                    }
                }

                AppendText("[감정] " + UserName + ": " + emotionEmoji + " " + emotionName +
                          (target != null ? " -> " + target : ""));
            } else {
                WriteOne("SYSTEM: " + result.getMessage() + "\n");
            }
        }

        /**
         * 역할 가이드 명령어 처리
         * 프로토콜: /guide 또는 /guide ROLE
         *
         * @param msg 메시지
         */
        private void handleGuideCommand(String msg) {
            String[] parts = msg.trim().split("\\s+");

            if (parts.length == 1) {
                // 자신의 역할 가이드
                if (role != null && !role.isEmpty()) {
                    String guide = roleGuideManager.getGuideText(role);
                    WriteOne("GUIDE:\n" + guide + "\n");
                    AppendText(UserName + " 역할 가이드 조회: " + role);
                } else {
                    WriteOne("SYSTEM: 역할이 배정되지 않았습니다.\n");
                }
            } else {
                // 특정 역할 가이드
                String requestedRole = parts[1].toUpperCase();
                String guide = roleGuideManager.getGuideText(requestedRole);

                if (guide.contains("찾을 수 없습니다")) {
                    WriteOne("SYSTEM: 존재하지 않는 역할입니다.\n");
                } else {
                    WriteOne("GUIDE:\n" + guide + "\n");
                    AppendText(UserName + " 역할 가이드 조회: " + requestedRole);
                }
            }
        }

        /**
         * 통계 명령어 처리
         * 프로토콜: /stats 또는 /stats PLAYER_NAME
         *
         * @param msg 메시지
         */
        private void handleStatsCommand(String msg) {
            String[] parts = msg.trim().split("\\s+", 2);

            if (parts.length == 1) {
                // 자신의 통계
                PlayerStatistics stats = statisticsManager.getStatistics(UserName);
                WriteOne("STATS:\n" + stats.toString() + "\n");
                AppendText(UserName + " 통계 조회");
            } else {
                // 특정 플레이어 통계
                String targetPlayer = parts[1];
                PlayerStatistics stats = statisticsManager.getStatistics(targetPlayer);
                WriteOne("STATS [" + targetPlayer + "]:\n" + stats.toString() + "\n");
                AppendText(UserName + " 통계 조회: " + targetPlayer);
            }
        }



        /**
         * 도움말 명령어 처리
         * 프로토콜: /help 또는 /도움말
         */
        private void handleHelpCommand() {
            StringBuilder help = new StringBuilder();
            help.append("============================================================\n");
            help.append("                  마피아 게임 명령어 도움말\n");
            help.append("============================================================\n\n");

            help.append("역할 가이드:\n");
            help.append("  /가이드 또는 /역할      - 자신의 역할 가이드 보기\n");
            help.append("  /가이드 마피아          - 특정 역할 가이드 보기\n");
            help.append("  예시: /가이드 의사, /가이드 경찰\n\n");

            help.append("통계 조회:\n");
            help.append("  /통계 또는 /전적        - 자신의 통계 보기\n");
            help.append("  /통계 플레이어이름      - 특정 플레이어 통계 보기\n");
            help.append("  예시: /통계 Player1\n\n");

            help.append("감정 표현:\n");
            help.append("  /감정                   - 사용 가능한 감정 목록 보기\n");
            help.append("  /감정 좋아요            - 감정 표현하기\n");
            help.append("  /감정 좋아요 Player1    - 특정 플레이어에게 감정 표현\n\n");

            help.append("기타:\n");
            help.append("  /도움말 또는 /명령어    - 이 도움말 보기\n\n");

            help.append("============================================================\n");

            WriteOne(help.toString());
            AppendText(UserName + " 도움말 조회");
        }

        /**
         * 귓속말 명령어 처리 (한글 버전)
         * 프로토콜: /귓속말 대상 메시지
         *
         * @param msg 메시지
         */
        private void handleWhisperCommand(String msg) {
            // /귓속말 대상 메시지 형식 파싱
            String[] parts = msg.trim().split("\\s+", 3);

            if (parts.length < 3) {
                WriteOne("사용법: /귓속말 대상플레이어 메시지내용\n");
                WriteOne("예시: /귓속말 Player1 안녕하세요\n");
                return;
            }

            String receiver = parts[1];
            String content = parts[2];

            // 낮 시간에만 쪽지 가능
            if (!gamePhase.equals("DAY")) {
                WriteOne("SYSTEM: 낮 시간에만 귓속말을 보낼 수 있습니다.\n");
                return;
            }

            // 죽은 플레이어는 쪽지 불가
            if (aliveStatus.get(UserName) != null && !aliveStatus.get(UserName)) {
                WriteOne("SYSTEM: 죽은 플레이어는 귓속말을 보낼 수 없습니다.\n");
                return;
            }

            // 쪽지 전송
            WhisperResult result = whisperManager.sendWhisper(UserName, receiver, content);

            if (result.isSuccess()) {
                WriteOne("SYSTEM: " + receiver + "님에게 익명 귓속말을 보냈습니다.\n");

                // 수신자에게 전송
                for (UserService user : UserVec) {
                    if (user.UserName.equals(receiver)) {
                        user.WriteOne("💬 [익명 귓속말]: " + content + "\n");
                        break;
                    }
                }

                AppendText("[귓속말] " + UserName + " -> " + receiver + ": " + content);
            } else {
                WriteOne("SYSTEM: " + result.getMessage() + "\n");
            }
        }

        /**
         * 감정 표현 명령어 처리 (한글 버전)
         * 프로토콜: /감정 또는 /감정 감정이름 또는 /감정 감정이름 대상
         *
         * @param msg 메시지
         */
        private void handleEmotionCommand(String msg) {
            String[] parts = msg.trim().split("\\s+", 3);

            // 감정 목록 보기
            if (parts.length == 1) {
                showEmotionList();
                return;
            }

            // 죽은 플레이어는 감정 표현 불가
            if (aliveStatus.get(UserName) != null && !aliveStatus.get(UserName)) {
                WriteOne("SYSTEM: 죽은 플레이어는 감정 표현을 할 수 없습니다.\n");
                return;
            }

            String emotionName = parts[1];
            String target = parts.length == 3 ? parts[2] : null;

            // 한글 이름을 ID로 변환
            String emotionId = getEmotionIdFromName(emotionName);

            if (emotionId == null) {
                WriteOne("SYSTEM: 알 수 없는 감정입니다. '/감정'으로 목록을 확인하세요.\n");
                return;
            }

            // 감정 표현
            EmotionResult result = emotionManager.express(UserName, emotionId, target);

            if (result.isSuccess()) {
                String emotionEmoji = result.getEmotion().getIcon();
                String emotionDispName = result.getEmotion().getName();

                // 모든 살아있는 플레이어에게 브로드캐스트
                String broadcastMsg = target != null
                    ? "💫 " + UserName + "님이 " + target + "님에게 " + emotionEmoji + " " + emotionDispName + "\n"
                    : "💫 " + UserName + "님이 " + emotionEmoji + " " + emotionDispName + "\n";

                for (UserService user : UserVec) {
                    if (aliveStatus.get(user.UserName) != null && aliveStatus.get(user.UserName)) {
                        user.WriteOne(broadcastMsg);
                    }
                }

                AppendText("[감정] " + UserName + ": " + emotionEmoji + " " + emotionDispName +
                          (target != null ? " -> " + target : ""));
            } else {
                WriteOne("SYSTEM: " + result.getMessage() + "\n");
            }
        }

        /**
         * 감정 목록 표시
         */
        private void showEmotionList() {
            StringBuilder list = new StringBuilder();
            list.append("============================================================\n");
            list.append("                     사용 가능한 감정\n");
            list.append("============================================================\n\n");

            list.append("😊 긍정적 감정:\n");
            list.append("  👍 좋아요        ❤️ 하트         😂 웃음\n");
            list.append("  👏 박수          🔥 불\n\n");

            list.append("😠 부정적 감정:\n");
            list.append("  👎 싫어요        😢 울음         😠 화남\n\n");

            list.append("😐 중립적 감정:\n");
            list.append("  🤔 생각          ❓ 물음표       ❗ 느낌표\n");
            list.append("  💤 잠\n\n");

            list.append("🎮 게임 특화:\n");
            list.append("  😱 충격          💀 해골         👻 유령\n");
            list.append("  🕵️ 탐정          🔪 칼           🛡️ 방패\n");
            list.append("  ✝️ 십자가\n\n");

            list.append("사용법:\n");
            list.append("  /감정 좋아요             - 모두에게 감정 표현\n");
            list.append("  /감정 좋아요 Player1     - 특정 플레이어에게 감정 표현\n");
            list.append("  /감정 탐정 Player2       - 다른 감정 사용 예시\n\n");

            list.append("============================================================\n");

            WriteOne(list.toString());
        }

        /**
         * 한글 감정 이름을 영어 ID로 변환
         */
        private String getEmotionIdFromName(String name) {
            switch(name) {
                case "좋아요": return "THUMBS_UP";
                case "싫어요": return "THUMBS_DOWN";
                case "하트": return "HEART";
                case "웃음": return "LAUGH";
                case "울음": return "CRY";
                case "화남": return "ANGRY";
                case "충격": return "SHOCKED";
                case "생각": return "THINK";
                case "박수": return "CLAP";
                case "불": return "FIRE";
                case "해골": return "SKULL";
                case "유령": return "GHOST";
                case "탐정": return "DETECTIVE";
                case "칼": return "KNIFE";
                case "방패": return "SHIELD";
                case "십자가": return "CROSS";
                case "물음표": return "QUESTION";
                case "느낌표": return "EXCLAMATION";
                case "잠": return "ZZZ";
                default: return null;
            }
        }

        /**
         * 투표 처리
         *
         * @param msg 메시지
         */
        private void handleVote(String msg) {
            // VOTE:TARGET 형식
            String[] parts = msg.split(":");
            if (parts.length != 2) {
                return;
            }

            String target = parts[1];

            // 중복 투표 확인
            if (hasVotedThisRound.contains(UserName)) {
                WriteOne("SYSTEM: 이미 투표하셨습니다! 한 라운드에 한 번만 투표할 수 있습니다.\n");
                return;
            }

            // 투표 제한 확인
            if (voteBanned.get(UserName) != null && voteBanned.get(UserName)) {
                WriteOne("SYSTEM: 건달에 의해 투표가 금지되었습니다!\n");
                return;
            }

            if (aliveStatus.get(UserName) != null && !aliveStatus.get(UserName)) {
                WriteOne("SYSTEM: 죽은 사람은 투표할 수 없습니다!\n");
                return;
            }

            if (aliveStatus.get(target) != null && !aliveStatus.get(target)) {
                WriteOne("SYSTEM: 죽은 사람에게는 투표할 수 없습니다!\n");
                return;
            }

            // 투표 처리
            if (voteCount.containsKey(target)) {
                // 투표 수 계산: 정치인(2표), 산타 선물(2표), 일반(1표)
                int votes = 1;
                String voteReason = "";

                if (role.equals("POLITICIAN")) {
                    votes = 2;
                    voteReason = " (정치인 - 2표)";
                } else if (christmasEventActive && UserName.equals(santaGiftReceiver)) {
                    votes = 2;
                    voteReason = " 🎁 (산타의 선물 - 2표)";
                }

                voteCount.put(target, voteCount.get(target) + votes);

                // 투표한 플레이어를 기록
                hasVotedThisRound.add(UserName);

                // VoteTracker에 등록 (실시간 추적)
                mafia.game.features.VoteTracker.VoteCastResult voteResult =
                    voteTracker.castVote(UserName, target);

                AppendText(UserName + "(" + role + ") -> " + target + " 투표 (" + votes + "표)" + voteReason);
                WriteOne("SYSTEM: [" + target + "]님에게 투표했습니다." + voteReason + "\n");

                // 실시간 투표 현황 브로드캐스트
                int currentVotes = voteTracker.getVoteCount(target);
                double voteRate = voteTracker.calculateVoteRate();
                WriteAll(String.format("SYSTEM: 💫 %s님이 투표했습니다. (참여율: %.0f%%)\n",
                                      UserName, voteRate * 100));

                // 마담의 유혹 능력 (투표 시 대상 유혹 -> 찬반투표 때 알림)
                if (role.equals("MADAME")) {
                    seduced.put(target, true);

                    // 마피아 자동 접선 처리
                    for (UserService targetUser : UserVec) {
                        if (targetUser.UserName.equals(target)) {
                            if (targetUser.role.equals("MAFIA") && !madameContactedMafia) {
                                handleMadameMafiaContact(targetUser);
                            }
                            break;
                        }
                    }
                }
            }
        }

        /**
         * 마담-마피아 접선 처리
         *
         * @param mafiaUser 마피아 사용자
         */
        private void handleMadameMafiaContact(UserService mafiaUser) {
            madameContactedMafia = true;

            // 마담에게 마피아 이미지 전송
            WriteOne("SYSTEM: [" + mafiaUser.UserName + "]님은 마피아입니다! 접선했습니다. 이제 밤에 대화할 수 있습니다.\n");
            WriteOne("REVEAL:" + mafiaUser.UserName + ":MAFIA");

            // 마피아에게 마담 이미지 전송
            mafiaUser.WriteOne("SYSTEM: [" + UserName + "]님이 마담으로 접선했습니다! 이제 동료입니다.\n");
            mafiaUser.WriteOne("REVEAL:" + UserName + ":MADAME");

            AppendText("마담과 마피아 접선 완료");

            // 접선 후 게임 종료 조건 체크
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    if (checkGameEnd()) {
                        AppendText("마담 접선 후 게임 종료 조건 충족");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        /**
         * 채팅 메시지 처리
         *
         * @param msg 메시지
         */
        private void handleChatMessage(String msg) {
            // 명령어 체크 (채팅 메시지에서 명령어 추출)
            // 형식: [Player] /command args
            String lowerMsg = msg.toLowerCase();

            // 도움말
            if (lowerMsg.contains("/help") || msg.contains("/도움말") || msg.contains("/명령어")) {
                handleHelpCommand();
                return;
            }
            // 역할 가이드
            else if (lowerMsg.contains("/guide") || msg.contains("/가이드") || msg.contains("/역할")) {
                int cmdIndex = lowerMsg.indexOf("/guide");
                if (cmdIndex == -1) cmdIndex = msg.indexOf("/가이드");
                if (cmdIndex == -1) cmdIndex = msg.indexOf("/역할");
                String command = msg.substring(cmdIndex);
                handleGuideCommand(command);
                return;
            }
            // 통계
            else if (lowerMsg.contains("/stats") || msg.contains("/통계") || msg.contains("/전적")) {
                int cmdIndex = lowerMsg.indexOf("/stats");
                if (cmdIndex == -1) cmdIndex = msg.indexOf("/통계");
                if (cmdIndex == -1) cmdIndex = msg.indexOf("/전적");
                String command = msg.substring(cmdIndex);
                handleStatsCommand(command);
                return;
            }
            // 감정 표현
            else if (lowerMsg.contains("/emotion") || msg.contains("/감정") || msg.contains("/이모지")) {
                int cmdIndex = lowerMsg.indexOf("/emotion");
                if (cmdIndex == -1) cmdIndex = msg.indexOf("/감정");
                if (cmdIndex == -1) cmdIndex = msg.indexOf("/이모지");
                String command = msg.substring(cmdIndex);
                handleEmotionCommand(command);
                return;
            }

            if (gamePhase.equals("FINAL_DEFENSE")) {
                handleFinalDefenseChat(msg);
            } else if (gamePhase.equals("NIGHT")) {
                handleNightChat(msg);
            } else {
                handleDayChat(msg);
            }
        }

        /**
         * 최후의 반론 채팅 처리
         *
         * @param msg 메시지
         */
        private void handleFinalDefenseChat(String msg) {
            // 죽은 플레이어는 채팅 가능
            if (aliveStatus.get(UserName) != null && !aliveStatus.get(UserName)) {
                handleDeadChat(msg);
            }
            // 최후의 반론 대상자만 채팅 가능 (단, 마담의 유혹을 받은 경우 제외)
            else if (UserName.equals(finalDefensePlayer)) {
                // 마담의 유혹을 받은 경우 말할 수 없음
                if (seduced.containsKey(UserName) && seduced.get(UserName)) {
                    WriteOne("SYSTEM: 마담의 유혹을 받아 말을 할 수 없습니다.\n");
                } else {
                    WriteAll(msg + "\n");
                }
            }
            // 다른 플레이어는 채팅 불가
            else {
                WriteOne("SYSTEM: 최후의 반론 시간에는 대상자만 채팅할 수 있습니다.\n");
            }
        }

        /**
         * 찬반 투표 처리
         *
         * @param msg 메시지 (AGREE_DISAGREE:AGREE 또는 AGREE_DISAGREE:DISAGREE)
         */
        private void handleAgreeDisagreeVote(String msg) {
            if (!gamePhase.equals("AGREE_DISAGREE")) {
                WriteOne("SYSTEM: 현재 찬반 투표 시간이 아닙니다.\n");
                return;
            }

            // 죽은 플레이어는 투표 불가
            if (aliveStatus.get(UserName) == null || !aliveStatus.get(UserName)) {
                WriteOne("SYSTEM: 죽은 플레이어는 투표할 수 없습니다.\n");
                return;
            }

            // 최후의 변론 대상자는 투표 불가
            if (UserName.equals(finalDefensePlayer)) {
                WriteOne("SYSTEM: 최후의 변론 대상자는 투표할 수 없습니다.\n");
                return;
            }

            // 중복 투표 방지
            if (hasVotedFinalDecision.contains(UserName)) {
                WriteOne("SYSTEM: 이미 투표했습니다.\n");
                return;
            }

            String[] parts = msg.split(":");
            if (parts.length != 2) {
                return;
            }

            String vote = parts[1];
            hasVotedFinalDecision.add(UserName);

            if (vote.equals("AGREE")) {
                agreeVotes++;
                AppendText(UserName + " 찬성 투표");
                WriteOne("SYSTEM: 찬성에 투표했습니다.\n");
            } else if (vote.equals("DISAGREE")) {
                disagreeVotes++;
                AppendText(UserName + " 반대 투표");
                WriteOne("SYSTEM: 반대에 투표했습니다.\n");
            }
        }

        /**
         * 밤 채팅 처리
         *
         * @param msg 메시지
         */
        private void handleNightChat(String msg) {
            // 죽은 플레이어
            if (aliveStatus.get(UserName) != null && !aliveStatus.get(UserName)) {
                handleDeadChat(msg);
            }
            // 마피아
            else if (role.equals("MAFIA")) {
                broadcastToMafiaTeam(msg);
            }
            // 마담
            else if (role.equals("MADAME")) {
                if (madameContactedMafia) {
                    broadcastToMafiaTeam(msg);
                } else {
                    WriteOne("SYSTEM: 마피아와 접선하기 전에는 채팅할 수 없습니다.\n");
                }
            }
            // 스파이
            else if (role.equals("SPY")) {
                if (spyContactedMafia) {
                    broadcastToMafiaTeam(msg);
                } else {
                    WriteOne("SYSTEM: 마피아와 접선하기 전에는 채팅할 수 없습니다.\n");
                }
            }
            // 일반 시민
            else {
                WriteOne("SYSTEM: 밤에는 채팅할 수 없습니다.\n");
            }
        }

        /**
         * 죽은 플레이어 채팅 처리
         *
         * @param msg 메시지
         */
        private void handleDeadChat(String msg) {
            if (blessedStatus.get(UserName) != null && blessedStatus.get(UserName)) {
                WriteOne("SYSTEM: 성불당해서 채팅할 수 없습니다.\n");
                return;
            }

            // 죽은 플레이어들과 영매에게 전송 (성불된 사람 제외)
            for (UserService user : UserVec) {
                // 죽은 플레이어 중 성불되지 않은 사람에게 전송
                if (aliveStatus.get(user.UserName) != null && !aliveStatus.get(user.UserName)) {
                    if (blessedStatus.get(user.UserName) == null || !blessedStatus.get(user.UserName)) {
                        user.WriteOne("[DEAD CHAT] " + msg + "\n");
                    }
                }

                // 살아있는 영매에게 전송
                if (user.role.equals("SHAMAN") &&
                        (aliveStatus.get(user.UserName) == null || aliveStatus.get(user.UserName))) {
                    user.WriteOne("[DEAD CHAT] " + msg + "\n");
                }
            }

            AppendText("[DEAD CHAT] " + msg);
        }

        /**
         * 마피아 팀에게 메시지 전송
         *
         * @param msg 메시지
         */
        private void broadcastToMafiaTeam(String msg) {
            for (UserService user : UserVec) {
                if (user.role.equals("MAFIA")) {
                    user.WriteOne("[MAFIA TEAM] " + msg + "\n");
                }

                if (user.role.equals("SPY") && spyContactedMafia) {
                    user.WriteOne("[MAFIA TEAM] " + msg + "\n");
                }

                if (user.role.equals("MADAME") && madameContactedMafia) {
                    user.WriteOne("[MAFIA TEAM] " + msg + "\n");
                }
            }

            AppendText("[MAFIA TEAM] " + msg);
        }

        /**
         * 낮 채팅 처리
         *
         * @param msg 메시지
         */
        private void handleDayChat(String msg) {
            // 투표 시간에 유혹당한 경우
            if (gamePhase.equals("VOTE") && seduced.get(UserName) != null &&
                    seduced.get(UserName) && !role.equals("MAFIA")) {
                WriteOne("SYSTEM: 마담에게 유혹당해 채팅할 수 없습니다!\n");
                return;
            }

            // 죽은 플레이어
            if (aliveStatus.get(UserName) != null && !aliveStatus.get(UserName)) {
                handleDeadChat(msg);
            }
            // 살아있는 플레이어
            else {
                WriteAll(msg + "\n");
            }
        }
    } // End of UserService class

    // ========================================
    // AI 플레이어 시스템
    // ========================================



    /**
     * 마피아 팀 정보 공유
     * 마피아끼리 상호 인식하고, 접선한 스파이/마담 정보를 공유합니다.
     */
    private void broadcastMafiaTeamInfo() {
        for (UserService user : UserVec) {
            // 마피아인 경우: 다른 마피아, 접선한 스파이, 접선한 마담 정보 수신
            if (user.role.equals("MAFIA")) {
                for (UserService other : UserVec) {
                    if (user == other)
                        continue;

                    if (other.role.equals("MAFIA")) {
                        user.WriteOne("REVEAL:" + other.UserName + ":MAFIA");
                    } else if (other.role.equals("SPY") && spyContactedMafia) {
                        user.WriteOne("REVEAL:" + other.UserName + ":SPY");
                    } else if (other.role.equals("MADAME") && madameContactedMafia) {
                        user.WriteOne("REVEAL:" + other.UserName + ":MADAME");
                    }
                }
            }
            // 접선한 스파이인 경우: 마피아, 접선한 마담 정보 수신
            else if (user.role.equals("SPY") && spyContactedMafia) {
                for (UserService other : UserVec) {
                    if (other.role.equals("MAFIA")) {
                        user.WriteOne("REVEAL:" + other.UserName + ":MAFIA");
                    } else if (other.role.equals("MADAME") && madameContactedMafia) {
                        user.WriteOne("REVEAL:" + other.UserName + ":MADAME");
                    }
                }
            }
            // 접선한 마담인 경우: 마피아, 접선한 스파이 정보 수신
            else if (user.role.equals("MADAME") && madameContactedMafia) {
                for (UserService other : UserVec) {
                    if (other.role.equals("MAFIA")) {
                        user.WriteOne("REVEAL:" + other.UserName + ":MAFIA");
                    } else if (other.role.equals("SPY") && spyContactedMafia) {
                        user.WriteOne("REVEAL:" + other.UserName + ":SPY");
                    }
                }
            }
        }
    }
}
// End of MafiaGameServer class
