import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChannelSelectPanel extends JPanel {
    private Image backgroundImage;

    public ChannelSelectPanel(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // ✅ Retina 대응 이미지 스케일링
        ImageIcon bgIcon = new ImageIcon("images/mafia42_left.png");
        Image scaledBg = bgIcon.getImage().getScaledInstance(880, 720, Image.SCALE_SMOOTH);
        backgroundImage = scaledBg;

        // -------------------- 오른쪽 패널 --------------------
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(25, 25, 25));
        rightPanel.setPreferredSize(new Dimension(400, 0));

        // ✅ 로고 이미지 (Retina 대응)
        JLabel logo = new JLabel(scaleIcon("images/mafia42_logo.png", 260, 80));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(60, 0, 40, 0));
        rightPanel.add(logo);

        // ✅ 운영체제별 폰트 자동 선택
        String fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        // ✅ 채널 버튼 목록
        String[] channels = {"초보 채널", "1 채널", "2 채널", "3 채널", "20세 이상 채널", "랭크 채널"};
        for (String ch : channels) {
            JButton channelButton = new JButton(ch);
            channelButton.setFont(new Font(fontName, Font.BOLD, 16));
            channelButton.setForeground(Color.WHITE);
            channelButton.setBackground(new Color(45, 45, 45));
            channelButton.setFocusPainted(false);
            channelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            channelButton.setMaximumSize(new Dimension(300, 45));
            channelButton.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
            channelButton.setOpaque(true);

            // ✅ 클릭 시 로그인 화면으로 전환
            channelButton.addActionListener((ActionEvent e) -> frame.switchTo("login"));

            rightPanel.add(channelButton);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        add(rightPanel, BorderLayout.EAST);
    }

    // ✅ Retina 대응 이미지 스케일링 유틸리티
    private ImageIcon scaleIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    // -------------------- 왼쪽 배경 그리기 --------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth() - 400, getHeight(), this);
    }
}