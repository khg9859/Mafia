// MafiaGameServer.java
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class MafiaGameServer extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    JTextArea textArea;
    private JTextField txtPortNumber;
    private JButton btnGameStart;

    private ServerSocket socket;
    private Socket client_socket;
    private Vector<UserService> UserVec = new Vector<>();
    
    // 게임 상태 변수
    private boolean gameStarted = false;
    private String gamePhase = "WAITING"; // WAITING, NIGHT, DAY, VOTE, RESULT
    private int dayCount = 0;
    private Map<String, Boolean> aliveStatus = new HashMap<>();
    private Map<String, Integer> voteCount = new HashMap<>();
    private Map<String, String> nightActions = new HashMap<>(); // 밤 행동 저장

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MafiaGameServer frame = new MafiaGameServer();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public MafiaGameServer() {
        setTitle("Mafia Game Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 500);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(12, 10, 410, 340);
        contentPane.add(scrollPane);

        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        JLabel lblNewLabel = new JLabel("Port Number");
        lblNewLabel.setBounds(12, 360, 100, 30);
        contentPane.add(lblNewLabel);

        txtPortNumber = new JTextField();
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setText("30000");
        txtPortNumber.setBounds(120, 360, 302, 30);
        contentPane.add(txtPortNumber);
        txtPortNumber.setColumns(10);

        JButton btnServerStart = new JButton("Server Start");
        btnServerStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
                } catch (NumberFormatException | IOException e1) {
                    e1.printStackTrace();
                }
                AppendText("Mafia Game Server Running...");
                btnServerStart.setText("Server Running");
                btnServerStart.setEnabled(false);
                txtPortNumber.setEnabled(false);
                btnGameStart.setEnabled(true);
                
                AcceptServer accept_server = new AcceptServer();
                accept_server.start();
            }
        });
        btnServerStart.setBounds(12, 400, 410, 40);
        contentPane.add(btnServerStart);

        btnGameStart = new JButton("Game Start (Need 4+ players)");
        btnGameStart.setEnabled(false);
        btnGameStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (UserVec.size() < 4) {
                    AppendText("최소 4명 이상 필요합니다!");
                    WriteAll("SYSTEM: 최소 4명 이상 필요합니다.\n");
                    return;
                }
                startGame();
            }
        });
        btnGameStart.setBounds(12, 445, 410, 40);
        contentPane.add(btnGameStart);
    }

    // 게임 시작
    private void startGame() {
        if (gameStarted) {
            AppendText("게임이 이미 시작되었습니다!");
            return;
        }

        gameStarted = true;
        dayCount = 0;
        btnGameStart.setEnabled(false);
        
        AppendText("===== 게임 시작! =====");
        AppendText("참가자 수: " + UserVec.size());
        
        // 역할 배정
        assignRoles();
        
        // 모든 플레이어를 살아있는 상태로 초기화
        for (UserService user : UserVec) {
            aliveStatus.put(user.UserName, true);
        }
        
        WriteAll("SYSTEM: ===== 마피아 게임이 시작되었습니다! =====\n");
        WriteAll("SYSTEM: 참가자 수: " + UserVec.size() + "명\n");
        
        // 게임 시작 후 밤 페이즈로 전환
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                startNightPhase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 역할 배정
    private void assignRoles() {
        List<String> roles = new ArrayList<>();
        int playerCount = UserVec.size();
        
        // 역할 구성 (4명: 마피아1, 의사1, 경찰1, 시민1)
        // 5-6명: 마피아2, 의사1, 경찰1, 시민 나머지
        // 7명 이상: 마피아2, 의사1, 경찰1, 시민 나머지
        
        if (playerCount == 4) {
            roles.add("MAFIA");
            roles.add("DOCTOR");
            roles.add("POLICE");
            roles.add("CITIZEN");
        } else if (playerCount <= 6) {
            roles.add("MAFIA");
            roles.add("MAFIA");
            roles.add("DOCTOR");
            roles.add("POLICE");
            for (int i = 0; i < playerCount - 4; i++) {
                roles.add("CITIZEN");
            }
        } else {
            roles.add("MAFIA");
            roles.add("MAFIA");
            roles.add("DOCTOR");
            roles.add("POLICE");
            for (int i = 0; i < playerCount - 4; i++) {
                roles.add("CITIZEN");
            }
        }
        
        // 역할 섞기
        Collections.shuffle(roles);
        
        // 역할 배정 및 전송
        for (int i = 0; i < UserVec.size(); i++) {
            UserService user = UserVec.get(i);
            String role = roles.get(i);
            user.setRole(role);
            
            String roleMsg = getRoleDescription(role);
            user.WriteOne("ROLE:" + role + "\n");
            user.WriteOne("SYSTEM: " + roleMsg + "\n");
            AppendText(user.UserName + " -> " + role);
        }
    }

    private String getRoleDescription(String role) {
        switch (role) {
            case "MAFIA":
                return "당신은 [마피아]입니다. 밤에 시민을 제거하세요!";
            case "DOCTOR":
                return "당신은 [의사]입니다. 밤에 한 명을 지정하여 보호하세요!";
            case "POLICE":
                return "당신은 [경찰]입니다. 밤에 한 명을 조사하여 마피아인지 확인하세요!";
            case "CITIZEN":
                return "당신은 [시민]입니다. 낮 투표로 마피아를 찾아내세요!";
            default:
                return "역할이 배정되었습니다.";
        }
    }

    // 밤 페이즈
    private void startNightPhase() {
        dayCount++;
        gamePhase = "NIGHT";
        nightActions.clear();
        
        AppendText("===== " + dayCount + "일차 밤 =====");
        WriteAll("PHASE:NIGHT\n");
        WriteAll("SYSTEM: ===== " + dayCount + "일차 밤이 되었습니다 =====\n");
        WriteAll("SYSTEM: 마피아는 제거할 대상을, 의사는 보호할 대상을, 경찰은 조사할 대상을 선택하세요.\n");
        
        // 살아있는 플레이어 목록 전송
        sendAlivePlayerList();
        
        // 30초 대기 후 낮 페이즈로 전환
        new Thread(() -> {
            try {
                Thread.sleep(30000);
                processNightActions();
                startDayPhase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 밤 행동 처리
    private void processNightActions() {
        String mafiaTarget = nightActions.get("MAFIA");
        String doctorTarget = nightActions.get("DOCTOR");
        String policeTarget = nightActions.get("POLICE");
        
        AppendText("=== 밤 행동 결과 ===");
        AppendText("마피아 타겟: " + (mafiaTarget != null ? mafiaTarget : "없음"));
        AppendText("의사 보호: " + (doctorTarget != null ? doctorTarget : "없음"));
        AppendText("경찰 조사: " + (policeTarget != null ? policeTarget : "없음"));
        
        // 마피아의 공격 처리
        if (mafiaTarget != null && !mafiaTarget.equals(doctorTarget)) {
            aliveStatus.put(mafiaTarget, false);
            WriteAll("SYSTEM: [" + mafiaTarget + "]님이 마피아에게 제거되었습니다.\n");
            AppendText(mafiaTarget + " 사망");
            
            // 해당 플레이어에게 사망 알림
            for (UserService user : UserVec) {
                if (user.UserName.equals(mafiaTarget)) {
                    user.WriteOne("DEAD:true\n");
                }
            }
        } else if (mafiaTarget != null && mafiaTarget.equals(doctorTarget)) {
            WriteAll("SYSTEM: 의사가 누군가를 구했습니다!\n");
            AppendText(mafiaTarget + " 의사가 구함");
        }
        
        // 경찰 조사 결과
        if (policeTarget != null) {
            for (UserService user : UserVec) {
                if (user.role.equals("POLICE")) {
                    for (UserService target : UserVec) {
                        if (target.UserName.equals(policeTarget)) {
                            String result = target.role.equals("MAFIA") ? "마피아입니다!" : "마피아가 아닙니다.";
                            user.WriteOne("SYSTEM: [" + policeTarget + "]님은 " + result + "\n");
                            AppendText("경찰이 " + policeTarget + " 조사 -> " + result);
                        }
                    }
                }
            }
        }
    }

    // 낮 페이즈
    private void startDayPhase() {
        gamePhase = "DAY";
        
        // 게임 종료 체크
        if (checkGameEnd()) {
            return;
        }
        
        AppendText("===== " + dayCount + "일차 낮 =====");
        WriteAll("PHASE:DAY\n");
        WriteAll("SYSTEM: ===== " + dayCount + "일차 낮이 되었습니다 =====\n");
        WriteAll("SYSTEM: 자유롭게 대화하고 의심되는 사람을 찾으세요.\n");
        WriteAll("SYSTEM: 30초 후 투표가 시작됩니다.\n");
        
        sendAlivePlayerList();
        
        // 30초 대기 후 투표 페이즈로 전환
        new Thread(() -> {
            try {
                Thread.sleep(30000);
                startVotePhase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 투표 페이즈
    private void startVotePhase() {
        gamePhase = "VOTE";
        voteCount.clear();
        
        // 살아있는 모든 플레이어를 투표 대상으로 초기화
        for (String player : aliveStatus.keySet()) {
            if (aliveStatus.get(player)) {
                voteCount.put(player, 0);
            }
        }
        
        AppendText("===== 투표 시작 =====");
        WriteAll("PHASE:VOTE\n");
        WriteAll("SYSTEM: ===== 투표 시작 =====\n");
        WriteAll("SYSTEM: 제거할 플레이어를 투표하세요! (20초)\n");
        
        sendAlivePlayerList();
        
        // 20초 대기 후 투표 결과 처리
        new Thread(() -> {
            try {
                Thread.sleep(20000);
                processVoteResult();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 투표 결과 처리
    private void processVoteResult() {
        AppendText("=== 투표 결과 ===");
        
        String maxVotedPlayer = null;
        int maxVotes = 0;
        boolean tie = false;
        
        for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
            AppendText(entry.getKey() + ": " + entry.getValue() + "표");
            WriteAll("SYSTEM: [" + entry.getKey() + "] " + entry.getValue() + "표\n");
            
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                maxVotedPlayer = entry.getKey();
                tie = false;
            } else if (entry.getValue() == maxVotes && maxVotes > 0) {
                tie = true;
            }
        }
        
        if (tie || maxVotes == 0) {
            WriteAll("SYSTEM: 동점 또는 투표 없음! 아무도 제거되지 않았습니다.\n");
            AppendText("투표 무효");
        } else {
            aliveStatus.put(maxVotedPlayer, false);
            
            // 역할 공개
            String eliminatedRole = "";
            for (UserService user : UserVec) {
                if (user.UserName.equals(maxVotedPlayer)) {
                    eliminatedRole = user.role;
                    user.WriteOne("DEAD:true\n");
                }
            }
            
            WriteAll("SYSTEM: [" + maxVotedPlayer + "]님이 투표로 제거되었습니다.\n");
            WriteAll("SYSTEM: [" + maxVotedPlayer + "]님의 역할은 [" + eliminatedRole + "]였습니다.\n");
            AppendText(maxVotedPlayer + " 제거됨 (역할: " + eliminatedRole + ")");
        }
        
        // 게임 종료 체크
        if (checkGameEnd()) {
            return;
        }
        
        // 다음 밤으로
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                startNightPhase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 게임 종료 체크
    private boolean checkGameEnd() {
        int aliveCount = 0;
        int mafiaCount = 0;
        
        for (UserService user : UserVec) {
            if (aliveStatus.get(user.UserName)) {
                aliveCount++;
                if (user.role.equals("MAFIA")) {
                    mafiaCount++;
                }
            }
        }
        
        AppendText("생존자: " + aliveCount + "명, 마피아: " + mafiaCount + "명");
        
        if (mafiaCount == 0) {
            // 시민 승리
            WriteAll("PHASE:END\n");
            WriteAll("SYSTEM: ===== 게임 종료! 시민 팀 승리! =====\n");
            WriteAll("SYSTEM: 모든 마피아가 제거되었습니다!\n");
            AppendText("===== 게임 종료: 시민 승리 =====");
            revealAllRoles();
            gameStarted = false;
            btnGameStart.setEnabled(true);
            return true;
        } else if (mafiaCount >= aliveCount - mafiaCount) {
            // 마피아 승리
            WriteAll("PHASE:END\n");
            WriteAll("SYSTEM: ===== 게임 종료! 마피아 팀 승리! =====\n");
            WriteAll("SYSTEM: 마피아가 시민 수와 같거나 많아졌습니다!\n");
            AppendText("===== 게임 종료: 마피아 승리 =====");
            revealAllRoles();
            gameStarted = false;
            btnGameStart.setEnabled(true);
            return true;
        }
        
        return false;
    }

    // 모든 역할 공개
    private void revealAllRoles() {
        WriteAll("SYSTEM: ===== 역할 공개 =====\n");
        for (UserService user : UserVec) {
            WriteAll("SYSTEM: [" + user.UserName + "] - " + user.role + "\n");
        }
    }

    // 살아있는 플레이어 목록 전송
    private void sendAlivePlayerList() {
        StringBuilder playerList = new StringBuilder("PLAYERS:");
        for (String player : aliveStatus.keySet()) {
            if (aliveStatus.get(player)) {
                playerList.append(player).append(",");
            }
        }
        WriteAll(playerList.toString() + "\n");
    }

    class AcceptServer extends Thread {
        public void run() {
            while (true) {
                try {
                    AppendText("Waiting for players...");
                    client_socket = socket.accept();
                    AppendText("새로운 플레이어 from " + client_socket);
                    
                    UserService new_user = new UserService(client_socket);
                    UserVec.add(new_user);
                    AppendText("플레이어 입장. 현재 플레이어 수: " + UserVec.size());
                    new_user.start();
                } catch (IOException e) {
                    AppendText("accept 에러 발생");
                }
            }
        }
    }

    public void AppendText(String str) {
        textArea.append(str + "\n");
        textArea.setCaretPosition(textArea.getText().length());
    }

    public void WriteAll(String str) {
        for (int i = 0; i < UserVec.size(); i++) {
            UserService user = UserVec.get(i);
            user.WriteOne(str);
        }
    }

    class UserService extends Thread {
        private InputStream is;
        private OutputStream os;
        private DataInputStream dis;
        private DataOutputStream dos;
        private Socket client_socket;
        private Vector<UserService> user_vc;
        private String UserName = "";
        String role = ""; // 플레이어 역할

        public UserService(Socket client_socket) {
            this.client_socket = client_socket;
            this.user_vc = UserVec;
            try {
                is = client_socket.getInputStream();
                dis = new DataInputStream(is);
                os = client_socket.getOutputStream();
                dos = new DataOutputStream(os);
                
                String line1 = dis.readUTF();
                String[] msg = line1.split(" ");
                UserName = msg[1].trim();
                
                AppendText("새로운 플레이어: " + UserName);
                WriteOne("SYSTEM: 마피아 게임 서버에 오신 것을 환영합니다!\n");
                WriteOne("SYSTEM: [" + UserName + "]님 환영합니다.\n");
                
                String br_msg = "SYSTEM: [" + UserName + "]님이 입장하였습니다.\n";
                WriteAll(br_msg);
                
            } catch (Exception e) {
                AppendText("UserService 생성 오류");
            }
        }

        public void setRole(String role) {
            this.role = role;
        }

        public void logout() {
            user_vc.removeElement(this);
            String br_msg = "SYSTEM: [" + UserName + "]님이 퇴장하였습니다.\n";
            WriteAll(br_msg);
            AppendText("플레이어 퇴장: " + UserName + " (현재 " + user_vc.size() + "명)");
        }

        public void WriteOne(String msg) {
            try {
                dos.writeUTF(msg);
            } catch (IOException e) {
                AppendText("전송 오류: " + UserName);
                try {
                    dos.close();
                    dis.close();
                    client_socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                logout();
            }
        }

        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF();
                    msg = msg.trim();
                    AppendText(msg);

                    // 게임 명령어 처리
                    if (msg.startsWith("NIGHT_ACTION:")) {
                        // NIGHT_ACTION:ROLE:TARGET 형식
                        String[] parts = msg.split(":");
                        if (parts.length == 3) {
                            String actionRole = parts[1];
                            String target = parts[2];
                            nightActions.put(actionRole, target);
                            AppendText(UserName + "(" + role + ") -> " + target);
                            WriteOne("SYSTEM: 선택이 완료되었습니다.\n");
                        }
                    } else if (msg.startsWith("VOTE:")) {
                        // VOTE:TARGET 형식
                        String[] parts = msg.split(":");
                        if (parts.length == 2) {
                            String target = parts[1];
                            if (voteCount.containsKey(target)) {
                                voteCount.put(target, voteCount.get(target) + 1);
                                AppendText(UserName + " -> " + target + " 투표");
                                WriteOne("SYSTEM: [" + target + "]님에게 투표했습니다.\n");
                            }
                        }
                    } else if (msg.contains("/exit")) {
                        logout();
                        return;
                    } else {
                        // 일반 채팅 메시지
                        WriteAll(msg + "\n");
                    }

                } catch (IOException e) {
                    AppendText("연결 오류: " + UserName);
                    try {
                        dos.close();
                        dis.close();
                        client_socket.close();
                        logout();
                        break;
                    } catch (Exception ee) {
                        break;
                    }
                }
            }
        }
    }
}
