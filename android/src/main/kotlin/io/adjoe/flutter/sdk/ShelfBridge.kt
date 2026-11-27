package io.adjoe.flutter.sdk

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.adjoe.sdk.AppCategory
import io.adjoe.sdk.PlaytimeException
import io.adjoe.sdk.dropui.PlaytimeShelfBindListener
import io.adjoe.sdk.dropui.PlaytimeShelfController
import io.adjoe.sdk.dropui.PlaytimeShelfOptions
import io.adjoe.sdk.dropui.PlaytimeShelfPlacement
import io.adjoe.sdk.dropui.PlaytimeShelfStatus
import io.adjoe.sdk.dropui.PlaytimeShelfView
import io.flutter.plugin.common.BinaryMessenger

internal class ShelfBridge(messenger: BinaryMessenger) : ShelfHostApi, LifecycleOwner {
    private val registry = LifecycleRegistry(this).apply {
        currentState = Lifecycle.State.STARTED
    }
    override val lifecycle: Lifecycle get() = registry
    private val callbacks = ShelfFlutterApi(messenger)
    private val sessions = mutableMapOf<Long, ShelfSession>()
    private val views = mutableMapOf<Long, ShelfPlatformView>()
    private var activity: FragmentActivity? = null

    fun setActivity(value: Activity?, permanent: Boolean = false) {
        if (value == null) {
            sessions.values.forEach { entry ->
                activity?.lifecycle?.removeObserver(entry.controller)
                entry.platformView?.container?.removeShelf()
                entry.shelf = null
            }
            if (permanent) reset()
        }
        activity = value as? FragmentActivity
        if (activity != null) {
            sessions.forEach { (id, entry) ->
                try {
                    bind(id, entry)
                } catch (error: Exception) {
                    reportBindingFailure(id, entry, error)
                }
            }
        }
    }

    override fun create(
        controllerId: Long,
        options: ShelfOptionsMessage,
        callback: (Result<Unit>) -> Unit,
    ) {
        callback(runCatching {
            requireActivity()
            if (sessions.containsKey(controllerId)) {
                throw FlutterError("duplicate-controller", "Shelf controller already exists.")
            }
            if (options.placement.isBlank()) {
                throw FlutterError("invalid-placement", "Shelf placement must not be blank.")
            }
            val category = when (options.category) {
                "gaming" -> AppCategory.GAMING
                "non-gaming" -> AppCategory.NON_GAMING
                "both" -> AppCategory.BOTH
                else -> throw FlutterError("invalid-category", "Unknown Shelf category.")
            }
            if (!io.adjoe.sdk.Playtime.getStatus().isInitialized) {
                throw FlutterError("not-initialized", "Initialize Playtime before creating a Shelf.")
            }
            val controller = PlaytimeShelfController(
                options = PlaytimeShelfOptions(
                    placement = PlaytimeShelfPlacement(options.placement),
                    requestedVariant = options.requestedVariant,
                    category = category,
                    actionObserver = { action ->
                        if (sessions.containsKey(controllerId)) {
                            callbacks.actionPerformed(controllerId, action.campaignId, action.actionName) { }
                        }
                    },
                    onStatusChanged = { status ->
                        sessions[controllerId]?.let { entry ->
                            callbacks.statusChanged(controllerId, ShelfStatusMapper.map(status)) { }
                            entry.platformView?.container?.scheduleMeasurement()
                        }
                    },
                    onSizeChanged = { _, _ ->
                        sessions[controllerId]?.platformView?.container?.scheduleMeasurement()
                    },
                ),
                lifecycleOwner = this,
            )
            val entry = ShelfSession(controller)
            sessions[controllerId] = entry
            try {
                bind(controllerId, entry)
            } catch (error: Exception) {
                dispose(controllerId)
                throw error
            }
        })
    }

    override fun prefetch(controllerId: Long) = session(controllerId).startIfNeeded()
    override fun refresh(controllerId: Long) = session(controllerId).refresh()

    override fun attach(controllerId: Long, viewId: Long, attachmentId: Long) {
        val entry = session(controllerId)
        if (entry.attachmentId != null) {
            throw FlutterError("already-attached", "Only one view may mount a Shelf controller.")
        }
        val view = views[viewId]?.takeIf { it.controllerId == controllerId }
            ?: throw FlutterError("missing-view", "Shelf platform view is no longer available.")
        requireActivity()
        entry.attachmentId = attachmentId
        entry.platformView = view
        try {
            bind(controllerId, entry)
            entry.startIfNeeded()
        } catch (error: Exception) {
            detach(controllerId, attachmentId)
            throw error
        }
    }

    private fun bind(id: Long, entry: ShelfSession) {
        val host = entry.platformView?.container
        val currentActivity = requireActivity()
        val shelf = PlaytimeShelfView(currentActivity)
        entry.shelf = shelf
        host?.install(shelf) { height ->
            val attachment = entry.attachmentId ?: return@install
            if (sessions[id] !== entry || entry.platformView?.container !== host) return@install
            callbacks.sizeChanged(id, attachment, height) { }
        }
        entry.controller.bind(shelf, currentActivity, object : PlaytimeShelfBindListener {
            override fun onSuccess() { host?.scheduleMeasurement() }
            override fun onFailure(error: PlaytimeException) {
                throw ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))
            }
        })
    }

    private fun reportBindingFailure(id: Long, entry: ShelfSession, error: Exception) {
        entry.platformView?.container?.removeShelf()
        entry.shelf = null
        activity?.lifecycle?.removeObserver(entry.controller)
        callbacks.statusChanged(
            id,
            ShelfStatusMessage(
                ShelfStatusKind.UNAVAILABLE,
                reason = "unknown",
                retryable = false,
                message = "Shelf binding failed: ${error.message ?: error.javaClass.simpleName}",
            ),
        ) { }
    }

    override fun measure(controllerId: Long, attachmentId: Long, width: Double) {
        val entry = session(controllerId)
        if (entry.attachmentId != attachmentId) return
        if (!width.isFinite() || width <= 0) {
            throw FlutterError("invalid-width", "Shelf width must be finite and positive.")
        }
        entry.platformView?.container?.setMeasurementWidth(width)
    }

    override fun detach(controllerId: Long, attachmentId: Long) {
        val entry = sessions[controllerId] ?: return
        if (entry.attachmentId != attachmentId) return
        entry.platformView?.container?.removeShelf()
        entry.platformView = null
        entry.attachmentId = null
    }

    override fun dispose(controllerId: Long) {
        val entry = sessions.remove(controllerId) ?: return
        entry.platformView?.container?.removeShelf()
        activity?.lifecycle?.removeObserver(entry.controller)
        registry.removeObserver(entry.controller)
        entry.controller.release()
    }

    fun reset() {
        sessions.keys.toList().forEach { id ->
            dispose(id)
            callbacks.disposed(id) { }
        }
    }

    fun close() {
        reset()
        views.clear()
        activity = null
        registry.currentState = Lifecycle.State.DESTROYED
    }

    fun register(id: Long, view: ShelfPlatformView) { views[id] = view }

    fun unregister(id: Long, view: ShelfPlatformView) {
        if (views[id] !== view) return
        views.remove(id)
        sessions[view.controllerId]?.let { entry ->
            if (entry.platformView === view) {
                entry.attachmentId?.let { detach(view.controllerId, it) }
            }
        }
    }

    private fun session(id: Long) = sessions[id]
        ?: throw FlutterError("disposed-controller", "Shelf controller is missing or disposed.")

    private fun requireActivity() = activity
        ?: throw FlutterError("unsupported-activity", "Android Shelf requires an attached FlutterFragmentActivity.")
}

internal class ShelfSession(val controller: PlaytimeShelfController) {
    var attachmentId: Long? = null
    var platformView: ShelfPlatformView? = null
    var shelf: PlaytimeShelfView? = null
    private var started = false

    fun startIfNeeded() {
        if (started) return
        refresh()
    }

    fun refresh() {
        controller.fetch()
        started = true
    }
}

internal object ShelfStatusMapper {
    fun map(status: PlaytimeShelfStatus): ShelfStatusMessage = when (status) {
        PlaytimeShelfStatus.Idle -> ShelfStatusMessage(ShelfStatusKind.IDLE)
        PlaytimeShelfStatus.Loading -> ShelfStatusMessage(ShelfStatusKind.LOADING)
        is PlaytimeShelfStatus.Ready -> ShelfStatusMessage(
            ShelfStatusKind.READY,
            availableCampaigns = status.availableCampaigns.toLong(),
            installedCampaigns = status.installedCampaigns.toLong(),
        )
        is PlaytimeShelfStatus.Empty -> ShelfStatusMessage(
            ShelfStatusKind.EMPTY, reason = reason(status.reason.name),
        )
        is PlaytimeShelfStatus.Unavailable -> ShelfStatusMessage(
            ShelfStatusKind.UNAVAILABLE, reason = reason(status.reason.name),
            retryable = status.retryable, message = status.message,
        )
    }

    private fun reason(value: String): String = when (value) {
        "NO_CAMPAIGNS" -> "noCampaigns"
        "ALL_CAMPAIGNS_SKIPPED" -> "allCampaignsSkipped"
        "NOT_CONFIGURED" -> "notConfigured"
        "UNSUPPORTED_SDK_VERSION" -> "unsupportedSdkVersion"
        "NETWORK" -> "network"
        "INVALID_CONFIGURATION" -> "invalidConfiguration"
        "SDK_NOT_INITIALIZED" -> "sdkNotInitialized"
        "TOS_NOT_ACCEPTED" -> "tosNotAccepted"
        else -> "unknown"
    }
}
