package com.poppang.PopPang.view.renewal

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poppang.PopPang.ui.theme.mainOrange

@Composable
internal fun RenewalSignupFlow(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    nicknameMessage: String,
    nicknameValid: Boolean,
    nicknameError: Boolean,
    onCheckNickname: () -> Unit,
    categories: List<String>,
    selectedCategories: List<String>,
    onToggleCategory: (String) -> Unit,
    keywords: List<String>,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    popularKeywords: List<String> = emptyList(),
    keywordLimit: Int = 10,
    onExit: () -> Unit = {},
    onRecommendNickname: (() -> Unit)? = null,
    nicknameRecommending: Boolean = false,
    nicknameChecking: Boolean = false,
    nicknameRecommendationError: String? = null,
    popularKeywordsLoading: Boolean = false,
    popularKeywordsError: String? = null,
    onKeywordStepEntered: () -> Unit = {},
    onRetryPopularKeywords: () -> Unit = {},
    categoryLoading: Boolean = false,
    categoryError: String? = null,
    onRetryCategories: () -> Unit = {},
    submitting: Boolean = false,
    submitError: String? = null,
    completed: Boolean = false,
    starting: Boolean = false,
    onCompletedBack: (() -> Unit)? = null,
    onSubmit: (skipKeywords: Boolean) -> Unit,
    onStart: () -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var keywordDraft by rememberSaveable { mutableStateOf("") }
    var submittedKeywords by rememberSaveable { mutableStateOf<List<String>?>(null) }
    LaunchedEffect(step, completed) { if (step == 2 && !completed) onKeywordStepEntered() }
    val goBack: () -> Unit = {
        if (!submitting) {
            if (step > 0) step-- else onExit()
        }
    }
    BackHandler(!completed, onBack = goBack)
    BackHandler(completed) { if (!starting) (onCompletedBack ?: onStart)() }
    if (completed) {
        SignupComplete(nickname, submittedKeywords ?: keywords, starting, onStart)
        return
    }
    val focus = LocalFocusManager.current

    fun canProceed(): Boolean =
        !submitting &&
            when (step) {
                0 -> nicknameValid && !nicknameRecommending && !nicknameChecking
                1 -> selectedCategories.isNotEmpty()
                else -> selectedCategories.isNotEmpty() && keywords.any { it.isNotBlank() }
            }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 393.dp)
            .fillMaxWidth()
            .systemBarsPadding()
            .imePadding(),
    ) {
        SignupProgress(step, !submitting, goBack)
        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f).fillMaxWidth().testTag("signup-content"),
            contentAlignment = Alignment.TopStart,
            transitionSpec = {
                (fadeIn(tween(220, delayMillis = 100)) togetherWith fadeOut(tween(100))).using(null)
            },
            label = "signup-step",
        ) { displayedStep ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Text(
                        when (displayedStep) {
                            0 -> "팝팡에서 쓸\n닉네임을 정해주세요"
                            1 -> "어떤 팝업을\n좋아하세요?"
                            else -> "알림 받을 키워드를\n등록해주세요"
                        },
                        style = renewalText(24, true, 1.47f).copy(color = Color.Black),
                        modifier = Modifier.padding(end = if (displayedStep == 2) 62.dp else 0.dp).testTag("signup-title"),
                    )
                    if (displayedStep == 2) {
                        Text(
                            "건너뛰기",
                            style = renewalText(14),
                            color = RenewalMuted,
                            modifier =
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 2.dp)
                                    .clickable(
                                        enabled = !submitting && displayedStep == step && selectedCategories.isNotEmpty(),
                                        role = Role.Button,
                                    ) {
                                        focus.clearFocus()
                                        submittedKeywords = emptyList()
                                        onSubmit(true)
                                    },
                        )
                    }
                }
                Spacer(
                    Modifier.height(
                        when (displayedStep) {
                            0 -> 42.44.dp
                            1 -> 33.44.dp
                            else -> 5.44.dp
                        },
                    ),
                )
                when (displayedStep) {
                    0 ->
                        NicknameEntry(
                            nickname,
                            onNicknameChange,
                            nicknameMessage,
                            nicknameValid,
                            nicknameError,
                            onCheckNickname,
                            onRecommendNickname,
                            nicknameRecommending,
                            nicknameChecking,
                            nicknameRecommendationError,
                        )
                    1 -> {
                        Text("카테고리", style = renewalText(14, true), color = Color(0xFF333333))
                        Spacer(Modifier.height(7.2.dp))
                        if (categoryLoading) CircularProgressIndicator(color = mainOrange, modifier = Modifier.size(24.dp))
                        FlowRow(
                            Modifier.fillMaxWidth().padding(end = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            categories.forEach { category ->
                                RenewalChip(category, category in selectedCategories, { onToggleCategory(category) })
                            }
                        }
                        if (categoryError != null) {
                            Text(
                                categoryError,
                                style = renewalText(12),
                                color = Color(0xFFB3261E),
                                modifier = Modifier.padding(top = 16.dp),
                            )
                            TextButton(onClick = onRetryCategories) { Text("다시 불러오기", color = mainOrange) }
                        }
                    }
                    else ->
                        KeywordEntry(
                            keywords,
                            onAddKeyword,
                            onRemoveKeyword,
                            popularKeywords,
                            keywordLimit,
                            popularKeywordsLoading,
                            popularKeywordsError,
                            onRetryPopularKeywords,
                            enabled = !submitting,
                            entry = keywordDraft,
                            onEntryChange = { keywordDraft = it },
                        )
                }
                if (submitError != null) {
                    Text(
                        submitError,
                        color = Color(0xFFB3261E),
                        style = renewalText(12),
                        modifier = Modifier.padding(top = 20.dp).testTag("signup-error"),
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
        RenewalPrimaryButton(
            if (submitting) "가입 중…" else "다음",
            {
                if (canProceed()) {
                    focus.clearFocus()
                    if (step < 2) {
                        step++
                    } else {
                        submittedKeywords = keywords.toList()
                        onSubmit(false)
                    }
                }
            },
            Modifier.padding(horizontal = 23.dp).testTag("signup-next"),
            enabled = canProceed(),
        )
    }
}

@Composable
private fun SignupProgress(
    step: Int,
    enabled: Boolean,
    onBack: () -> Unit,
) {
    Box(Modifier.fillMaxWidth().height(37.dp)) {
        Box(
            Modifier
                .width(59.dp)
                .fillMaxHeight()
                .semantics { contentDescription = "뒤로가기" }
                .clickable(enabled = enabled, role = Role.Button, onClick = onBack),
        ) {
            BasicText(
                "‹",
                style = renewalText(30).copy(fontFamily = RenewalLight),
                modifier = Modifier.offset(x = 22.dp, y = 1.dp).clearAndSetSemantics {},
            )
        }
        Row(
            Modifier.fillMaxSize().padding(start = 59.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val progress by animateFloatAsState(
                targetValue = (step + 1) / 3f,
                animationSpec = tween(380, easing = FastOutSlowInEasing),
                label = "signup-progress-fill",
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .testTag("signup-progress")
                    .progressSemantics(progress)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFEDEDED)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(mainOrange, RoundedCornerShape(2.dp)),
                )
            }
            Spacer(Modifier.width(20.dp))
            Text("${step + 1}/3", style = renewalText(14).copy(lineHeight = 22.sp), color = Color(0xFF8B8B8B))
        }
    }
}

@Composable
private fun SignupInput(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier,
    height: Dp,
    placeholder: String,
    style: TextStyle,
    enabled: Boolean = true,
    error: Boolean = false,
    valid: Boolean = false,
    filled: Boolean = false,
    search: Boolean = false,
    onDone: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val emphasized = focused || valid || error
    val border =
        when {
            error -> Color(0xFFB3261E)
            focused || valid -> mainOrange
            filled -> Color(0xFFF2F2F2)
            else -> RenewalLine
        }
    BasicTextField(
        value,
        onChange,
        modifier.height(height).onFocusChanged { focused = it.isFocused },
        enabled = enabled,
        singleLine = true,
        textStyle = style,
        cursorBrush = SolidColor(mainOrange),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        decorationBox = { inner ->
            Row(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (filled) Color(0xFFF2F2F2) else Color.White)
                    .border(if (emphasized) 2.dp else 1.dp, border, RoundedCornerShape(12.dp))
                    .padding(horizontal = if (filled) 15.dp else 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (search) {
                    Text("⌕", style = style, color = Color(0xFF999999))
                    Spacer(Modifier.width(6.dp))
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Text(placeholder, style = style, color = Color(0xFF999999))
                    inner()
                }
            }
        },
    )
}

@Composable
private fun NicknameEntry(
    value: String,
    onChange: (String) -> Unit,
    message: String,
    valid: Boolean,
    error: Boolean,
    onCheck: () -> Unit,
    onRecommend: (() -> Unit)?,
    recommending: Boolean,
    checking: Boolean,
    recommendationError: String?,
) {
    Row(
        Modifier.fillMaxWidth().padding(end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SignupInput(
            value,
            onChange,
            Modifier.weight(1f).testTag("signup-nickname"),
            48.dp,
            "닉네임 입력",
            renewalText(17),
            error = error,
            valid = valid,
        ) { if (!checking && !recommending && value.trim().length >= 2) onCheck() }
        Button(
            onCheck,
            modifier = Modifier.size(62.dp, 45.dp),
            enabled = value.trim().length >= 2 && !checking && !recommending,
            shape = RoundedCornerShape(5.dp),
            contentPadding = PaddingValues(horizontal = 15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = mainOrange),
        ) { Text(if (checking) "확인 중…" else "확인", style = renewalText(12), color = Color.White) }
    }
    Spacer(Modifier.height(13.dp))
    Text(
        message.ifBlank { " " },
        style = renewalText(13, true),
        color =
            when {
                error -> Color(0xFFB3261E)
                valid -> Color(0xFF1A8C3D)
                else -> RenewalMuted
            },
        modifier = Modifier.heightIn(min = 16.dp),
    )
    Spacer(Modifier.height(21.dp))
    if (onRecommend != null) {
        Surface(
            onClick = onRecommend,
            enabled = !recommending && !checking,
            modifier = Modifier.padding(start = 5.dp).height(39.dp).testTag("nickname-recommend"),
            shape = RoundedCornerShape(999.dp),
            color = Color(0xFFF4F5F7),
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "↻",
                    style =
                        TextStyle(
                            fontFamily = RenewalInter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            lineHeight = 19.sp,
                            lineHeightStyle = RenewalLineHeight,
                        ),
                    color = Color(0xFF666B73),
                )
                Text(
                    if (recommending) "닉네임 추천 중…" else "다른 닉네임 추천받기",
                    style = renewalText(14).copy(lineHeight = 17.sp),
                    color = Color(0xFF545961),
                )
            }
        }
    }
    if (recommendationError != null) {
        Text(recommendationError, style = renewalText(12), color = Color(0xFFB3261E), modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun KeywordEntry(
    keywords: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    popular: List<String>,
    limit: Int,
    popularLoading: Boolean,
    popularError: String?,
    onRetryPopular: () -> Unit,
    enabled: Boolean,
    entry: String,
    onEntryChange: (String) -> Unit,
) {
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun add(value: String) {
        if (!enabled) return
        val text = value.trim()
        error =
            when {
                text.isBlank() -> "키워드를 입력해주세요."
                text in keywords -> "이미 추가한 키워드예요."
                keywords.size >= limit -> "키워드는 최대 ${limit}개까지 등록할 수 있어요."
                else -> null
            }
        if (error == null) {
            onAdd(text)
            onEntryChange("")
        }
    }
    Text(
        "브랜드나 캐릭터 이름이면 더 정확해요.",
        style = renewalText(14, lineHeight = 1.47f).copy(fontFamily = RenewalRegular),
        color = Color(0xFF555555),
    )
    Spacer(Modifier.height(23.42.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        SignupInput(
            entry,
            {
                onEntryChange(it)
                error = null
            },
            Modifier.weight(1f).testTag("signup-keyword"),
            50.dp,
            "예: 두쫀쿠, 짱구, 레고",
            renewalText(15).copy(fontFamily = RenewalLight),
            enabled = enabled,
            filled = true,
            search = true,
        ) { add(entry) }
        Button(
            { add(entry) },
            Modifier.size(64.dp, 50.dp),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(containerColor = RenewalInk),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(10.dp),
        ) { Text("추가", style = renewalText(15, true), color = Color.White) }
    }
    if (error != null) Text(error.orEmpty(), color = Color(0xFFB3261E), style = renewalText(12), modifier = Modifier.padding(top = 8.dp))
    Spacer(Modifier.height(38.dp))
    Row(Modifier.fillMaxWidth().testTag("signup-own-keywords"), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("내 키워드", style = renewalText(14, true), color = Color(0xFF333333))
        Text("${keywords.size} / $limit", style = renewalText(14, true), color = mainOrange, modifier = Modifier.testTag("keyword-count"))
    }
    Spacer(Modifier.height(11.2.dp))
    FlowRow(
        Modifier.fillMaxWidth().heightIn(min = 39.dp).padding(end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        keywords.forEach { keyword ->
            RenewalChip(keyword, true, { if (enabled) onRemove(keyword) }, trailing = "×", checkMark = false, selectedOutline = false)
        }
    }
    Spacer(Modifier.height(28.2.dp))
    Text("인기 키워드", style = renewalText(14, true), color = Color(0xFF333333))
    Spacer(Modifier.height(12.2.dp))
    when {
        popularLoading -> Text("인기 키워드를 불러오는 중…", style = renewalText(12), color = RenewalMuted)
        popularError != null -> {
            Text(popularError, style = renewalText(12), color = RenewalMuted)
            TextButton(onClick = onRetryPopular, enabled = enabled, modifier = Modifier.testTag("popular-keywords-retry")) {
                Text("다시 불러오기", style = renewalText(12), color = mainOrange)
            }
        }
        popular.isEmpty() -> Text("아직 인기 키워드가 없어요. 원하는 키워드를 직접 입력해주세요.", style = renewalText(12), color = RenewalMuted)
        else ->
            FlowRow(
                Modifier.fillMaxWidth().padding(end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                popular.filterNot { it in keywords }.forEach { keyword -> RenewalChip("+ $keyword", false, { add(keyword) }) }
            }
    }
}

@Composable
private fun SignupComplete(
    nickname: String,
    keywords: List<String>,
    starting: Boolean,
    onStart: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 393.dp)
            .fillMaxWidth()
            .systemBarsPadding(),
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(84.dp).background(RenewalPeach, CircleShape), contentAlignment = Alignment.Center) {
                Box(Modifier.size(58.dp).background(mainOrange, CircleShape), contentAlignment = Alignment.Center) {
                    Text(
                        "✓",
                        style =
                            TextStyle(
                                fontFamily = RenewalInter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                lineHeight = 34.sp,
                                lineHeightStyle = RenewalLineHeight,
                            ),
                        color = Color.White,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("준비 끝!", style = renewalText(26, true), color = Color(0xFF121212))
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = mainOrange)) { append(nickname) }
                        append("님, 환영해요")
                    },
                    style = renewalText(26, true),
                    color = Color(0xFF121212),
                    modifier = Modifier.testTag("signup-complete"),
                )
            }
            Spacer(Modifier.height(61.6.dp))
            Surface(
                Modifier.fillMaxWidth().padding(start = 35.dp, end = 20.dp),
                shape = RoundedCornerShape(15.dp),
                color = Color.White,
                shadowElevation = 5.dp,
            ) {
                Column(Modifier.padding(26.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                    SignupBenefit("키워드 팝업 오픈 알림", keywords.joinToString(" · ").ifBlank { "좋아하는 키워드를 알림 설정에서 추가해보세요" })
                    SignupBenefit("받을 알림은 직접 고를 수 있어요", "마이 > 알림 설정에서 언제든 바꿀 수 있어요")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        RenewalPrimaryButton(
            if (starting) "알림 설정 확인 중…" else "알림 켜고 시작하기",
            onStart,
            Modifier.padding(start = 20.dp, end = 26.dp),
            enabled = !starting,
        )
    }
}

@Composable
private fun SignupBenefit(
    title: String,
    description: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(26.dp).background(RenewalPeach, CircleShape), contentAlignment = Alignment.Center) {
            Text(
                "✓",
                style =
                    TextStyle(
                        fontFamily = RenewalInter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        lineHeightStyle = RenewalLineHeight,
                    ),
                color = mainOrange,
            )
        }
        Column(Modifier.weight(1f).padding(top = 2.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = renewalText(16), color = Color(0xFF121212))
            Text(description, style = renewalText(13).copy(fontFamily = RenewalLight), color = RenewalMuted)
        }
    }
}
