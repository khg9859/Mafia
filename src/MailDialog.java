import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class MailDialog extends JDialog {
    private String fontName;
    private JPanel mailListPanel;
    private String currentTab = "받은 편지함";

    public MailDialog(JFrame parent) {
        super(parent, "우편함", true);
        
        fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        setSize(700, 600);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(40, 40, 40));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 상단: 제목 + 닫기 버튼
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 중앙: 탭 + 검색 + 메일 목록
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 하단: 보상 정보
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("우편함");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(fontName, Font.BOLD, 20));

        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font(fontName, Font.PLAIN, 18));
        closeBtn.setBackground(new Color(60, 60, 60));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        closeBtn.addActionListener(e -> dispose());

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(closeBtn, BorderLayout.EAST);

        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 40));

        // 탭 패널
        JPanel tabPanel = createTabPanel();
        panel.add(tabPanel, BorderLayout.NORTH);

        // 검색 + 필터 패널
        JPanel searchPanel = createSearchPanel();
        panel.add(searchPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTabPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(new EmptyBorder(0, 0, 15, 0));

        String[] tabs = {"받은 편지함", "보낸 편지함"};
        
        for (int i = 0; i < tabs.length; i++) {
            String tab = tabs[i];
            JButton tabBtn = new JButton(tab);
            tabBtn.setFont(new Font(fontName, Font.BOLD, 15));
            tabBtn.setForeground(i == 0 ? new Color(255, 200, 100) : Color.LIGHT_GRAY);
            tabBtn.setBackground(new Color(40, 40, 40));
            tabBtn.setFocusPainted(false);
            tabBtn.setBorderPainted(false);
            tabBtn.setContentAreaFilled(false);
            
            if (i == 0) {
                tabBtn.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(255, 200, 100)));
            }

            tabBtn.addActionListener(e -> {
                currentTab = tab;
                // 모든 탭 버튼 초기화
                for (Component c : panel.getComponents()) {
                    if (c instanceof JButton) {
                        JButton btn = (JButton) c;
                        btn.setForeground(Color.LIGHT_GRAY);
                        btn.setBorder(null);
                    }
                }
                // 선택된 탭 강조
                tabBtn.setForeground(new Color(255, 200, 100));
                tabBtn.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(255, 200, 100)));
                
                loadMailsByTab(tab);
            });

            panel.add(tabBtn);
        }

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 40));

        // 상단: 메일 개수 + 검색창
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(new Color(40, 40, 40));
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // 메일 개수
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        countPanel.setBackground(new Color(40, 40, 40));

        JLabel countLabel = new JLabel("0/42");
        countLabel.setForeground(Color.WHITE);
        countLabel.setFont(new Font(fontName, Font.BOLD, 14));

        JButton addBtn = new JButton("+");
        addBtn.setFont(new Font(fontName, Font.BOLD, 16));
        addBtn.setBackground(new Color(200, 150, 50));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setPreferredSize(new Dimension(30, 30));
        addBtn.setBorder(BorderFactory.createEmptyBorder());

        countPanel.add(countLabel);
        countPanel.add(addBtn);

        // 검색창 + 알림 버튼
        JPanel searchBarPanel = new JPanel(new BorderLayout(10, 0));
        searchBarPanel.setBackground(new Color(40, 40, 40));

        JTextField searchField = new JTextField("내편지를 검색해주세요");
        searchField.setFont(new Font(fontName, Font.PLAIN, 13));
        searchField.setBackground(new Color(50, 50, 50));
        searchField.setForeground(Color.GRAY);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(new EmptyBorder(8, 15, 8, 15));

        JButton notificationBtn = new JButton("🔔");
        notificationBtn.setFont(new Font(fontName, Font.PLAIN, 18));
        notificationBtn.setBackground(new Color(50, 50, 50));
        notificationBtn.setForeground(Color.WHITE);
        notificationBtn.setFocusPainted(false);
        notificationBtn.setPreferredSize(new Dimension(40, 35));
        notificationBtn.setBorder(BorderFactory.createEmptyBorder());

        searchBarPanel.add(searchField, BorderLayout.CENTER);
        searchBarPanel.add(notificationBtn, BorderLayout.EAST);

        topPanel.add(countPanel, BorderLayout.WEST);
        topPanel.add(searchBarPanel, BorderLayout.CENTER);

        // 필터 버튼
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(new Color(40, 40, 40));
        filterPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JComboBox<String> filterCombo = new JComboBox<>(new String[]{"최신순"});
        filterCombo.setFont(new Font(fontName, Font.PLAIN, 12));
        filterCombo.setBackground(new Color(50, 50, 50));
        filterCombo.setForeground(Color.WHITE);

        JButton receiveAllBtn = new JButton("내려받기");
        receiveAllBtn.setFont(new Font(fontName, Font.BOLD, 13));
        receiveAllBtn.setBackground(new Color(200, 150, 50));
        receiveAllBtn.setForeground(Color.WHITE);
        receiveAllBtn.setFocusPainted(false);
        receiveAllBtn.setBorder(new EmptyBorder(8, 20, 8, 20));

        filterPanel.add(filterCombo);
        filterPanel.add(receiveAllBtn);

        // 메일 목록
        mailListPanel = new JPanel();
        mailListPanel.setLayout(new BoxLayout(mailListPanel, BoxLayout.Y_AXIS));
        mailListPanel.setBackground(new Color(40, 40, 40));

        // 더미 데이터 - 빈 메일함
        JLabel emptyLabel = new JLabel("메일이 없습니다", SwingConstants.CENTER);
        emptyLabel.setForeground(Color.GRAY);
        emptyLabel.setFont(new Font(fontName, Font.PLAIN, 16));
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mailListPanel.add(Box.createRigidArea(new Dimension(0, 100)));
        mailListPanel.add(emptyLabel);

        JScrollPane scrollPane = new JScrollPane(mailListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(40, 40, 40));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(filterPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // 보상 정보
        String[] rewards = {"내역 없음", "출석 무료", "🪙"};
        String[] counts = {"0", "0", "0"};

        for (int i = 0; i < rewards.length; i++) {
            JPanel rewardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            rewardPanel.setBackground(new Color(40, 40, 40));

            JLabel iconLabel = new JLabel(rewards[i]);
            iconLabel.setForeground(Color.LIGHT_GRAY);
            iconLabel.setFont(new Font(fontName, Font.PLAIN, 14));

            JLabel countLabel = new JLabel(counts[i]);
            countLabel.setForeground(Color.WHITE);
            countLabel.setFont(new Font(fontName, Font.BOLD, 14));

            rewardPanel.add(iconLabel);
            rewardPanel.add(countLabel);
            panel.add(rewardPanel);
        }

        return panel;
    }

    private void loadMailsByTab(String tab) {
        mailListPanel.removeAll();

        // 탭별 더미 데이터 (현재는 모두 비어있음)
        JLabel emptyLabel = new JLabel("메일이 없습니다", SwingConstants.CENTER);
        emptyLabel.setForeground(Color.GRAY);
        emptyLabel.setFont(new Font(fontName, Font.PLAIN, 16));
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mailListPanel.add(Box.createRigidArea(new Dimension(0, 100)));
        mailListPanel.add(emptyLabel);

        mailListPanel.revalidate();
        mailListPanel.repaint();
    }
}
