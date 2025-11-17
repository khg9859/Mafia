import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.*;
import database.DatabaseConnection;

public class MyInfoPanel extends JPanel {
    private MainFrame frame;
    private String nickname;
    private int userId;
    private String username;
    private String fontName;
    
    // 사용자 정보
    private int level = 2;
    private int exp = 450;
    private int maxExp = 1000;
    private String bio = "";
    private int totalGames = 0;
    private int wins = 0;

    public MyInfoPanel(MainFrame frame, String nickname, int userId) {
        this.frame = frame;
        this.nickname = nickname;
        this.userId = userId;
        
        fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 20));

        // 데이터베이스에서 사용자 정보 로드
        loadUserInfo();

        // 상단 네비게이션 바
        JPanel navBar = createNavBar();
        add(navBar, BorderLayout.NORTH);

        // 메인 컨텐츠 (왼쪽: 프로필, 중앙: 자기소개, 오른쪽: 통계)
        JPanel mainContent = createMainContent();
        add(mainContent, BorderLayout.CENTER);
    }

    private void loadUserInfo() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT nickname, username FROM user WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                nickname = rs.getString("nickname");
                username = rs.getString("username");
            }
            // level, exp, bio 등은 기본값 사용
            bio = "안녕하세요✌";
        } catch (SQLException e) {
            System.out.println("사용자 정보 로드 실패: " + e.getMessage());
        }
    }

    private JPanel createNavBar() {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        navBar.setBackground(new Color(30, 30, 30));

        JLabel logo = new JLabel("내 정보");
        logo.setForeground(new Color(255, 100, 100));
        logo.setFont(new Font(fontName, Font.BOLD, 24));
        navBar.add(logo);

        navBar.add(Box.createHorizontalStrut(600));

        String[] menus = {"로비", "내 정보", "상점", "길드"};
        for (String m : menus) {
            JButton btn = new JButton(m);
            styleNavButton(btn);
            if (m.equals("로비")) {
                btn.addActionListener(e -> returnToLobby());
            } else if (m.equals("상점")) {
                btn.addActionListener(e -> showShop());
            } else if (m.equals("길드")) {
                btn.addActionListener(e -> showGuild());
            }
            navBar.add(btn);
        }

        return navBar;
    }

    private void styleNavButton(JButton btn) {
        btn.setFont(new Font(fontName, Font.BOLD, 14));
        btn.setBackground(new Color(50, 50, 50));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setPreferredSize(new Dimension(90, 35));
    }

    private JPanel createMainContent() {
        JPanel mainContent = new JPanel(new BorderLayout(20, 0));
        mainContent.setBackground(new Color(20, 20, 20));
        mainContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 왼쪽: 프로필 + 자기소개
        JPanel leftPanel = createLeftPanel();
        mainContent.add(leftPanel, BorderLayout.WEST);

        // 중앙: 직업 카드
        JPanel centerPanel = createCenterPanel();
        mainContent.add(centerPanel, BorderLayout.CENTER);

        // 오른쪽: 통계
        JPanel rightPanel = createRightPanel();
        mainContent.add(rightPanel, BorderLayout.EAST);

        return mainContent;
    }

    // 왼쪽: 프로필 + 자기소개
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBackground(new Color(30, 30, 30));
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 상단: 프로필 정보
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(new Color(30, 30, 30));

        // 프로필 사진
        JPanel profileBox = new JPanel(new BorderLayout());
        profileBox.setMaximumSize(new Dimension(200, 200));
        profileBox.setBackground(new Color(50, 50, 50));
        profileBox.setBorder(new LineBorder(new Color(100, 100, 100), 2));

        // 마피아 이미지 로드
        JLabel profileIcon;
        try {
            ImageIcon originalIcon = new ImageIcon("/Users/yongju/Desktop/test_hong/Mafia/images/info/default.png");
            if (originalIcon.getIconWidth() > 0) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                profileIcon = new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
            } else {
                throw new Exception("이미지 로드 실패");
            }
        } catch (Exception e) {
            profileIcon = new JLabel("?", SwingConstants.CENTER);
            profileIcon.setFont(new Font(fontName, Font.PLAIN, 80));
            profileIcon.setForeground(Color.LIGHT_GRAY);
        }

        JLabel levelBadge = new JLabel("Lv " + level, SwingConstants.CENTER);
        levelBadge.setFont(new Font(fontName, Font.BOLD, 14));
        levelBadge.setForeground(Color.WHITE);
        levelBadge.setBackground(new Color(200, 50, 50));
        levelBadge.setOpaque(true);
        levelBadge.setBorder(new EmptyBorder(5, 10, 5, 10));

        profileBox.add(profileIcon, BorderLayout.CENTER);
        profileBox.add(levelBadge, BorderLayout.SOUTH);

        // 닉네임
        JLabel nameLabel = new JLabel(nickname, SwingConstants.CENTER);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font(fontName, Font.BOLD, 20));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setBorder(new EmptyBorder(15, 0, 10, 0));

        // 경험치 바
        JPanel expPanel = new JPanel();
        expPanel.setLayout(new BoxLayout(expPanel, BoxLayout.Y_AXIS));
        expPanel.setBackground(new Color(30, 30, 30));
        expPanel.setMaximumSize(new Dimension(200, 50));

        JLabel expLabel = new JLabel("Lv " + level);
        expLabel.setForeground(Color.LIGHT_GRAY);
        expLabel.setFont(new Font(fontName, Font.PLAIN, 12));
        expLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar expBar = new JProgressBar(0, maxExp);
        expBar.setValue(exp);
        expBar.setStringPainted(true);
        expBar.setString(exp + " / " + maxExp);
        expBar.setForeground(new Color(255, 200, 100));
        expBar.setBackground(new Color(50, 50, 50));
        expBar.setFont(new Font(fontName, Font.PLAIN, 11));
        expBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        expPanel.add(expLabel);
        expPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        expPanel.add(expBar);

        profilePanel.add(profileBox);
        profilePanel.add(nameLabel);
        profilePanel.add(expPanel);

        // 하단: 자기소개
        JPanel bioPanel = new JPanel(new BorderLayout());
        bioPanel.setBackground(new Color(30, 30, 30));
        bioPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel bioTitle = new JLabel("자기 소개");
        bioTitle.setForeground(Color.WHITE);
        bioTitle.setFont(new Font(fontName, Font.BOLD, 14));
        bioTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JTextArea bioArea = new JTextArea(bio);
        bioArea.setFont(new Font(fontName, Font.PLAIN, 12));
        bioArea.setBackground(new Color(40, 40, 40));
        bioArea.setForeground(Color.WHITE);
        bioArea.setCaretColor(Color.WHITE);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        bioArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        bioArea.setRows(3);
        bioArea.setEditable(false);

        JScrollPane bioScroll = new JScrollPane(bioArea);
        bioScroll.setBorder(new LineBorder(new Color(60, 60, 60), 1));
        bioScroll.setPreferredSize(new Dimension(200, 100));

        bioPanel.add(bioTitle, BorderLayout.NORTH);
        bioPanel.add(bioScroll, BorderLayout.CENTER);

        leftPanel.add(profilePanel, BorderLayout.NORTH);
        leftPanel.add(bioPanel, BorderLayout.CENTER);

        return leftPanel;
    }

    // 중앙: 직업 카드
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(30, 30, 30));
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("직업 숙련도");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(fontName, Font.BOLD, 16));
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // 직업 카드 그리드
        JPanel jobGrid = new JPanel(new GridLayout(3, 5, 10, 10));
        jobGrid.setBackground(new Color(30, 30, 30));

        String[] jobs = {
            "시민", "경찰", "의사", "자경단원", "영매",
            "군인", "정치인", "마피아", "건달", "기자",
            "탐정", "도굴꾼", "테러리스트", "성직자"
        };

        for (String job : jobs) {
            jobGrid.add(createJobCard(job, 0, 550));
        }

        JScrollPane scrollPane = new JScrollPane(jobGrid);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        centerPanel.add(titleLabel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        return centerPanel;
    }

    // 직업 카드 생성
    private JPanel createJobCard(String jobName, int jobLevel, int maxLevel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(110, 140));
        card.setBackground(new Color(40, 40, 40));
        card.setBorder(new LineBorder(new Color(80, 80, 80), 2));

        // 직업 이미지
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(110, 100));
        imagePanel.setBackground(new Color(50, 50, 50));
        
        // 직업별 이미지 파일 매핑
        String imagePath = getJobImagePath(jobName);
        JLabel imageLabel;
        
        try {
            ImageIcon originalIcon = new ImageIcon(imagePath);
            if (originalIcon.getIconWidth() > 0) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(110, 100, Image.SCALE_SMOOTH);
                imageLabel = new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
            } else {
                throw new Exception("이미지 로드 실패");
            }
        } catch (Exception e) {
            imageLabel = new JLabel("?", SwingConstants.CENTER);
            imageLabel.setFont(new Font(fontName, Font.BOLD, 40));
            imageLabel.setForeground(Color.GRAY);
        }
        
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        // 직업 정보
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(40, 40, 40));
        infoPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel nameLabel = new JLabel(jobName, SwingConstants.CENTER);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font(fontName, Font.BOLD, 13));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel levelLabel = new JLabel(jobLevel + "/" + maxLevel, SwingConstants.CENTER);
        levelLabel.setForeground(Color.LIGHT_GRAY);
        levelLabel.setFont(new Font(fontName, Font.PLAIN, 11));
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(levelLabel);

        card.add(imagePanel, BorderLayout.CENTER);
        card.add(infoPanel, BorderLayout.SOUTH);

        return card;
    }

    // 직업명에 따른 이미지 경로 반환
    private String getJobImagePath(String jobName) {
        String basePath = "/Users/yongju/Desktop/test_hong/Mafia/images/info/";
        switch (jobName) {
            case "시민": return basePath + "simin.png";
            case "경찰": return basePath + "police.png";
            case "의사": return basePath + "doctor.png";
            case "자경단원": return basePath + "jagyeong.png";
            case "영매": return basePath + "yeongmae.png";
            case "군인": return basePath + "soldier.png";
            case "정치인": return basePath + "jeongchi.png";
            case "마피아": return basePath + "mafia.png";
            case "건달": return basePath + "geondal.png";
            case "기자": return basePath + "gija.png";
            case "탐정": return basePath + "tamjung.png";
            case "도굴꾼": return basePath + "dogul.png";
            case "테러리스트": return basePath + "list.png";
            case "성직자": return basePath + "seongzik.png";
            default: return "";
        }
    }

    // 오른쪽: 통계 (승률, RP, CP, 명성)
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setBackground(new Color(30, 30, 30));
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("통계");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(fontName, Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightPanel.add(titleLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 승률
        double winRate = totalGames > 0 ? (wins * 100.0 / totalGames) : 0.0;
        rightPanel.add(createStatRow("승률", String.format("%.1f%%", winRate)));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // RP (랭크 포인트)
        rightPanel.add(createStatRow("RP", "UNRANKED"));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // CP (캐릭터 포인트)
        rightPanel.add(createStatRow("CP", "0"));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 명성
        rightPanel.add(createStatRow("명성", "0"));

        return rightPanel;
    }

    private JPanel createStatRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(260, 50));
        row.setBackground(new Color(40, 40, 40));
        row.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setForeground(Color.LIGHT_GRAY);
        lblLabel.setFont(new Font(fontName, Font.PLAIN, 14));

        JLabel lblValue = new JLabel(value);
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font(fontName, Font.BOLD, 16));

        row.add(lblLabel, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.EAST);

        return row;
    }

    private void returnToLobby() {
        frame.showLobby(nickname, userId, username);
    }

    private void showShop() {
        ShopPanel shopPanel = new ShopPanel(frame, nickname, userId, username);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(shopPanel);
        frame.revalidate();
        frame.repaint();
    }

    private void showGuild() {
        GuildPanel guildPanel = new GuildPanel(frame, nickname, userId, username);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(guildPanel);
        frame.revalidate();
        frame.repaint();
    }
}
