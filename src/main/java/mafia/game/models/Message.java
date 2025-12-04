package mafia.game.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 게임 내 메시지 데이터 모델
 *
 * 이 클래스는 채팅, 쪽지, 시스템 메시지 등을 통합 관리합니다.
 *
 * 설계 원칙:
 * - Value Object Pattern: 불변 객체로 설계
 * - Type Safety: Enum을 사용한 타입 안전성 보장
 * - Flexibility: 다양한 메시지 타입 지원
 *
 * @author Mafia Game Team
 * @version 2.0
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    // ========================================
    // 메시지 타입 정의
    // ========================================

    /**
     * 메시지 타입 열거형
     */
    public enum MessageType {
        CHAT,           // 일반 채팅
        WHISPER,        // 익명 쪽지
        SYSTEM,         // 시스템 메시지
        DEAD_CHAT,      // 죽은 자 채팅
        MAFIA_CHAT,     // 마피아 팀 채팅
        EMOTION         // 감정 표현
    }

    // ========================================
    // 필드 (Immutable)
    // ========================================

    /**
     * 메시지 타입
     */
    private final MessageType type;

    /**
     * 발신자 (익명인 경우 "Anonymous")
     */
    private final String sender;

    /**
     * 수신자 (null인 경우 전체 공개)
     */
    private final String receiver;

    /**
     * 메시지 내용
     */
    private final String content;

    /**
     * 메시지 생성 시간
     */
    private final LocalDateTime timestamp;

    /**
     * 익명 여부
     */
    private final boolean anonymous;

    // ========================================
    // 생성자 (Builder 패턴 사용 권장)
    // ========================================

    /**
     * 전체 필드 생성자 (private)
     */
    private Message(MessageType type, String sender, String receiver,
                   String content, boolean anonymous) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.anonymous = anonymous;
    }

    // ========================================
    // 정적 팩토리 메소드
    // ========================================

    /**
     * 일반 채팅 메시지 생성
     *
     * @param sender 발신자
     * @param content 내용
     * @return 채팅 메시지
     */
    public static Message chat(String sender, String content) {
        return new Message(MessageType.CHAT, sender, null, content, false);
    }

    /**
     * 익명 쪽지 생성
     *
     * @param receiver 수신자
     * @param content 내용
     * @return 익명 쪽지
     */
    public static Message whisper(String receiver, String content) {
        return new Message(MessageType.WHISPER, "Anonymous", receiver, content, true);
    }

    /**
     * 시스템 메시지 생성
     *
     * @param content 내용
     * @return 시스템 메시지
     */
    public static Message system(String content) {
        return new Message(MessageType.SYSTEM, "SYSTEM", null, content, false);
    }

    /**
     * 죽은 자 채팅 생성
     *
     * @param sender 발신자
     * @param content 내용
     * @return 죽은 자 채팅
     */
    public static Message deadChat(String sender, String content) {
        return new Message(MessageType.DEAD_CHAT, sender, null, content, false);
    }

    /**
     * 마피아 팀 채팅 생성
     *
     * @param sender 발신자
     * @param content 내용
     * @return 마피아 팀 채팅
     */
    public static Message mafiaChat(String sender, String content) {
        return new Message(MessageType.MAFIA_CHAT, sender, null, content, false);
    }

    /**
     * 감정 표현 생성
     *
     * @param sender 발신자
     * @param emotion 감정 (이모지)
     * @return 감정 표현 메시지
     */
    public static Message emotion(String sender, String emotion) {
        return new Message(MessageType.EMOTION, sender, null, emotion, false);
    }

    // ========================================
    // Getter 메소드
    // ========================================

    public MessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * 전체 공개 메시지 여부
     *
     * @return 전체 공개 여부
     */
    public boolean isPublic() {
        return receiver == null;
    }

    /**
     * 개인 메시지 여부
     *
     * @return 개인 메시지 여부
     */
    public boolean isPrivate() {
        return receiver != null;
    }

    // ========================================
    // 포맷팅 메소드
    // ========================================

    /**
     * 클라이언트 전송용 프로토콜 문자열 생성
     *
     * @return 프로토콜 문자열
     */
    public String toProtocol() {
        String prefix;

        switch (type) {
            case CHAT:
                prefix = "";
                break;
            case WHISPER:
                prefix = "[익명 쪽지] ";
                break;
            case SYSTEM:
                prefix = "SYSTEM: ";
                break;
            case DEAD_CHAT:
                prefix = "[DEAD CHAT] ";
                break;
            case MAFIA_CHAT:
                prefix = "[MAFIA TEAM] ";
                break;
            case EMOTION:
                prefix = "[감정] ";
                break;
            default:
                prefix = "";
        }

        if (type == MessageType.CHAT || type == MessageType.DEAD_CHAT ||
            type == MessageType.MAFIA_CHAT) {
            return prefix + "[" + sender + "] " + content;
        } else if (type == MessageType.EMOTION) {
            return prefix + sender + " " + content;
        } else {
            return prefix + content;
        }
    }

    /**
     * 로그용 문자열 생성
     *
     * @return 로그 문자열
     */
    public String toLogString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String timeStr = timestamp.format(formatter);

        return String.format("[%s] [%s] %s -> %s: %s",
            timeStr,
            type,
            sender,
            receiver != null ? receiver : "ALL",
            content
        );
    }

    /**
     * 메시지 요약 정보
     *
     * @return 요약 문자열
     */
    @Override
    public String toString() {
        return toProtocol();
    }

    // ========================================
    // Builder 패턴
    // ========================================

    /**
     * 메시지 빌더 클래스
     * 복잡한 메시지 생성 시 사용
     */
    public static class Builder {
        private MessageType type;
        private String sender;
        private String receiver;
        private String content;
        private boolean anonymous;

        public Builder type(MessageType type) {
            this.type = type;
            return this;
        }

        public Builder sender(String sender) {
            this.sender = sender;
            return this;
        }

        public Builder receiver(String receiver) {
            this.receiver = receiver;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder anonymous(boolean anonymous) {
            this.anonymous = anonymous;
            return this;
        }

        public Message build() {
            return new Message(type, sender, receiver, content, anonymous);
        }
    }
}
