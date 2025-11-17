import client.GameClient;
import protocol.Message;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 게임 플레이 화면 - 실제 게임이 진행되는 화면
 */
public class GamePlayPanel extends JPanel implements GameClient.MessageListener {
    private MainFrame frame;
    private GameClient client;
    private int roomId;
    private String roomName;
    private String myNickname;
    private String myRole; // "MAFIA", "CITIZEN", "DOCTOR", "POLICE"

    private JTextArea gameLogArea;
    private JTextField chatInput;
    private List<PlayerCard> playerCards;
    private JLabel myRoleLabel;
    private JLabel myIconLabel;
    private JPanel abilityPanel;

    // 플레이어 카드 (오른쪽 8개 슬롯)
    private class PlayerCard extends JPanel {
        private JLabel numberLabel;
        private JLabel iconLabel;
        private JLabel nameLabel;
        private JLabel roleLabel;
        private String playerName;
        private boolean isAlive = true;
        private int slotNumber;

        public PlayerCard(int slotNumber, String fontName) {
            this.slotNumber = slotNumber;
            setLayout(new BorderLayout(5, 5));
            setBackground(new Color(40, 40, 40));
            setPreferredSize(new Dimension(120, 140));
            setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // 상단 번호
            numberLabel = new JLabel(String.valueOf(slotNumber));
            numberLabel.setFont(new Font(fontName, Font.BOLD, 12));
            numberLabel.setForeground(Color.WHITE);
            numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
            numberLabel.setPreferredSize(new Dimension(0, 20));

            // 중앙 아이콘
            iconLabel = new JLabel("?");
            iconLabel.setFont(new Font(fontName, Font.BOLD, 50));
            iconLabel.setForeground(Color.GRAY);
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // 하단 정보 패널
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
            bottomPanel.setBackground(new Color(40, 40, 40));

            nameLabel = new JLabel("빈 슬롯");
            nameLabel.setFont(new Font(fontName, Font.BOLD, 12));
            nameLabel.setForeground(Color.GRAY);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            roleLabel = new JLabel("");
            roleLabel.setFont(new Font(fontName, Font.PLAIN, 10));
            roleLabel.setForeground(Color.YELLOW);
            roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            bottomPanel.add(nameLabel);
            bottomPanel.add(Box.createVerticalStrut(3));
            bottomPanel.add(roleLabel);

            add(numberLabel, BorderLayout.NORTH);
            add(iconLabel, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);

            // 클릭 이벤트 추가
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    onCardClicked();
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!isEmpty() && isAlive && canUseAbility()) {
                        setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2));
                }
            });
        }

        private void onCardClicked() {
            if (isEmpty() || !isAlive) {
                return;
            }

            if (playerName.equals(myNickname)) {
                JOptionPane.showMessageDialog(GamePlayPanel.this, "자기 자신은 선택할 수 없습니다.");
                return;
            }

            useAbilityOn(this);
        }

        private boolean canUseAbility() {
            return myRole.equals("MAFIA") || myRole.equals("DOCTOR") || myRole.equals("POLICE") ||
                   myRole.equals("MEDIUM") || myRole.equals("GANGSTER") ||
                   myRole.equals("REPORTER") || myRole.equals("DETECTIVE");
        }

        public void setPlayer(String nickname) {
            this.playerName = nickname;
            iconLabel.setText("👤");
            iconLabel.setForeground(Color.WHITE);
            nameLabel.setText(nickname);
            nameLabel.setForeground(Color.WHITE);
            setBackground(new Color(50, 50, 60));
            isAlive = true;
        }

        public void clearPlayer() {
            this.playerName = null;
            iconLabel.setText("?");
            iconLabel.setForeground(Color.GRAY);
            nameLabel.setText("빈 슬롯");
            nameLabel.setForeground(Color.GRAY);
            roleLabel.setText("");
            setBackground(new Color(40, 40, 40));
        }

        public void setDead() {
            isAlive = false;
            setBackground(new Color(60, 40, 40));
            iconLabel.setForeground(Color.DARK_GRAY);
            nameLabel.setForeground(Color.DARK_GRAY);
        }

        public void showRole(String role) {
            roleLabel.setText(role);
        }

        public boolean isEmpty() {
            return playerName == null;
        }

        public String getPlayerName() {
            return playerName;
        }
    }

    public GamePlayPanel(MainFrame frame, int roomId, String roomName, GameClient client,
                         String myNickname, String myRole) {
        this.frame = frame;
        this.roomId = roomId;
        this.roomName = roomName;
        this.client = client;
        this.myNickname = myNickname;
        this.myRole = myRole;
        this.playerCards = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 20));

        String fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        // 상단 헤더
        JPanel header = createHeader(fontName);
        add(header, BorderLayout.NORTH);

        // 메인 영역 (왼쪽: 내 정보, 중앙: 게임로그/채팅, 오른쪽: 플레이어 카드)
        JPanel mainArea = new JPanel(new BorderLayout(10, 0));
        mainArea.setBackground(new Color(20, 20, 20));
        mainArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 왼쪽: 내 정보
        JPanel leftPanel = createMyInfoPanel(fontName);
        leftPanel.setPreferredSize(new Dimension(250, 0));

        // 중앙: 게임 로그 & 채팅
        JPanel centerPanel = createCenterPanel(fontName);

        // 오른쪽: 플레이어 카드 (12개 슬롯 - 4행 3열)
        JPanel rightPanel = createPlayerCardsPanel(fontName);
        rightPanel.setPreferredSize(new Dimension(400, 0));

        mainArea.add(leftPanel, BorderLayout.WEST);
        mainArea.add(centerPanel, BorderLayout.CENTER);
        mainArea.add(rightPanel, BorderLayout.EAST);

        add(mainArea, BorderLayout.CENTER);

        // 메시지 리스너 등록
        client.addMessageListener(this);
    }

    // 상단 헤더
    private JPanel createHeader(String fontName) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 30));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("🎮 " + roomName + " - 1번째 밤");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(fontName, Font.BOLD, 18));

        JLabel timerLabel = new JLabel("⏱ 00:19");
        timerLabel.setForeground(new Color(255, 100, 100));
        timerLabel.setFont(new Font(fontName, Font.BOLD, 18));

        header.add(titleLabel, BorderLayout.WEST);
        header.add(timerLabel, BorderLayout.EAST);

        return header;
    }

    // 왼쪽: 내 정보 패널
    private JPanel createMyInfoPanel(String fontName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 30));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60), 2),
            BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));

        // 내 캐릭터 아이콘
        myIconLabel = new JLabel(getRoleIcon(myRole));
        myIconLabel.setFont(new Font(fontName, Font.BOLD, 100));
        myIconLabel.setForeground(Color.WHITE);
        myIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 내 역할
        myRoleLabel = new JLabel(getRoleKorean(myRole));
        myRoleLabel.setFont(new Font(fontName, Font.BOLD, 20));
        myRoleLabel.setForeground(getRoleColor(myRole));
        myRoleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 내 닉네임
        JLabel nameLabel = new JLabel(myNickname);
        nameLabel.setFont(new Font(fontName, Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 역할 설명
        JTextArea roleDesc = new JTextArea(getRoleDescription(myRole));
        roleDesc.setFont(new Font(fontName, Font.PLAIN, 12));
        roleDesc.setForeground(Color.LIGHT_GRAY);
        roleDesc.setBackground(new Color(35, 35, 40));
        roleDesc.setLineWrap(true);
        roleDesc.setWrapStyleWord(true);
        roleDesc.setEditable(false);
        roleDesc.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        roleDesc.setMaximumSize(new Dimension(220, 150));

        // 능력 사용 안내 텍스트
        abilityPanel = new JPanel();
        abilityPanel.setLayout(new BoxLayout(abilityPanel, BoxLayout.Y_AXIS));
        abilityPanel.setBackground(new Color(25, 25, 30));
        abilityPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (!myRole.equals("CITIZEN")) {
            JLabel instructionLabel = new JLabel("<html><center>👉 오른쪽 플레이어<br>카드를 클릭하여<br>능력을 사용하세요</center></html>");
            instructionLabel.setFont(new Font(fontName, Font.BOLD, 12));
            instructionLabel.setForeground(Color.YELLOW);
            instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            abilityPanel.add(instructionLabel);
        }

        panel.add(myIconLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(myRoleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(roleDesc);
        panel.add(Box.createVerticalStrut(20));
        panel.add(abilityPanel);

        return panel;
    }

    // 중앙: 게임 로그 & 채팅
    private JPanel createCenterPanel(String fontName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 30));

        // 게임 로그
        gameLogArea = new JTextArea();
        gameLogArea.setEditable(false);
        gameLogArea.setLineWrap(true);
        gameLogArea.setWrapStyleWord(true);
        gameLogArea.setBackground(new Color(40, 40, 40));
        gameLogArea.setForeground(Color.WHITE);
        gameLogArea.setFont(new Font(fontName, Font.PLAIN, 13));
        gameLogArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane logScroll = new JScrollPane(gameLogArea);
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // 입력 영역
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(new Color(30, 30, 30));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        chatInput = new JTextField();
        chatInput.setBackground(new Color(50, 50, 50));
        chatInput.setForeground(Color.WHITE);
        chatInput.setFont(new Font(fontName, Font.PLAIN, 14));
        chatInput.setCaretColor(Color.WHITE);
        chatInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JButton sendBtn = new JButton("▶");
        sendBtn.setBackground(new Color(80, 150, 200));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFont(new Font(fontName, Font.BOLD, 16));
        sendBtn.setFocusPainted(false);
        sendBtn.setPreferredSize(new Dimension(60, 35));
        sendBtn.addActionListener(e -> sendChat());

        chatInput.addActionListener(e -> sendChat());

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        panel.add(logScroll, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        // 초기 게임 로그 추가
        addGameLog("📢 당신의 직업은 " + getRoleKorean(myRole) + " 입니다.");
        addGameLog("📢 밤이 되었습니다.");

        return panel;
    }

    // 오른쪽: 플레이어 카드 그리드 (4행 2열 = 8슬롯)
    private JPanel createPlayerCardsPanel(String fontName) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        panel.setBackground(new Color(25, 25, 25));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 8개 슬롯 생성
        for (int i = 1; i <= 8; i++) {
            PlayerCard card = new PlayerCard(i, fontName);
            playerCards.add(card);
            panel.add(card);
        }

        return panel;
    }

    /**
     * 플레이어 카드 클릭 시 능력 사용
     */
    private void useAbilityOn(PlayerCard targetCard) {
        String targetName = targetCard.getPlayerName();

        switch (myRole) {
            case "MAFIA":
                int confirmKill = JOptionPane.showConfirmDialog(this,
                    targetName + "을(를) 죽이시겠습니까?",
                    "마피아 능력",
                    JOptionPane.YES_NO_OPTION);
                if (confirmKill == JOptionPane.YES_OPTION) {
                    addGameLog("🔪 " + targetName + "을(를) 죽이기로 선택했습니다.");
                }
                break;

            case "DOCTOR":
                int confirmHeal = JOptionPane.showConfirmDialog(this,
                    targetName + "을(를) 치료하시겠습니까?",
                    "의사 능력",
                    JOptionPane.YES_NO_OPTION);
                if (confirmHeal == JOptionPane.YES_OPTION) {
                    addGameLog("💊 " + targetName + "을(를) 치료하기로 선택했습니다.");
                }
                break;

            case "POLICE":
                // 경찰 - 마피아 여부 조사
                boolean isMafia = Math.random() < 0.3; // 임시
                if (isMafia) {
                    addGameLog("🔍 조사 결과: " + targetName + "은(는) 마피아입니다!");
                } else {
                    addGameLog("🔍 조사 결과: " + targetName + "은(는) 마피아가 아닙니다.");
                }
                break;

            case "MEDIUM":
                // 영매 - 직업 확인 및 성불
                String[] mediumOptions = {"직업 확인", "성불(능력 제거)"};
                int mediumChoice = JOptionPane.showOptionDialog(this,
                    targetName + "에게 어떤 능력을 사용하시겠습니까?",
                    "영매 능력",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    mediumOptions,
                    mediumOptions[0]);

                if (mediumChoice == 0) {
                    // 직업 확인 (임시로 랜덤)
                    String[] randomRoles = {"마피아", "의사", "경찰", "시민", "군인", "정치인"};
                    String role = randomRoles[(int)(Math.random() * randomRoles.length)];
                    addGameLog("🔮 " + targetName + "의 직업은 " + role + "입니다.");
                } else if (mediumChoice == 1) {
                    addGameLog("🔮 " + targetName + "에게 성불을 걸었습니다. 밤 능력이 제거됩니다.");
                }
                break;

            case "GANGSTER":
                // 건달 - 투표 방해 또는 마피아 방해
                String[] gangsterOptions = {"투표 불능", "마피아 처형 차단"};
                int gangsterChoice = JOptionPane.showOptionDialog(this,
                    targetName + "에게 어떤 능력을 사용하시겠습니까?",
                    "건달 능력",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    gangsterOptions,
                    gangsterOptions[0]);

                if (gangsterChoice == 0) {
                    addGameLog("🥊 " + targetName + "의 투표를 막았습니다.");
                } else if (gangsterChoice == 1) {
                    addGameLog("🥊 " + targetName + "에 대한 마피아 처형을 차단했습니다.");
                }
                break;

            case "REPORTER":
                // 기자 - 취재 (직업 확인 후 다음 날 공개)
                int confirmReport = JOptionPane.showConfirmDialog(this,
                    targetName + "을(를) 취재하시겠습니까?\n다음 날 직업이 공개됩니다.",
                    "기자 능력",
                    JOptionPane.YES_NO_OPTION);
                if (confirmReport == JOptionPane.YES_OPTION) {
                    addGameLog("📰 " + targetName + "을(를) 취재했습니다. 내일 공개됩니다.");
                }
                break;

            case "DETECTIVE":
                // 사립탐정 - 밤 행동 추적
                addGameLog("🕵️ " + targetName + "의 밤 행동을 추적합니다...");
                // 임시로 랜덤 결과
                String[] targets = {"아무도 지목하지 않음", "플레이어A를 지목함", "플레이어B를 지목함"};
                String result = targets[(int)(Math.random() * targets.length)];
                addGameLog("🕵️ 추적 결과: " + targetName + "은(는) " + result);
                break;
        }
    }

    private void sendChat() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            client.sendChatMessage(message);
            chatInput.setText("");
        }
    }

    private void addGameLog(String message) {
        gameLogArea.append(message + "\n");
        gameLogArea.setCaretPosition(gameLogArea.getDocument().getLength());
    }

    // 역할별 아이콘
    private String getRoleIcon(String role) {
        switch (role) {
            // 마피아 팀
            case "MAFIA": return "🔪";
            case "SPY": return "🕶️";
            case "HOSTESS": return "💃";
            case "THIEF": return "🦹";
            case "BEAST_MAN": return "🐺";
            // 시민 팀
            case "POLICE": return "🔍";
            case "DOCTOR": return "💊";
            case "SOLDIER": return "🎖️";
            case "POLITICIAN": return "📜";
            case "MEDIUM": return "🔮";
            case "LOVER": return "💕";
            case "REPORTER": return "📰";
            case "DETECTIVE": return "🕵️";
            case "GHOUL": return "👻";
            case "MARTYR": return "💣";
            case "PRIEST": return "⛪";
            case "GANGSTER": return "🥊";
            default: return "👤";
        }
    }

    // 역할 한글 이름
    private String getRoleKorean(String role) {
        switch (role) {
            // 마피아 팀
            case "MAFIA": return "마피아";
            case "SPY": return "스파이";
            case "HOSTESS": return "마담";
            case "THIEF": return "도둑";
            case "BEAST_MAN": return "짐승인간";
            // 시민 팀
            case "POLICE": return "경찰";
            case "DOCTOR": return "의사";
            case "SOLDIER": return "군인";
            case "POLITICIAN": return "정치인";
            case "MEDIUM": return "영매";
            case "LOVER": return "연인";
            case "REPORTER": return "기자";
            case "DETECTIVE": return "사립탐정";
            case "GHOUL": return "도굴꾼";
            case "MARTYR": return "테러리스트";
            case "PRIEST": return "성직자";
            case "GANGSTER": return "건달";
            default: return "시민";
        }
    }

    // 역할 색상
    private Color getRoleColor(String role) {
        switch (role) {
            // 마피아 팀 - 빨간색 계열
            case "MAFIA": return new Color(255, 100, 100);
            case "SPY": return new Color(220, 80, 80);
            case "HOSTESS": return new Color(255, 120, 150);
            case "THIEF": return new Color(200, 70, 70);
            case "BEAST_MAN": return new Color(180, 60, 60);
            // 시민 팀 - 파란색/초록색 계열
            case "POLICE": return new Color(100, 150, 255);
            case "DOCTOR": return new Color(100, 255, 150);
            case "SOLDIER": return new Color(255, 200, 100);
            case "POLITICIAN": return new Color(255, 215, 0);
            case "MEDIUM": return new Color(180, 100, 255);
            case "LOVER": return new Color(255, 150, 200);
            case "REPORTER": return new Color(100, 200, 255);
            case "DETECTIVE": return new Color(200, 150, 100);
            case "GHOUL": return new Color(150, 100, 200);
            case "MARTYR": return new Color(255, 100, 50);
            case "PRIEST": return new Color(200, 200, 255);
            case "GANGSTER": return new Color(150, 150, 150);
            default: return Color.WHITE;
        }
    }

    // 역할 설명
    private String getRoleDescription(String role) {
        switch (role) {
            // 마피아 팀
            case "MAFIA":
                return "밤에 한 명을 죽일 수 있습니다. 시민 수가 마피아 수와 같거나 적어지면 승리합니다.";
            case "SPY":
                return "밤마다 1명을 골라 직업/팀 정보를 확인할 수 있습니다. 마피아팀입니다.";
            case "HOSTESS":
                return "밤에 1명을 유혹하여 다음 낮 투표를 못하게 하거나 밤 능력 사용을 막습니다. 마피아팀입니다.";
            case "THIEF":
                return "밤에 1명의 직업 능력을 훔쳐 자신이 사용합니다. 대상은 시민이 됩니다. 마피아팀입니다.";
            case "BEAST_MAN":
                return "마피아에게 공격당하면 길들여져 마피아팀으로 전환됩니다. 처음엔 시민팀입니다.";
            // 시민 팀
            case "POLICE":
                return "밤에 한 명을 조사하여 마피아인지 확인할 수 있습니다. 시민팀입니다.";
            case "DOCTOR":
                return "밤에 한 명을 치료하여 마피아의 공격으로부터 보호할 수 있습니다. 시민팀입니다.";
            case "SOLDIER":
                return "마피아의 공격을 1회 자동으로 방어합니다. 시민팀입니다.";
            case "POLITICIAN":
                return "낮 투표에서 2표로 계산됩니다. 시민팀입니다.";
            case "MEDIUM":
                return "죽은 사람의 대화를 보고, 밤에 1명의 직업을 확인하면 그 사람은 성불됩니다. 시민팀입니다.";
            case "LOVER":
                return "2인 1세트. 서로 밤에 대화 가능하고, 한 명이 죽으면 다른 한 명이 대신 죽습니다. 시민팀입니다.";
            case "REPORTER":
                return "밤에 조사하여 다음 날 아침 그 사람의 직업을 모두에게 공개합니다. 시민팀입니다.";
            case "DETECTIVE":
                return "밤에 1명을 골라 그 사람이 밤에 누구를 지목했는지 추적합니다. 시민팀입니다.";
            case "GHOUL":
                return "밤에 마피아에게 죽은 사람의 직업을 훔쳐 자기 직업으로 바꿉니다. 시민팀입니다.";
            case "MARTYR":
                return "낮에 투표로 죽을 때 미리 골라둔 1명과 동반자살합니다. 시민팀입니다.";
            case "PRIEST":
                return "게임 중 1번, 죽은 플레이어 한 명을 부활시킬 수 있습니다. 시민팀입니다.";
            case "GANGSTER":
                return "밤에 1명을 협박해서 다음 낮에 투표를 못하게 합니다. 시민팀입니다.";
            default:
                return "마피아를 찾아 투표로 제거하세요. 모든 마피아를 제거하면 시민팀이 승리합니다.";
        }
    }

    /**
     * 플레이어 목록 업데이트
     */
    public void updatePlayerList(String playerData) {
        // 모든 카드 초기화
        for (PlayerCard card : playerCards) {
            card.clearPlayer();
        }

        // 플레이어 데이터 파싱
        if (playerData != null && !playerData.isEmpty()) {
            String[] players = playerData.split("\\|");
            for (int i = 0; i < players.length && i < playerCards.size(); i++) {
                playerCards.get(i).setPlayer(players[i]);
            }
        }
    }

    @Override
    public void onMessageReceived(Message msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {
                case PLAYER_LIST:
                    updatePlayerList(msg.getData());
                    break;

                case CHAT_MESSAGE:
                    String[] parts = msg.getData().split("\\|", 2);
                    if (parts.length == 2) {
                        addGameLog("[" + parts[0] + "] " + parts[1]);
                    }
                    break;

                case SYSTEM_MESSAGE:
                    addGameLog("📢 " + msg.getData());
                    break;

                default:
                    break;
            }
        });
    }
}
