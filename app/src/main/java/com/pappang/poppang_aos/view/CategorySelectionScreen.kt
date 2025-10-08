package com.pappang.poppang_aos.view

import android.R.attr.color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pappang.poppang_aos.ui.theme.largeTitlie
import com.pappang.poppang_aos.ui.theme.mainGray1
import com.pappang.poppang_aos.ui.theme.mainGray2
import com.pappang.poppang_aos.ui.theme.mainGray4
import com.pappang.poppang_aos.ui.theme.mainOrange
import com.pappang.poppang_aos.ui.theme.title1
import com.pappang.poppang_aos.viewmodel.CategoryItemViewModel
import androidx.compose.foundation.layout.FlowRow

@Composable
fun CategorySelectionScreen(categoryViewModel: CategoryItemViewModel) {
    val viewModel = remember { categoryViewModel }
    val categories = viewModel.categories
    Box(
        modifier = Modifier.background(Color(0xFFFFFFFF)),
    ) {
        Column {
            Text(
                text = "추천받고 싶은 항목을\n선택해주세요.",
                style = largeTitlie,
                color = Color.Black,
                modifier = Modifier.padding(start = 24.dp, top = 44.dp)
            )
            Text(
                text = "선택하신 항목에 맞게 추천해드려요.",
                style = title1,
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
                        text = category,
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
    text: String,
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
            text = text,
            style = title1,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = if (isSelected) selectedTextColor else unselectedTextColor
        )
    }
}

@Composable
@Preview
fun CategoryButtonPreview() {
    CategoryButton(text = "예술/디자인")
}
@Composable
@Preview
fun CategorySelectionScreenPreview() {
    CategorySelectionScreen(categoryViewModel = CategoryItemViewModel())
}