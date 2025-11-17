import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class GameServer extends JFrame {
    private JPanel contentPane;
    private JTextArea textArea;
    private JTextField txtPortNumber;
    private ServerSocket serverSocket;
    private Vector<ClientHandler> clients;
    private Vector<GameRoom> rooms;
    private AtomicInteger roomIdCounter;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                GameServer frame = new GameServer();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public GameServer() {
        clients = new Vector<>();
        rooms = new Vector<>();
        roomIdCounter = new AtomicInteger(1);

        setTitle("마피아42 게임 서버");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 500);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(12, 10, 410, 350);
        contentPane.add(scrollPane);

        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        JLabel lblPort = new JLabel("Port Number:");
        lblPort.setBounds(12, 370, 100, 30);
        contentPane.add(lblPort);

        txtPortNumber = new JTextField("30000");
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setBounds(120, 370, 150, 30);
        contentPane.add(txtPortNumber);
        txtPortNumber.setColumns(10);

        JButton btnStart = new JButton("서버 시작");
        btnStart.setBounds(12, 410, 410, 40);
        contentPane.add(btnStart);

        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int port = Integer.parseInt(txtPortNumber.getText());
                    serverSocket = new ServerSocket(port);
                    appendText("서버 시작됨 - 포트: " + port);
                    btnStart.setEnabled(false);
                    txtPortNumber.setEnabled(false);
                    
                    new AcceptThread().start();
                } catch (IOException ex) {
                    appendText("서버 시작 실패: " + ex.getMessage());
                }
            }
        });
    }

    public void appendText(String text) {
        textArea.append(text + "\n");
        textArea.setCaretPosition(textArea.getText().length());
    }

    public synchronized void createRoom(String roomName, int maxPlayers, String hostName, ClientHandler client) {
        int roomId = roomIdCounter.getAndIncrement();
        GameRoom room = new GameRoom(roomId, roomName, maxPlayers, hostName);
        room.addPlayer(client);
        client.setCurrentRoom(room);
        rooms.add(room);
        
        appendText("방 생성: " + roomName + " (ID: " + roomId + ", 방장: " + hostName + ")");
        client.sendMessage("/room_created " + roomId + "|" + roomName + "|" + maxPlayers);
        
        // 모든 클라이언트에게 방 목록 업데이트 전송
        broadcastRoomList();
    }

    public synchronized void joinRoom(int roomId, ClientHandler client) {
        for (GameRoom room : rooms) {
            if (room.getRoomId() == roomId) {
                if (room.addPlayer(client)) {
                    client.setCurrentRoom(room);
                    appendText(client.getUserName() + "님이 방 " + room.getRoomName() + "에 입장");
                    room.broadcast("/player_joined " + client.getUserName());
                    client.sendMessage("/join_success " + roomId + "|" + room.getRoomName() + "|" + room.getMaxPlayers());
                    
                    // 방 목록 업데이트
                    broadcastRoomList();
                    return;
                } else {
                    client.sendMessage("/join_failed 방이 가득 찼거나 게임이 진행중입니다.");
                    return;
                }
            }
        }
        client.sendMessage("/join_failed 방을 찾을 수 없습니다.");
    }

    public synchronized void leaveRoom(ClientHandler client) {
        GameRoom room = client.getCurrentRoom();
        if (room != null) {
            room.removePlayer(client);
            room.broadcast("/player_left " + client.getUserName());
            appendText(client.getUserName() + "님이 방 " + room.getRoomName() + "에서 퇴장");
            
            // 방이 비었으면 삭제
            if (room.getCurrentPlayers() == 0) {
                rooms.remove(room);
                appendText("방 삭제: " + room.getRoomName());
            }
            
            client.setCurrentRoom(null);
            client.sendMessage("/leave_success");
            
            // 방 목록 업데이트
            broadcastRoomList();
        }
    }

    public synchronized void sendRoomList(ClientHandler client) {
        StringBuilder sb = new StringBuilder("/room_list");
        for (GameRoom room : rooms) {
            sb.append("|").append(room.getRoomId())
              .append(",").append(room.getRoomName())
              .append(",").append(room.getCurrentPlayers())
              .append(",").append(room.getMaxPlayers())
              .append(",").append(room.getHostName());
        }
        client.sendMessage(sb.toString());
    }

    private void broadcastRoomList() {
        for (ClientHandler client : clients) {
            if (client.getCurrentRoom() == null) {
                sendRoomList(client);
            }
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        appendText("클라이언트 연결 종료: " + client.getUserName());
    }

    class AcceptThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    appendText("클라이언트 대기 중...");
                    Socket socket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(socket, GameServer.this);
                    clients.add(handler);
                    handler.start();
                    appendText("새로운 클라이언트 연결: " + socket.getInetAddress());
                } catch (IOException e) {
                    appendText("Accept 오류: " + e.getMessage());
                    break;
                }
            }
        }
    }
}
