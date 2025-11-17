import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class DailyQuestDialog extends JDialog {
    private String fontName;

    public DailyQuestDialog(JFrame parent) {
        super(parent, "일일 퀘스트", true);
        
        fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        setSize(800, 400);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(50, 50, 50));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 상단: 제목 + 닫기 버튼
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 중앙: 퀘스트 카드
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(50, 50, 50));
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("일일 퀘스트");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(fontName, Font.BOLD, 22));

        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font(fontName, Font.PLAIN, 20));
        closeBtn.setBackground(new Color(70, 70, 70));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        closeBtn.addActionListener(e -> dispose());

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(closeBtn, BorderLayout.EAST);

        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(50, 50, 50));

        // 퀘스트 카드
        JPanel questCard = createQuestCard();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 100, 0, 100);

        panel.add(questCard, gbc);

        return panel;
    }

    private JPanel createQuestCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(60, 60, 60));
        card.setBorder(new LineBorder(new Color(80, 80, 80), 2));
        card.setPreferredSize(new Dimension(600, 250));

        // 상단: 퀘스트 제목 + 새로고침
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(60, 60, 60));
        topPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(new Color(60, 60, 60));


        JLabel questTitle = new JLabel("수집 퀘스트");
        questTitle.setForeground(Color.WHITE);
        questTitle.setFont(new Font(fontName, Font.BOLD, 16));

        JLabel questStatus = new JLabel("예비 임무");
        questStatus.setForeground(new Color(200, 150, 50));
        questStatus.setFont(new Font(fontName, Font.BOLD, 12));
        questStatus.setBackground(new Color(80, 60, 20));
        questStatus.setOpaque(true);
        questStatus.setBorder(new EmptyBorder(3, 8, 3, 8));

        titlePanel.add(questTitle);
        titlePanel.add(questStatus);

        JButton refreshBtn = new JButton("🔄");
        refreshBtn.setFont(new Font(fontName, Font.PLAIN, 18));
        refreshBtn.setBackground(new Color(70, 70, 70));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(refreshBtn, BorderLayout.EAST);

        // 중앙: 퀘스트 설명
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(60, 60, 60));
        centerPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JLabel descLabel = new JLabel("마피아로 2회 승리하세요.", SwingConstants.CENTER);
        descLabel.setForeground(Color.WHITE);
        descLabel.setFont(new Font(fontName, Font.PLAIN, 15));

        centerPanel.add(descLabel, BorderLayout.CENTER);

        // 하단: 보상 + 버튼
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(60, 60, 60));
        bottomPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // 보상 정보
        JPanel rewardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        rewardPanel.setBackground(new Color(60, 60, 60));

        // 코인 보상
        JPanel coinReward = createRewardItem("🪙", "1");
        rewardPanel.add(coinReward);

        // EXP 보상
        JPanel expReward = createRewardItem("EXP", "1100");
        rewardPanel.add(expReward);

        // 실버 보상
        JPanel silverReward = createRewardItem("⚪", "1000");
        rewardPanel.add(silverReward);

        // 보상 받기 버튼
        JButton rewardBtn = new JButton("보상 받기");
        rewardBtn.setFont(new Font(fontName, Font.BOLD, 14));
        rewardBtn.setBackground(new Color(200, 150, 50));
        rewardBtn.setForeground(Color.WHITE);
        rewardBtn.setFocusPainted(false);
        rewardBtn.setBorder(new EmptyBorder(10, 30, 10, 30));

        bottomPanel.add(rewardPanel, BorderLayout.WEST);
        bottomPanel.add(rewardBtn, BorderLayout.EAST);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createRewardItem(String icon, String amount) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBackground(new Color(60, 60, 60));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font(fontName, Font.PLAIN, 18));
        iconLabel.setForeground(Color.WHITE);

        JLabel amountLabel = new JLabel(amount);
        amountLabel.setFont(new Font(fontName, Font.BOLD, 15));
        amountLabel.setForeground(Color.WHITE);

        panel.add(iconLabel);
        panel.add(amountLabel);

        return panel;
    }
}
