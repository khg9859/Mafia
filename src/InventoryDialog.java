import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class InventoryDialog extends JDialog {
    private String fontName;
    private JPanel itemPanel;

    public InventoryDialog(JFrame parent) {
        super(parent, "인벤토리", true);
        
        fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        setSize(600, 500);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(40, 40, 40));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 상단: 제목 + 닫기 버튼
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 중앙: 탭 + 아이템 그리드
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("인벤토리");
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

        // 아이템 그리드
        itemPanel = new JPanel(new GridLayout(0, 6, 10, 10));
        itemPanel.setBackground(new Color(40, 40, 40));
        itemPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        // 더미 아이템 데이터 (전체 탭)
        loadAllItems();

        JScrollPane scrollPane = new JScrollPane(itemPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(40, 40, 40));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTabPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        String[] tabs = {"전체", "명예", "소셜", "기타", "이벤트"};
        
        for (int i = 0; i < tabs.length; i++) {
            String tab = tabs[i];
            JButton tabBtn = new JButton(tab);
            tabBtn.setFont(new Font(fontName, Font.BOLD, 14));
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
                
                // 탭에 따라 아이템 로드
                loadItemsByTab(tab);
            });

            panel.add(tabBtn);
        }

        return panel;
    }

    private void loadAllItems() {
        itemPanel.removeAll();
        
        // 더미 아이템 (전체)
        String[][] items = {
            {"정복자", "1"},
            {"정복자", "1"},
            {"텐서", "42"}
        };

        for (String[] item : items) {
            itemPanel.add(createItemCard(item[0], item[1]));
        }

        itemPanel.revalidate();
        itemPanel.repaint();
    }

    private void loadItemsByTab(String tab) {
        itemPanel.removeAll();

        // 탭별 더미 데이터
        if (tab.equals("전체")) {
            loadAllItems();
        } else if (tab.equals("명예")) {
            String[][] items = {
                {"정복자", "1"},
                {"정복자", "1"}
            };
            for (String[] item : items) {
                itemPanel.add(createItemCard(item[0], item[1]));
            }
        } else if (tab.equals("소셜")) {
            // 소셜 아이템 없음
        } else if (tab.equals("기타")) {
            String[][] items = {
                {"텐서", "42"}
            };
            for (String[] item : items) {
                itemPanel.add(createItemCard(item[0], item[1]));
            }
        } else if (tab.equals("이벤트")) {
            // 이벤트 아이템 없음
        }

        itemPanel.revalidate();
        itemPanel.repaint();
    }

    private JPanel createItemCard(String itemName, String count) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(80, 80));
        card.setBackground(new Color(50, 50, 50));
        card.setBorder(new LineBorder(new Color(80, 80, 80), 2));

        // 아이템 이미지 (더미)
        JLabel imageLabel = new JLabel("📦", SwingConstants.CENTER);
        imageLabel.setFont(new Font(fontName, Font.PLAIN, 30));

        // 수량 표시
        if (!count.equals("1")) {
            JLabel countLabel = new JLabel("x" + count);
            countLabel.setForeground(Color.WHITE);
            countLabel.setFont(new Font(fontName, Font.BOLD, 11));
            countLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            countLabel.setBorder(new EmptyBorder(0, 0, 5, 5));
            
            JPanel overlay = new JPanel(new BorderLayout());
            overlay.setOpaque(false);
            overlay.add(countLabel, BorderLayout.SOUTH);
            
            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.setOpaque(false);
            imagePanel.add(imageLabel, BorderLayout.CENTER);
            imagePanel.add(overlay, BorderLayout.SOUTH);
            
            card.add(imagePanel, BorderLayout.CENTER);
        } else {
            card.add(imageLabel, BorderLayout.CENTER);
        }

        return card;
    }
}
