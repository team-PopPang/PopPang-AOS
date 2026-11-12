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

카카오 SDK는 공식 안내에 따라 `com.kakao.sdk.**.model.*`의 필드도 보존한다. SDK의 `GenericEnumSerializer`는 `enum.name`으로 `getDeclaredField`를 호출하므로 오류 코드 필드가 제거되거나 이름이 바뀌면 `NoSuchFieldException: InternalError`로 배포 앱이 종료될 수 있다. `ReleaseSdkReflectionProbe.java`는 실제 최적화된 DEX에서 카카오 오류 enum 네 종류의 모든 필드를 조회하는 회귀 검사다. 이 소스를 `javac`와 `d8`로 별도 DEX JAR로 만들고, AAB의 `base/dex/*.dex`를 루트에 넣은 ZIP과 함께 `dalvikvm -cp probe.jar:release-dex.zip com.poppang.PopPang.ReleaseSdkReflectionProbe`로 실행한다. 이 검사는 로그인·네트워크·앱 저장소에 접근하지 않는다.

`./gradlew :app:bundleRelease`로 생성한 AAB의 `BUNDLE-METADATA/com.android.tools/r8.json`에 최적화 지표가 포함된다. `stats.noObfuscationPercentage`, `noOptimizationPercentage`, `noShrinkingPercentage`를 각각 100에서 뺀 값으로 적용 비율을 확인할 수 있다. 난독화 매핑은 AAB 내부와 `app/build/outputs/mapping/release/mapping.txt`에 생성된다. Play Console 지표는 새 AAB를 업로드한 뒤 갱신되며, 기존 업로드에 소급 적용되지 않는다.

저장소의 기본 빌드에는 배포 서명이 설정되어 있지 않다. Play에 올릴 때는 Android Studio의 기존 서명 절차로 업로드 키를 사용해 AAB를 생성한다.

## AOS 화면 리뉴얼

Figma `팝팡 리뉴얼 최종` (`asdKpjfFHPVtvKvDT3mMLs`, `2467:10300`) 기준으로 온보딩, 가입 3단계·완료 화면, 지도 카드·목록·필터 화면을 Compose로 구성했다. 기존 `mainOrange`와 S-Core Dream 폰트를 재사용한다. 온보딩의 지도·캘린더 원본 이미지와 아이콘은 해당 Figma 노드의 제공 자산을 로컬 PNG로 저장해 사용하며 임시 Figma URL은 런타임에서 사용하지 않는다. Android 상태바와 내비게이션 영역은 시스템이 표시한다.

최종본의 화면별 글꼴·행간·여백을 구분해 적용한다. 가입 닉네임 입력칸은 48dp, 키워드 입력칸은 50dp이며 건너뛰기는 키워드 제목 오른쪽에 둔다. 지도 목록 사진은 76×100dp, 선택 카드 사진은 72×92dp, 하단 메뉴는 시스템 영역을 제외한 최소 58dp를 사용한다. 하단 메뉴와 지도 정렬 헤더는 글자의 실제 높이에 따라 늘어나며, 큰 글꼴에서는 메뉴 이름을 두 줄까지 표시한다. 온보딩 알림 카드와 필터·메뉴에는 원본의 모서리와 간격을 적용한다. 일부 원본에 사용된 Inter와 Noto Sans KR은 로컬 폰트로 포함하며 OFL 라이선스는 `app/src/main/assets/licenses/`에 보관한다. 화면 폭과 글자 확대, 키보드에 따라 스크롤을 허용한다.

온보딩 하단 버튼은 시스템 내비게이션 영역 위로 16dp 여백을 두고 고정한다. 다음·뒤로가기에서는 본문을 슬라이드와 페이드로 전환하며 페이지 표시도 부드럽게 바뀐다. 건너뛰기는 앞의 두 페이지에만 표시하고, 건너뛰기·시작하기로 로그인 화면에 진입할 때도 전환 애니메이션을 적용한다. 전환 중 중복 완료를 막으며 자동 로그인 성공 시 메인으로 진입하는 흐름은 유지한다.

지도 마커는 46dp 링·34dp 내부 원·16dp 원본 아이콘을 사용하고 선택 시 1.35배 확대한다. 실제 지도와 미리보기는 같은 마커 렌더러를 사용하며 선택하지 않은 마커는 흐리게 표시한다. 묶음 마커도 최종본의 44dp 적색 원과 숫자를 적용하고 기존 확대 동작은 유지한다.

지도의 날짜 직접 선택은 참고 달력처럼 한 달과 이동 화살표를 표시한다. 기간의 양 끝은 앱의 메인 주황색(`#FF7A00`), 사이는 옅은 주황색(`#FFF3E8`)으로 구분한다. 시작일 다음에 종료일을 누르면 범위를 적용하고 닫는다. 종료일보다 이른 날짜를 누르면 시작일을 다시 정하며, 같은 날짜를 두 번 누르면 하루만 선택한다. 뒤로가기·바깥 터치는 미완료 선택을 취소한다. 전체 필터 시트에서는 날짜를 골라도 적용 버튼을 누르기 전까지 임시 선택으로 유지한다.

`view/renewal/`의 화면은 UI 모델·상태·콜백을 받아 표시한다. `debug` 빌드의 별도 런처 **팝팡 리뉴얼 미리보기**는 `preview.RenewalPreviewActivity`에서 온보딩 → 가입 → 지도를 테스트용 데이터로 연결한다. 이 진입점은 실제 계정·토큰·설정에 접근하지 않고 가입·찜 API를 호출하지 않으며 `release`에는 포함되지 않는다. `screen` 실행 인자에 `signup` 또는 `map`을 주면 해당 화면부터 확인할 수 있다. 미리보기 지도 배경은 디자인 확인용 그림이며 실제 앱 지도는 기존 Naver Map을 사용한다.

실제 가입 화면은 기존 Signup Token과 서버 카테고리 ID를 그대로 사용한다. 임의의 카테고리 ID나 샘플 추천 키워드를 서버로 전송하지 않는다. 실제 가입은 기존 5개 키워드 제한을 유지하며, 미리보기에서만 최종 디자인의 10개 입력·삭제·중복·초과 상태를 확인한다. 가입 완료 화면은 실제 가입 성공 이후에만 나타난다. 건너뛰기는 키워드 빈 목록을 전송하되 실패 시 입력 상태를 지우지 않는다.

가입 카테고리는 하나 이상 선택해야 다음 단계로 이동할 수 있다. 키워드 단계의 다음 버튼은 직접 추가하거나 인기 목록에서 선택한 키워드가 하나 이상일 때만 활성화하며, 입력칸에만 적은 내용과 공백은 등록으로 세지 않는다. 마지막 카테고리나 키워드를 해제하면 다음 버튼이 다시 비활성화된다. 키워드는 건너뛰기로 비워둔 채 가입할 수 있다.

가입 단계의 진행바는 회색 트랙 위에 주황색을 겹쳐 중간 공백 없이 채우며, 단계 변경 시 부드럽게 늘어나거나 줄어든다. 본문만 페이드 전환하고 하단 다음 버튼은 같은 위치에 유지한다. 입력값은 전환 영역 밖에서 관리해 뒤로가도 보존하며, 사라지는 키워드 화면의 건너뛰기는 가입을 제출할 수 없도록 막는다.

가입 완료의 `알림 켜고 시작하기`는 OS 알림 권한을 다시 확인하고, 권한이 없으면 재요청한다. 재요청이 막혔거나 시스템에서 알림을 차단한 경우 앱 알림 설정으로 안내한다. 실제 허용을 확인한 뒤 설정 화면과 같은 `PATCH /api/v2/user/alert-status`에 Access Token과 `{"isAlerted":true}`를 보내고, 저장된 ON 상태를 메인에 전달한다. 권한 거절·뒤로가기·`나중에`는 알림을 켜지 않으며, 저장 오류는 재시도하거나 나중에 계속할 수 있다. 요청 중 중복 탭을 막는다. 별도 UI 미리보기에서는 OS 권한 흐름만 확인하며 실제 계정의 알림 API를 호출하지 않는다.

닉네임 추천은 `GET /api/v2/user/nickname/recommendation`을 호출한다. 가입 화면에 처음 진입하면 Signup Token으로 추천을 받아 빈 닉네임 입력칸을 채운다. 이미 입력한 값은 화면 재생성·재진입 시 유지한다. 서버에서 받은 수식어+동물 닉네임은 예약이 아니므로 기존 중복확인과 가입 시 서버 검사를 거친다. HTTP 503 / code 4208 또는 네트워크 오류 시 입력값을 보존하고 재시도·직접 입력을 허용한다. 응답을 기다리는 동안 사용자가 수정한 닉네임은 늦은 추천 응답으로 덮어쓰지 않는다.

키워드 단계에 진입하면 `GET /api/v2/alert-keyword/popular`로 전체 사용자 기준 인기 키워드를 조회한다. 카테고리별 추천이 아니며 서버 순서대로 최대 6개를 표시한다. 정상 빈 목록은 빈 상태로 처리하고, 조회 실패는 직접 입력·가입을 막지 않으며 재시도할 수 있다. 두 API 모두 query/body가 없고 가입 중에는 요청 시점의 최신 Signup Token, 가입 후에는 Access Token을 사용하는 클라이언트를 제공한다. 미리보기에서는 계정 없이 확인할 수 있도록 고정 샘플을 사용한다.

지도 필터는 카테고리·기간만 제공하며, 상단 메뉴와 전체 필터 시트에서 상태 선택을 제거했다. 이전에 저장된 상태 선택값은 복원 시 무시하고 카테고리·날짜는 유지한다. 동일한 필터 결과를 마커·목록·결과 수에 사용하며 검색 0건을 일반 목록으로 바꾸지 않는다. 필터 시트의 선택은 적용 버튼을 눌렀을 때 반영하고, 닫으면 취소한다. 현재 API가 반환한 데이터에서만 검색·카테고리·날짜를 필터링하므로 실제 서버의 오픈 예정 결과를 확장한 것으로 해석하면 안 된다. 기존 지역·정렬·좌표 조회와 찜·상세 API는 유지한다.

백엔드 연결 전 합의할 항목:

- 가입·알림 설정 공통 키워드 한도 10개 지원 여부.
- 세분화된 카테고리의 ID·이름·정렬 순서와 선택 필수 여부. 디자인의 `1~20개 추가` 메모가 목록 확장인지 선택 개수인지 확인.
- 지도에서 카테고리 다중 선택, 날짜 구간, 지역, 정렬, 결과 수를 함께 조회할 계약. 현재 지도 API는 명세상 진행 중인 팝업만 반환한다.
- 날짜 구간은 UI에서 양 끝을 포함한 운영 기간 겹침으로 계산하며 한국 날짜를 기준으로 한다. 실제 서버 기준·주 시작일을 합의한다.
- 찜한 팝업 종료 알림 발송 시점, 알림 동의 저장, FCM payload·탭 시 이동. 이번 변경에서는 새로운 알림 발송·동의 API를 추가하지 않는다.

RN 소스·SDK·JS 번들은 이번 리뉴얼의 변경 대상이 아니다.

`RenewalMapFiltersTest`는 날짜 경계·카테고리·기간·검색 조합과 빈 결과, 이전 상태 선택값을 복원해도 숨은 필터가 남지 않는지 검증한다. `RenewalInteractionTest`는 가입 단계 왕복·키워드 한도·검색 0건·필터 적용을 계정 없이 검증한다. `RenewalNativeMapTest`는 `renewalNativeMap=true`를 명시한 테스트 에뮬레이터에서만 실행한다. 이 검사는 위치 권한과 `geo fix 129.3114 35.5384`로 설정한 고정 모의 위치가 필요하며, 실제 계정·팝업 API를 호출하지 않는다.

## AOS V2 API 전환

AOS의 일반 사용자 API는 V2를 사용한다. `local.properties`의 `V2_BASE_URL`을 설정하면 해당 주소를 쓰고, 생략하면 운영 주소 `https://api.poppang.co.kr/api/v2/`를 사용한다. V1 `BASE_URL`로부터 주소를 만드는 호환 처리와 사용하지 않는 V1 API별 BuildConfig 필드는 제거했다. 기존 로컬 파일의 해당 키들은 더 이상 읽지 않는다. 이미지 주소 `URL_IMAGE`와 SDK 키는 별도 로컬 설정을 유지한다. 실제 엔드포인트 경로는 `network/`의 인터페이스에 명시한다.

팝업 이미지가 상대 경로이면 `URL_IMAGE`와 경로 사이의 `/`를 하나로 맞춰 요청한다. 예를 들어 `https://poppang.co.kr/`와 `/images/...`를 합칠 때 `//images/...`가 되면 서버가 404를 반환한다. 완전한 `http://`·`https://` 이미지 URL은 그대로 사용한다. `LivePopupImageTest`는 `liveImages=true`, `imagePath=/images/...` 실행 인자를 명시했을 때만 앱과 같은 Coil 로더로 실제 서버 이미지 로딩·디코딩을 검증한다.

지도는 최근 위치를 먼저 표시하고 최신 위치를 별도로 갱신한다. 화면의 5초 간격 위치 수집은 제거하고 지도 SDK의 위치 변경과 같은 좌표 상태를 사용한다. 최초 위치 또는 ‘내 위치’ 버튼 요청에는 바로 카메라를 이동하고, 버튼을 누를 때마다 SDK의 `Follow` 모드를 복구한다. 지도를 직접 옮기거나 팝업을 선택하면 대기 중인 자동 이동을 취소하며, 오래된 캐시 응답은 최신 위치를 덮어쓰지 않는다. 팝업 목록의 거리 기준은 50m 이상 이동했을 때 갱신한다. `MapLocationInteractionTest`는 `liveMap=true` 인자를 명시한 실기기에서 최초 이동과 버튼 반복 동작을 검증한다.

캘린더 카드는 목록의 `favoriteCount`·`viewCount`를 즉시 표시하며 스크롤이나 상세 닫기로 항목별 카운트 API를 호출하지 않는다. 상세 조회와 관심 등록·해제 후 확인한 수치는 공유 상태로 목록에도 전달한다. 관심 수 조회의 300ms 대기를 제거하고 쓰기 성공 후 수치를 갱신한다. 조회수 기록 후 누적 조회수도 성공 순서대로 조회하되, 서버가 Redis 증가분을 DB에 반영하기 전에는 누적값이 그대로일 수 있다(명세상 약 60초 주기). 실패 시 기존 수치를 0으로 바꾸지 않는다. `CalendarCountInteractionTest`는 가짜 API와 40개 카드로 반복 스크롤 시 추가 요청이 없고 갱신된 수치가 유지되는지 검사한다.

가입 전 닉네임 확인(`user/nickname/duplicated`)과 관심 카테고리 목록 조회(`recommend`)도 V2를 사용한다. 서버팀의 최신 계약에 따라 가입 중에는 Signup Token을 보내며, 가입 후에는 같은 API에 Access Token을 보낸다. 기존 마이그레이션 명세의 가입 전 조회 제한은 이 계약으로 대체한다.

카카오·구글 로그인은 `signupStatus`로 가입 대기와 완료를 구분한다. 회원가입에는 Signup Token과 `signupOs: "ANDROID"`를 보내며, 성공 응답의 사용자 정보와 Access·Refresh Token을 저장한다. V1 UUID만 저장된 기존 사용자는 소셜 로그인을 다시 해야 한다.

로그인·자동 로그인·가입 완료 후 처음 메인 화면에 진입하면 Access Token으로 `PATCH /api/v2/user/signup-os`에 `{"signupOs":"ANDROID"}`를 비동기 전송한다. 성공 응답은 본문 없는 200이다. 기존 OS 유무는 서버가 판단한다. Activity 범위의 `UserDataViewModel`이 호출 시도를 기억해 화면 재진입·회전·재로그인 시 같은 실행에서 중복 호출하지 않는다. 네트워크·서버 오류는 화면 진입을 막지 않으며 즉시 재시도하지 않는다. 시도 여부를 영구 저장하지 않아 다음 앱 실행에서는 다시 호출한다.

온보딩 완료 기록인 `app_prefs.xml`은 클라우드 백업·기기 이전 대상에서 제외한다. Android 11 이하의 `backup_rules.xml`과 Android 12 이상의 `data_extraction_rules.xml` 양쪽에 적용해, 새 설치가 이전 완료 기록을 복원해 온보딩을 건너뛰지 않게 한다. 기존 앱의 업데이트에서는 완료 기록을 유지한다.

JWT는 Android Keystore로 암호화해 저장하고 백업·기기 이전에서 제외한다. `SessionManager`가 토큰 회전을 관리한다. Access API의 `401 / 5002`에만 갱신하고 원래 요청을 한 번 재전송한다. 동시 요청은 실패 결과도 공유한다. 갱신의 401은 세션을 지우고, 일시적인 서버·네트워크 실패는 세션을 보존한다. 로그아웃·계정 전환 이후의 늦은 응답은 적용하지 않는다.

로그아웃은 서버의 성공 응답을 확인한 뒤 로컬 세션을 지운다. 실패하면 로그인 상태를 유지하고 재시도를 안내한다. 회원탈퇴는 V2의 계정 비활성화이며, 같은 소셜 계정의 재가입은 지원하지 않는다. 로그아웃 API는 서버의 FCM 토큰을 삭제하지 않으므로 로그아웃 이후 푸시 정책은 서버와 별도로 정해야 한다.

RN V2 전환은 보류했다. RN SDK `0.1.0`, JS 번들, 팝업 제보·제보 관리의 기존 V1 호출을 유지한다. AOS는 V2 로그인·가입 응답의 사용자 UUID를 기존 RN 진입 인자로 전달한다. AOS 안의 관리자 팝업 비활성화도 아직 `ADMIN_BASE_URL`과 `POPUP_DEACTIVATE_API`를 사용하는 V1 예외다. 해당 V2 계약이 없으므로 기능을 삭제하거나 경로만 임의로 바꾸지 않았다.

검증은 JDK 21과 Android SDK 36 환경에서 수행한다. `:app:testDebugUnitTest`에는 토큰 회전·동시 요청·로그아웃·계정 전환, 실제 로컬 HTTP 요청을 통한 가입 전·후 토큰 구분과 V2 요청/응답 계약 검증이 포함된다. 실제 소셜 로그인·Keystore·푸시는 기기에서 별도로 확인한다.

실서버 조회 검증은 `LiveV2SignupTest`를 사용한다. 기본 실행에서는 건너뛰며, `adb -s <device> shell am instrument -w -e liveV2 true -e class com.poppang.PopPang.LiveV2SignupTest#<method> com.poppang.PopPang.test/androidx.test.runner.AndroidJUnitRunner`로 특정 검사만 실행한다. `signupQueriesRequireAuthentication`은 비인증 응답, `pendingSignupTokenCanQueryNicknameAndCategories`는 가입 대기 계정의 두 조회, `completedSignupSessionCanReadProfileAndCategories`는 가입 완료 계정의 프로필·카테고리를 검증한다. 각 계정 상태는 앱에서 테스트 계정으로 직접 준비해야 한다. 이 검사는 계정을 생성하지 않으며 인증 토큰이나 개인정보를 출력하지 않는다.
