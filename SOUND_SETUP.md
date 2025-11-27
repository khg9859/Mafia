# 게임 시작 효과음 추가 완료

## 변경 사항

게임이 시작되어 역할이 배정될 때 자동으로 효과음이 재생되도록 수정했습니다.

### 구현 내용

1. **사운드 재생 기능 추가**

   - `playGameStartSound()` 메서드 추가
   - 역할 배정 시 (`ROLE:` 메시지 수신 시) 자동 재생
   - 게임당 1회만 재생 (중복 재생 방지)

2. **파일 형식 지원**

   - **WAV 형식** (권장): Java에서 기본 지원
   - **MP3 형식**: 시도하지만 Java 기본 라이브러리에서 지원하지 않을 수 있음

3. **파일 검색 순서**
   ```
   1. game_start.wav (우선)
   2. 마피아42 - 게임시작 효과음.mp3 (대체)
   ```

## 사용 방법

### 옵션 1: WAV 파일로 변환 (권장)

MP3 파일을 WAV로 변환하면 Java에서 문제없이 재생됩니다.

**온라인 변환기 사용:**

1. [Online Audio Converter](https://online-audio-converter.com/) 또는 [CloudConvert](https://cloudconvert.com/mp3-to-wav) 접속
2. `마피아42 - 게임시작 효과음.mp3` 업로드
3. WAV 형식으로 변환
4. `game_start.wav`로 저장하여 마피아 폴더에 저장

**ffmpeg 사용 (설치 필요):**

```bash
# Homebrew로 ffmpeg 설치
brew install ffmpeg

# MP3를 WAV로 변환
ffmpeg -i "마피아42 - 게임시작 효과음.mp3" -acodec pcm_s16le -ar 44100 "game_start.wav"
```

### 옵션 2: MP3 그대로 사용

현재 MP3 파일을 그대로 사용할 수도 있지만, Java의 기본 오디오 시스템이 MP3를 지원하지 않을 수 있습니다. 이 경우 게임은 정상 작동하지만 효과음만 재생되지 않습니다.

## 테스트 방법

1. 서버 시작:

   ```bash
   java MafiaGameServer
   ```

2. 클라이언트 실행:

   ```bash
   java MafiaGameClientMain
   ```

3. 게임 시작:
   - 충분한 인원이 모이면 서버에서 게임 시작
   - 각 클라이언트에 역할이 배정될 때 효과음 재생

## 문제 해결

### 효과음이 재생되지 않는 경우

1. **파일 위치 확인**

   - `game_start.wav` 또는 `마피아42 - 게임시작 효과음.mp3` 파일이 `.java` 파일과 같은 디렉터리에 있는지 확인

2. **콘솔 메시지 확인**

   - "Playing game start sound: ..." 메시지가 출력되는지 확인
   - 에러 메시지가 있다면 파일 형식 문제일 수 있음

3. **WAV로 변환**
   - 가장 확실한 해결 방법은 WAV 형식으로 변환하는 것입니다

## 코드 변경 사항

### 추가된 import

```java
import javax.sound.sampled.*;
```

### 추가된 필드

```java
private boolean gameStartSoundPlayed = false;
```

### 수정된 부분

- `ListenNetwork` 클래스의 `ROLE:` 메시지 처리 부분에 사운드 재생 코드 추가
- `playGameStartSound()` 메서드 추가 (별도 스레드에서 실행)

## 향후 개선 사항

- 밤/낮 전환 효과음 추가
- 투표 시작 효과음 추가
- 사망 시 효과음 추가
- 볼륨 조절 기능 추가
