package mafia.game.events;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

/**
 * 이벤트 모드 시스템 관리자
 *
 * 이 클래스는 시즌별 특별 이벤트 모드를 관리합니다.
 *
 * 설계 원칙:
 * - Singleton Pattern: 하나의 매니저만 존재
 * - Strategy Pattern: 이벤트별 룰 전략
 * - Factory Pattern: 이벤트 생성
 *
 * 기능:
 * - 시즌별 이벤트 자동 활성화
 * - 특별 역할 추가
 * - 룰 변경
 * - 테마 적용
 *
 * @author Mafia Game Team
 * @version 2.0
 */
public class EventModeManager {

    // ========================================
    // Singleton 패턴
    // ========================================

    private static EventModeManager instance;

    /**
     * Singleton 인스턴스 조회
     *
     * @return EventModeManager 인스턴스
     */
    public static synchronized EventModeManager getInstance() {
        if (instance == null) {
            instance = new EventModeManager();
        }
        return instance;
    }

    // ========================================
    // 필드
    // ========================================

    /**
     * 등록된 이벤트 모드 (ID -> 이벤트)
     */
    private final Map<String, EventMode> eventModes;

    /**
     * 현재 활성 이벤트
     */
    private EventMode currentEvent;

    /**
     * 기능 활성화 여부
     */
    private boolean enabled;

    // ========================================
    // 생성자
    // ========================================

    /**
     * private 생성자 (Singleton)
     */
    private EventModeManager() {
        this.eventModes = new HashMap<>();
        this.currentEvent = null;
        this.enabled = true;

        // 기본 이벤트 등록
        registerDefaultEvents();

        // 현재 날짜에 맞는 이벤트 자동 활성화
        activateSeasonalEvent();
    }

    // ========================================
    // 이벤트 등록
    // ========================================

    /**
     * 기본 이벤트 등록
     */
    private void registerDefaultEvents() {
        // 할로윈 이벤트 (10월)
        registerEvent(createHalloweenEvent());

        // 크리스마스 이벤트 (12월)
        registerEvent(createChristmasEvent());

        // 설날 이벤트 (1~2월)
        registerEvent(createLunarNewYearEvent());

        // 여름 이벤트 (7~8월)
        registerEvent(createSummerEvent());

        // 테스트 이벤트
        registerEvent(createTestEvent());
    }

    /**
     * 할로윈 이벤트 생성
     */
    private EventMode createHalloweenEvent() {
        return new EventMode.Builder("HALLOWEEN", "할로윈")
            .description("으스스한 할로윈 밤! 특별한 역할이 등장합니다.")
            .activePeriod(Month.OCTOBER, 1, Month.OCTOBER, 31)
            .addSpecialRole("VAMPIRE", "뱀파이어", "밤에 플레이어를 물어 뱀파이어로 만듭니다.")
            .addSpecialRole("WEREWOLF", "늑대인간", "보름달에는 2명을 공격할 수 있습니다.")
            .addSpecialRole("GHOST", "유령", "죽어도 능력을 1회 사용할 수 있습니다.")
            .addRuleModifier("NIGHT_DURATION", "45") // 밤 시간 45초
            .addRuleModifier("FULL_MOON", "true") // 보름달 효과
            .addThemeColor("#FF6600", "#000000") // 주황색, 검은색
            .addThemeSound("/GameSound/events/halloween_bgm.wav")
            .build();
    }

    /**
     * 크리스마스 이벤트 생성
     */
    private EventMode createChristmasEvent() {
        return new EventMode.Builder("CHRISTMAS", "크리스마스")
            .description("메리 크리스마스! 산타가 선물을 가져왔습니다.")
            .activePeriod(Month.DECEMBER, 1, Month.DECEMBER, 31)
            .addSpecialRole("SANTA", "산타", "밤에 랜덤 플레이어에게 아이템을 선물합니다.")
            .addSpecialRole("REINDEER", "루돌프", "산타를 보호하며 산타의 위치를 알 수 있습니다.")
            .addSpecialRole("GRINCH", "그린치", "마피아 팀이며 산타를 찾아야 합니다.")
            .addRuleModifier("GIFT_DROP", "true") // 선물 드롭
            .addRuleModifier("SNOWSTORM", "true") // 눈보라 (일부 능력 제한)
            .addThemeColor("#FF0000", "#FFFFFF") // 빨간색, 흰색
            .addThemeSound("/GameSound/events/christmas_bgm.wav")
            .build();
    }

    /**
     * 설날 이벤트 생성
     */
    private EventMode createLunarNewYearEvent() {
        return new EventMode.Builder("LUNAR_NEW_YEAR", "설날")
            .description("새해 복 많이 받으세요! 행운의 떡국을 드세요.")
            .activePeriod(Month.JANUARY, 1, Month.FEBRUARY, 28)
            .addSpecialRole("FORTUNE_TELLER", "점쟁이", "다른 플레이어의 운세를 봐줄 수 있습니다.")
            .addSpecialRole("ANCESTOR", "조상님", "죽은 후 마지막 유언을 남길 수 있습니다.")
            .addRuleModifier("LUCKY_DRAW", "true") // 행운 뽑기
            .addRuleModifier("FORTUNE", "true") // 운세 효과
            .addThemeColor("#FFD700", "#FF0000") // 금색, 빨간색
            .addThemeSound("/GameSound/events/newyear_bgm.wav")
            .build();
    }

    /**
     * 여름 이벤트 생성
     */
    private EventMode createSummerEvent() {
        return new EventMode.Builder("SUMMER", "여름 바캉스")
            .description("시원한 여름 휴가! 해변에서의 마피아 게임.")
            .activePeriod(Month.JULY, 1, Month.AUGUST, 31)
            .addSpecialRole("LIFEGUARD", "라이프가드", "밤에 익사 위기의 플레이어를 구합니다.")
            .addSpecialRole("SHARK", "상어", "마피아 팀이며 물속에서 은밀히 공격합니다.")
            .addRuleModifier("HEATWAVE", "true") // 폭염 (투표 시간 단축)
            .addRuleModifier("BEACH_MODE", "true") // 해변 모드
            .addThemeColor("#00BFFF", "#FFD700") // 하늘색, 모래색
            .addThemeSound("/GameSound/events/summer_bgm.wav")
            .build();
    }

    /**
     * 테스트 이벤트 생성
     */
    private EventMode createTestEvent() {
        return new EventMode.Builder("TEST", "테스트 이벤트")
            .description("개발자용 테스트 이벤트입니다.")
            .alwaysActive() // 항상 활성화 가능
            .addSpecialRole("TESTER", "테스터", "모든 정보를 볼 수 있습니다.")
            .addRuleModifier("DEBUG_MODE", "true")
            .addThemeColor("#00FF00", "#000000")
            .build();
    }

    /**
     * 이벤트 등록
     *
     * @param event 이벤트 모드
     */
    public void registerEvent(EventMode event) {
        eventModes.put(event.getId(), event);
    }

    // ========================================
    // 이벤트 활성화
    // ========================================

    /**
     * 시즌별 이벤트 자동 활성화
     */
    private void activateSeasonalEvent() {
        LocalDate today = LocalDate.now();
        Month currentMonth = today.getMonth();
        int currentDay = today.getDayOfMonth();

        for (EventMode event : eventModes.values()) {
            if (event.isActiveOn(currentMonth, currentDay)) {
                activateEvent(event.getId());
                return;
            }
        }

        // 활성 이벤트가 없으면 null
        currentEvent = null;
    }

    /**
     * 이벤트 활성화
     *
     * @param eventId 이벤트 ID
     * @return 활성화 성공 여부
     */
    public boolean activateEvent(String eventId) {
        EventMode event = eventModes.get(eventId);

        if (event == null) {
            return false;
        }

        currentEvent = event;
        return true;
    }

    /**
     * 이벤트 비활성화
     */
    public void deactivateEvent() {
        currentEvent = null;
    }

    // ========================================
    // 이벤트 조회
    // ========================================

    /**
     * 현재 활성 이벤트 조회
     *
     * @return 이벤트 (없으면 null)
     */
    public EventMode getCurrentEvent() {
        return currentEvent;
    }

    /**
     * 이벤트가 활성화되어 있는지 확인
     *
     * @return 활성화 여부
     */
    public boolean hasActiveEvent() {
        return currentEvent != null;
    }

    /**
     * 특정 이벤트 조회
     *
     * @param eventId 이벤트 ID
     * @return 이벤트 (없으면 null)
     */
    public EventMode getEvent(String eventId) {
        return eventModes.get(eventId);
    }

    /**
     * 모든 이벤트 목록
     *
     * @return 이벤트 리스트
     */
    public List<EventMode> getAllEvents() {
        return new ArrayList<>(eventModes.values());
    }

    /**
     * 현재 시즌의 이벤트 조회
     *
     * @return 시즌 이벤트 리스트
     */
    public List<EventMode> getSeasonalEvents() {
        LocalDate today = LocalDate.now();
        Month currentMonth = today.getMonth();
        int currentDay = today.getDayOfMonth();

        return eventModes.values().stream()
            .filter(event -> event.isActiveOn(currentMonth, currentDay))
            .toList();
    }

    // ========================================
    // 설정 메소드
    // ========================================

    /**
     * 기능 활성화/비활성화
     *
     * @param enabled 활성화 여부
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 기능 활성화 상태 조회
     *
     * @return 활성화 여부
     */
    public boolean isEnabled() {
        return enabled;
    }

    // ========================================
    // 내부 클래스 - EventMode
    // ========================================

    /**
     * 이벤트 모드 데이터 클래스
     */
    public static class EventMode {
        private final String id;
        private final String name;
        private final String description;
        private final Month startMonth;
        private final int startDay;
        private final Month endMonth;
        private final int endDay;
        private final boolean alwaysActive;
        private final Map<String, SpecialRole> specialRoles;
        private final Map<String, String> ruleModifiers;
        private final String primaryColor;
        private final String secondaryColor;
        private final String themeSoundPath;

        private EventMode(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.description = builder.description;
            this.startMonth = builder.startMonth;
            this.startDay = builder.startDay;
            this.endMonth = builder.endMonth;
            this.endDay = builder.endDay;
            this.alwaysActive = builder.alwaysActive;
            this.specialRoles = builder.specialRoles;
            this.ruleModifiers = builder.ruleModifiers;
            this.primaryColor = builder.primaryColor;
            this.secondaryColor = builder.secondaryColor;
            this.themeSoundPath = builder.themeSoundPath;
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Map<String, SpecialRole> getSpecialRoles() { return new HashMap<>(specialRoles); }
        public Map<String, String> getRuleModifiers() { return new HashMap<>(ruleModifiers); }
        public String getPrimaryColor() { return primaryColor; }
        public String getSecondaryColor() { return secondaryColor; }
        public String getThemeSoundPath() { return themeSoundPath; }

        /**
         * 특정 날짜에 활성화되는지 확인
         *
         * @param month 월
         * @param day 일
         * @return 활성화 여부
         */
        public boolean isActiveOn(Month month, int day) {
            if (alwaysActive) {
                return true;
            }

            if (startMonth == null || endMonth == null) {
                return false;
            }

            // 같은 월 내 범위
            if (startMonth == endMonth) {
                return month == startMonth && day >= startDay && day <= endDay;
            }

            // 월을 넘어가는 범위
            if (month == startMonth) {
                return day >= startDay;
            } else if (month == endMonth) {
                return day <= endDay;
            } else {
                // 중간 월
                int monthValue = month.getValue();
                int startValue = startMonth.getValue();
                int endValue = endMonth.getValue();

                if (startValue < endValue) {
                    return monthValue > startValue && monthValue < endValue;
                } else {
                    // 연도를 넘어가는 경우 (예: 12월~1월)
                    return monthValue > startValue || monthValue < endValue;
                }
            }
        }

        /**
         * 이벤트 정보 문자열
         *
         * @return 정보 문자열
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== ").append(name).append(" ===\n");
            sb.append(description).append("\n\n");

            if (!specialRoles.isEmpty()) {
                sb.append("【특별 역할】\n");
                for (SpecialRole role : specialRoles.values()) {
                    sb.append("• ").append(role.name).append(": ")
                      .append(role.description).append("\n");
                }
                sb.append("\n");
            }

            if (!ruleModifiers.isEmpty()) {
                sb.append("【룰 변경】\n");
                for (Map.Entry<String, String> entry : ruleModifiers.entrySet()) {
                    sb.append("• ").append(entry.getKey()).append(": ")
                      .append(entry.getValue()).append("\n");
                }
            }

            return sb.toString();
        }

        // Builder 패턴
        public static class Builder {
            private final String id;
            private final String name;
            private String description = "";
            private Month startMonth;
            private int startDay;
            private Month endMonth;
            private int endDay;
            private boolean alwaysActive = false;
            private final Map<String, SpecialRole> specialRoles = new HashMap<>();
            private final Map<String, String> ruleModifiers = new HashMap<>();
            private String primaryColor = "#FFFFFF";
            private String secondaryColor = "#000000";
            private String themeSoundPath = null;

            public Builder(String id, String name) {
                this.id = id;
                this.name = name;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder activePeriod(Month startMonth, int startDay, Month endMonth, int endDay) {
                this.startMonth = startMonth;
                this.startDay = startDay;
                this.endMonth = endMonth;
                this.endDay = endDay;
                return this;
            }

            public Builder alwaysActive() {
                this.alwaysActive = true;
                return this;
            }

            public Builder addSpecialRole(String roleId, String roleName, String description) {
                specialRoles.put(roleId, new SpecialRole(roleId, roleName, description));
                return this;
            }

            public Builder addRuleModifier(String ruleName, String value) {
                ruleModifiers.put(ruleName, value);
                return this;
            }

            public Builder addThemeColor(String primaryColor, String secondaryColor) {
                this.primaryColor = primaryColor;
                this.secondaryColor = secondaryColor;
                return this;
            }

            public Builder addThemeSound(String soundPath) {
                this.themeSoundPath = soundPath;
                return this;
            }

            public EventMode build() {
                return new EventMode(this);
            }
        }
    }

    // ========================================
    // 내부 클래스 - SpecialRole
    // ========================================

    /**
     * 특별 역할 데이터 클래스
     */
    public static class SpecialRole {
        private final String id;
        private final String name;
        private final String description;

        public SpecialRole(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }

        @Override
        public String toString() {
            return name + ": " + description;
        }
    }
}
