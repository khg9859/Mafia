# 로비 배경 이미지 설정 가이드

## 이미지 위치

로비 배경 이미지를 다음 경로에 저장하세요:

```
/Users/honggeunkim/Desktop/마피아2/src/main/resources/info/lobby_background.png
```

또는

```
/Users/honggeunkim/Desktop/마피아2/src/main/resources/info/lobby_background.jpg
```

## 권장 사양

- **해상도**: 1920x1080 (Full HD) 또는 1280x720 (HD)
- **포맷**: PNG (투명 배경 지원) 또는 JPG
- **용량**: 5MB 이하 권장
- **스타일**: 마피아 게임 분위기에 맞는 어두운 톤

## 사용 방법

1. 이미지를 위 경로에 저장
2. 프로젝트 재컴파일:
   ```bash
   cd /Users/honggeunkim/Desktop/마피아2
   mvn clean compile
   ```
3. 서버 재시작

## 현재 상태

로비 시스템은 **준비 완료** 상태입니다:
- LobbyManager 클래스 구현됨
- 플레이어 접속 시 자동으로 로비 상태
- 게임 시작 버튼으로 게임 시작

## 향후 개선 가능 사항

- 로비 UI에 배경 이미지 표시
- 플레이어 준비 상태 표시
- 채팅 기능 추가
- 게임 설정 변경 (역할 비율 등)

**작성일**: 2025-12-04
**버전**: 2.0
