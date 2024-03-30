package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.data.local.StatusObj

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StatusCard(
    selectedStatusId: Int,
    status: StatusObj,
    onClick: () -> Unit = {},
    onLongClicked: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(status.color)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(
                onLongClick = onLongClicked,
                onClick = onClick
            ).let {
                // Border for selected status
                if (status.id == selectedStatusId) {
                    it.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = RoundedCornerShape(10.dp)
                    )
                } else {
                    it
                }
            }
    ) {
        Text(
            text = status.title,
            modifier = Modifier.padding(10.dp)
        )
    }
}