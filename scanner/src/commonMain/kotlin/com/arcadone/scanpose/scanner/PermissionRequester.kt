package com.arcadone.scanpose.scanner

import androidx.compose.runtime.Composable

interface PermissionRequester {
    suspend fun isPermissionGranted(): Boolean

    suspend fun requestPermission(): Boolean
}

@Composable
expect fun rememberPermissionRequester(): PermissionRequester
