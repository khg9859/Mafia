import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ShopPanel extends JPanel {
    private MainFrame frame;
    private String nickname;
    private int userId;
    private String username;
    private String fontName;

    public ShopPanel(MainFrame frame, String nickname, int userId, String username) {
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

        JLabel logo = new JLabel("상점");
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

        // 왼쪽: 카테고리
        JPanel leftPanel = createCategoryPanel();
        mainContent.add(leftPanel, BorderLayout.WEST);

        // 중앙: 아이템 그리드
        JPanel centerPanel = createItemPanel();
        mainContent.add(centerPanel, BorderLayout.CENTER);

        // 오른쪽: 패키지 배너
        JPanel rightPanel = createPackagePanel();
        mainContent.add(rightPanel, BorderLayout.EAST);

        return mainContent;
    }

    // 왼쪽: 카테고리
    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(100, 0));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(new EmptyBorder(20, 10, 20, 10));

        String[] categories = {"캐릭", "스킨", "명예", "경험", "일반"};
        
        for (String category : categories) {
            JButton btn = new JButton(category);
            btn.setFont(new Font(fontName, Font.BOLD, 14));
            btn.setBackground(new Color(40, 40, 40));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(80, 40));
            btn.setBorder(new LineBorder(new Color(60, 60, 60), 1));
            
            panel.add(btn);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return panel;
    }

    // 중앙: 아이템 그리드
    private JPanel createItemPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 아이템 그리드
        JPanel itemGrid = new JPanel(new GridLayout(0, 4, 15, 15));
        itemGrid.setBackground(new Color(30, 30, 30));

        // 더미 아이템 데이터
        String[][] items = {
            {"렉스 토큰", "5", "coin"},
            {"할로윈 묘비석", "2,500", "silver"},
            {"스케치북", "150", "coin"},
            {"밀서", "750", "coin"},
            {"경험치권", "42", "coin"},
            {"청색 염색약", "10,000", "silver"},
            {"녹색 염색약", "10,000", "silver"},
            {"빨간 염색약", "10,000", "silver"},
            {"금색 염색약", "25,000", "silver"},
            {"백색 염색약", "5,000", "silver"},
            {"무지개 염색약", "50,000", "silver"},
            {"신문지", "2,500", "silver"}
        };

        for (String[] item : items) {
            itemGrid.add(createItemCard(item[0], item[1], item[2]));
        }

        JScrollPane scrollPane = new JScrollPane(itemGrid);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // 아이템 카드 생성
    private JPanel createItemCard(String itemName, String price, String currency) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(130, 180));
        card.setBackground(new Color(40, 40, 40));
        card.setBorder(new LineBorder(new Color(80, 80, 80), 2));

        // 아이템 이미지
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(130, 120));
        imagePanel.setBackground(new Color(50, 50, 50));

        // 아이템별 이미지 로드
        String imagePath = getItemImagePath(itemName);
        JLabel imageLabel;
        
        try {
            ImageIcon originalIcon = new ImageIcon(imagePath);
            if (originalIcon.getIconWidth() > 0) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(120, 110, Image.SCALE_SMOOTH);
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

        // 아이템 정보
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(40, 40, 40));
        infoPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel nameLabel = new JLabel(itemName, SwingConstants.CENTER);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font(fontName, Font.BOLD, 12));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 가격 패널
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        pricePanel.setBackground(new Color(40, 40, 40));

        JLabel currencyIcon = new JLabel(currency.equals("coin") ? "🪙" : "⚪");
        currencyIcon.setFont(new Font(fontName, Font.PLAIN, 14));

        JLabel priceLabel = new JLabel(price);
        priceLabel.setForeground(currency.equals("coin") ? new Color(255, 200, 100) : Color.LIGHT_GRAY);
        priceLabel.setFont(new Font(fontName, Font.BOLD, 13));

        pricePanel.add(currencyIcon);
        pricePanel.add(priceLabel);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(pricePanel);

        card.add(imagePanel, BorderLayout.CENTER);
        card.add(infoPanel, BorderLayout.SOUTH);

        return card;
    }

    // 오른쪽: 패키지 배너
    private JPanel createPackagePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("패키지");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(fontName, Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 패키지 배너들
        String[][] packages = {
            {"42팩팩 업그레이드\n패키지", "5,000원"},
            {"PC버전 특가\n패키지!", "10,000원"},
            {"데이터팩구\n패키지!", "11,000원"}
        };

        Color[] colors = {
            new Color(180, 120, 60),
            new Color(60, 120, 180),
            new Color(120, 60, 180)
        };

        for (int i = 0; i < packages.length; i++) {
            panel.add(createPackageBanner(packages[i][0], packages[i][1], colors[i]));
            panel.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        return panel;
    }

    // 패키지 배너 생성
    private JPanel createPackageBanner(String title, String price, Color bgColor) {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setPreferredSize(new Dimension(310, 150));
        banner.setMaximumSize(new Dimension(310, 150));
        banner.setBackground(bgColor);
        banner.setBorder(new LineBorder(bgColor.darker(), 2));

        JLabel titleLabel = new JLabel("<html>" + title.replace("\n", "<br>") + "</html>");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(fontName, Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(15, 15, 10, 15));

        JLabel priceLabel = new JLabel(price);
        priceLabel.setForeground(Color.WHITE);
        priceLabel.setFont(new Font(fontName, Font.BOLD, 16));
        priceLabel.setBorder(new EmptyBorder(10, 15, 15, 15));
        priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        banner.add(titleLabel, BorderLayout.NORTH);
        banner.add(priceLabel, BorderLayout.SOUTH);

        return banner;
    }

    private void showMyInfo() {
        MyInfoPanel myInfoPanel = new MyInfoPanel(frame, nickname, userId);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(myInfoPanel);
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

    // 아이템명에 따른 이미지 경로 반환
    private String getItemImagePath(String itemName) {
        String basePath = "/Users/yongju/Desktop/test_hong/Mafia/images/Shop/";
        switch (itemName) {
            case "렉스 토큰": return basePath + "token.png";
            case "할로윈 묘비석": return basePath + "myobe.png";
            case "스케치북": return basePath + "sketch.png";
            case "밀서": return basePath + "milseo.png";
            case "경험치권": return basePath + "EXP.png";
            case "청색 염색약": return basePath + "blue.png";
            case "녹색 염색약": return basePath + "green.png";
            case "빨간 염색약": return basePath + "red.png";
            case "금색 염색약": return basePath + "yellow.png";
            case "백색 염색약": return basePath + "white.png";
            case "무지개 염색약": return basePath + "rainbow.png";
            case "신문지": return basePath + "letter.png";
            default: return "";
        }
    }
}
