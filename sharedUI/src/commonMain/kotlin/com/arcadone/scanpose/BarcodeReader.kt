package com.arcadone.scanpose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arcadone.scanpose.scanner.BarcodeOverlay
import com.arcadone.scanpose.scanner.BarcodeScanner

@Composable
fun BarcodeReader(
    isScanning: Boolean,
    lastScannedCode: String?,
    onBarcodeScanned: (String) -> Unit,
    onResetScan: () -> Unit,
) {
    PermissionScreen(
        blockOnGranted = {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (isScanning) {
                    BarcodeScanner(
                        modifier = Modifier.fillMaxSize(),
                        onBarcodeScanned = { code ->
                            if (isScanning && code != lastScannedCode) {
                                onBarcodeScanned(code)
                            }
                        },
                    )

                    BarcodeOverlay()
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Result: $lastScannedCode",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = onResetScan) {
                            Text(text = "Scan Again")
                        }
                    }
                }
            }
        }
    )

}
