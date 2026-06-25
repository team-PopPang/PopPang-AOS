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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.poppang.PopPang.BuildConfig
import com.poppang.PopPang.R
import com.poppang.PopPang.model.AdminPopupSubmissionDetail
import com.poppang.PopPang.model.AdminPopupSubmissionListItem
import com.poppang.PopPang.model.AdminPopupSubmissionUpdateRequest
import com.poppang.PopPang.model.CategoryItem
import com.poppang.PopPang.model.PopupSubmissionImage
import com.poppang.PopPang.model.PopupSubmissionTime
import com.poppang.PopPang.ui.theme.Bold15
import com.poppang.PopPang.ui.theme.Medium12
import com.poppang.PopPang.ui.theme.Medium15
import com.poppang.PopPang.ui.theme.Medium18
import com.poppang.PopPang.ui.theme.Regular10
import com.poppang.PopPang.ui.theme.mainBlack
import com.poppang.PopPang.ui.theme.mainGray1
import com.poppang.PopPang.ui.theme.mainGray2
import com.poppang.PopPang.ui.theme.mainGray5
import com.poppang.PopPang.ui.theme.mainOrange
import com.poppang.PopPang.viewmodel.AdminSubmissionFilter
import com.poppang.PopPang.viewmodel.AdminSubmissionViewModel
import com.poppang.PopPang.viewmodel.CategoryItemViewModel
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun AdminSubmissionScreen(
    adminUuid: String,
    categoryViewModel: CategoryItemViewModel,
    onClose: () -> Unit,
    viewModel: AdminSubmissionViewModel = viewModel()
) {
    var selectedSubmission by remember {
        mutableStateOf<AdminPopupSubmissionListItem?>(null)
    }

    LaunchedEffect(adminUuid) {
        viewModel.refresh(adminUuid)
        categoryViewModel.fetchCategoryItems()
    }

    BackHandler {
        if (selectedSubmission != null) {
            selectedSubmission = null
            viewModel.clearDetail()
        } else {
            onClose()
        }
    }

    selectedSubmission?.let { submission ->
        AdminSubmissionDetailContent(
            adminUuid = adminUuid,
            popupUuid = submission.popupUuid,
            popupSubmissionId = submission.popupSubmissionId,
            detail = viewModel.selectedDetail,
            categories = categoryViewModel.categories,
            isCategoryLoading = categoryViewModel.isLoading,
            categoryErrorMessage = categoryViewModel.errorMessage,
            onCategoryRetry = categoryViewModel::retryFetchCategoryItems,
            isLoading = viewModel.isDetailLoading,
            isUpdating = viewModel.isUpdating,
            errorMessage = viewModel.errorMessage,
            onBack = {
                selectedSubmission = null
                viewModel.clearDetail()
            },
            onRetry = {
                viewModel.loadDetail(
                    adminUuid = adminUuid,
                    popupUuid = submission.popupUuid,
                    popupSubmissionId = submission.popupSubmissionId
                )
            },
            onStatusUpdated = {
                selectedSubmission = null
            },
            viewModel = viewModel
        )
    } ?: run {
        AdminSubmissionListContent(
            submissions = viewModel.submissions,
            allSubmissions = viewModel.allSubmissions,
            selectedFilter = viewModel.selectedFilter,
            isLoading = viewModel.isLoading,
            errorMessage = viewModel.errorMessage,
            onClose = onClose,
            onRefresh = { viewModel.refresh(adminUuid) },
            onFilterSelected = { filter ->
                viewModel.loadSubmissions(adminUuid, filter)
            },
            onSubmissionClick = { submission ->
                selectedSubmission = submission
                viewModel.loadDetail(
                    adminUuid = adminUuid,
                    popupUuid = submission.popupUuid,
                    popupSubmissionId = submission.popupSubmissionId
                )
            }
        )
    }
}

@Composable
private fun AdminSubmissionListContent(
    submissions: List<AdminPopupSubmissionListItem>,
    allSubmissions: List<AdminPopupSubmissionListItem>,
    selectedFilter: AdminSubmissionFilter,
    isLoading: Boolean,
    errorMessage: String?,
    onClose: () -> Unit,
    onRefresh: () -> Unit,
    onFilterSelected: (AdminSubmissionFilter) -> Unit,
    onSubmissionClick: (AdminPopupSubmissionListItem) -> Unit
) {
    val pendingCount = allSubmissions.count { it.status.isSubmissionPending() }
    val approvedCount = allSubmissions.count { it.status.isSubmissionApproved() }
    val rejectedCount = allSubmissions.count { it.status.isSubmissionRejected() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ADMIN_BACKGROUND)
    ) {
        AdminSubmissionTopBar(
            title = "팝업 제보 관리",
            onBack = onClose,
            onRefresh = onRefresh
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 14.dp,
                end = 16.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminSubmissionCountCard("대기", pendingCount, Modifier.weight(1f))
                    AdminSubmissionCountCard("승인", approvedCount, Modifier.weight(1f))
                    AdminSubmissionCountCard("반려", rejectedCount, Modifier.weight(1f))
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminSubmissionFilter.entries.forEach { filter ->
                        AdminSubmissionFilterChip(
                            filter = filter,
                            selected = filter == selectedFilter,
                            onClick = { onFilterSelected(filter) }
                        )
                    }
                }
            }

            when {
                isLoading && submissions.isEmpty() -> item { AdminSubmissionLoading() }
                errorMessage != null && submissions.isEmpty() -> {
                    item { AdminSubmissionError(errorMessage, onRefresh) }
                }
                submissions.isEmpty() -> {
                    item {
                        Text(
                            text = "해당 상태의 제보가 없습니다.",
                            style = Medium12,
                            color = mainGray1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 50.dp)
                        )
                    }
                }
                else -> {
                    items(
                        items = submissions,
                        key = {
                            it.popupUuid
                                ?: it.popupSubmissionId?.let { id -> "submission-$id" }
                                ?: "${it.name}-${it.submittedAt}"
                        }
                    ) { submission ->
                        AdminSubmissionListCard(
                            submission = submission,
                            onClick = { onSubmissionClick(submission) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSubmissionTopBar(
    title: String,
    onBack: () -> Unit,
    onRefresh: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .background(Color.White)
            .border(width = 0.5.dp, color = mainGray5)
    ) {
        Image(
            painter = painterResource(R.drawable.back_icon),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
                .size(14.dp)
                .clickable { onBack() }
        )
        Text(
            text = title,
            style = Medium18,
            color = mainBlack,
            modifier = Modifier.align(Alignment.Center)
        )
        if (onRefresh != null) {
            Text(
                text = "↻",
                style = Medium18,
                color = mainBlack,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
                    .size(32.dp)
                    .clickable { onRefresh() }
            )
        }
    }
}

@Composable
private fun AdminSubmissionCountCard(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(82.dp)
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = Medium12, color = mainGray1)
        Text(text = count.toString(), style = Medium18, color = mainBlack)
    }
}

@Composable
private fun AdminSubmissionFilterChip(
    filter: AdminSubmissionFilter,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (selected) mainOrange else Color.White,
        shape = RoundedCornerShape(22.dp),
        border = if (selected) null else {
            androidx.compose.foundation.BorderStroke(1.dp, mainGray5)
        }
    ) {
        Text(
            text = filter.label,
            style = Medium12,
            color = if (selected) Color.White else mainGray1,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun AdminSubmissionListCard(
    submission: AdminPopupSubmissionListItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = submission.name,
                style = Bold15,
                color = mainBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = submission.roadAddress,
                style = Medium12,
                color = mainGray1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 5.dp)
            )
            Text(
                text = listOf(
                    submission.region,
                    submission.submitterNickname,
                    submission.submittedAt.toSubmissionDisplayDate()
                ).filter { it.isNotBlank() }.joinToString("  •  "),
                style = Regular10,
                color = mainGray2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        AdminSubmissionStatusBadge(submission.status)
        Text(
            text = "›",
            style = Medium18,
            color = mainGray2,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
private fun AdminSubmissionStatusBadge(status: String) {
    val label: String
    val textColor: Color
    val backgroundColor: Color
    when {
        status.isSubmissionApproved() -> {
            label = "승인"
            textColor = Color(0xFF2E7D32)
            backgroundColor = Color(0xFFEAF6EC)
        }
        status.isSubmissionRejected() -> {
            label = "반려"
            textColor = Color(0xFFC94747)
            backgroundColor = Color(0xFFFFEEEE)
        }
        else -> {
            label = "검토 대기"
            textColor = mainOrange
            backgroundColor = Color(0xFFFFF3E9)
        }
    }

    Text(
        text = label,
        style = Medium12,
        color = textColor,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 13.dp, vertical = 8.dp)
    )
}

@Composable
private fun AdminSubmissionDetailContent(
    adminUuid: String,
    popupUuid: String?,
    popupSubmissionId: Long?,
    detail: AdminPopupSubmissionDetail?,
    categories: List<CategoryItem>,
    isCategoryLoading: Boolean,
    categoryErrorMessage: String?,
    onCategoryRetry: () -> Unit,
    isLoading: Boolean,
    isUpdating: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onStatusUpdated: () -> Unit,
    viewModel: AdminSubmissionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AdminSubmissionTopBar(
            title = "제보 상세 수정",
            onBack = onBack
        )
        when {
            isLoading -> AdminSubmissionLoading()
            detail == null -> {
                AdminSubmissionError(
                    message = errorMessage ?: "제보 상세 정보를 불러오지 못했습니다.",
                    onRetry = onRetry
                )
            }
            else -> {
                AdminSubmissionEditForm(
                    adminUuid = adminUuid,
                    popupUuid = popupUuid,
                    popupSubmissionId = popupSubmissionId,
                    detail = detail,
                    categories = categories,
                    isCategoryLoading = isCategoryLoading,
                    categoryErrorMessage = categoryErrorMessage,
                    onCategoryRetry = onCategoryRetry,
                    isUpdating = isUpdating,
                    onStatusUpdated = onStatusUpdated,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun AdminSubmissionEditForm(
    adminUuid: String,
    popupUuid: String?,
    popupSubmissionId: Long?,
    detail: AdminPopupSubmissionDetail,
    categories: List<CategoryItem>,
    isCategoryLoading: Boolean,
    categoryErrorMessage: String?,
    onCategoryRetry: () -> Unit,
    isUpdating: Boolean,
    onStatusUpdated: () -> Unit,
    viewModel: AdminSubmissionViewModel
) {
    val context = LocalContext.current
    val detailStateKey =
        detail.popupUuid ?: popupUuid ?: detail.popupSubmissionId ?: popupSubmissionId
    var name by remember(detailStateKey) { mutableStateOf(detail.name) }
    var startDate by remember(detailStateKey) { mutableStateOf(detail.startDate) }
    var endDate by remember(detailStateKey) { mutableStateOf(detail.endDate) }
    var roadAddress by remember(detailStateKey) { mutableStateOf(detail.roadAddress) }
    var region by remember(detailStateKey) { mutableStateOf(detail.region) }
    var description by remember(detailStateKey) { mutableStateOf(detail.description) }
    var address by remember(detailStateKey) { mutableStateOf(detail.address.orEmpty()) }
    var openTime by remember(detailStateKey) {
        mutableStateOf(detail.openTime?.toDisplayTime().orEmpty())
    }
    var closeTime by remember(detailStateKey) {
        mutableStateOf(detail.closeTime?.toDisplayTime().orEmpty())
    }
    var latitude by remember(detailStateKey) {
        mutableStateOf(detail.latitude?.toString().orEmpty())
    }
    var longitude by remember(detailStateKey) {
        mutableStateOf(detail.longitude?.toString().orEmpty())
    }
    var instaPostUrl by remember(detailStateKey) {
        mutableStateOf(detail.instaPostUrl.orEmpty())
    }
    val selectedCategoryIds = remember(detailStateKey) {
        mutableStateListOf<Long>().apply {
            addAll(
                detail.recommendIdList.ifEmpty {
                    detail.recommendList.map { it.recommendId }
                }
            )
        }
    }
    val imageUrls = remember(detailStateKey) {
        mutableStateListOf<String>().apply {
            addAll(detail.imageList.sortedBy { it.sortOrder }.map { it.imageUrl })
        }
    }
    var pendingStatus by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val remainingCount = (MAX_ADMIN_SUBMISSION_IMAGE_COUNT - imageUrls.size).coerceAtLeast(0)
        uris.take(remainingCount)
            .map(Uri::toString)
            .filterNot(imageUrls::contains)
            .forEach(imageUrls::add)
    }

    val approvalEnabled =
        name.isNotBlank() &&
            startDate.isNotBlank() &&
            endDate.isNotBlank() &&
            roadAddress.isNotBlank() &&
            region.isNotBlank() &&
            description.isNotBlank() &&
            selectedCategoryIds.isNotEmpty() &&
            imageUrls.isNotEmpty()

    fun createRequest(status: String) = AdminPopupSubmissionUpdateRequest(
        status = status,
        name = name.trim(),
        startDate = startDate,
        endDate = endDate,
        roadAddress = roadAddress.trim(),
        region = region.trim(),
        address = address.trim().ifBlank { null },
        openTime = openTime.toSubmissionTimeOrNull(),
        closeTime = closeTime.toSubmissionTimeOrNull(),
        latitude = latitude.toDoubleOrNull(),
        longitude = longitude.toDoubleOrNull(),
        description = description.trim(),
        instaPostUrl = instaPostUrl.trim().ifBlank { null },
        imageList = imageUrls.mapIndexed { index, imageUrl ->
            PopupSubmissionImage(
                imageUrl = imageUrl,
                sortOrder = index
            )
        },
        recommendIdList = selectedCategoryIds.toList()
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 92.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "제보 내용 수정", style = Bold15, color = mainBlack)
                AdminSubmissionStatusBadge(detail.status)
            }
            AdminEditTextField(
                label = "팝업명",
                required = true,
                value = name,
                onValueChange = { name = it },
                placeholder = "팝업명을 입력해 주세요"
            )
            AdminEditDateRange(
                startDate = startDate,
                endDate = endDate,
                onStartDateChange = { startDate = it },
                onEndDateChange = { endDate = it }
            )
            AdminEditTextField(
                label = "도로명 주소",
                required = true,
                value = roadAddress,
                onValueChange = { roadAddress = it },
                placeholder = "도로명 주소를 입력해 주세요"
            )
            AdminEditTextField(
                label = "지역",
                required = true,
                value = region,
                onValueChange = { region = it },
                placeholder = "지역을 입력해 주세요"
            )
            AdminEditTextField(
                label = "팝업 소개",
                required = true,
                value = description,
                onValueChange = { description = it },
                placeholder = "팝업 소개를 입력해 주세요",
                minLines = 5,
                height = 156.dp
            )
            AdminEditCategorySection(
                categories = categories,
                selectedCategoryIds = selectedCategoryIds,
                isLoading = isCategoryLoading,
                errorMessage = categoryErrorMessage,
                onRetry = onCategoryRetry,
                onCategoryClick = { category ->
                    if (selectedCategoryIds.contains(category.id)) {
                        selectedCategoryIds.remove(category.id)
                    } else {
                        selectedCategoryIds.add(category.id)
                    }
                }
            )
            AdminEditImageSection(
                imageUrls = imageUrls,
                onSelectImages = { imagePicker.launch("image/*") },
                onRemoveImage = imageUrls::remove
            )
            Text(
                text = "선택 입력",
                style = Bold15,
                color = mainBlack,
                modifier = Modifier.padding(top = 28.dp, bottom = 18.dp)
            )
            AdminEditTextField(
                label = "지번 주소",
                value = address,
                onValueChange = { address = it },
                placeholder = "지번 주소를 입력해 주세요"
            )
            AdminEditTimeFields(
                openTime = openTime,
                closeTime = closeTime,
                onOpenTimeChange = { openTime = it },
                onCloseTimeChange = { closeTime = it }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AdminEditTextField(
                        label = "위도",
                        value = latitude,
                        onValueChange = { latitude = it },
                        placeholder = "37.544"
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    AdminEditTextField(
                        label = "경도",
                        value = longitude,
                        onValueChange = { longitude = it },
                        placeholder = "127.055"
                    )
                }
            }
            AdminEditTextField(
                label = "인스타그램 URL",
                value = instaPostUrl,
                onValueChange = { instaPostUrl = it },
                placeholder = "https://instagram.com/p/..."
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminSubmissionActionButton(
                text = "반려",
                enabled = !isUpdating,
                backgroundColor = Color.White,
                textColor = Color(0xFFC94747),
                borderColor = Color(0xFFC94747),
                modifier = Modifier.weight(1f),
                onClick = {
                    pendingStatus = AdminSubmissionViewModel.STATUS_REJECTED
                }
            )
            AdminSubmissionActionButton(
                text = if (isUpdating) "처리 중..." else "승인",
                enabled = approvalEnabled && !isUpdating,
                backgroundColor = if (approvalEnabled) mainOrange else Color(0xFFFFC48A),
                textColor = Color.White,
                borderColor = if (approvalEnabled) mainOrange else Color(0xFFFFC48A),
                modifier = Modifier.weight(1f),
                onClick = {
                    pendingStatus = AdminSubmissionViewModel.STATUS_APPROVED
                }
            )
        }
    }

    pendingStatus?.let { status ->
        val approving = status == AdminSubmissionViewModel.STATUS_APPROVED
        AlertDialog(
            onDismissRequest = { pendingStatus = null },
            title = {
                Text(
                    text = if (approving) "제보 승인" else "제보 반려",
                    style = Medium18,
                    color = mainBlack
                )
            },
            text = {
                Text(
                    text = if (approving) {
                        "수정한 내용으로 팝업을 승인하시겠습니까?"
                    } else {
                        "수정한 내용을 포함해 이 제보를 반려하시겠습니까?"
                    },
                    style = Medium12,
                    color = mainGray1
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingStatus = null
                        viewModel.updateSubmission(
                            adminUuid = adminUuid,
                            popupUuid = popupUuid,
                            popupSubmissionId = popupSubmissionId,
                            request = createRequest(status),
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    if (approving) {
                                        "수정한 내용으로 승인했습니다."
                                    } else {
                                        "제보를 반려했습니다."
                                    },
                                    Toast.LENGTH_SHORT
                                ).show()
                                onStatusUpdated()
                            },
                            onError = { message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                ) {
                    Text(
                        text = if (approving) "승인" else "반려",
                        style = Medium12,
                        color = if (approving) mainOrange else Color(0xFFC94747)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingStatus = null }) {
                    Text(text = "취소", style = Medium12, color = mainGray1)
                }
            }
        )
    }
}

@Composable
private fun AdminEditTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    required: Boolean = false,
    minLines: Int = 1,
    height: androidx.compose.ui.unit.Dp = 64.dp
) {
    AdminEditFieldLabel(label, required)
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
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(bottom = 18.dp),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = mainOrange,
            unfocusedBorderColor = mainGray5,
            cursorColor = mainOrange,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
private fun AdminEditFieldLabel(
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
            Text(text = " *", style = Bold15, color = mainOrange)
        }
    }
}

@Composable
private fun AdminEditDateRange(
    startDate: String,
    endDate: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    var target by remember { mutableStateOf<AdminDateTarget?>(null) }

    AdminEditFieldLabel("운영 기간", required = true)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AdminClickableField(
            value = startDate,
            placeholder = "시작 날짜",
            onClick = { target = AdminDateTarget.START },
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "-",
            style = Medium15,
            color = mainGray1,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp)
        )
        AdminClickableField(
            value = endDate,
            placeholder = "종료 날짜",
            onClick = { target = AdminDateTarget.END },
            modifier = Modifier.weight(1f)
        )
    }

    target?.let { dateTarget ->
        val initialDate = when (dateTarget) {
            AdminDateTarget.START -> startDate
            AdminDateTarget.END -> endDate
        }
        AdminDatePickerDialog(
            initialDate = initialDate,
            onDismiss = { target = null },
            onDateSelected = { selectedDate ->
                when (dateTarget) {
                    AdminDateTarget.START -> onStartDateChange(selectedDate)
                    AdminDateTarget.END -> onEndDateChange(selectedDate)
                }
                target = null
            }
        )
    }
}

@Composable
private fun AdminClickableField(
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(46.dp)
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
private fun AdminDatePickerDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val initialMillis = runCatching {
        java.time.LocalDate.parse(initialDate)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 340.dp),
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onDateSelected(millis.toAdminDateText())
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
            state = state,
            modifier = Modifier.widthIn(max = 340.dp)
        )
    }
}

@Composable
private fun AdminEditTimeFields(
    openTime: String,
    closeTime: String,
    onOpenTimeChange: (String) -> Unit,
    onCloseTimeChange: (String) -> Unit
) {
    var target by remember { mutableStateOf<AdminTimeTarget?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AdminEditFieldLabel("오픈 시간")
            AdminClickableField(
                value = openTime,
                placeholder = "10:00",
                onClick = { target = AdminTimeTarget.OPEN },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            AdminEditFieldLabel("마감 시간")
            AdminClickableField(
                value = closeTime,
                placeholder = "20:00",
                onClick = { target = AdminTimeTarget.CLOSE },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
    }

    target?.let { timeTarget ->
        AdminTimePickerDialog(
            initialTime = when (timeTarget) {
                AdminTimeTarget.OPEN -> openTime
                AdminTimeTarget.CLOSE -> closeTime
            },
            defaultHour = if (timeTarget == AdminTimeTarget.OPEN) 10 else 20,
            onDismiss = { target = null },
            onTimeSelected = { selectedTime ->
                when (timeTarget) {
                    AdminTimeTarget.OPEN -> onOpenTimeChange(selectedTime)
                    AdminTimeTarget.CLOSE -> onCloseTimeChange(selectedTime)
                }
                target = null
            }
        )
    }
}

@Composable
private fun AdminTimePickerDialog(
    initialTime: String,
    defaultHour: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    val initialHour = initialTime.substringBefore(":").toIntOrNull() ?: defaultHour
    val initialMinute = initialTime.substringAfter(":", "").toIntOrNull() ?: 0
    val state = rememberTimePickerState(
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
                TimePicker(state = state)
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
                                "${state.hour.toTwoDigits()}:${state.minute.toTwoDigits()}"
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
private fun AdminEditCategorySection(
    categories: List<CategoryItem>,
    selectedCategoryIds: List<Long>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onCategoryClick: (CategoryItem) -> Unit
) {
    AdminEditFieldLabel("카테고리", required = true)
    when {
        isLoading && categories.isEmpty() -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = errorMessage, style = Medium12, color = mainGray1)
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
                            1.dp,
                            if (selected) mainOrange else mainGray5
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
private fun AdminEditImageSection(
    imageUrls: List<String>,
    onSelectImages: () -> Unit,
    onRemoveImage: (String) -> Unit
) {
    val context = LocalContext.current

    AdminEditFieldLabel("이미지", required = true)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .border(1.dp, mainOrange, RoundedCornerShape(8.dp))
            .clickable { onSelectImages() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "이미지 선택", style = Medium15, color = mainOrange)
        Text(
            text = "${imageUrls.size}/$MAX_ADMIN_SUBMISSION_IMAGE_COUNT",
            style = Medium15,
            color = mainGray1
        )
    }
    if (imageUrls.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            imageUrls.forEach { imageUrl ->
                Box(modifier = Modifier.size(82.dp)) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl.toSubmissionImageUrl())
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .crossfade(true)
                            .build(),
                        contentDescription = "제보 이미지",
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.default_icon),
                        error = painterResource(R.drawable.default_icon),
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF8F8F8))
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(5.dp)
                            .size(24.dp)
                            .background(Color(0xCC3A251A), CircleShape)
                            .clickable { onRemoveImage(imageUrl) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "×",
                            style = Medium18,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSubmissionActionButton(
    text: String,
    enabled: Boolean,
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = Medium15, color = textColor)
    }
}

@Composable
private fun AdminSubmissionLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = mainOrange,
            strokeWidth = 3.dp
        )
    }
}

@Composable
private fun AdminSubmissionError(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, style = Medium12, color = mainGray1)
        TextButton(onClick = onRetry) {
            Text(text = "다시 시도", style = Medium12, color = mainOrange)
        }
    }
}

private fun String.isSubmissionPending(): Boolean =
    equals("PENDING", ignoreCase = true) || this == "대기"

private fun String.isSubmissionApproved(): Boolean =
    equals("APPROVED", ignoreCase = true) || this == "승인"

private fun String.isSubmissionRejected(): Boolean =
    equals("REJECTED", ignoreCase = true) || this == "반려"

private fun String.toSubmissionDisplayDate(): String =
    take(10).replace("-", ".")

private fun PopupSubmissionTime.toDisplayTime(): String =
    "${hour.toTwoDigits()}:${minute.toTwoDigits()}"

private fun String.toSubmissionTimeOrNull(): PopupSubmissionTime? {
    val parts = split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return PopupSubmissionTime(hour = hour, minute = minute)
}

private fun Long.toAdminDateText(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(DateTimeFormatter.ISO_LOCAL_DATE)

private fun Int.toTwoDigits(): String = toString().padStart(2, '0')

private fun String.toSubmissionImageUrl(): String =
    trim()
        .trim('"')
        .replace("\\/", "/")
        .let { imageUrl ->
            when {
                imageUrl.startsWith("http://") ||
                    imageUrl.startsWith("https://") ||
                    imageUrl.startsWith("content://") ||
                    imageUrl.startsWith("file://") -> imageUrl

                imageUrl.startsWith("//") -> "https:$imageUrl"
                imageUrl.startsWith("/") ->
                    BuildConfig.URL_IMAGE.trimEnd('/') + imageUrl

                else ->
                    BuildConfig.URL_IMAGE.trimEnd('/') + "/" + imageUrl
            }
        }

private enum class AdminDateTarget {
    START,
    END
}

private enum class AdminTimeTarget {
    OPEN,
    CLOSE
}

private val ADMIN_BACKGROUND = Color(0xFFF8F8F8)
private const val MAX_ADMIN_SUBMISSION_IMAGE_COUNT = 10
