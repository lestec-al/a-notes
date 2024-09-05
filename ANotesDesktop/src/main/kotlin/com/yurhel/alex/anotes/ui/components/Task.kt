package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.data.local.StatusObj
import com.yurhel.alex.anotes.data.local.TasksObj

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    modifier: Modifier,
    onClick: (() -> Unit)?,
    content: @Composable (ColumnScope.() -> Unit)
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = modifier,
            content = content
        )
    } else {
        // No needed ?
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = modifier,
            content = content
        )
    }
}

@Composable
fun Task(
    task: TasksObj,
    onClick: (() -> Unit)?,
    modifier: Modifier,
    tasksTextPadding: Int,
    statuses: List<StatusObj>,
    onBackgroundColor: Color
) {
    TaskCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(horizontalArrangement = Arrangement.Center) {
            // Color indicator
            Canvas(
                modifier = Modifier
                    .padding(top = (tasksTextPadding + 8).dp) // 8 - text native padding?
                    .size(10.dp)
            ) {
                drawCircle(
                    color = try {
                        Color(statuses.find { it.id == task.status }!!.color)
                    } catch (e: Exception) {
                        onBackgroundColor
                    }
                )
            }

            // Description
            Text(
                text = task.description,
                modifier = Modifier.padding(
                    horizontal = 5.dp,
                    vertical = tasksTextPadding.dp
                )
            )
        }
    }
}