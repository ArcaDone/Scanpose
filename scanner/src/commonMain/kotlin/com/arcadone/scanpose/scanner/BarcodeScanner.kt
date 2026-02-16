package com.arcadone.scanpose.scanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun BarcodeScanner(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String) -> Unit,
)
