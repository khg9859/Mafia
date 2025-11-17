import java.util.Vector;

public class GameRoom {
    private int roomId;
    private String roomName;
    private int maxPlayers;
    private Vector<ClientHandler> players;
    private String hostName;
    private boolean isPlaying;

    public GameRoom(int roomId, String roomName, int maxPlayers, String hostName) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.maxPlayers = maxPlayers;
        this.hostName = hostName;
        this.players = new Vector<>();
        this.isPlaying = false;
    }

    public synchronized boolean addPlayer(ClientHandler player) {
        if (players.size() < maxPlayers && !isPlaying) {
            players.add(player);
            return true;
        }
        return false;
    }

    public synchronized void removePlayer(ClientHandler player) {
        players.remove(player);
    }

    public synchronized void broadcast(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    public int getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getCurrentPlayers() { return players.size(); }
    public String getHostName() { return hostName; }
    public boolean isPlaying() { return isPlaying; }
    public void setPlaying(boolean playing) { isPlaying = playing; }
    public Vector<ClientHandler> getPlayers() { return players; }
}
