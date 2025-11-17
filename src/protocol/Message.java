package protocol;

/**
 * 서버-클라이언트 간 통신 메시지 프로토콜
 * 간단한 구분자 기반 프로토콜 사용 (JSON 라이브러리 없이)
 */
public class Message {

    // 메시지 타입
    public enum Type {
        // 연결 관련
        LOGIN,              // 로그인 (username|nickname)
        LOGIN_SUCCESS,      // 로그인 성공
        LOGIN_FAILED,       // 로그인 실패

        // 방 목록 관련
        ROOM_LIST_REQUEST,  // 방 목록 요청
        ROOM_LIST_RESPONSE, // 방 목록 응답 (roomId|roomName|current/max|status;...)

        // 방 입장/퇴장
        ROOM_JOIN,          // 방 입장 요청 (roomId)
        ROOM_JOIN_SUCCESS,  // 방 입장 성공 (roomId|roomName)
        ROOM_JOIN_FAILED,   // 방 입장 실패 (reason)
        ROOM_LEAVE,         // 방 퇴장

        // 플레이어 알림
        PLAYER_JOINED,      // 플레이어 입장 알림 (nickname)
        PLAYER_LEFT,        // 플레이어 퇴장 알림 (nickname)
        PLAYER_LIST,        // 방의 플레이어 목록 (nick1|nick2|nick3...)

        // 채팅
        CHAT_MESSAGE,       // 채팅 메시지 (nickname|message)
        SYSTEM_MESSAGE,     // 시스템 메시지 (message)

        // 게임
        GAME_START,         // 게임 시작
        GAME_STATE,         // 게임 상태 업데이트

        // 기타
        ERROR,              // 에러 메시지
        DISCONNECT          // 연결 종료
    }

    private Type type;
    private String data;

    public Message(Type type, String data) {
        this.type = type;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    /**
     * 메시지를 문자열로 직렬화
     * 형식: TYPE:data
     */
    public String serialize() {
        return type.name() + ":" + (data != null ? data : "");
    }

    /**
     * 문자열을 메시지로 역직렬화
     */
    public static Message deserialize(String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }

        int colonIndex = line.indexOf(':');
        if (colonIndex == -1) {
            return null;
        }

        try {
            String typeStr = line.substring(0, colonIndex);
            String data = line.substring(colonIndex + 1);
            Type type = Type.valueOf(typeStr);
            return new Message(type, data);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ 잘못된 메시지 타입: " + line);
            return null;
        }
    }

    // 편의 메서드들
    public static Message login(String username, String nickname) {
        return new Message(Type.LOGIN, username + "|" + nickname);
    }

    public static Message loginSuccess() {
        return new Message(Type.LOGIN_SUCCESS, "");
    }

    public static Message loginFailed(String reason) {
        return new Message(Type.LOGIN_FAILED, reason);
    }

    public static Message roomListRequest() {
        return new Message(Type.ROOM_LIST_REQUEST, "");
    }

    public static Message roomListResponse(String roomListData) {
        return new Message(Type.ROOM_LIST_RESPONSE, roomListData);
    }

    public static Message roomJoin(int roomId) {
        return new Message(Type.ROOM_JOIN, String.valueOf(roomId));
    }

    public static Message roomJoinSuccess(int roomId, String roomName) {
        return new Message(Type.ROOM_JOIN_SUCCESS, roomId + "|" + roomName);
    }

    public static Message roomJoinFailed(String reason) {
        return new Message(Type.ROOM_JOIN_FAILED, reason);
    }

    public static Message roomLeave() {
        return new Message(Type.ROOM_LEAVE, "");
    }

    public static Message playerJoined(String nickname) {
        return new Message(Type.PLAYER_JOINED, nickname);
    }

    public static Message playerLeft(String nickname) {
        return new Message(Type.PLAYER_LEFT, nickname);
    }

    public static Message playerList(String players) {
        return new Message(Type.PLAYER_LIST, players);
    }

    public static Message chatMessage(String nickname, String message) {
        return new Message(Type.CHAT_MESSAGE, nickname + "|" + message);
    }

    public static Message systemMessage(String message) {
        return new Message(Type.SYSTEM_MESSAGE, message);
    }

    public static Message gameStart(String roleAssignments) {
        return new Message(Type.GAME_START, roleAssignments);
    }

    public static Message error(String errorMsg) {
        return new Message(Type.ERROR, errorMsg);
    }

    @Override
    public String toString() {
        return "Message{type=" + type + ", data='" + data + "'}";
    }
}
