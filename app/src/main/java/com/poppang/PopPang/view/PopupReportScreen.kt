package com.poppang.PopPang.view

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.poppang.PopPang.R
import com.poppang.PopPang.model.CategoryItem
import com.poppang.PopPang.model.PopupSubmissionImage
import com.poppang.PopPang.model.PopupSubmissionRequest
import com.poppang.PopPang.model.PopupSubmissionTime
import com.poppang.PopPang.ui.theme.Bold15
import com.poppang.PopPang.ui.theme.Medium12
import com.poppang.PopPang.ui.theme.Medium15
import com.poppang.PopPang.ui.theme.Medium18
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainGray1
import com.poppang.PopPang.ui.theme.mainGray2
import com.poppang.PopPang.ui.theme.mainGray5
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.viewmodel.CategoryItemViewModel
import com.poppang.PopPang.viewmodel.SubmissionViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PopupReportScreen(
    onClose: () -> Unit,
    userUuid: String,
    categoryViewModel: CategoryItemViewModel,
    submissionViewModel: SubmissionViewModel = viewModel(),
    onSubmit: () -> Unit = {}
) {
    BackHandler { onClose() }
    val context = LocalContext.current

    LaunchedEffect(categoryViewModel) {
        categoryViewModel.fetchCategoryItems()
    }

    var popupName by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var roadAddress by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var popupDescription by remember { mutableStateOf("") }
    var lotAddress by remember { mutableStateOf("") }
    var openTime by remember { mutableStateOf("") }
    var closeTime by remember { mutableStateOf("") }
    var instagramUrl by remember { mutableStateOf("") }
    val selectedCategoryIds = remember { mutableStateListOf<Long>() }
    val selectedImages = remember { mutableStateListOf<Uri>() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages.clear()
        selectedImages.addAll(uris.take(MAX_REPORT_IMAGE_COUNT))
    }

    val isSubmitEnabled =
        userUuid.isNotBlank() &&
        popupName.isNotBlank() &&
            startDate.isNotBlank() &&
            endDate.isNotBlank() &&
            roadAddress.isNotBlank() &&
            region.isNotBlank() &&
            popupDescription.isNotBlank() &&
            selectedCategoryIds.isNotEmpty() &&
            selectedImages.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PopupReportTopBar(onClose = onClose)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding( bottom = 60.dp)
            ) {
                PopupReportSectionTitle(text = "필수 입력")
                PopupReportTextField(
                    label = "팝업명",
                    required = true,
                    value = popupName,
                    onValueChange = { popupName = it },
                    placeholder = "팝업명을 입력해 주세요"
                )
                PopupReportDateRangeField(
                    startDate = startDate,
                    endDate = endDate,
                    onStartDateChange = { startDate = it },
                    onEndDateChange = { endDate = it }
                )
                PopupReportTextField(
                    label = "도로명 주소",
                    required = true,
                    value = roadAddress,
                    onValueChange = { roadAddress = it },
                    placeholder = "예: 서울 성동구 성수이로 00"
                )
                PopupReportTextField(
                    label = "지역",
                    required = true,
                    value = region,
                    onValueChange = { region = it },
                    placeholder = "예: 서울"
                )
                PopupReportTextField(
                    label = "팝업 소개",
                    required = true,
                    value = popupDescription,
                    onValueChange = { popupDescription = it },
                    placeholder = "팝업의 주요 내용과 참고할 정보를 입력해 주세요",
                    minLines = 5,
                    height = POPUP_REPORT_TEXT_AREA_HEIGHT
                )
                PopupReportCategorySection(
                    categories = categoryViewModel.categories,
                    selectedCategoryIds = selectedCategoryIds,
                    isLoading = categoryViewModel.isLoading,
                    errorMessage = categoryViewModel.errorMessage,
                    onRetry = categoryViewModel::retryFetchCategoryItems,
                    onCategoryClick = { category ->
                        if (selectedCategoryIds.contains(category.id)) {
                            selectedCategoryIds.remove(category.id)
                        } else {
                            selectedCategoryIds.add(category.id)
                        }
                    }
                )
                PopupReportImagePicker(
                    selectedImages = selectedImages,
                    onClick = { imagePicker.launch("image/*") },
                    onRemoveImage = { selectedImages.remove(it) }
                )
                PopupReportSectionTitle(
                    text = "선택 입력",
                    modifier = Modifier.padding(top = 28.dp)
                )
                PopupReportTextField(
                    label = "지번 주소",
                    value = lotAddress,
                    onValueChange = { lotAddress = it },
                    placeholder = "도로명 주소와 다를 때 입력"
                )
                PopupReportTimeFields(
                    openTime = openTime,
                    closeTime = closeTime,
                    onOpenTimeChange = { openTime = it },
                    onCloseTimeChange = { closeTime = it }
                )
                PopupReportTextField(
                    label = "인스타그램 URL",
                    value = instagramUrl,
                    onValueChange = { instagramUrl = it },
                    placeholder = "https://instagram.com/p/..."
                )
            }
        }
        PopupReportBottomButton(
            enabled = isSubmitEnabled && !submissionViewModel.isSubmitting,
            text = if (submissionViewModel.isSubmitting) "제보 중..." else "제보하기",
            onClick = {
                if (isSubmitEnabled) {
                    val request = PopupSubmissionRequest(
                        userUuid = userUuid,
                        name = popupName.trim(),
                        startDate = startDate.toSubmissionDateText(),
                        endDate = endDate.toSubmissionDateText(),
                        openTime = openTime.toSubmissionTimeOrNull(),
                        closeTime = closeTime.toSubmissionTimeOrNull(),
                        address = lotAddress.trim().ifBlank { null },
                        roadAddress = roadAddress.trim(),
                        region = region.trim(),
                        instaPostUrl = instagramUrl.trim().ifBlank { null },
                        description = popupDescription.trim(),
                        imageList = selectedImages.mapIndexed { index, uri ->
                            PopupSubmissionImage(
                                imageUrl = uri.toString(),
                                sortOrder = index
                            )
                        },
                        recommendIdList = selectedCategoryIds.toList()
                    )
                    submissionViewModel.submitPopup(
                        request = request,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                "팝업 제보가 등록되었습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            onSubmit()
                            onClose()
                        },
                        onError = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        )
    }
}

@Composable
private fun PopupReportTopBar(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.back_icon),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
                .size(14.dp)
                .clickable { onClose() }
        )
        Text(
            text = "팝업 제보하기",
            style = Medium18,
            color = mainBlack,
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PopupReportSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = Bold15,
        color = mainBlack,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)
    )
}

@Composable
private fun PopupReportTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    minLines: Int = 1,
    height: androidx.compose.ui.unit.Dp = POPUP_REPORT_FIELD_HEIGHT
) {
    PopupReportFieldLabel(label = label, required = required)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = Medium12,
                color = mainGray2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        textStyle = Medium12.copy(color = mainBlack),
        singleLine = minLines == 1,
        minLines = minLines,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(bottom = 18.dp),
        shape = RoundedCornerShape(8.dp),
        colors = popupReportTextFieldColors()
    )
}

@Composable
private fun PopupReportDateRangeField(
    startDate: String,
    endDate: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    var datePickerTarget by remember { mutableStateOf<PopupReportDateTarget?>(null) }

    PopupReportFieldLabel(label = "운영 기간", required = true)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PopupReportDateField(
            value = startDate,
            placeholder = "2026. 6. 4.",
            onClick = { datePickerTarget = PopupReportDateTarget.START },
            modifier = Modifier
                .weight(1f)
                .height(POPUP_REPORT_VISIBLE_FIELD_HEIGHT)
        )
        Text(
            text = "-",
            style = Medium15,
            color = mainGray1,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp)
        )
        PopupReportDateField(
            value = endDate,
            placeholder = "2026. 6. 11.",
            onClick = { datePickerTarget = PopupReportDateTarget.END },
            modifier = Modifier
                .weight(1f)
                .height(POPUP_REPORT_VISIBLE_FIELD_HEIGHT)
        )
    }

    datePickerTarget?.let { target ->
        PopupReportDatePickerDialog(
            onDismiss = { datePickerTarget = null },
            onDateSelected = { formattedDate ->
                when (target) {
                    PopupReportDateTarget.START -> onStartDateChange(formattedDate)
                    PopupReportDateTarget.END -> onEndDateChange(formattedDate)
                }
                datePickerTarget = null
            }
        )
    }
}

@Composable
private fun PopupReportDateField(
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, mainGray5, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.ifBlank { placeholder },
            style = Medium12,
            color = if (value.isBlank()) mainGray2 else mainBlack,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PopupReportDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 340.dp),
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis ->
                        onDateSelected(selectedDateMillis.toPopupReportDateText())
                    }
                }
            ) {
                Text(text = "확인", style = Medium12, color = mainOrange)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소", style = Medium12, color = mainGray1)
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.widthIn(max = 340.dp)
        )
    }
}

@Composable
private fun PopupReportTimeFields(
    openTime: String,
    closeTime: String,
    onOpenTimeChange: (String) -> Unit,
    onCloseTimeChange: (String) -> Unit
) {
    var timePickerTarget by remember { mutableStateOf<PopupReportTimeTarget?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            PopupReportTimeField(
                label = "오픈 시간",
                value = openTime,
                placeholder = "10:00",
                onClick = { timePickerTarget = PopupReportTimeTarget.OPEN }
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            PopupReportTimeField(
                label = "마감 시간",
                value = closeTime,
                placeholder = "20:00",
                onClick = { timePickerTarget = PopupReportTimeTarget.CLOSE }
            )
        }
    }

    timePickerTarget?.let { target ->
        PopupReportTimePickerDialog(
            initialTime = when (target) {
                PopupReportTimeTarget.OPEN -> openTime
                PopupReportTimeTarget.CLOSE -> closeTime
            },
            defaultHour = when (target) {
                PopupReportTimeTarget.OPEN -> 10
                PopupReportTimeTarget.CLOSE -> 20
            },
            onDismiss = { timePickerTarget = null },
            onTimeSelected = { selectedTime ->
                when (target) {
                    PopupReportTimeTarget.OPEN -> onOpenTimeChange(selectedTime)
                    PopupReportTimeTarget.CLOSE -> onCloseTimeChange(selectedTime)
                }
                timePickerTarget = null
            }
        )
    }
}

@Composable
private fun PopupReportTimeField(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    PopupReportFieldLabel(label = label)
    PopupReportDateField(
        value = value,
        placeholder = placeholder,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(POPUP_REPORT_VISIBLE_FIELD_HEIGHT)
    )
    Spacer(modifier = Modifier.height(18.dp))
}

@Composable
private fun PopupReportTimePickerDialog(
    initialTime: String,
    defaultHour: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    val initialHour = initialTime.substringBefore(":").toIntOrNull() ?: defaultHour
    val initialMinute = initialTime.substringAfter(":", "").toIntOrNull() ?: 0
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour.coerceIn(0, 23),
        initialMinute = initialMinute.coerceIn(0, 59),
        is24Hour = false
    )

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.widthIn(max = 360.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 24.dp,
                    top = 24.dp,
                    end = 24.dp,
                    bottom = 8.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "시간 선택",
                    style = Medium15,
                    color = mainBlack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )
                TimePicker(state = timePickerState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "취소", style = Medium12, color = mainGray1)
                    }
                    TextButton(
                        onClick = {
                            onTimeSelected(
                                timePickerState.hour.toTwoDigitTimePart() +
                                    ":" +
                                    timePickerState.minute.toTwoDigitTimePart()
                            )
                        }
                    ) {
                        Text(text = "확인", style = Medium12, color = mainOrange)
                    }
                }
            }
        }
    }
}

@Composable
private fun PopupReportFieldLabel(
    label: String,
    required: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = Bold15, color = mainBlack)
        if (required) {
            Text(
                text = " *",
                style = Bold15,
                color = mainOrange
            )
        }
    }
}

@Composable
private fun PopupReportCategorySection(
    categories: List<CategoryItem>,
    selectedCategoryIds: List<Long>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onCategoryClick: (CategoryItem) -> Unit
) {
    PopupReportFieldLabel(label = "추천 카테고리", required = true)

    when {
        isLoading && categories.isEmpty() -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = mainOrange,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "카테고리를 불러오는 중입니다.",
                    style = Medium12,
                    color = mainGray1,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }

        errorMessage != null && categories.isEmpty() -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = errorMessage,
                    style = Medium12,
                    color = mainGray1
                )
                TextButton(onClick = onRetry) {
                    Text(text = "다시 시도", style = Medium12, color = mainOrange)
                }
            }
        }

        else -> {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categories.forEach { category ->
                    val selected = selectedCategoryIds.contains(category.id)
                    Surface(
                        modifier = Modifier.clickable { onCategoryClick(category) },
                        color = if (selected) Color(0xFFFFF4EA) else Color.White,
                        shape = RoundedCornerShape(25.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (selected) mainOrange else mainGray5
                        )
                    ) {
                        Text(
                            text = category.recommendName,
                            style = Medium12,
                            color = if (selected) mainOrange else mainGray2,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PopupReportImagePicker(
    selectedImages: List<Uri>,
    onClick: () -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    PopupReportFieldLabel(label = "이미지", required = true)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(POPUP_REPORT_VISIBLE_FIELD_HEIGHT)
            .border(1.dp, mainOrange, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.plus_icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "이미지 선택", style = Medium15, color = mainOrange)
        }
        Text(
            text = "${selectedImages.size}/$MAX_REPORT_IMAGE_COUNT",
            style = Medium15,
            color = mainGray1
        )
    }

    if (selectedImages.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            selectedImages.forEach { imageUri ->
                PopupReportImagePreview(
                    imageUri = imageUri,
                    onRemove = { onRemoveImage(imageUri) }
                )
            }
        }
    }
}

@Composable
private fun PopupReportImagePreview(
    imageUri: Uri,
    onRemove: () -> Unit
) {
    Box(modifier = Modifier.size(82.dp)) {
        AsyncImage(
            model = imageUri,
            contentDescription = "선택한 이미지 미리보기",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp)
                .size(24.dp)
                .background(Color(0xCC3A251A), CircleShape)
                .semantics { contentDescription = "이미지 삭제" }
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "×",
                style = Medium18,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PopupReportBottomButton(
    enabled: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .height(52.dp)
            .background(
                color = if (enabled) mainOrange else Color(0xFFFFC48A),
                shape = RoundedCornerShape(5.dp)
            )
            .clickable(enabled = enabled) { onClick() }
    ) {
        Text(
            text = text,
            style = Medium15,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun popupReportTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = mainOrange,
    unfocusedBorderColor = mainGray5,
    cursorColor = mainOrange,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White
)

private enum class PopupReportDateTarget {
    START,
    END
}

private enum class PopupReportTimeTarget {
    OPEN,
    CLOSE
}

private fun Long.toPopupReportDateText(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(POPUP_REPORT_DATE_FORMATTER)

private fun Int.toTwoDigitTimePart(): String = toString().padStart(2, '0')

private fun String.toSubmissionDateText(): String =
    LocalDate.parse(this, POPUP_REPORT_DATE_FORMATTER)
        .format(DateTimeFormatter.ISO_LOCAL_DATE)

private fun String.toSubmissionTimeOrNull(): PopupSubmissionTime? {
    val timeParts = split(":")
    if (timeParts.size != 2) return null

    val hour = timeParts[0].toIntOrNull() ?: return null
    val minute = timeParts[1].toIntOrNull() ?: return null
    return PopupSubmissionTime(
        hour = hour,
        minute = minute
    )
}

private val POPUP_REPORT_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("uuuu. M. d.")

private val POPUP_REPORT_FIELD_HEIGHT = 64.dp
private val POPUP_REPORT_VISIBLE_FIELD_HEIGHT = 46.dp
private val POPUP_REPORT_TEXT_AREA_HEIGHT = 156.dp

private const val MAX_REPORT_IMAGE_COUNT = 10
