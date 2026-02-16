package com.arcadone.scanpose.scanner

import androidx.compose.runtime.Composable
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import kotlin.coroutines.resume

class IOSPermissionRequester : PermissionRequester {
    override suspend fun isPermissionGranted(): Boolean = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized

    override suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { cont ->
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
            cont.resume(granted)
        }
    }
}

@Composable
actual fun rememberPermissionRequester(): PermissionRequester = IOSPermissionRequester()
