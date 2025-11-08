import javax.swing.*;
import java.awt.*;

public class LobbyPanel extends JPanel {

    public LobbyPanel(MainFrame frame, String nickname) {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 20));

        // 상단 네비게이션 바
        JPanel navBar = createNavBar(frame);
        add(navBar, BorderLayout.NORTH);

        // 왼쪽 사용자 정보 + 광고 패널
        JPanel leftPanel = createLeftPanel(nickname);
        add(leftPanel, BorderLayout.WEST);

        // 오른쪽 방 목록 패널
        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.CENTER);
    }

    // 🔹 1. 상단 네비게이션 바
    private JPanel createNavBar(MainFrame frame) {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        navBar.setBackground(new Color(30, 30, 30));

        // 로고
        JLabel logo = new JLabel(new ImageIcon("images/mafia42_logo.png"));
        navBar.add(logo);

        // 메뉴 버튼들
        String[] menus = {"로비", "내 정보", "덱 설정", "상점", "길드"};
        for (String m : menus) {
            JButton btn = new JButton(m);
            styleNavButton(btn);
            navBar.add(btn);
        }

        // 오른쪽 여백용 filler
        navBar.add(Box.createHorizontalStrut(200));

        // 채널명, 설정, 종료, 메신저
        JLabel channelLabel = new JLabel("랭크 채널");
        channelLabel.setForeground(Color.LIGHT_GRAY);
        navBar.add(channelLabel);

        JButton settingBtn = new JButton("⚙");
        JButton exitBtn = new JButton("❌");
        JButton messengerBtn = new JButton("💬");

        styleIconButton(settingBtn);
        styleIconButton(exitBtn);
        styleIconButton(messengerBtn);

        navBar.add(settingBtn);
        navBar.add(exitBtn);
        navBar.add(messengerBtn);

        return navBar;
    }

    private void styleNavButton(JButton btn) {
        btn.setBackground(new Color(50, 50, 50));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(90, 35));
    }

    private void styleIconButton(JButton btn) {
        btn.setBackground(new Color(45, 45, 45));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(40, 35));
    }

    // 🔹 2. 왼쪽 사용자 정보 + 광고
    private JPanel createLeftPanel(String nickname) {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBackground(new Color(25, 25, 25));

        // 상단 유저 정보
        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setBackground(new Color(25, 25, 25));
        userInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel profileImg = new JLabel(new ImageIcon("images/profile.png"));
        profileImg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(nickname, SwingConstants.CENTER);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel guildLabel = new JLabel("엔테라", SwingConstants.CENTER);
        guildLabel.setForeground(Color.GRAY);
        guildLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lunaLabel = new JLabel("루나: 123");
        JLabel rubleLabel = new JLabel("루블: 96,601");
        lunaLabel.setForeground(Color.LIGHT_GRAY);
        rubleLabel.setForeground(Color.LIGHT_GRAY);
        lunaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rubleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        userInfo.add(profileImg);
        userInfo.add(Box.createRigidArea(new Dimension(0, 10)));
        userInfo.add(nameLabel);
        userInfo.add(guildLabel);
        userInfo.add(Box.createRigidArea(new Dimension(0, 10)));
        userInfo.add(lunaLabel);
        userInfo.add(rubleLabel);
        userInfo.add(Box.createRigidArea(new Dimension(0, 15)));

        // 버튼 아이콘들
        String[] icons = {"인벤토리", "우편함", "선물함", "일일퀘스트", "마피아패스", "대부현황", "최후의 반론"};
        for (String icon : icons) {
            JButton btn = new JButton(icon);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setBackground(new Color(45, 45, 45));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setMaximumSize(new Dimension(250, 35));
            userInfo.add(btn);
            userInfo.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        // 하단 광고
        JLabel adLabel = new JLabel(new ImageIcon("images/ad_event.png"));
        adLabel.setHorizontalAlignment(SwingConstants.CENTER);
        adLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // 배치
        leftPanel.add(userInfo, BorderLayout.CENTER);
        leftPanel.add(adLabel, BorderLayout.SOUTH);

        return leftPanel;
    }

    // 🔹 3. 오른쪽 방 목록 영역
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(35, 35, 35));

        // 상단 버튼
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topButtons.setBackground(new Color(40, 40, 40));

        JButton createRoomBtn = new JButton("방 만들기");
        JButton quickJoinBtn = new JButton("빠른 입장");
        JButton viewWaitingBtn = new JButton("대기방만 보기");

        JButton[] btns = {createRoomBtn, quickJoinBtn, viewWaitingBtn};
        for (JButton b : btns) {
            b.setBackground(new Color(60, 60, 60));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(120, 35));
            topButtons.add(b);
        }

        // 방 목록
        JPanel roomList = new JPanel();
        roomList.setLayout(new BoxLayout(roomList, BoxLayout.Y_AXIS));
        roomList.setBackground(new Color(35, 35, 35));

        // 예시 방 5개 추가
        for (int i = 1; i <= 5; i++) {
            JPanel room = new JPanel(new BorderLayout());
            room.setMaximumSize(new Dimension(700, 60));
            room.setBackground(new Color(45, 45, 45));
            room.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel roomTitle = new JLabel(i + ". 마피아42 테스트방");
            roomTitle.setForeground(Color.WHITE);
            JLabel roomCount = new JLabel("5/9", SwingConstants.CENTER);
            roomCount.setForeground(Color.GREEN);

            room.add(roomTitle, BorderLayout.WEST);
            room.add(roomCount, BorderLayout.EAST);

            roomList.add(room);
            roomList.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JScrollPane scrollPane = new JScrollPane(roomList);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(35, 35, 35));

        rightPanel.add(topButtons, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        return rightPanel;
    }
}