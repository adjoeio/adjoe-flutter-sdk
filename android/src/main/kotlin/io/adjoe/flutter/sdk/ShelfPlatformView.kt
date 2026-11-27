package io.adjoe.flutter.sdk

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import kotlin.math.roundToInt

internal class ShelfPlatformViewFactory(private val bridge: ShelfBridge) :
    PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val controllerId = (args as? Number)?.toLong()
            ?: throw IllegalArgumentException("Shelf controller ID is required.")
        return ShelfPlatformView(context, viewId.toLong(), controllerId, bridge).also {
            bridge.register(viewId.toLong(), it)
        }
    }
}

internal class ShelfPlatformView(
    context: Context,
    private val id: Long,
    val controllerId: Long,
    private val bridge: ShelfBridge,
) : PlatformView {
    val container = ShelfHostView(context)
    override fun getView(): View = container
    override fun dispose() {
        bridge.unregister(id, this)
        container.removeShelf()
    }
}

internal class ShelfHostView(context: Context) : FrameLayout(context) {
    private var shelf: View? = null
    private var measurementWidth = 0
    private var lastHeight: Int? = null
    private var report: ((Double) -> Unit)? = null
    private var measuring = false
    private var measurementPending = false
    private var measuredContentWidth = 0
    private val measurement = Runnable {
        measurementPending = false
        measureContent()
        shelf?.let { view ->
            if (view.width != measuredContentWidth || view.height != view.measuredHeight || view.isLayoutRequested) {
                requestLayout()
            }
        }
    }

    fun install(view: View, onHeight: (Double) -> Unit) {
        removeShelf()
        shelf = view
        report = onHeight
        addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        scheduleMeasurement()
    }

    fun removeShelf() {
        removeCallbacks(measurement)
        measurementPending = false
        removeAllViews()
        shelf = null
        report = null
        lastHeight = null
        measuredContentWidth = 0
    }

    fun setMeasurementWidth(logicalWidth: Double) {
        measurementWidth = (logicalWidth * resources.displayMetrics.density).roundToInt().coerceAtLeast(1)
        scheduleMeasurement()
    }

    fun scheduleMeasurement() {
        if (measuring || measurementPending) return
        measurementPending = true
        post(measurement)
    }

    private fun measureContent() {
        val view = shelf ?: return
        val contentWidth = measurementWidth.takeIf { it > 0 } ?: width
        if (contentWidth <= 0 || measuring) return
        measuring = true
        try {
            if (measuredContentWidth != contentWidth || view.isLayoutRequested) {
                view.measure(
                    MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                )
                measuredContentWidth = contentWidth
            }
            val height = view.measuredHeight
            if (lastHeight != height) {
                lastHeight = height
                val callback = report
                val logicalHeight = height / resources.displayMetrics.density.toDouble()
                post {
                    if (shelf === view && report === callback && lastHeight == height) {
                        callback?.invoke(logicalHeight)
                    }
                }
            }
        } finally {
            measuring = false
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (measurementWidth == 0) {
            measurementWidth = MeasureSpec.getSize(widthMeasureSpec)
        }
        measureContent()
        setMeasuredDimension(
            resolveSize(0, widthMeasureSpec),
            resolveSize(0, heightMeasureSpec),
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        shelf?.let { view ->
            if (view.width != measuredContentWidth || view.height != view.measuredHeight || view.isLayoutRequested) {
                view.layout(0, 0, measuredContentWidth, view.measuredHeight)
            }
        }
    }
}
