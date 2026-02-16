package com.arcadone.scanpose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arcadone.scanpose.scanner.rememberPermissionRequester

@Composable
fun PermissionScreen(blockOnGranted: @Composable () -> Unit) {
    val permissionRequester = rememberPermissionRequester()
    var permissionGranted by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        val isGranted = permissionRequester.isPermissionGranted()
        if (isGranted) {
            permissionGranted = true
        } else {
            permissionGranted = permissionRequester.requestPermission()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (permissionGranted) {
            true -> {
                blockOnGranted()
            }

            false -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Empty State",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
