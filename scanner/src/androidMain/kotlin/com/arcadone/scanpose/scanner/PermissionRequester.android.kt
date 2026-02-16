package com.arcadone.scanpose.scanner

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidPermissionRequester(private val permission: String, private val activity: Activity, private val launcher: ManagedActivityResultLauncher<String, Boolean>) : PermissionRequester {
    private var continuation: (Continuation<Boolean>)? = null

    override suspend fun isPermissionGranted(): Boolean = ContextCompat.checkSelfPermission(
        activity,
        permission,
    ) == PackageManager.PERMISSION_GRANTED

    override suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { cont ->
        continuation = cont
        launcher.launch(permission)
    }

    fun onPermissionResult(granted: Boolean) {
        continuation?.resume(granted)
        continuation = null
    }
}

@Composable
actual fun rememberPermissionRequester(): PermissionRequester {
    val activity = rememberActivity()
    val permission = Manifest.permission.CAMERA

    // Hold reference to requester
    var requester by remember { mutableStateOf<AndroidPermissionRequester?>(null) }

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                requester?.onPermissionResult(granted)
            },
        )

    return remember {
        AndroidPermissionRequester(
            permission = permission,
            activity = activity,
            launcher = launcher,
        ).also { requester = it }
    }
}

@Composable
fun rememberActivity(): Activity {
    val context = LocalContext.current
    return context as Activity
}
