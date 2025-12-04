package mafia.game.ai;

import java.util.*;

/**
 * 기본 AI 플레이어 구현
 *
 * 이 클래스는 시민 역할의 기본 AI 동작을 구현합니다.
 * 역할별 AI는 이 클래스를 상속받아 특화된 동작을 추가합니다.
 *
 * 설계 원칙:
 * - Strategy Pattern: 역할별 전략
 * - Factory Pattern: AI 생성
 *
 * @author Mafia Game Team
 * @version 2.0
 */
public class BasicAIPlayer extends AIPlayer {

    /**
     * 생성자
     *
     * @param name 이름
     * @param difficulty 난이도
     */
    public BasicAIPlayer(String name, Difficulty difficulty) {
        super(name, difficulty);
    }

    /**
     * 밤 행동 결정
     * 시민은 밤에 행동할 수 없음
     *
     * @param alivePlayers 살아있는 플레이어 목록
     * @return null (행동 없음)
     */
    @Override
    public String decideNightAction(List<String> alivePlayers) {
        // 시민/일반 역할은 밤 행동 없음
        return null;
    }

    /**
     * 투표 대상 결정
     *
     * @param alivePlayers 살아있는 플레이어 목록
     * @return 투표 대상
     */
    @Override
    public String decideVote(List<String> alivePlayers) {
        List<String> candidates = excludeSelf(alivePlayers);

        if (candidates.isEmpty()) {
            return null;
        }

        // 난이도에 따른 전략
        String optimalChoice = findMostSuspicious(candidates);

        return makeChoice(optimalChoice != null ? optimalChoice : chooseRandom(candidates), candidates);
    }

    /**
     * 발언 생성
     *
     * @param context 게임 컨텍스트
     * @return 발언 내용
     */
    @Override
    public String generateStatement(GameContext context) {
        // 간단한 발언 템플릿
        List<String> statements = new ArrayList<>();

        // 의심 표현
        if (!suspicionLevels.isEmpty()) {
            String mostSuspicious = findMostSuspicious(context.getAlivePlayers());
            if (mostSuspicious != null && !mostSuspicious.equals(name)) {
                statements.add(mostSuspicious + "님이 조금 의심스럽습니다.");
            }
        }

        // 투표 제안
        if (context.getDayCount() > 1) {
            statements.add("신중하게 투표해야 할 것 같습니다.");
        }

        // 발언 확률
        if (random.nextDouble() < 0.3 && !statements.isEmpty()) {
            return statements.get(random.nextInt(statements.size()));
        }

        return null;
    }
}
