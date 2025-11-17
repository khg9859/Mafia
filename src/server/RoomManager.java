package server;

import database.RoomDAO;
import protocol.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 게임 방 관리 및 메시지 브로드캐스트 담당
 */
public class RoomManager {
    // roomId -> List of ClientHandler
    private Map<Integer, List<ClientHandler>> roomClients = new ConcurrentHashMap<>();

    /**
     * 클라이언트를 방에 추가
     */
    public synchronized boolean addClientToRoom(int roomId, ClientHandler client) {
        roomClients.putIfAbsent(roomId, Collections.synchronizedList(new ArrayList<>()));
        List<ClientHandler> clients = roomClients.get(roomId);

        if (!clients.contains(client)) {
            clients.add(client);
            System.out.println("✅ RoomManager: " + client.getNickname() + " -> Room " + roomId);
            return true;
        }
        return false;
    }

    /**
     * 클라이언트를 방에서 제거
     */
    public synchronized boolean removeClientFromRoom(int roomId, ClientHandler client) {
        List<ClientHandler> clients = roomClients.get(roomId);
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
        List<ClientHandler> clients = roomClients.get(roomId);
        if (clients != null) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.sendMessage(message);
                }
            }
            System.out.println("📢 Broadcast to Room " + roomId + ": " + message.getType());
        }
    }

    /**
     * 특정 방의 모든 클라이언트에게 메시지 브로드캐스트 (발신자 제외)
     */
    public void broadcastToRoomExcept(int roomId, Message message, ClientHandler except) {
        List<ClientHandler> clients = roomClients.get(roomId);
        if (clients != null) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
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
        List<ClientHandler> clients = roomClients.get(roomId);
        return clients != null ? clients.size() : 0;
    }

    /**
     * 클라이언트가 현재 어느 방에 있는지 찾기
     */
    public Integer findRoomByClient(ClientHandler client) {
        for (Map.Entry<Integer, List<ClientHandler>> entry : roomClients.entrySet()) {
            if (entry.getValue().contains(client)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
