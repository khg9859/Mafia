# Maven 프로젝트 재구성 완료 보고서

## 📋 작업 요약

기존의 단순 Java 프로젝트를 **Maven 표준 프로젝트 구조**로 성공적으로 재구성했습니다.

### ✅ 완료된 작업

1. **Maven 프로젝트 구조 생성**
2. **Java 코드 리소스 로딩 방식 변경**
3. **pom.xml 설정 (빌드 자동화)**
4. **GitHub Actions CI/CD 파이프라인 구축**
5. **중복 파일 정리**

---

## 📦 최종 프로젝트 구조

```
마피아2/
├── src/
│   └── main/
│       ├── java/mafia/game/          ✅ Java 소스 (3개 파일)
│       │   ├── MafiaGameServer.java
│       │   ├── MafiaGameClientMain.java
│       │   └── MafiaGameClientView.java
│       │
│       └── resources/                 ✅ 리소스 파일
│           ├── GameSound/             (16개 사운드 파일)
│           │   ├── game_start.wav
│           │   ├── night.wav
│           │   ├── morning.wav
│           │   ├── vote.wav
│           │   ├── Citizen/
│           │   └── Mafia_team/
│           │
│           └── info/                  (15개 이미지 파일)
│               ├── background.png
│               ├── server_background.jpg
│               ├── mafia.png
│               ├── doctor.png
│               └── ... (기타 역할 이미지)
│
├── .github/
│   └── workflows/
│       └── build.yml                  ✅ CI/CD 워크플로우
│
├── pom.xml                            ✅ Maven 설정
├── .gitignore                         ✅ 업데이트됨
└── README.md                          ✅ 업데이트됨
```

---

## 🔧 주요 변경 사항

### 1. 패키지 구조

**변경 전:**
```java
// 패키지 없음
public class MafiaGameServer {
    ...
}
```

**변경 후:**
```java
package mafia.game;

public class MafiaGameServer {
    ...
}
```

### 2. 리소스 로딩 방식

**변경 전 (파일 시스템 기반):**
```java
// ❌ JAR 파일에서 작동 안 함
new File("info/mafia.png")
new ImageIcon("info/ServerImg.png")
ImageIO.read(new File("info/background.png"))
```

**변경 후 (클래스패스 기반):**
```java
// ✅ JAR 파일에서도 작동
getClass().getResource("/info/mafia.png")
getClass().getResource("/info/ServerImg.png")
ImageIO.read(getClass().getResourceAsStream("/info/background.png"))
```

### 3. pom.xml 설정

- **Java 버전:** 17
- **빌드 플러그인:**
  - `maven-compiler-plugin` - Java 컴파일
  - `maven-jar-plugin` - JAR 파일 생성
  - `maven-shade-plugin` - 실행 가능한 JAR 생성
- **출력 파일:**
  - `mafia-game-1.0.0-server-executable.jar` (서버)
  - `mafia-game-1.0.0-client-executable.jar` (클라이언트)

---

## 🚀 빌드 및 실행 방법

### Maven 설치 (필요시)

```bash
# macOS
brew install maven

# Windows (Chocolatey)
choco install maven

# Linux (Ubuntu/Debian)
sudo apt install maven
```

### 빌드

```bash
cd "/Users/honggeunkim/Desktop/마피아2"

# 전체 빌드
mvn clean package

# 테스트 건너뛰고 빌드
mvn clean package -DskipTests
```

### 실행

```bash
# 서버 실행
java -jar target/mafia-game-1.0.0-server-executable.jar

# 클라이언트 실행
java -jar target/mafia-game-1.0.0-client-executable.jar
```

---

## 🔄 GitHub Actions CI/CD

### 자동 빌드 트리거

- `push` to `main`, `master`, `dev`, `feat/**` 브랜치
- Pull Request to `main`, `master`, `dev`

### 자동 릴리스

태그를 푸시하면 GitHub Releases에 자동으로 JAR 파일이 업로드됩니다:

```bash
# 태그 생성 및 푸시
git tag -a v1.0.0 -m "First release"
git push origin v1.0.0
```

GitHub Actions가 자동으로:
1. Java 17 환경 설정
2. Maven으로 프로젝트 빌드
3. Server/Client JAR 파일 생성
4. GitHub Releases에 업로드

---

## ✅ 검증 완료 항목

### 프로젝트 구조
- ✅ Java 소스 파일: 3개 (`src/main/java/mafia/game/`)
- ✅ 이미지 리소스: 15개 PNG 파일
- ✅ 사운드 리소스: 16개 WAV 파일
- ✅ `pom.xml` 설정 완료
- ✅ GitHub Actions 워크플로우 설정

### 코드 수정
- ✅ 모든 Java 파일에 `package mafia.game;` 선언 추가
- ✅ 모든 리소스 로딩을 classpath 방식으로 변경
- ✅ 리소스 경로에 `/` 접두사 추가 (예: `/info/mafia.png`)

### 파일 정리
- ✅ 루트 디렉토리의 중복된 `.java` 파일 삭제 (3개)
- ✅ 루트 디렉토리의 `.class` 파일 삭제 (18개)
- ✅ 루트 디렉토리의 중복된 `GameSound/`, `info/` 디렉토리 삭제

---

## 🎯 다음 단계

### 1. 로컬 빌드 테스트 (Maven 설치 후)

```bash
cd "/Users/honggeunkim/Desktop/마피아2"
mvn clean package
```

### 2. Git 커밋 및 푸시

```bash
git add .
git commit -m "Maven 프로젝트 구조로 재구성

- Maven 표준 디렉토리 구조 적용
- 리소스 로딩을 classpath 기반으로 변경
- GitHub Actions CI/CD 파이프라인 추가
- 실행 가능한 JAR 자동 생성 설정
- 중복 파일 정리"

git push origin feat/#1
```

### 3. GitHub에서 Actions 확인

푸시 후 GitHub 저장소의 **Actions** 탭에서 자동 빌드가 성공하는지 확인하세요.

### 4. 첫 릴리스 생성 (선택)

```bash
git tag -a v1.0.0 -m "첫 번째 Maven 빌드 릴리스"
git push origin v1.0.0
```

---

## 📊 변경 통계

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 프로젝트 구조 | 플랫 구조 | Maven 표준 |
| 리소스 로딩 | File 기반 | Classpath 기반 |
| 빌드 방식 | 수동 javac | Maven 자동화 |
| JAR 생성 | 수동 | 자동 (2개 파일) |
| CI/CD | 없음 | GitHub Actions |
| 배포 | 수동 | 자동 릴리스 |

---

## ⚠️ 주의사항

1. **Java 17 필수**: 이 프로젝트는 Java 17 이상이 필요합니다.
2. **Maven 필수**: 빌드하려면 Maven 3.6 이상이 설치되어 있어야 합니다.
3. **리소스 경로**: 모든 리소스는 `/`로 시작하는 절대 경로를 사용합니다.
4. **GitHub Actions**: 첫 푸시 시 GitHub Actions가 활성화되어 있는지 확인하세요.

---

## 🎉 결과

이제 프로젝트는:
- ✅ **표준 Maven 프로젝트 구조** 준수
- ✅ **GitHub Actions로 자동 빌드** 가능
- ✅ **실행 가능한 JAR 파일** 자동 생성 (서버/클라이언트)
- ✅ **모든 리소스 포함** (사운드, 이미지)
- ✅ **GitHub Releases 자동 업로드**
- ✅ **프로페셔널한 프로젝트 구조**

---

**작성일:** 2025-11-27
**작성자:** Claude Code
