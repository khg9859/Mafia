import client.GameClient;
import database.RoomDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class LobbyPanel extends JPanel {
    private MainFrame frame;
    private GameClient client;
    private String nickname;
    private int userId;

    public LobbyPanel(MainFrame frame, String nickname, GameClient client) {
        this.frame = frame;
        this.client = client;
        this.nickname = nickname;
        this.userId = client != null ? client.getUserId() : 1; // 임시로 1 사용

        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 20));

        // ✅ OS별 폰트 자동 감지
        String fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        // 상단 네비게이션 바
        JPanel navBar = createNavBar(frame, fontName);
        add(navBar, BorderLayout.NORTH);

        // 왼쪽 사용자 정보 + 광고 패널
        JPanel leftPanel = createLeftPanel(nickname, fontName);
        add(leftPanel, BorderLayout.WEST);

        // 오른쪽 방 목록 패널
        JPanel rightPanel = createRightPanel(fontName);
        add(rightPanel, BorderLayout.CENTER);
    }

    // 🔹 1. 상단 네비게이션 바
    private JPanel createNavBar(MainFrame frame, String fontName) {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        navBar.setBackground(new Color(30, 30, 30));

        // ✅ 로고 (Retina 대응)
        JLabel logo = new JLabel(scaleIcon("Mafia/images/mafia42_logo.png", 180, 50));
        navBar.add(logo);

        // 메뉴 버튼들
        String[] menus = {"로비", "내 정보", "상점", "길드"};
        for (String m : menus) {
            JButton btn = new JButton(m);
            styleNavButton(btn, fontName);

            // 내 정보 버튼 클릭 이벤트
            if (m.equals("내 정보")) {
                btn.addActionListener(e -> showMyInfo());
            } else if (m.equals("상점")) {
                btn.addActionListener(e -> showShop());
            } else if (m.equals("길드")) {
                btn.addActionListener(e -> showGuild());
            }

            navBar.add(btn);
        }

        navBar.add(Box.createHorizontalStrut(200));

        JLabel channelLabel = new JLabel("랭크 채널");
        channelLabel.setForeground(Color.LIGHT_GRAY);
        channelLabel.setFont(new Font(fontName, Font.PLAIN, 14));
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

    private void styleNavButton(JButton btn, String fontName) {
        btn.setFont(new Font(fontName, Font.BOLD, 14));
        btn.setBackground(new Color(50, 50, 50));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setPreferredSize(new Dimension(90, 35));
    }

    private void styleIconButton(JButton btn) {
        btn.setBackground(new Color(45, 45, 45));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setPreferredSize(new Dimension(40, 35));
    }

    // 🔹 2. 왼쪽 사용자 정보 + 광고
    private JPanel createLeftPanel(String nickname, String fontName) {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBackground(new Color(25, 25, 25));

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setBackground(new Color(25, 25, 25));
        userInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel profileImg = new JLabel(scaleIcon("Mafia/images/profile.png", 120, 120));
        profileImg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(nickname, SwingConstants.CENTER);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font(fontName, Font.BOLD, 18));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel guildLabel = new JLabel("엔테라", SwingConstants.CENTER);
        guildLabel.setForeground(Color.GRAY);
        guildLabel.setFont(new Font(fontName, Font.PLAIN, 13));
        guildLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lunaLabel = new JLabel("루나: 123");
        JLabel rubleLabel = new JLabel("루블: 96,601");
        lunaLabel.setForeground(Color.LIGHT_GRAY);
        rubleLabel.setForeground(Color.LIGHT_GRAY);
        lunaLabel.setFont(new Font(fontName, Font.PLAIN, 13));
        rubleLabel.setFont(new Font(fontName, Font.PLAIN, 13));
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

        String[] icons = {"인벤토리", "우편함","일일퀘스트","최후의 반론"};
        for (String icon : icons) {
            JButton btn = new JButton(icon);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setBackground(new Color(45, 45, 45));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            btn.setFont(new Font(fontName, Font.PLAIN, 13));
            btn.setMaximumSize(new Dimension(250, 35));
            btn.setBorder(BorderFactory.createEmptyBorder());

            // 버튼 이벤트
            if (icon.equals("인벤토리")) {
                btn.addActionListener(e -> showInventory());
            } else if (icon.equals("우편함")) {
                btn.addActionListener(e -> showMail());
            } else if (icon.equals("일일퀘스트")) {
                btn.addActionListener(e -> showDailyQuest());
            } else if (icon.equals("최후의 반론")) {
                btn.addActionListener(e -> showForum());
            }

            userInfo.add(btn);
            userInfo.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JLabel adLabel = new JLabel(scaleIcon("Mafia/images/ad_event.png", 250, 120));
        adLabel.setHorizontalAlignment(SwingConstants.CENTER);
        adLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        leftPanel.add(userInfo, BorderLayout.CENTER);
        leftPanel.add(adLabel, BorderLayout.SOUTH);

        return leftPanel;
    }

    // 🔹 3. 오른쪽 방 목록 영역
    private JPanel createRightPanel(String fontName) {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(35, 35, 35));

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topButtons.setBackground(new Color(40, 40, 40));

        JButton createRoomBtn = new JButton("방 만들기");
        JButton quickJoinBtn = new JButton("빠른 입장");
        JButton viewWaitingBtn = new JButton("대기방만 보기");

        JButton[] btns = {createRoomBtn, quickJoinBtn, viewWaitingBtn};
        for (JButton b : btns) {
            b.setBackground(new Color(60, 60, 60));
            b.setForeground(Color.WHITE);
            b.setFont(new Font(fontName, Font.BOLD, 13));
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setPreferredSize(new Dimension(120, 35));
            topButtons.add(b);
        }

        JPanel roomList = new JPanel();
        roomList.setLayout(new BoxLayout(roomList, BoxLayout.Y_AXIS));
        roomList.setBackground(new Color(35, 35, 35));

        // ✅ DB에서 실제 방 목록 불러오기
        List<RoomDAO.Room> rooms = RoomDAO.getAllRooms();
        for (RoomDAO.Room roomData : rooms) {
            JPanel room = new JPanel(new BorderLayout());
            room.setMaximumSize(new Dimension(700, 60));
            room.setBackground(new Color(45, 45, 45));
            room.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            room.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel roomTitle = new JLabel(roomData.roomId + ". " + roomData.roomName);
            roomTitle.setForeground(Color.WHITE);
            roomTitle.setFont(new Font(fontName, Font.PLAIN, 14));

            String countColor = roomData.currentPlayers >= roomData.maxPlayers ? "RED" : "GREEN";
            JLabel roomCount = new JLabel(roomData.currentPlayers + "/" + roomData.maxPlayers, SwingConstants.CENTER);
            roomCount.setForeground(countColor.equals("RED") ? Color.RED : Color.GREEN);
            roomCount.setFont(new Font(fontName, Font.PLAIN, 13));

            room.add(roomTitle, BorderLayout.WEST);
            room.add(roomCount, BorderLayout.EAST);

            // ✅ 방 클릭 이벤트 - 방 입장
            final int roomId = roomData.roomId;
            final String roomName = roomData.roomName;
            room.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    joinRoom(roomId, roomName);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    room.setBackground(new Color(60, 60, 60));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    room.setBackground(new Color(45, 45, 45));
                }
            });

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

    // ✅ Retina 대응 이미지 스케일링
    private ImageIcon scaleIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /**
     * 방 입장 처리
     */
    private void joinRoom(int roomId, String roomName) {
        System.out.println("🚪 방 입장 시도: " + roomName + " (ID: " + roomId + ")");

        // 서버에 방 입장 요청
        client.joinRoom(roomId);

        // 방 입장 성공 시 GameRoomPanel로 이동 (서버 응답 대기)
        // 실제로는 GameClient의 메시지 리스너에서 ROOM_JOIN_SUCCESS를 받아야 하지만
        // 간단하게 바로 이동
        frame.showGameRoom(roomId, roomName);
    }

    /**
     * 내 정보 화면으로 이동
     */
    private void showMyInfo() {
        MyInfoPanel myInfoPanel = new MyInfoPanel(frame, nickname, userId);
        frame.switchPanel(myInfoPanel);
    }

    /**
     * 상점 화면으로 이동
     */
    private void showShop() {
        String username = client != null ? client.getUsername() : "guest";
        ShopPanel shopPanel = new ShopPanel(frame, nickname, userId, username);
        frame.switchPanel(shopPanel);
    }

    /**
     * 길드 화면으로 이동
     */
    private void showGuild() {
        String username = client != null ? client.getUsername() : "guest";
        GuildPanel guildPanel = new GuildPanel(frame, nickname, userId, username);
        frame.switchPanel(guildPanel);
    }

    /**
     * 인벤토리 다이얼로그 표시
     */
    private void showInventory() {
        InventoryDialog dialog = new InventoryDialog(frame);
        dialog.setVisible(true);
    }

    /**
     * 우편함 다이얼로그 표시
     */
    private void showMail() {
        MailDialog dialog = new MailDialog(frame);
        dialog.setVisible(true);
    }

    /**
     * 일일 퀘스트 다이얼로그 표시
     */
    private void showDailyQuest() {
        DailyQuestDialog dialog = new DailyQuestDialog(frame);
        dialog.setVisible(true);
    }

    /**
     * 최후의 변론 다이얼로그 표시
     */
    private void showForum() {
        ForumDialog dialog = new ForumDialog(frame);
        dialog.setVisible(true);
    }
}
