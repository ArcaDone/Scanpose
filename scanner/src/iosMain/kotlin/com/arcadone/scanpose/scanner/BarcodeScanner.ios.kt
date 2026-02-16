package com.arcadone.scanpose.scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.useContents
import kotlinx.cinterop.value
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Code
import platform.AVFoundation.AVMetadataObjectTypeCode93Code
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVMetadataObjectTypeUPCECode
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun BarcodeScanner(
    modifier: Modifier,
    onBarcodeScanned: (String) -> Unit,
) {
    NSLog("BarcodeScanner: Composable inizializzato")

    val coordinator = remember {
        NSLog("BarcodeScanner: Creazione coordinator")
        ScannerCoordinator(onBarcodeScanned)
    }

    UIKitView(
        factory = {
            NSLog("BarcodeScanner: Factory chiamata")

            val session = AVCaptureSession()
            session.sessionPreset = AVCaptureSessionPresetHigh
            NSLog("BarcodeScanner: AVCaptureSession creata con preset High")

            // Configurazione Input (Camera)
            val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            NSLog("BarcodeScanner: Device camera: ${if (device != null) "trovato" else "NULL!"}")

            if (device != null) {
                memScoped {
                    val errorPtr = alloc<ObjCObjectVar<NSError?>>()
                    val input = AVCaptureDeviceInput.deviceInputWithDevice(device, errorPtr.ptr)

                    if (errorPtr.value != null) {
                        NSLog("BarcodeScanner: ERRORE creazione input: ${errorPtr.value?.localizedDescription}")
                    }

                    if (input != null) {
                        NSLog("BarcodeScanner: Input creato con successo")
                        if (session.canAddInput(input)) {
                            session.addInput(input)
                            NSLog("BarcodeScanner: Input aggiunto alla sessione")
                        } else {
                            NSLog("BarcodeScanner: ERRORE - canAddInput ritorna false!")
                        }
                    } else {
                        NSLog("BarcodeScanner: ERRORE - input è NULL!")
                    }

                    errorPtr.value
                }
            }

            // Configurazione Output (Meta dati) - IMPORTANTE: prima di startRunning
            val metadataOutput = AVCaptureMetadataOutput()
            NSLog("BarcodeScanner: MetadataOutput creato")

            if (session.canAddOutput(metadataOutput)) {
                session.addOutput(metadataOutput)
                NSLog("BarcodeScanner: MetadataOutput aggiunto")

                metadataOutput.setMetadataObjectsDelegate(coordinator, dispatch_get_main_queue())
                NSLog("BarcodeScanner: Delegate impostato")

                // IMPORTANTE: impostare i tipi DOPO aver aggiunto l'output alla sessione
                val types = listOf(
                    AVMetadataObjectTypeQRCode,
                    AVMetadataObjectTypeEAN13Code,
                    AVMetadataObjectTypeEAN8Code,
                    AVMetadataObjectTypeCode128Code,
                    AVMetadataObjectTypeCode39Code,
                    AVMetadataObjectTypeCode93Code,
                    AVMetadataObjectTypeUPCECode,
                )

                metadataOutput.metadataObjectTypes = types
                NSLog("BarcodeScanner: Tipi di metadata impostati: ${metadataOutput.metadataObjectTypes.size}")
                NSLog("BarcodeScanner: Tipi disponibili: ${metadataOutput.availableMetadataObjectTypes.size}")
            } else {
                NSLog("BarcodeScanner: ERRORE - canAddOutput ritorna false!")
            }

            val previewLayer = AVCaptureVideoPreviewLayer(session = session)
            previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
            NSLog("BarcodeScanner: PreviewLayer creato")

            // Creiamo una UIView container custom
            val view = object : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
                override fun layoutSubviews() {
                    super.layoutSubviews()

                    NSLog("BarcodeScanner: layoutSubviews chiamato")

                    val viewBounds = this.bounds
                    NSLog("BarcodeScanner: View bounds: width=${viewBounds.useContents { size.width }}, height=${viewBounds.useContents { size.height }}")

                    // Aggiorna il frame del preview layer
                    CATransaction.begin()
                    CATransaction.setValue(true, kCATransactionDisableActions)

                    layer.sublayers?.firstOrNull()?.let { sublayer ->
                        if (sublayer is AVCaptureVideoPreviewLayer) {
                            sublayer.frame = viewBounds
                            NSLog("BarcodeScanner: PreviewLayer frame aggiornato")
                        }
                    }

                    CATransaction.commit()
                }
            }

            view.backgroundColor = UIColor.blackColor
            view.layer.addSublayer(previewLayer)
            NSLog("BarcodeScanner: PreviewLayer aggiunto come sublayer")

            coordinator.previewLayer = previewLayer
            coordinator.session = session
            coordinator.containerView = view

            // Avvia la sessione DOPO aver configurato tutto
            session.startRunning()
            NSLog("BarcodeScanner: Session avviata: ${session.running}")

            // Forza layout iniziale
            view.setNeedsLayout()
            view.layoutIfNeeded()

            view
        },
        modifier = modifier,
        update = { view ->
            NSLog("BarcodeScanner: update chiamato")
            view.setNeedsLayout()
            view.layoutIfNeeded()
        },
        onRelease = {
            NSLog("BarcodeScanner: onRelease chiamato")
            coordinator.session?.stopRunning()
        },
    )

    DisposableEffect(Unit) {
        NSLog("BarcodeScanner: DisposableEffect - component montato")
        onDispose {
            NSLog("BarcodeScanner: DisposableEffect - cleanup")
            coordinator.session?.stopRunning()
        }
    }
}

class ScannerCoordinator(val onScanned: (String) -> Unit) :
    NSObject(),
    AVCaptureMetadataOutputObjectsDelegateProtocol {

    var session: AVCaptureSession? = null
    var previewLayer: AVCaptureVideoPreviewLayer? = null
    var containerView: UIView? = null
    private var lastScannedCode: String? = null
    private var lastScannedTime: Long = 0

    init {
        NSLog("ScannerCoordinator: Inizializzato")
    }

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        NSLog("ScannerCoordinator: ========================================")
        NSLog("ScannerCoordinator: captureOutput chiamato!")
        NSLog("ScannerCoordinator: Numero di oggetti ricevuti: ${didOutputMetadataObjects.size}")

        didOutputMetadataObjects.forEachIndexed { index, obj ->
            NSLog("ScannerCoordinator: Oggetto [$index]: ${obj?.toString()}")

            if (obj is AVMetadataMachineReadableCodeObject) {
                NSLog("ScannerCoordinator: È un AVMetadataMachineReadableCodeObject!")
                NSLog("ScannerCoordinator: Tipo: ${obj.type}")
                NSLog("ScannerCoordinator: StringValue: ${obj.stringValue}")
                NSLog("ScannerCoordinator: Corners: ${obj.corners.size}")

                obj.stringValue?.let { code ->
                    NSLog("ScannerCoordinator: ✅ CODICE SCANSIONATO: '$code'")

                    // Evita scansioni duplicate troppo rapide (debounce di 1 secondo)
                    val currentTime = NSDate().timeIntervalSinceReferenceDate().toLong()
                    if (code != lastScannedCode || (currentTime - lastScannedTime) > 1) {
                        lastScannedCode = code
                        lastScannedTime = currentTime
                        NSLog("ScannerCoordinator: 🎯 Notificando il callback con il codice")
                        onScanned(code)
                    } else {
                        NSLog("ScannerCoordinator: ⏭️ Codice duplicato ignorato (debounce)")
                    }
                } ?: NSLog("ScannerCoordinator: ⚠️ stringValue è NULL!")
            } else {
                NSLog("ScannerCoordinator: ⚠️ Non è un AVMetadataMachineReadableCodeObject, tipo: ${obj?.toString()}")
            }
        }

        if (didOutputMetadataObjects.isEmpty()) {
            NSLog("ScannerCoordinator: ⚠️ Lista di oggetti VUOTA")
        }
        NSLog("ScannerCoordinator: ========================================")
    }
}
