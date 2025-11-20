// MafiaGameClientView.java
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class MafiaGameClientView extends JFrame {
    private JPanel contentPane;
    private JTextField txtInput;
    private String UserName;
    private JButton btnSend;
    private JTextArea textArea;
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;
    private JLabel lblUserName;
    private JLabel lblRole;
    private JLabel lblPhase;
    private JLabel lblStatus;
    private JList<String> playerList;
    private DefaultListModel<String> listModel;
    private JButton btnAction;
    
    private String myRole = "";
    private String currentPhase = "WAITING";
    private boolean isDead = false;

    public MafiaGameClientView(String username, String ip_addr, String port_no) {
        setTitle("Mafia Game - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 700, 600);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // 게임 상태 패널
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new TitledBorder(null, "Game Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        statusPanel.setBounds(12, 10, 660, 80);
        contentPane.add(statusPanel);
        statusPanel.setLayout(null);

        lblUserName = new JLabel("Player: " + username);
        lblUserName.setFont(new Font("Arial", Font.BOLD, 14));
        lblUserName.setBounds(10, 20, 200, 25);
        statusPanel.add(lblUserName);

        lblRole = new JLabel("Role: Waiting...");
        lblRole.setFont(new Font("Arial", Font.BOLD, 14));
        lblRole.setForeground(Color.BLUE);
        lblRole.setBounds(10, 45, 200, 25);
        statusPanel.add(lblRole);

        lblPhase = new JLabel("Phase: WAITING");
        lblPhase.setFont(new Font("Arial", Font.BOLD, 14));
        lblPhase.setHorizontalAlignment(SwingConstants.CENTER);
        lblPhase.setBounds(220, 20, 220, 25);
        statusPanel.add(lblPhase);

        lblStatus = new JLabel("Status: ALIVE");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
        lblStatus.setForeground(Color.GREEN);
        lblStatus.setHorizontalAlignment(SwingConstants.RIGHT);
        lblStatus.setBounds(450, 20, 200, 25);
        statusPanel.add(lblStatus);

        // 채팅 영역
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(12, 100, 450, 330);
        contentPane.add(scrollPane);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        scrollPane.setViewportView(textArea);

        // 플레이어 목록
        JPanel playerPanel = new JPanel();
        playerPanel.setBorder(new TitledBorder(null, "Players", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        playerPanel.setBounds(474, 100, 198, 280);
        contentPane.add(playerPanel);
        playerPanel.setLayout(null);

        listModel = new DefaultListModel<>();
        playerList = new JList<>(listModel);
        playerList.setFont(new Font("Arial", Font.PLAIN, 12));

        // Custom Cell Renderer for dead players
        playerList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String playerName = value.toString();
                if (playerName.startsWith("[DEAD]")) {
                    c.setForeground(Color.RED);
                } else if (!isSelected) {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane listScrollPane = new JScrollPane(playerList);
        listScrollPane.setBounds(10, 20, 178, 250);
        playerPanel.add(listScrollPane);

        // 행동 버튼 (밤/투표 시)
        btnAction = new JButton("Select Target");
        btnAction.setFont(new Font("Arial", Font.BOLD, 14));
        btnAction.setEnabled(false);
        btnAction.setBounds(474, 390, 198, 40);
        contentPane.add(btnAction);
        btnAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performAction();
            }
        });

        // 입력 영역
        txtInput = new JTextField();
        txtInput.setFont(new Font("Arial", Font.PLAIN, 12));
        txtInput.setBounds(12, 440, 350, 40);
        contentPane.add(txtInput);
        txtInput.setColumns(10);

        btnSend = new JButton("Send");
        btnSend.setFont(new Font("Arial", Font.BOLD, 14));
        btnSend.setBounds(374, 440, 88, 40);
        contentPane.add(btnSend);

        lblUserName.setText(username);
        setVisible(true);

        AppendText("Connecting to " + ip_addr + ":" + port_no + "...\n");
        UserName = username;

        try {
            socket = new Socket(ip_addr, Integer.parseInt(port_no));
            is = socket.getInputStream();
            dis = new DataInputStream(is);
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);

            SendMessage("/login " + UserName);
            ListenNetwork net = new ListenNetwork();
            net.start();
            
            Myaction action = new Myaction();
            btnSend.addActionListener(action);
            txtInput.addActionListener(action);
            txtInput.requestFocus();
            
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
            AppendText("Connection error!\n");
        }
    }

    // 서버 메시지 수신
    class ListenNetwork extends Thread {
        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF();
                    
                    // 프로토콜 처리
                    if (msg.startsWith("ROLE:")) {
                        String role = msg.substring(5).trim();
                        myRole = role;
                        lblRole.setText("Role: " + getRoleDisplayName(role));
                        lblRole.setForeground(getRoleColor(role));
                    } else if (msg.startsWith("PHASE:")) {
                        String phase = msg.substring(6).trim();
                        currentPhase = phase;
                        lblPhase.setText("Phase: " + phase);
                        updateActionButton();
                    } else if (msg.startsWith("PLAYERS:")) {
                        String players = msg.substring(8).trim();
                        updatePlayerList(players);
                    } else if (msg.startsWith("DEAD:")) {
                        isDead = true;
                        lblStatus.setText("Status: DEAD");
                        lblStatus.setForeground(Color.RED);
                        btnAction.setEnabled(false);
                        AppendText("=== You are dead. You can chat with other dead players. SHAMAN can see your chat. ===\n");
                    } else {
                        AppendText(msg);
                    }
                    
                } catch (IOException e) {
                    AppendText("Connection lost!\n");
                    try {
                        dos.close();
                        dis.close();
                        socket.close();
                        break;
                    } catch (Exception ee) {
                        break;
                    }
                }
            }
        }
    }

    // 채팅 메시지 전송
    class Myaction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnSend || e.getSource() == txtInput) {
                String msg = txtInput.getText().trim();
                if (msg.isEmpty()) return;

                String fullMsg = String.format("[%s] %s", UserName, msg);
                SendMessage(fullMsg);
                txtInput.setText("");
                txtInput.requestFocus();

                if (msg.contains("/exit")) {
                    System.exit(0);
                }
            }
        }
    }

    // 행동 수행 (밤 행동 또는 투표)
    private void performAction() {
        if (isDead) {
            AppendText("You are dead and cannot perform actions.\n");
            return;
        }

        String selectedPlayer = playerList.getSelectedValue();
        if (selectedPlayer == null) {
            AppendText("Please select a player first!\n");
            return;
        }

        // [DEAD] 접두사 제거
        if (selectedPlayer.startsWith("[DEAD]")) {
            selectedPlayer = selectedPlayer.substring(6);
        }

        if (currentPhase.equals("NIGHT")) {
            // 밤 행동
            if (myRole.equals("MAFIA")) {
                SendMessage("NIGHT_ACTION:MAFIA:" + selectedPlayer);
                AppendText("You selected [" + selectedPlayer + "] to eliminate.\n");
            } else if (myRole.equals("SPY")) {
                SendMessage("NIGHT_ACTION:SPY:" + selectedPlayer);
                AppendText("You selected [" + selectedPlayer + "] to investigate role.\n");
            } else if (myRole.equals("DOCTOR")) {
                SendMessage("NIGHT_ACTION:DOCTOR:" + selectedPlayer);
                AppendText("You selected [" + selectedPlayer + "] to protect.\n");
            } else if (myRole.equals("POLICE")) {
                SendMessage("NIGHT_ACTION:POLICE:" + selectedPlayer);
                AppendText("You selected [" + selectedPlayer + "] to investigate.\n");
            } else if (myRole.equals("SHAMAN")) {
                SendMessage("NIGHT_ACTION:SHAMAN:" + selectedPlayer);
                AppendText("You selected [" + selectedPlayer + "] to bless.\n");
            } else if (myRole.equals("REPORTER")) {
                SendMessage("NIGHT_ACTION:REPORTER:" + selectedPlayer);
                AppendText("You selected [" + selectedPlayer + "] to investigate for scoop.\n");
            } else if (myRole.equals("GANGSTER")) {
                SendMessage("NIGHT_ACTION:GANGSTER:" + selectedPlayer);
                AppendText("You selected [" + selectedPlayer + "] to ban from voting.\n");
            }
            btnAction.setEnabled(false);
        } else if (currentPhase.equals("VOTE")) {
            // 투표 (서버에서 결과 메시지를 받음)
            SendMessage("VOTE:" + selectedPlayer);
            btnAction.setEnabled(false);
        }
    }

    // 행동 버튼 업데이트
    private void updateActionButton() {
        if (isDead) {
            btnAction.setEnabled(false);
            return;
        }

        if (currentPhase.equals("NIGHT")) {
            if (myRole.equals("CITIZEN") || myRole.equals("POLITICIAN") || myRole.equals("SOLDIER")) {
                btnAction.setEnabled(false);
                btnAction.setText("No Night Action");
            } else {
                btnAction.setEnabled(true);
                if (myRole.equals("MAFIA")) {
                    btnAction.setText("Kill Selected");
                } else if (myRole.equals("SPY")) {
                    btnAction.setText("Investigate Role");
                } else if (myRole.equals("DOCTOR")) {
                    btnAction.setText("Protect Selected");
                } else if (myRole.equals("POLICE")) {
                    btnAction.setText("Investigate Selected");
                } else if (myRole.equals("SHAMAN")) {
                    btnAction.setText("Bless Selected");
                } else if (myRole.equals("REPORTER")) {
                    btnAction.setText("Investigate for Scoop");
                } else if (myRole.equals("GANGSTER")) {
                    btnAction.setText("Ban from Voting");
                }
            }
        } else if (currentPhase.equals("VOTE")) {
            btnAction.setEnabled(true);
            if (myRole.equals("POLITICIAN")) {
                btnAction.setText("Vote Selected (2 votes)");
            } else {
                btnAction.setText("Vote Selected");
            }
        } else {
            btnAction.setEnabled(false);
            btnAction.setText("No Action");
        }
    }

    // 플레이어 목록 업데이트
    private void updatePlayerList(String players) {
        listModel.clear();
        if (!players.isEmpty()) {
            String[] playerArray = players.split(",");
            for (String player : playerArray) {
                if (!player.trim().isEmpty()) {
                    listModel.addElement(player.trim());
                }
            }
        }
        updateActionButton();
    }

    // 역할 표시 이름
    private String getRoleDisplayName(String role) {
        switch (role) {
            case "MAFIA": return "MAFIA (악당)";
            case "SPY": return "SPY (스파이)";
            case "DOCTOR": return "DOCTOR (의사)";
            case "POLICE": return "POLICE (경찰)";
            case "POLITICIAN": return "POLITICIAN (정치인)";
            case "SOLDIER": return "SOLDIER (군인)";
            case "SHAMAN": return "SHAMAN (영매)";
            case "REPORTER": return "REPORTER (기자)";
            case "GANGSTER": return "GANGSTER (건달)";
            case "CITIZEN": return "CITIZEN (시민)";
            default: return role;
        }
    }

    // 역할 색상
    private Color getRoleColor(String role) {
        switch (role) {
            case "MAFIA": return new Color(220, 20, 60); // Crimson
            case "SPY": return new Color(138, 43, 226); // Blue Violet
            case "DOCTOR": return new Color(34, 139, 34); // Forest Green
            case "POLICE": return new Color(30, 144, 255); // Dodger Blue
            case "POLITICIAN": return new Color(255, 215, 0); // Gold
            case "SOLDIER": return new Color(139, 69, 19); // Saddle Brown
            case "SHAMAN": return new Color(148, 0, 211); // Dark Violet
            case "REPORTER": return new Color(255, 140, 0); // Dark Orange
            case "GANGSTER": return new Color(105, 105, 105); // Dim Gray
            case "CITIZEN": return new Color(128, 128, 128); // Gray
            default: return Color.BLACK;
        }
    }

    // 화면에 출력
    public void AppendText(String msg) {
        textArea.append(msg);
        textArea.setCaretPosition(textArea.getText().length());
    }

    // 서버에게 메시지 전송
    public void SendMessage(String msg) {
        try {
            dos.writeUTF(msg);
        } catch (IOException e) {
            AppendText("Failed to send message.\n");
            try {
                dos.close();
                dis.close();
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                System.exit(0);
            }
        }
    }
}
