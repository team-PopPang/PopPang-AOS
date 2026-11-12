package com.poppang.PopPang.view.renewal

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectedFilterMenuPositionTest {
    private val position = ConnectedFilterMenuPosition(16)

    @Test fun optionsJoinTheChipBottomWithoutReplacingItsHeader() {
        assertEquals(
            IntOffset(48, 133),
            position.calculatePosition(IntRect(48, 104, 140, 134), IntSize(384, 832), LayoutDirection.Ltr, IntSize(220, 440)),
        )
    }

    @Test fun panelsStayInsideTheWindowAtTheRightAndBottomEdges() {
        assertEquals(
            IntOffset(148, 516),
            position.calculatePosition(IntRect(300, 760, 360, 790), IntSize(384, 832), LayoutDirection.Ltr, IntSize(220, 300)),
        )
        assertEquals(
            IntOffset.Zero,
            position.calculatePosition(IntRect(30, 40, 60, 70), IntSize(180, 100), LayoutDirection.Ltr, IntSize(220, 140)),
        )
    }

    @Test fun rightToLeftLayoutsKeepTheHeaderAlignedToTheChipRightEdge() {
        assertEquals(
            IntOffset(140, 133),
            position.calculatePosition(IntRect(300, 104, 360, 134), IntSize(384, 832), LayoutDirection.Rtl, IntSize(220, 300)),
        )
    }

    @Test fun theSeamOnlyConnectsThePartDirectlyUnderTheChip() {
        var seam = IntRange.EMPTY
        val connected = ConnectedFilterMenuPosition(16, 1) { seam = it }
        connected.calculatePosition(IntRect(300, 104, 360, 134), IntSize(384, 832), LayoutDirection.Ltr, IntSize(220, 300))
        assertEquals(152..211, seam)
        connected.calculatePosition(IntRect(300, 760, 360, 790), IntSize(384, 832), LayoutDirection.Ltr, IntSize(220, 300))
        assertEquals(IntRange.EMPTY, seam)
    }
}
