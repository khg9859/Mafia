import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String userName;
    private GameServer server;
    private GameRoom currentRoom;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentRoom(GameRoom room) {
        this.currentRoom = room;
    }

    public GameRoom getCurrentRoom() {
        return currentRoom;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public void run() {
        try {
            // 첫 메시지로 사용자 이름 받기
            String loginMsg = dis.readUTF();
            if (loginMsg.startsWith("/login ")) {
                userName = loginMsg.substring(7).trim();
                server.appendText("새로운 사용자 접속: " + userName);
                sendMessage("/login_success");
                
                // 현재 방 목록 전송
                server.sendRoomList(this);
            }

            while (true) {
                String message = dis.readUTF().trim();
                server.appendText("[" + userName + "] " + message);

                if (message.startsWith("/create_room ")) {
                    // 방 생성: /create_room 방이름|최대인원
                    String[] parts = message.substring(13).split("\\|");
                    if (parts.length == 2) {
                        String roomName = parts[0];
                        int maxPlayers = Integer.parseInt(parts[1]);
                        server.createRoom(roomName, maxPlayers, userName, this);
                    }
                } else if (message.startsWith("/join_room ")) {
                    // 방 입장: /join_room 방ID
                    int roomId = Integer.parseInt(message.substring(11));
                    server.joinRoom(roomId, this);
                } else if (message.startsWith("/leave_room")) {
                    // 방 나가기
                    server.leaveRoom(this);
                } else if (message.startsWith("/room_list")) {
                    // 방 목록 요청
                    server.sendRoomList(this);
                } else if (message.equals("/exit")) {
                    break;
                } else {
                    // 일반 채팅 메시지
                    if (currentRoom != null) {
                        String chatMsg = "[" + userName + "] " + message;
                        currentRoom.broadcast(chatMsg);
                        server.appendText("방 " + currentRoom.getRoomId() + " 채팅: " + chatMsg);
                    }
                }
            }
        } catch (IOException e) {
            server.appendText(userName + " 연결 종료");
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        try {
            if (currentRoom != null) {
                server.leaveRoom(this);
            }
            server.removeClient(this);
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
