package com.pappang.poppang_aos.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.model.CategoryItem
import com.pappang.poppang_aos.ui.theme.ExtraBold18
import com.pappang.poppang_aos.ui.theme.Medium12
import com.pappang.poppang_aos.ui.theme.mainGray1
import com.pappang.poppang_aos.ui.theme.mainGray2
import com.pappang.poppang_aos.ui.theme.mainGray4
import com.pappang.poppang_aos.ui.theme.mainOrange
import com.pappang.poppang_aos.viewmodel.CategoryItemViewModel

@Composable
fun CategorySelectionScreen(categoryViewModel: CategoryItemViewModel) {
    LaunchedEffect(Unit) {
        categoryViewModel.fetchCategoryItems()
    }
    val viewModel = remember { categoryViewModel }
    val categories = viewModel.categories
    Box(
        modifier = Modifier.background(Color(0xFFFFFFFF)),
    ) {
        Column {
            Text(
                text = "추천받고 싶은 항목을\n선택해주세요.",
                style = ExtraBold18,
                color = Color.Black,
                modifier = Modifier.padding(start = 24.dp, top = 44.dp)
            )
            Text(
                text = "선택하신 항목에 맞게 추천해드려요.",
                style = Medium12,
                color = mainGray1,
                modifier = Modifier.padding(start = 24.dp, top = 11.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            FlowRow(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = viewModel.selectedCategories.contains(category)
                    CategoryButton(
                        category = category,
                        isSelected = isSelected,
                        onClick = { viewModel.toggleCategory(category) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryButton(
    category: CategoryItem,
    selectedColor: Color = Color(0xFFFFF4EA),
    unselectedColor: Color = mainGray4,
    selectedTextColor: Color = mainOrange,
    unselectedTextColor: Color = mainGray2,
    selectedBorderColor: Color = mainOrange,
    unselectedBorderColor: Color = mainGray4,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() },
        color = if (isSelected) selectedColor else unselectedColor,
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, if (isSelected) selectedBorderColor else unselectedBorderColor)
    ) {
        Text(
            text = category.recommendName,
            style = Medium12,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = if (isSelected) selectedTextColor else unselectedTextColor
        )
    }
}
