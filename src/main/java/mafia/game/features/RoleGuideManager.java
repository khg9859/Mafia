package mafia.game.features;

import java.util.*;

/**
 * 역할 가이드 & 튜토리얼 시스템
 *
 * 이 클래스는 게임 내 역할별 가이드와 튜토리얼을 제공합니다.
 *
 * 설계 원칙:
 * - Singleton Pattern: 하나의 가이드 매니저만 존재
 * - Strategy Pattern: 역할별 가이드 전략
 * - Template Method Pattern: 가이드 구조 표준화
 *
 * 기능:
 * - 역할별 상세 가이드
 * - 전략 팁 제공
 * - 튜토리얼 진행
 * - 단축키 안내
 *
 * @author Mafia Game Team
 * @version 2.0
 */
public class RoleGuideManager {

    // ========================================
    // Singleton 패턴
    // ========================================

    private static RoleGuideManager instance;

    /**
     * Singleton 인스턴스 조회
     *
     * @return RoleGuideManager 인스턴스
     */
    public static synchronized RoleGuideManager getInstance() {
        if (instance == null) {
            instance = new RoleGuideManager();
        }
        return instance;
    }

    // ========================================
    // 필드
    // ========================================

    /**
     * 역할 가이드 맵 (역할 -> 가이드 객체)
     */
    private final Map<String, RoleGuide> roleGuides;

    /**
     * 튜토리얼 완료 여부 (플레이어 -> 완료 여부)
     */
    private final Map<String, Boolean> tutorialCompleted;

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
    private RoleGuideManager() {
        this.roleGuides = new HashMap<>();
        this.tutorialCompleted = new HashMap<>();
        this.enabled = true;

        // 역할 가이드 초기화
        initializeRoleGuides();
    }

    // ========================================
    // 역할 가이드 초기화
    // ========================================

    /**
     * 모든 역할의 가이드 초기화
     */
    private void initializeRoleGuides() {
        // 마피아 팀
        roleGuides.put("MAFIA", createMafiaGuide());
        roleGuides.put("SPY", createSpyGuide());
        roleGuides.put("MADAME", createMadameGuide());

        // 시민 팀
        roleGuides.put("DOCTOR", createDoctorGuide());
        roleGuides.put("POLICE", createPoliceGuide());
        roleGuides.put("POLITICIAN", createPoliticianGuide());
        roleGuides.put("SOLDIER", createSoldierGuide());
        roleGuides.put("SHAMAN", createShamanGuide());
        roleGuides.put("REPORTER", createReporterGuide());
        roleGuides.put("GHOUL", createGhoulGuide());
        roleGuides.put("GANGSTER", createGangsterGuide());
        roleGuides.put("PRIEST", createPriestGuide());
        roleGuides.put("CITIZEN", createCitizenGuide());
    }

    /**
     * 마피아 가이드 생성
     */
    private RoleGuide createMafiaGuide() {
        return new RoleGuide.Builder("MAFIA", "마피아")
            .team("마피아")
            .description("밤에 시민을 제거하는 핵심 악역입니다.")
            .ability("밤마다 한 명의 플레이어를 제거할 수 있습니다.")
            .winCondition("마피아 팀이 시민 팀과 같거나 많아지면 승리합니다.")
            .addStrategy("초반에는 조용히 행동하며 의사와 경찰을 찾아 제거하세요.")
            .addStrategy("낮에는 시민처럼 행동하며 다른 시민을 의심하도록 유도하세요.")
            .addStrategy("동료 마피아를 적극 방어하지 마세요. 의심받을 수 있습니다.")
            .addStrategy("밤에 동료 마피아와 전략을 상의하세요.")
            .addTip("첫 밤에는 랜덤으로 제거하되, 주도적인 플레이어를 우선 타겟하세요.")
            .addTip("투표 시 너무 적극적이거나 소극적이면 의심받습니다.")
            .addShortcut("/night [대상]", "밤에 대상 지정")
            .build();
    }

    /**
     * 스파이 가이드 생성
     */
    private RoleGuide createSpyGuide() {
        return new RoleGuide.Builder("SPY", "스파이")
            .team("마피아")
            .description("정보를 수집하는 마피아 팀의 정보원입니다.")
            .ability("밤마다 한 명의 직업을 알아낼 수 있습니다. 마피아를 조사하면 접선합니다.")
            .winCondition("마피아 팀이 승리하면 함께 승리합니다.")
            .addStrategy("초반에 마피아를 빠르게 찾아 접선하세요.")
            .addStrategy("능력자들을 찾아 마피아에게 정보를 제공하세요.")
            .addStrategy("군인을 조사하면 정체가 드러나니 조심하세요.")
            .addTip("접선 후에는 마피아와 밤에 대화할 수 있습니다.")
            .addShortcut("/night [대상]", "밤에 대상 조사")
            .build();
    }

    /**
     * 마담 가이드 생성
     */
    private RoleGuide createMadameGuide() {
        return new RoleGuide.Builder("MADAME", "마담")
            .team("마피아")
            .description("플레이어를 유혹하여 능력 사용을 막는 역할입니다.")
            .ability("낮 투표로 플레이어를 유혹하여 밤에 능력을 사용하지 못하게 합니다.")
            .winCondition("마피아 팀이 승리하면 함께 승리합니다.")
            .addStrategy("능력자로 의심되는 플레이어를 유혹하세요.")
            .addStrategy("마피아에게 투표하면 자동으로 접선됩니다.")
            .addStrategy("유혹당한 플레이어는 투표 시 말을 할 수 없습니다.")
            .addTip("정치인을 유혹하면 투표로 제거할 수 있습니다.")
            .addShortcut("/vote [대상]", "투표 시 대상 유혹")
            .build();
    }

    /**
     * 의사 가이드 생성
     */
    private RoleGuide createDoctorGuide() {
        return new RoleGuide.Builder("DOCTOR", "의사")
            .team("시민")
            .description("밤에 플레이어를 보호하는 생명의 수호자입니다.")
            .ability("밤마다 한 명을 지정하여 마피아의 공격으로부터 보호합니다.")
            .winCondition("모든 마피아를 제거하면 승리합니다.")
            .addStrategy("중요한 능력자(경찰, 기자 등)를 보호하세요.")
            .addStrategy("자신을 보호할 수도 있습니다.")
            .addStrategy("마담에게 유혹당하면 능력을 사용할 수 없으니 조심하세요.")
            .addTip("패턴을 피하세요. 같은 사람을 계속 보호하면 마피아가 패턴을 파악합니다.")
            .addTip("경찰이 신원을 밝히면 우선적으로 보호하세요.")
            .addShortcut("/night [대상]", "밤에 대상 보호")
            .build();
    }

    /**
     * 경찰 가이드 생성
     */
    private RoleGuide createPoliceGuide() {
        return new RoleGuide.Builder("POLICE", "경찰")
            .team("시민")
            .description("마피아를 찾아내는 탐정입니다.")
            .ability("밤마다 한 명을 조사하여 마피아 여부를 확인할 수 있습니다.")
            .winCondition("모든 마피아를 제거하면 승리합니다.")
            .addStrategy("의심스러운 플레이어를 우선 조사하세요.")
            .addStrategy("마피아를 찾으면 신중하게 공개하세요. 너무 빨��� 공개하면 타겟이 됩니다.")
            .addStrategy("확실한 증거가 있을 때 신원을 밝히세요.")
            .addTip("스파이를 조사해도 마피아로 표시됩니다.")
            .addTip("경찰임을 밝힌 후에는 의사의 보호가 필수입니다.")
            .addShortcut("/night [대상]", "밤에 대상 조사")
            .build();
    }

    /**
     * 정치인 가이드 생성
     */
    private RoleGuide createPoliticianGuide() {
        return new RoleGuide.Builder("POLITICIAN", "정치인")
            .team("시민")
            .description("강력한 투표권을 가진 정치인입니다.")
            .ability("투표로 죽지 않으며, 2표를 행사합니다.")
            .winCondition("모든 마피아를 제거하면 승리합니다.")
            .addStrategy("당신의 2표는 매우 중요합니다. 신중하게 사용하세요.")
            .addStrategy("마담에게 유혹당하면 투표로 제거될 수 있으니 조심하세요.")
            .addStrategy("투표에서 주도권을 잡으세요.")
            .addTip("정치인임을 일찍 밝히면 마담의 타겟이 될 수 있습니다.")
            .addShortcut("/vote [대상]", "투표 (2표)")
            .build();
    }

    /**
     * 군인 가이드 생성
     */
    private RoleGuide createSoldierGuide() {
        return new RoleGuide.Builder("SOLDIER", "군인")
            .team("시민")
            .description("마피아의 공격을 한 번 버틸 수 있는 전사입니다.")
            .ability("마피아의 공격을 한 차례 방어합니다. (방어막 1회)")
            .winCondition("모든 마피아를 제거하면 승리합니다.")
            .addStrategy("방어막을 낭비하지 마세요. 의사의 보호와 겹치지 않도록 하세요.")
            .addStrategy("스파이가 당신을 조사하면 스파이의 정체를 알 수 있습니다.")
            .addStrategy("마담에게 유혹당하면 방어막이 무효화되니 조심하세요.")
            .addTip("방어막을 사용한 후에는 일반 시민과 같습니다.")
            .addShortcut("없음", "패시브 능력")
            .build();
    }

    /**
     * 영매 가이드 생성
     */
    private RoleGuide createShamanGuide() {
        return new RoleGuide.Builder("SHAMAN", "영매")
            .team("시민")
            .description("죽은 자의 영혼과 소통하는 신비한 능력자입니다.")
            .ability("죽은 플레이어의 대화를 보고, 밤에 한 명을 성불시켜 직업을 알아냅니다.")
            .winCondition("모든 마피아를 제거하면 승리합니다.")
            .addStrategy("죽은 자들의 대화에서 마피아의 단서를 찾으세요.")
            .addStrategy("중요한 플레이어를 성불시켜 정보를 얻으세요.")
            .addStrategy("성불된 플레이어는 부활할 수 없으니 신중하게 선택하세요.")
            .addTip("죽은 자 채팅은 영매에게만 보입니다.")
            .addShortcut("/night [대상]", "밤에 대상 성불")
            .build();
    }

    /**
     * 기자 가이드 생성
     */
    private RoleGuide createReporterGuide() {
        return new RoleGuide.Builder("REPORTER", "기자")
            .team("시민")
            .description("특종으로 진실을 밝히는 언론인입니다.")
            .ability("2~8일차 밤에 한 명을 취재하여 다음 날 직업을 공개합니다.")
            .winCondition("모든 마피아를 제거하면 승리합니다.")
            .addStrategy("의심스러운 플레이어를 취재하여 정체를 밝히세요.")
            .addStrategy("특종 발표 전에 제거될 수 있으니 조심하세요.")
            .addStrategy("타이밍이 중요합니다. 너무 일찍 사용하면 정보가 부족합니다.")
            .addTip("능력 사용 기회가 제한적이니 신중하게 선택하세요.")
            .addShortcut("/night [대상]", "밤에 대상 취재")
            .build();
    }

    /**
     * 도굴꾼 가이드 생성
     */
    private RoleGuide createGhoulGuide() {
        return new RoleGuide.Builder("GHOUL", "도굴꾼")
            .team("시민")
            .description("죽은 자의 직업을 훔치는 이단자입니다.")
            .ability("첫날 밤 마피아에게 살해당한 사람의 직업을 얻습니다.")
            .winCondition("변신한 직업에 따라 승리 조건이 바뀝니다.")
            .addStrategy("첫날 밤까지 살아남는 것이 중요합니다.")
            .addStrategy("변신 후 새로운 역할에 맞게 플레이하세요.")
            .addStrategy("부활하면 원래 희생자는 시민이 됩니다.")
            .addTip("첫날 밤 사망자가 없으면 도굴꾼 상태가 유지됩니다.")
            .addShortcut("없음", "자동 능력")
            .build();
    }

    /**
     * 건달 가이드 생성
     */
    private RoleGuide createGangsterGuide() {
        return new RoleGuide.Builder("GANGSTER", "건달")
            .team("시민")
            .description("협박으로 투표를 방해하는 무법자입니다.")
            .ability("밤마다 한 명을 선택하여 다음 날 투표를 못하게 만듭니다.")
            .winCondition("모든 마피아를 제거하면 승리합니다.")
            .addStrategy("마피아로 의심되는 플레이어를 방해하세요.")
            .addStrategy("정치인이나 주도적인 플레이어를 타겟으로 삼으세요.")
            .addTip("너무 많은 사람을 방해하면 게임 진행이 어려워집니다.")
            .addShortcut("/night [대상]", "밤에 대상 협박")
            .build();
    }

    /**
     * 성직자 가이드 생성
     */
    private RoleGuide createPriestGuide() {
        return new RoleGuide.Builder("PRIEST", "성직자")
            .team("시민")
            .description("죽은 자를 부활시키는 신성한 능력을 가진 자입니다.")
            .ability("게임 중 단 한 번, 죽은 플레이어를 부활시킬 수 있습니다.")
            .winCondition("모든 마피아를 제거하면 승리합니다.")
            .addStrategy("중요한 능력자(경찰, 의사 등)를 부활시키세요.")
            .addStrategy("타이밍이 매우 중요합니다. 너무 일찍 사용하지 마세요.")
            .addStrategy("성불된 플레이어는 부활할 수 없습니다.")
            .addTip("능력 사용 기회가 단 1회이니 신중하게 결정하세요.")
            .addTip("도굴꾼의 희생자를 부활시키면 시민이 됩니다.")
            .addShortcut("/night [대상]", "밤에 대상 부활")
            .build();
    }

    /**
     * 시민 가이드 생성
     */
    private RoleGuide createCitizenGuide() {
        return new RoleGuide.Builder("CITIZEN", "시민")
            .team("시민")
            .description("특별한 능력은 없지만 투표로 마피아를 찾아야 하는 일반 시민입니다.")
            .ability("특별한 능력이 없습니다.")
            .winCondition("모든 마피아를 제거하면 승리합니다.")
            .addStrategy("대화를 주의 깊게 듣고 의심스러운 행동을 관찰하세요.")
            .addStrategy("능력자를 보호하고 정보를 공유하세요.")
            .addStrategy("투표에 적극 참여하세요.")
            .addTip("당신의 투표가 게임의 승패를 가를 수 있습니다.")
            .addTip("능력자인 척하여 마피아의 공격을 유도할 수도 있습니다.")
            .addShortcut("/vote [대상]", "투표")
            .build();
    }

    // ========================================
    // 가이드 조회
    // ========================================

    /**
     * 역할 가이드 조회
     *
     * @param role 역할 이름
     * @return 가이드 객체
     */
    public RoleGuide getGuide(String role) {
        return roleGuides.get(role);
    }

    /**
     * 역할 가이드 문자열
     *
     * @param role 역할 이름
     * @return 가이드 문자열
     */
    public String getGuideText(String role) {
        RoleGuide guide = roleGuides.get(role);
        return guide != null ? guide.toString() : "해당 역할의 가이드를 찾을 수 없습니다.";
    }

    /**
     * 모든 역할 목록 조회
     *
     * @return 역할 목록
     */
    public List<String> getAllRoles() {
        return new ArrayList<>(roleGuides.keySet());
    }

    // ========================================
    // 튜토리얼 관리
    // ========================================

    /**
     * 튜토리얼 완료 여부 확인
     *
     * @param playerName 플레이어 이름
     * @return 완료 여부
     */
    public boolean isTutorialCompleted(String playerName) {
        return tutorialCompleted.getOrDefault(playerName, false);
    }

    /**
     * 튜토리얼 완료 표시
     *
     * @param playerName 플레이어 이름
     */
    public void completeTutorial(String playerName) {
        tutorialCompleted.put(playerName, true);
    }

    /**
     * 기본 튜토리얼 텍스트
     *
     * @return 튜토리얼 문자열
     */
    public String getBasicTutorial() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 마피아 게임 기본 가이드 ===\n\n");

        sb.append("【게임 목표】\n");
        sb.append("• 시민 팀: 모든 마피아를 제거\n");
        sb.append("• 마피아 팀: 시민 수와 같거나 많아지기\n\n");

        sb.append("【게임 진행】\n");
        sb.append("1. 밤 (30초): 능력자들이 능력 사용\n");
        sb.append("2. 낮 (30초): 모두가 자유롭게 토론\n");
        sb.append("3. 투표 (20초): 제거할 플레이어 투표\n");
        sb.append("4. 최후의 반론: 투표 1위가 변론 후 찬반투표\n\n");

        sb.append("【기본 명령어】\n");
        sb.append("• /guide [역할]: 역할 가이드 보기\n");
        sb.append("• /whisper [대상] [내용]: 익명 쪽지 보내기\n");
        sb.append("• /stats: 내 통계 보기\n");
        sb.append("• /help: 도움말\n\n");

        sb.append("【초보자 팁】\n");
        sb.append("• 대화를 주의 깊게 관찰하세요\n");
        sb.append("• 투표 패턴을 분석하세요\n");
        sb.append("• 능력자는 신원을 신중하게 밝히세요\n");
        sb.append("• 마피아는 시민처럼 행동하세요\n");

        return sb.toString();
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
    // 내부 클래스 - RoleGuide
    // ========================================

    /**
     * 역할 가이드 데이터 클래스
     */
    public static class RoleGuide {
        private final String roleId;
        private final String roleName;
        private final String team;
        private final String description;
        private final String ability;
        private final String winCondition;
        private final List<String> strategies;
        private final List<String> tips;
        private final Map<String, String> shortcuts;

        private RoleGuide(Builder builder) {
            this.roleId = builder.roleId;
            this.roleName = builder.roleName;
            this.team = builder.team;
            this.description = builder.description;
            this.ability = builder.ability;
            this.winCondition = builder.winCondition;
            this.strategies = builder.strategies;
            this.tips = builder.tips;
            this.shortcuts = builder.shortcuts;
        }

        // Getters
        public String getRoleId() { return roleId; }
        public String getRoleName() { return roleName; }
        public String getTeam() { return team; }
        public String getDescription() { return description; }
        public String getAbility() { return ability; }
        public String getWinCondition() { return winCondition; }
        public List<String> getStrategies() { return new ArrayList<>(strategies); }
        public List<String> getTips() { return new ArrayList<>(tips); }
        public Map<String, String> getShortcuts() { return new HashMap<>(shortcuts); }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== ").append(roleName).append(" 가이드 ===\n\n");
            sb.append("【소속】").append(team).append(" 팀\n\n");
            sb.append("【설명】\n").append(description).append("\n\n");
            sb.append("【능력】\n").append(ability).append("\n\n");
            sb.append("【승리 조건】\n").append(winCondition).append("\n\n");

            if (!strategies.isEmpty()) {
                sb.append("【전략】\n");
                for (int i = 0; i < strategies.size(); i++) {
                    sb.append((i + 1)).append(". ").append(strategies.get(i)).append("\n");
                }
                sb.append("\n");
            }

            if (!tips.isEmpty()) {
                sb.append("【팁】\n");
                for (String tip : tips) {
                    sb.append("• ").append(tip).append("\n");
                }
                sb.append("\n");
            }

            if (!shortcuts.isEmpty()) {
                sb.append("【단축키】\n");
                for (Map.Entry<String, String> entry : shortcuts.entrySet()) {
                    sb.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }

            return sb.toString();
        }

        // Builder 패턴
        public static class Builder {
            private final String roleId;
            private final String roleName;
            private String team;
            private String description;
            private String ability;
            private String winCondition;
            private final List<String> strategies = new ArrayList<>();
            private final List<String> tips = new ArrayList<>();
            private final Map<String, String> shortcuts = new HashMap<>();

            public Builder(String roleId, String roleName) {
                this.roleId = roleId;
                this.roleName = roleName;
            }

            public Builder team(String team) {
                this.team = team;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder ability(String ability) {
                this.ability = ability;
                return this;
            }

            public Builder winCondition(String winCondition) {
                this.winCondition = winCondition;
                return this;
            }

            public Builder addStrategy(String strategy) {
                this.strategies.add(strategy);
                return this;
            }

            public Builder addTip(String tip) {
                this.tips.add(tip);
                return this;
            }

            public Builder addShortcut(String key, String description) {
                this.shortcuts.put(key, description);
                return this;
            }

            public RoleGuide build() {
                return new RoleGuide(this);
            }
        }
    }
}
