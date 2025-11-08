import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChannelSelectPanel extends JPanel {

    private Image backgroundImage;

    public ChannelSelectPanel(MainFrame frame) {
        setLayout(new BorderLayout());

        // 왼쪽 배경 이미지 (이미지 경로 수정!)
        backgroundImage = new ImageIcon("images/mafia42_left.png").getImage();

        // -------------------- 오른쪽 영역 --------------------
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(25, 25, 25));
        rightPanel.setPreferredSize(new Dimension(400, 0)); // 오른쪽 폭 고정

        // 로고
        JLabel logo = new JLabel(new ImageIcon("images/mafia42_logo.png"));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(60, 0, 40, 0));
        rightPanel.add(logo);

        // 채널 버튼 패널
        String[] channels = {"초보 채널", "1 채널", "2 채널", "3 채널", "20세 이상 채널", "랭크 채널"};

        for (String ch : channels) {
            JButton channelButton = new JButton(ch);
            channelButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
            channelButton.setForeground(Color.WHITE);
            channelButton.setBackground(new Color(45, 45, 45));
            channelButton.setFocusPainted(false);
            channelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            channelButton.setMaximumSize(new Dimension(300, 45));
            channelButton.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));

            // 클릭 시 로그인화면 전환
            channelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.switchTo("login");
                }
            });

            rightPanel.add(channelButton);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 15))); // 간격
        }

        add(rightPanel, BorderLayout.EAST);
    }

    // -------------------- 왼쪽 이미지 영역 그리기 --------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth() - 400, getHeight(), this);
    }
}