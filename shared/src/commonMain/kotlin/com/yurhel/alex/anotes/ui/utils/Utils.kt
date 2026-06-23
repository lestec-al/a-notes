package com.yurhel.alex.anotes.ui.utils

import androidx.compose.runtime.Composable
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.app_name
import org.jetbrains.compose.resources.stringResource

@Composable
fun getAppName() = stringResource(Res.string.app_name)