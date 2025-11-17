import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ForumDialog extends JDialog {
    private String fontName;
    private JPanel postListPanel;

    public ForumDialog(JFrame parent) {
        super(parent, "최후의 변론", true);
        
        fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        setSize(900, 700);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(40, 40, 40));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 상단: 제목 + 닫기 버튼
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 중앙: 탭 + 게시글 목록
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 하단: 글쓰기 버튼
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("최후의 변론");
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

        // 서버 선택
        JPanel serverPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        serverPanel.setBackground(new Color(40, 40, 40));

        JLabel serverLabel = new JLabel("로컬 서버");
        serverLabel.setForeground(Color.LIGHT_GRAY);
        serverLabel.setFont(new Font(fontName, Font.PLAIN, 13));

        serverPanel.add(serverLabel);

        // 탭 패널
        JPanel tabPanel = createTabPanel();

        // 게시글 목록
        postListPanel = new JPanel();
        postListPanel.setLayout(new BoxLayout(postListPanel, BoxLayout.Y_AXIS));
        postListPanel.setBackground(new Color(40, 40, 40));

        // 더미 게시글 데이터
        loadPosts();

        JScrollPane scrollPane = new JScrollPane(postListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(40, 40, 40));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(40, 40, 40));
        centerPanel.add(serverPanel, BorderLayout.NORTH);
        centerPanel.add(tabPanel, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTabPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(new EmptyBorder(10, 0, 15, 0));

        String[] tabs = {"뉴토픽", "핫토픽", "보관함"};
        
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
                
                loadPosts();
            });

            panel.add(tabBtn);
        }

        return panel;
    }

    private void loadPosts() {
        postListPanel.removeAll();

        // 더미 게시글 데이터
        String[][] posts = {
            {"게임 같이할사람", "10895679", "190517", "43982", "online"},
            {"친구구해요", "1", "3", "1", "online"},
            {"뜰사람", "1", "2", "1", "online"},
            {"레벨10이하", "1", "0", "0", "offline"}
        };

        for (String[] post : posts) {
            postListPanel.add(createPostItem(post[0], post[1], post[2], post[3], post[4]));
            postListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        postListPanel.revalidate();
        postListPanel.repaint();
    }

    private JPanel createPostItem(String title, String comments, String likes, String dislikes, String status) {
        JPanel item = new JPanel(new BorderLayout(15, 0));
        item.setMaximumSize(new Dimension(850, 80));
        item.setBackground(new Color(50, 50, 50));
        item.setBorder(new LineBorder(new Color(60, 60, 60), 1));

        // 왼쪽: 프로필 이미지
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setPreferredSize(new Dimension(60, 60));
        profilePanel.setBackground(new Color(70, 70, 70));
        profilePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel profileIcon = new JLabel("👤", SwingConstants.CENTER);
        profileIcon.setFont(new Font(fontName, Font.PLAIN, 30));
        profilePanel.add(profileIcon, BorderLayout.CENTER);

        // 중앙: 제목 + 통계
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(50, 50, 50));
        infoPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(fontName, Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        statsPanel.setBackground(new Color(50, 50, 50));
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 댓글 수
        JLabel commentLabel = new JLabel("💬 " + comments);
        commentLabel.setForeground(Color.LIGHT_GRAY);
        commentLabel.setFont(new Font(fontName, Font.PLAIN, 12));

        // 좋아요
        JLabel likeLabel = new JLabel("👍 " + likes);
        likeLabel.setForeground(Color.LIGHT_GRAY);
        likeLabel.setFont(new Font(fontName, Font.PLAIN, 12));

        // 싫어요
        JLabel dislikeLabel = new JLabel("👎 " + dislikes);
        dislikeLabel.setForeground(Color.LIGHT_GRAY);
        dislikeLabel.setFont(new Font(fontName, Font.PLAIN, 12));

        statsPanel.add(commentLabel);
        statsPanel.add(likeLabel);
        statsPanel.add(dislikeLabel);

        infoPanel.add(titleLabel);
        infoPanel.add(statsPanel);

        // 오른쪽: 온라인 상태
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(50, 50, 50));
        statusPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel statusIcon = new JLabel("●");
        statusIcon.setForeground(status.equals("online") ? new Color(0, 255, 0) : Color.GRAY);
        statusIcon.setFont(new Font(fontName, Font.PLAIN, 20));
        statusPanel.add(statusIcon, BorderLayout.CENTER);

        item.add(profilePanel, BorderLayout.WEST);
        item.add(infoPanel, BorderLayout.CENTER);
        item.add(statusPanel, BorderLayout.EAST);

        return item;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 15));
        panel.setBackground(new Color(40, 40, 40));

        JButton writeBtn = new JButton("✏");
        writeBtn.setFont(new Font(fontName, Font.PLAIN, 24));
        writeBtn.setBackground(new Color(60, 60, 60));
        writeBtn.setForeground(Color.WHITE);
        writeBtn.setFocusPainted(false);
        writeBtn.setPreferredSize(new Dimension(60, 60));
        writeBtn.setBorder(BorderFactory.createEmptyBorder());

        panel.add(writeBtn);

        return panel;
    }
}
