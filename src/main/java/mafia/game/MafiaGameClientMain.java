package mafia.game;

/**
 * 마피아 게임 클라이언트 로그인 화면 클래스
 *
 * 이 클래스는 마피아 게임의 초기 로그인 화면을 담당합니다.
 * 사용자 이름, IP 주소, 포트 번호를 입력받아 서버에 연결합니다.
 *
 * 주요 기능:
 * - 사용자 정보 입력 폼 제공 (사용자명, IP 주소, 포트)
 * - 서버 연결 처리
 * - 로그인 후 게임 화면(MafiaGameClientView)으로 전환
 * - 커스텀 UI 디자인 (배경 이미지, 테마 색상)
 *
 * @author Mafia Game Team
 * @version 2.0
 */

// AWT 및 이벤트 처리 관련 임포트
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.Color;

// Swing UI 컴포넌트 임포트
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * MafiaGameClientMain 메인 클래스
 * JFrame을 상속받아 로그인 GUI 인터페이스를 제공합니다.
 */
public class MafiaGameClientMain extends JFrame {

    // ========================================
    // 테스트 모드 설정
    // ========================================

    /**
     * 테스트 모드 활성화 여부
     * true: 자동 로그인 + 랜덤 이름 생성
     * false: 일반 로그인 화면
     *
     * 배포 전에는 반드시 false로 변경할 것!
     */
    private static final boolean TEST_MODE = true;

    // ========================================
    // UI 컴포넌트
    // ========================================

    /**
     * 메인 컨텐츠 패널
     */
    private JPanel contentPane;

    /**
     * 사용자 이름 입력 필드
     */
    private JTextField txtUserName;

    /**
     * IP 주소 입력 필드
     */
    private JTextField txtIpAddress;

    /**
     * 포트 번호 입력 필드
     */
    private JTextField txtPortNumber;

    // ========================================
    // 메인 메소드
    // ========================================

    /**
     * 프로그램 진입점
     * 로그인 화면을 생성하고 표시합니다.
     *
     * @param args 커맨드 라인 인자
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    if (TEST_MODE) {
                        // 테스트 모드: 자동 로그인
                        autoLogin();
                    } else {
                        // 일반 모드: 로그인 화면 표시
                        MafiaGameClientMain frame = new MafiaGameClientMain();
                        frame.setVisible(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 테스트 모드 자동 로그인
     * 랜덤 이름을 생성하여 자동으로 서버에 접속합니다.
     */
    private static void autoLogin() {
        // 랜덤 이름 생성 (Player_랜덤숫자)
        String randomName = "Player_" + (int)(Math.random() * 9999);
        String ipAddress = "127.0.0.1";
        String portNumber = "30000";

        System.out.println("[TEST MODE] Auto login as: " + randomName);

        // 게임 뷰 직접 생성
        new MafiaGameClientView(randomName, ipAddress, portNumber);
    }

    // ========================================
    // 생성자 및 UI 초기화
    // ========================================

    /**
     * 로그인 화면을 초기화하는 생성자
     * UI 컴포넌트를 생성하고 배치합니다.
     */
    public MafiaGameClientMain() {
        initializeFrame();
        initializeThemeColors();
        createUI();
        setupActionListeners();
    }

    /**
     * 프레임 기본 설정 초기화
     */
    private void initializeFrame() {
        setTitle("Mafia Game - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 550);
    }

    /**
     * 테마 색상 정의 및 초기화
     *
     * @return ThemeColors 객체
     */
    private ThemeColors initializeThemeColors() {
        return new ThemeColors();
    }

    /**
     * UI 컴포넌트 생성 및 배치
     */
    private void createUI() {
        // 테마 색상 객체
        ThemeColors theme = new ThemeColors();

        // 커스텀 배경 패널 생성
        contentPane = new BackgroundPanel();
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);
        contentPane.setLayout(new java.awt.GridBagLayout());

        // 메인 패널 생성
        JPanel mainPanel = createMainPanel(theme);

        contentPane.add(mainPanel);
    }

    /**
     * 메인 패널 생성
     *
     * @param theme 테마 색상 객체
     * @return 생성된 메인 패널
     */
    private JPanel createMainPanel(ThemeColors theme) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new java.awt.GridBagLayout());
        mainPanel.setBackground(theme.panelColor);
        mainPanel.setBorder(new javax.swing.border.LineBorder(new Color(60, 60, 60), 1, true));

        // GridBagConstraints 설정
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 20, 10, 20);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // UI 컴포넌트들 추가
        addLogo(mainPanel, gbc);
        addTitle(mainPanel, gbc, theme);
        addSubtitle(mainPanel, gbc);
        addInputFields(mainPanel, gbc, theme);
        addConnectButton(mainPanel, gbc, theme);

        return mainPanel;
    }

    /**
     * 로고 이미지 추가
     *
     * @param panel 추가할 패널
     * @param gbc GridBagConstraints
     */
    private void addLogo(JPanel panel, java.awt.GridBagConstraints gbc) {
        try {
            java.net.URL iconURL = getClass().getResource("/info/ServerImg.png");
            if (iconURL != null) {
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(iconURL);
                java.awt.Image img = icon.getImage();
                java.awt.Image newImg = img.getScaledInstance(80, 80, java.awt.Image.SCALE_SMOOTH);

                JLabel logoLabel = new JLabel(new javax.swing.ImageIcon(newImg));
                logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

                gbc.gridy = 0;
                gbc.insets = new java.awt.Insets(30, 20, 10, 20);
                panel.add(logoLabel, gbc);
            }
        } catch (Exception e) {
            // 이미지 로드 실패 시 무시
        }
    }

    /**
     * 타이틀 레이블 추가
     *
     * @param panel 추가할 패널
     * @param gbc GridBagConstraints
     * @param theme 테마 색상
     */
    private void addTitle(JPanel panel, java.awt.GridBagConstraints gbc, ThemeColors theme) {
        JLabel lblTitle = new JLabel("MAFIA GAME");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitle.setForeground(theme.accentColor);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy++;
        gbc.insets = new java.awt.Insets(5, 20, 5, 20);
        panel.add(lblTitle, gbc);
    }

    /**
     * 서브타이틀 레이블 추가
     *
     * @param panel 추가할 패널
     * @param gbc GridBagConstraints
     */
    private void addSubtitle(JPanel panel, java.awt.GridBagConstraints gbc) {
        JLabel lblSubTitle = new JLabel("Find the Mafia!");
        lblSubTitle.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblSubTitle.setForeground(new Color(180, 180, 180));
        lblSubTitle.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy++;
        gbc.insets = new java.awt.Insets(0, 20, 30, 20);
        panel.add(lblSubTitle, gbc);
    }

    /**
     * 입력 필드들 추가 (사용자명, IP, 포트)
     *
     * @param panel 추가할 패널
     * @param gbc GridBagConstraints
     * @param theme 테마 색상
     */
    private void addInputFields(JPanel panel, java.awt.GridBagConstraints gbc, ThemeColors theme) {
        // 사용자 이름 입력
        addLabel(panel, "User Name", theme.textColor, gbc);
        txtUserName = addTextField(panel, "", theme.inputColor, theme.textColor, gbc);

        // IP 주소 입력
        addLabel(panel, "IP Address", theme.textColor, gbc);
        txtIpAddress = addTextField(panel, "127.0.0.1", theme.inputColor, theme.textColor, gbc);

        // 포트 번호 입력
        addLabel(panel, "Port Number", theme.textColor, gbc);
        txtPortNumber = addTextField(panel, "30000", theme.inputColor, theme.textColor, gbc);
    }

    /**
     * 연결 버튼 추가
     *
     * @param panel 추가할 패널
     * @param gbc GridBagConstraints
     * @param theme 테마 색상
     */
    private void addConnectButton(JPanel panel, java.awt.GridBagConstraints gbc, ThemeColors theme) {
        JButton btnConnect = new JButton("Connect to Game");
        btnConnect.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnConnect.setBackground(theme.accentColor);
        btnConnect.setForeground(Color.WHITE);
        btnConnect.setFocusPainted(false);
        btnConnect.setBorderPainted(false);
        btnConnect.setOpaque(true);
        btnConnect.setPreferredSize(new java.awt.Dimension(200, 45));

        gbc.gridy++;
        gbc.insets = new java.awt.Insets(30, 20, 30, 20);
        panel.add(btnConnect, gbc);

        // 액션 리스너는 별도 메소드에서 설정
        btnConnect.setName("btnConnect");
    }

    /**
     * 액션 리스너 설정
     */
    private void setupActionListeners() {
        Myaction action = new Myaction();

        // 버튼 찾기
        findAndSetActionListener(contentPane, action);

        // 입력 필드에도 엔터키로 연결 가능하도록 설정
        if (txtUserName != null) txtUserName.addActionListener(action);
        if (txtIpAddress != null) txtIpAddress.addActionListener(action);
        if (txtPortNumber != null) txtPortNumber.addActionListener(action);
    }

    /**
     * 컴포넌트를 재귀적으로 탐색하여 버튼에 액션 리스너 설정
     *
     * @param panel 탐색할 패널
     * @param action 액션 리스너
     */
    private void findAndSetActionListener(java.awt.Container panel, Myaction action) {
        for (java.awt.Component comp : panel.getComponents()) {
            if (comp instanceof JButton && "btnConnect".equals(comp.getName())) {
                ((JButton) comp).addActionListener(action);
            } else if (comp instanceof java.awt.Container) {
                findAndSetActionListener((java.awt.Container) comp, action);
            }
        }
    }

    // ========================================
    // UI 헬퍼 메소드
    // ========================================

    /**
     * 레이블 추가 헬퍼 메소드
     *
     * @param panel 추가할 패널
     * @param text 레이블 텍스트
     * @param color 텍스트 색상
     * @param gbc GridBagConstraints
     */
    private void addLabel(JPanel panel, String text, Color color, java.awt.GridBagConstraints gbc) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(color);

        gbc.gridy++;
        gbc.insets = new java.awt.Insets(10, 20, 5, 20);
        panel.add(label, gbc);
    }

    /**
     * 텍스트 필드 추가 헬퍼 메소드
     *
     * @param panel 추가할 패널
     * @param text 기본 텍스트
     * @param bg 배경색
     * @param fg 전경색
     * @param gbc GridBagConstraints
     * @return 생성된 텍스트 필드
     */
    private JTextField addTextField(JPanel panel, String text, Color bg, Color fg,
                                    java.awt.GridBagConstraints gbc) {
        JTextField textField = new JTextField(text);
        textField.setHorizontalAlignment(SwingConstants.CENTER);
        textField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textField.setBackground(bg);
        textField.setForeground(fg);
        textField.setCaretColor(fg);
        textField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(80, 80, 80)),
                javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        gbc.gridy++;
        gbc.insets = new java.awt.Insets(0, 20, 5, 20);
        panel.add(textField, gbc);

        return textField;
    }

    // ========================================
    // 내부 클래스 - 배경 패널
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
                java.net.URL bgURL = getClass().getResource("/info/background.png");
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
            }
        }
    }

    // ========================================
    // 내부 클래스 - 테마 색상
    // ========================================

    /**
     * 테마 색상을 정의하는 클래스
     * 일관된 UI 디자인을 위한 색상 팔레트를 제공합니다.
     */
    private class ThemeColors {
        /**
         * 배경 색상 (매우 어두운 회색)
         */
        Color backgroundColor = new Color(18, 18, 18);

        /**
         * 패널 색상 (어두운 회색, 투명도 220)
         */
        Color panelColor = new Color(30, 30, 30, 220);

        /**
         * 텍스트 색상 (밝은 흰색)
         */
        Color textColor = new Color(240, 240, 240);

        /**
         * 강조 색상 (진한 빨간색 - 마피아 레드)
         */
        Color accentColor = new Color(192, 57, 43);

        /**
         * 입력 필드 배경색 (어두운 회색)
         */
        Color inputColor = new Color(45, 45, 45);
    }

    // ========================================
    // 내부 클래스 - 액션 리스너
    // ========================================

    /**
     * 버튼 클릭 및 엔터키 이벤트를 처리하는 액션 리스너
     * 사용자 입력을 검증하고 게임 화면으로 전환합니다.
     */
    class Myaction implements ActionListener {
        /**
         * 액션 이벤트 처리
         * 연결 버튼 클릭 또는 입력 필드에서 엔터키 입력 시 호출됩니다.
         *
         * @param e 액션 이벤트
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // 입력 값 가져오기
            String username = txtUserName.getText().trim();
            String ip_addr = txtIpAddress.getText().trim();
            String port_no = txtPortNumber.getText().trim();

            // 사용자 이름 검증
            if (!validateUsername(username)) {
                return;
            }

            // 게임 화면으로 전환
            connectToGame(username, ip_addr, port_no);
        }

        /**
         * 사용자 이름 검증
         *
         * @param username 사용자 이름
         * @return 유효하면 true, 그렇지 않으면 false
         */
        private boolean validateUsername(String username) {
            if (username.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "사용자 이름을 입력하세요!",
                    "입력 오류",
                    javax.swing.JOptionPane.WARNING_MESSAGE
                );
                return false;
            }
            return true;
        }

        /**
         * 게임 서버에 연결
         * 게임 화면(MafiaGameClientView)을 생성하고 현재 로그인 창을 숨깁니다.
         *
         * @param username 사용자 이름
         * @param ip_addr IP 주소
         * @param port_no 포트 번호
         */
        private void connectToGame(String username, String ip_addr, String port_no) {
            // 게임 뷰 생성 (자동으로 서버 연결 시도)
            MafiaGameClientView view = new MafiaGameClientView(username, ip_addr, port_no);

            // 로그인 창 숨기기
            setVisible(false);
        }
    }
}
