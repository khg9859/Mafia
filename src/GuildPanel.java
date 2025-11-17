import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class GuildPanel extends JPanel {
    private MainFrame frame;
    private String nickname;
    private int userId;
    private String username;
    private String fontName;

    public GuildPanel(MainFrame frame, String nickname, int userId, String username) {
        this.frame = frame;
        this.nickname = nickname;
        this.userId = userId;
        this.username = username;

        fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 20));

        // 상단 네비게이션 바
        JPanel navBar = createNavBar();
        add(navBar, BorderLayout.NORTH);

        // 메인 컨텐츠
        JPanel mainContent = createMainContent();
        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createNavBar() {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        navBar.setBackground(new Color(30, 30, 30));

        JLabel logo = new JLabel("길드");
        logo.setForeground(new Color(255, 200, 100));
        logo.setFont(new Font(fontName, Font.BOLD, 24));
        navBar.add(logo);

        navBar.add(Box.createHorizontalStrut(600));

        String[] menus = {"로비", "내 정보", "상점", "길드"};
        for (String m : menus) {
            JButton btn = new JButton(m);
            styleNavButton(btn);
            if (m.equals("로비")) {
                btn.addActionListener(e -> frame.showLobby(nickname, userId, username));
            } else if (m.equals("내 정보")) {
                btn.addActionListener(e -> showMyInfo());
            } else if (m.equals("상점")) {
                btn.addActionListener(e -> showShop());
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
        JPanel mainContent = new JPanel(new BorderLayout(0, 0));
        mainContent.setBackground(new Color(20, 20, 20));

        // 왼쪽: 길드 목록
        JPanel leftPanel = createGuildListPanel();
        mainContent.add(leftPanel, BorderLayout.WEST);

        // 오른쪽: 길드 정보 (가입 안 함)
        JPanel rightPanel = createNoGuildPanel();
        mainContent.add(rightPanel, BorderLayout.CENTER);

        return mainContent;
    }

    // 왼쪽: 길드 목록
    private JPanel createGuildListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(600, 0));
        panel.setBackground(new Color(25, 25, 25));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 검색창
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(new Color(25, 25, 25));
        searchPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JTextField searchField = new JTextField("길드명을 입력해주세요.");
        searchField.setFont(new Font(fontName, Font.PLAIN, 14));
        searchField.setBackground(new Color(40, 40, 40));
        searchField.setForeground(Color.GRAY);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(new EmptyBorder(10, 15, 10, 15));

        searchPanel.add(searchField, BorderLayout.CENTER);

        // 길드 목록
        JPanel guildListPanel = new JPanel();
        guildListPanel.setLayout(new BoxLayout(guildListPanel, BoxLayout.Y_AXIS));
        guildListPanel.setBackground(new Color(25, 25, 25));

        // 더미 길드 데이터
        String[][] guilds = {
            {"단체", "즐겜 ㄱ", "02.28.27 입장"},
            {"스타박스", "같이 할 사람\n한명만 더 구합니다 구합니다 구합니다 구합니다 구합니다 구합니다 구합니다 구합니다 구합니다", "04.13.33 입장"},
            {"산업", "무섭다\n온라 무섭다 기자도 무섭다 카즈도 무섭다 이게 뭐야 왜 이렇게 무섭냐", "23.22.30 입장"},
            {"사이코 길드", "WD 길드 문의 좀 생겼는데 신청 가능하신가요? 다음 모든 일정 공지사항 바랍니다", "12.21.14 입장"},
            {"ㅎㅇ", "마피아 공방\n실력만 있으면 다 괜찮습니다", "22.12.01 입장"},
            {"위인", "구독하면 대단 (하단기준하면 2000경험치+1 6일 최소 5일치+1 급 3000 🔴 시청 🟣 티 단체 ", "16.22.00 입장"},
            {"자우", "", "00.34.45 입장"}
        };

        for (String[] guild : guilds) {
            guildListPanel.add(createGuildItem(guild[0], guild[1], guild[2]));
            guildListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JScrollPane scrollPane = new JScrollPane(guildListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // 길드 아이템 생성
    private JPanel createGuildItem(String guildName, String description, String time) {
        JPanel item = new JPanel(new BorderLayout(15, 0));
        item.setMaximumSize(new Dimension(560, 80));
        item.setBackground(new Color(35, 35, 35));
        item.setBorder(new LineBorder(new Color(50, 50, 50), 1));

        // 길드 아이콘
        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setPreferredSize(new Dimension(60, 60));
        iconPanel.setBackground(new Color(50, 50, 50));
        iconPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel iconLabel = new JLabel("🛡", SwingConstants.CENTER);
        iconLabel.setFont(new Font(fontName, Font.PLAIN, 30));
        iconPanel.add(iconLabel, BorderLayout.CENTER);

        // 길드 정보
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(35, 35, 35));
        infoPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel nameLabel = new JLabel(guildName);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font(fontName, Font.BOLD, 15));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea descArea = new JTextArea(description);
        descArea.setFont(new Font(fontName, Font.PLAIN, 12));
        descArea.setForeground(Color.LIGHT_GRAY);
        descArea.setBackground(new Color(35, 35, 35));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setRows(2);
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(descArea);

        // 시간
        JLabel timeLabel = new JLabel(time);
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setFont(new Font(fontName, Font.PLAIN, 11));
        timeLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        item.add(iconPanel, BorderLayout.WEST);
        item.add(infoPanel, BorderLayout.CENTER);
        item.add(timeLabel, BorderLayout.EAST);

        return item;
    }

    // 오른쪽: 가입한 길드 없음
    private JPanel createNoGuildPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(20, 20, 20));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(20, 20, 20));

        // GO MAFIA 이미지 (텍스트로 대체)
        JLabel imageLabel = new JLabel("🎯", SwingConstants.CENTER);
        imageLabel.setFont(new Font(fontName, Font.PLAIN, 100));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel goLabel = new JLabel("GO MAFIA", SwingConstants.CENTER);
        goLabel.setForeground(new Color(200, 50, 50));
        goLabel.setFont(new Font(fontName, Font.BOLD, 36));
        goLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(imageLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(goLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel message1 = new JLabel("가입한 길드가 없습니다.", SwingConstants.CENTER);
        message1.setForeground(Color.WHITE);
        message1.setFont(new Font(fontName, Font.BOLD, 18));
        message1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message2 = new JLabel("길드에서 친구들을 초대하고 함께 게임을 즐겨보세요.", SwingConstants.CENTER);
        message2.setForeground(Color.LIGHT_GRAY);
        message2.setFont(new Font(fontName, Font.PLAIN, 14));
        message2.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(message1);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(message2);

        panel.add(contentPanel);

        return panel;
    }

    private void showMyInfo() {
        MyInfoPanel myInfoPanel = new MyInfoPanel(frame, nickname, userId);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(myInfoPanel);
        frame.revalidate();
        frame.repaint();
    }

    private void showShop() {
        ShopPanel shopPanel = new ShopPanel(frame, nickname, userId, username);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(shopPanel);
        frame.revalidate();
        frame.repaint();
    }
}
