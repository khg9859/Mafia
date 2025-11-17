package server;

import database.RoomDAO;
import protocol.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 테스트용 게임 방 관리자
 * - 2명만으로 게임 시작 가능
 * - 한 명이 게임 시작하면 모든 플레이어에게 전파
 */
public class TestRoomManager {
    private Map<Integer, List<TestClientHandler>> roomClients = new ConcurrentHashMap<>();
    private TestMafiaServer server;

    public TestRoomManager(TestMafiaServer server) {
        this.server = server;
    }

    /**
     * 클라이언트를 방에 추가
     */
    public synchronized boolean addClientToRoom(int roomId, TestClientHandler client) {
        roomClients.putIfAbsent(roomId, Collections.synchronizedList(new ArrayList<>()));
        List<TestClientHandler> clients = roomClients.get(roomId);

        if (!clients.contains(client)) {
            clients.add(client);
            System.out.println("✅ TestRoomManager: " + client.getNickname() + " -> Room " + roomId);
            return true;
        }
        return false;
    }

    /**
     * 클라이언트를 방에서 제거
     */
    public synchronized boolean removeClientFromRoom(int roomId, TestClientHandler client) {
        List<TestClientHandler> clients = roomClients.get(roomId);
        if (clients != null) {
            boolean removed = clients.remove(client);
            if (clients.isEmpty()) {
                roomClients.remove(roomId);
            }
            return removed;
        }
        return false;
    }

    /**
     * 특정 방의 모든 클라이언트에게 메시지 브로드캐스트
     */
    public void broadcastToRoom(int roomId, Message message) {
        List<TestClientHandler> clients = roomClients.get(roomId);
        if (clients != null) {
            synchronized (clients) {
                for (TestClientHandler client : clients) {
                    client.sendMessage(message);
                }
            }
            System.out.println("📢 Broadcast to Room " + roomId + ": " + message.getType());
        }
    }

    /**
     * 특정 방의 모든 클라이언트에게 메시지 브로드캐스트 (발신자 제외)
     */
    public void broadcastToRoomExcept(int roomId, Message message, TestClientHandler except) {
        List<TestClientHandler> clients = roomClients.get(roomId);
        if (clients != null) {
            synchronized (clients) {
                for (TestClientHandler client : clients) {
                    if (client != except) {
                        client.sendMessage(message);
                    }
                }
            }
        }
    }

    /**
     * 특정 방의 플레이어 목록을 문자열로 반환
     */
    public String getPlayerListString(int roomId) {
        List<String> players = RoomDAO.getPlayersInRoom(roomId);
        return String.join("|", players);
    }

    /**
     * 방에 있는 클라이언트 수 반환
     */
    public int getClientCountInRoom(int roomId) {
        List<TestClientHandler> clients = roomClients.get(roomId);
        return clients != null ? clients.size() : 0;
    }

    /**
     * 클라이언트가 현재 어느 방에 있는지 찾기
     */
    public Integer findRoomByClient(TestClientHandler client) {
        for (Map.Entry<Integer, List<TestClientHandler>> entry : roomClients.entrySet()) {
            if (entry.getValue().contains(client)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 게임 시작 처리 (테스트용: 2명 이상이면 시작 가능)
     */
    public void handleGameStart(int roomId, TestClientHandler requester) {
        int playerCount = getClientCountInRoom(roomId);
        int minPlayers = server.getMinPlayers();

        System.out.println("🎮 게임 시작 요청 - Room " + roomId + " (현재 인원: " + playerCount + "/" + minPlayers + ")");

        if (playerCount >= minPlayers) {
            // 모든 플레이어에게 게임 시작 메시지 전송
            broadcastToRoom(roomId, new Message(Message.Type.GAME_START, ""));
            System.out.println("✅ 게임 시작! Room " + roomId);
        } else {
            // 요청자에게만 인원 부족 메시지 전송
            requester.sendMessage(Message.systemMessage(
                "게임을 시작하려면 최소 " + minPlayers + "명이 필요합니다. (현재: " + playerCount + "명)"
            ));
            System.out.println("⚠️ 게임 시작 실패 - 인원 부족 (현재: " + playerCount + "/" + minPlayers + ")");
        }
    }
}
