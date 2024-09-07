package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.data.StatusObj

@OptIn(ExperimentalFoundationApi::class)
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
            .clip(CardDefaults.shape)
            .combinedClickable(
                onLongClick = onLongClicked,
                onClick = onClick
            ).let {
                // Border for selected status
                if (status.id == selectedStatusId) {
                    it.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = CardDefaults.shape
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