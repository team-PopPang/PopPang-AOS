# Poppang-AOS
<img width="1024" height="500" alt="그래핀디자인" src="https://github.com/user-attachments/assets/24b8fe0b-798c-45e6-b10b-b9d861bdecfa" />
# React Native integration

The React Native screens are distributed from `PopPang-RN` as a prebuilt Android SDK and a JavaScript bundle. Install the matching release artifacts before building the Android app:

```powershell
.\scripts\install-rn-android.ps1 v0.1.0
.\gradlew.bat :app:assembleDebug
```

The Android build requires JDK 17 or newer. In Android Studio, select a compatible Gradle JDK before syncing.

The generated Maven repository under `Vendor/PopPangRN` and `app/src/main/assets/index.android.bundle` are local build artifacts and are intentionally excluded from Git. When the RN release version changes, update both the install command and the `com.poppang:poppang-rn-android` version in `app/build.gradle.kts`.

## 배포 빌드 최적화

`release` 빌드는 R8 코드 최적화·난독화·축소와 리소스 축소를 사용한다. `debug` 설정은 그대로 유지한다. Gson이 리플렉션으로 읽는 `model` 패키지의 DTO와 `network.StoredSession`은 `app/proguard-rules.pro`에서 보존해 API 필드 이름·제네릭 응답·기존 로그인 JSON의 호환성을 유지한다. RN SDK의 소비자 규칙은 SDK에 포함된 것을 사용한다.

`./gradlew :app:bundleRelease`로 생성한 AAB의 `BUNDLE-METADATA/com.android.tools/r8.json`에 최적화 지표가 포함된다. `stats.noObfuscationPercentage`, `noOptimizationPercentage`, `noShrinkingPercentage`를 각각 100에서 뺀 값으로 적용 비율을 확인할 수 있다. 난독화 매핑은 AAB 내부와 `app/build/outputs/mapping/release/mapping.txt`에 생성된다. Play Console 지표는 새 AAB를 업로드한 뒤 갱신되며, 기존 업로드에 소급 적용되지 않는다.

저장소의 기본 빌드에는 배포 서명이 설정되어 있지 않다. Play에 올릴 때는 Android Studio의 기존 서명 절차로 업로드 키를 사용해 AAB를 생성한다.

## AOS V2 API 전환

AOS 앱 API는 V2를 사용한다. `local.properties`의 `V2_BASE_URL`을 설정하면 해당 주소를 쓰고, 생략하면 기존 `BASE_URL`의 마지막 `/api/v1/`을 `/api/v2/`로 바꿔 사용한다. 호스트와 키는 기존처럼 로컬 설정에서 주입한다. 실제 엔드포인트 경로는 `network/`의 인터페이스에 명시한다.

팝업 이미지가 상대 경로이면 `URL_IMAGE`와 경로 사이의 `/`를 하나로 맞춰 요청한다. 예를 들어 `https://poppang.co.kr/`와 `/images/...`를 합칠 때 `//images/...`가 되면 서버가 404를 반환한다. 완전한 `http://`·`https://` 이미지 URL은 그대로 사용한다. `LivePopupImageTest`는 `liveImages=true`, `imagePath=/images/...` 실행 인자를 명시했을 때만 앱과 같은 Coil 로더로 실제 서버 이미지 로딩·디코딩을 검증한다.

지도는 최근 위치를 먼저 표시하고 최신 위치를 별도로 갱신한다. 화면의 5초 간격 위치 수집은 제거하고 지도 SDK의 위치 변경과 같은 좌표 상태를 사용한다. 최초 위치 또는 ‘내 위치’ 버튼 요청에는 바로 카메라를 이동하고, 버튼을 누를 때마다 SDK의 `Follow` 모드를 복구한다. 지도를 직접 옮기거나 팝업을 선택하면 대기 중인 자동 이동을 취소하며, 오래된 캐시 응답은 최신 위치를 덮어쓰지 않는다. 팝업 목록의 거리 기준은 50m 이상 이동했을 때 갱신한다. `MapLocationInteractionTest`는 `liveMap=true` 인자를 명시한 실기기에서 최초 이동과 버튼 반복 동작을 검증한다.

캘린더 카드는 목록의 `favoriteCount`·`viewCount`를 즉시 표시하며 스크롤이나 상세 닫기로 항목별 카운트 API를 호출하지 않는다. 상세 조회와 관심 등록·해제 후 확인한 수치는 공유 상태로 목록에도 전달한다. 관심 수 조회의 300ms 대기를 제거하고 쓰기 성공 후 수치를 갱신한다. 조회수 기록 후 누적 조회수도 성공 순서대로 조회하되, 서버가 Redis 증가분을 DB에 반영하기 전에는 누적값이 그대로일 수 있다(명세상 약 60초 주기). 실패 시 기존 수치를 0으로 바꾸지 않는다. `CalendarCountInteractionTest`는 가짜 API와 40개 카드로 반복 스크롤 시 추가 요청이 없고 갱신된 수치가 유지되는지 검사한다.

가입 전 닉네임 확인(`user/nickname/duplicated`)과 관심 카테고리 목록 조회(`recommend`)도 V2를 사용한다. 서버팀의 최신 계약에 따라 가입 중에는 Signup Token을 보내며, 가입 후에는 같은 API에 Access Token을 보낸다. 기존 마이그레이션 명세의 가입 전 조회 제한은 이 계약으로 대체한다.

카카오·구글 로그인은 `signupStatus`로 가입 대기와 완료를 구분한다. 회원가입에는 Signup Token과 `signupOs: "ANDROID"`를 보내며, 성공 응답의 사용자 정보와 Access·Refresh Token을 저장한다. V1 UUID만 저장된 기존 사용자는 소셜 로그인을 다시 해야 한다.

JWT는 Android Keystore로 암호화해 저장하고 백업·기기 이전에서 제외한다. `SessionManager`가 토큰 회전을 관리한다. Access API의 `401 / 5002`에만 갱신하고 원래 요청을 한 번 재전송한다. 동시 요청은 실패 결과도 공유한다. 갱신의 401은 세션을 지우고, 일시적인 서버·네트워크 실패는 세션을 보존한다. 로그아웃·계정 전환 이후의 늦은 응답은 적용하지 않는다.

로그아웃은 서버의 성공 응답을 확인한 뒤 로컬 세션을 지운다. 실패하면 로그인 상태를 유지하고 재시도를 안내한다. 회원탈퇴는 V2의 계정 비활성화이며, 같은 소셜 계정의 재가입은 지원하지 않는다. 로그아웃 API는 서버의 FCM 토큰을 삭제하지 않으므로 로그아웃 이후 푸시 정책은 서버와 별도로 정해야 한다.

RN V2 전환은 보류했다. RN SDK `0.1.0`, JS 번들, 팝업 제보·제보 관리의 기존 V1 호출을 유지한다. AOS는 V2 로그인·가입 응답의 사용자 UUID를 기존 RN 진입 인자로 전달한다. 기존 관리자 팝업 비활성화 API도 이번 앱용 V2 명세의 범위 밖이다.

검증은 JDK 21과 Android SDK 36 환경에서 수행한다. `:app:testDebugUnitTest`에는 토큰 회전·동시 요청·로그아웃·계정 전환, 실제 로컬 HTTP 요청을 통한 가입 전·후 토큰 구분과 V2 요청/응답 계약 검증이 포함된다. 실제 소셜 로그인·Keystore·푸시는 기기에서 별도로 확인한다.

실서버 조회 검증은 `LiveV2SignupTest`를 사용한다. 기본 실행에서는 건너뛰며, `adb -s <device> shell am instrument -w -e liveV2 true -e class com.poppang.PopPang.LiveV2SignupTest#<method> com.poppang.PopPang.test/androidx.test.runner.AndroidJUnitRunner`로 특정 검사만 실행한다. `signupQueriesRequireAuthentication`은 비인증 응답, `pendingSignupTokenCanQueryNicknameAndCategories`는 가입 대기 계정의 두 조회, `completedSignupSessionCanReadProfileAndCategories`는 가입 완료 계정의 프로필·카테고리를 검증한다. 각 계정 상태는 앱에서 테스트 계정으로 직접 준비해야 한다. 이 검사는 계정을 생성하지 않으며 인증 토큰이나 개인정보를 출력하지 않는다.
