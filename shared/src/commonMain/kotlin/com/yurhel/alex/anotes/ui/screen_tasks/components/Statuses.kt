package com.yurhel.alex.anotes.ui.screen_tasks.components

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yurhel.alex.anotes.ui.screen_tasks.TasksViewModel

@Composable
fun Statuses(
    vm: TasksViewModel,
    modifier: Modifier
) {
    if (vm.statuses.isNotEmpty()) {
        LazyRow(modifier = modifier) {
            items(items = vm.statuses) {
                StatusCard(
                    selectedStatusId = vm.selectedStatus,
                    status = it,
                    onClick = vm::changeStatus,
                    onLongClicked = vm::editStatus
                )
            }
        }
    }
}