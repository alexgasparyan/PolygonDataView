package com.armdroid.polygondataview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View


/**
 * Created by Alex Gasparyan on 3/2/2018.
 *
 */


class PolygonDataView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var data: List<Coordinate>
    private lateinit var range: Coordinate
    private var highlight: Coordinate? = null
    private var polygonData: List<Coordinate>? = null
    private var polygonPaint: Paint = Paint()
    private var polygonStrokePaint: Paint = Paint()
    private var highlightPaint: Paint = Paint()
    private var highlightStrokePaint: Paint = Paint()
    private var polygonPath: Path = Path()
    private var highlightPath: Path = Path()
    private var canvasWidth = 0
    private var canvasHeight = 0
    private var hasPendingDraw = false
    private var hasStroke = false
    private var hasHighlightStroke = false
    private var vertexType = Vertex.SHARP

    enum class Vertex {
        SHARP, CURVED
    }

    init {
        var typedArray: TypedArray? = null
        attrs?.let {
            typedArray = context.obtainStyledAttributes(it, R.styleable.PolygonDataView)
        }

        polygonPaint.color = typedArray?.getColor(R.styleable.PolygonDataView_color, Color.LTGRAY) ?: Color.LTGRAY
        polygonPaint.style = Paint.Style.FILL

        polygonStrokePaint.color = typedArray?.getColor(R.styleable.PolygonDataView_strokeColor, Color.BLACK) ?: Color.BLACK
        polygonStrokePaint.style = Paint.Style.STROKE
        polygonStrokePaint.strokeWidth = typedArray?.getDimension(R.styleable.PolygonDataView_strokeWidth, 0F) ?: 0F
        polygonStrokePaint.isAntiAlias = true
        hasStroke = polygonStrokePaint.strokeWidth != 0F

        highlightPaint.color = typedArray?.getColor(R.styleable.PolygonDataView_highlightColor, Color.BLUE) ?: Color.BLUE
        highlightPaint.style = Paint.Style.FILL

        highlightStrokePaint.color = typedArray?.getColor(R.styleable.PolygonDataView_highlightStrokeColor, Color.BLUE) ?: Color.BLUE
        highlightStrokePaint.style = Paint.Style.STROKE
        highlightStrokePaint.strokeWidth = typedArray?.getDimension(R.styleable.PolygonDataView_highlightStrokeWidth, 0F) ?: 0F
        highlightStrokePaint.isAntiAlias = true
        hasHighlightStroke = polygonStrokePaint.strokeWidth != 0F

        setVertexType(Vertex.values()[typedArray?.getInt(R.styleable.PolygonDataView_vertexType, Vertex.SHARP.ordinal)
                ?: Vertex.SHARP.ordinal])
        typedArray?.recycle()
    }

    fun setColor(@ColorInt color: Int) {
        polygonPaint.color = color
    }

    fun setStrokeColor(@ColorInt color: Int) {
        polygonStrokePaint.color = color
    }

    fun setStrokeWidth(strokeWidth: Float) {
        polygonStrokePaint.strokeWidth = strokeWidth
        hasStroke = strokeWidth != 0F
    }

    fun setHighlightColor(@ColorInt color: Int) {
        highlightPaint.color = color
    }

    fun setHighlightStrokeColor(@ColorInt color: Int) {
        highlightStrokePaint.color = color
    }

    fun setHighlightStrokeWidth(strokeWidth: Float) {
        highlightStrokePaint.strokeWidth = strokeWidth
        hasHighlightStroke = strokeWidth != 0F

    }

    fun setVertexType(type: Vertex) {
        this.vertexType = type
    }

    fun getVertexType(): Vertex = vertexType

    fun setData(inputData: List<Coordinate>) {
        this.data = inputData
        if (isValidForDraw()) {
            prepareDraw()
        }
    }

    fun setRange(startX: Float, endX: Float) {
        this.range = startX to endX
    }

    fun setHighlightRange(startX: Float, endX: Float) {
        this.highlight = startX to endX
        if (isValidForDraw()) {
            invalidate()
        }
    }

    fun clearHighlightRange() {
        this.highlight = null
        if (isValidForDraw()) {
            invalidate()
        }
    }

    private fun isValidForDraw(): Boolean {
        if (canvasWidth == 0 || canvasHeight == 0) {
            hasPendingDraw = true
            return false
        }
        return true
    }

    private fun dataToScreenSize(input: Coordinate): Coordinate {
        val maxHeight = data.maxBy { it.second }!!.second
        val xCoordinate = (input.first - range.first) * canvasWidth / (range.second - range.first) + polygonStrokePaint.strokeWidth
        val yCoordinate = canvasHeight - input.second * canvasHeight / maxHeight + polygonStrokePaint.strokeWidth
        return Coordinate(xCoordinate, yCoordinate)
    }

    private fun prepareDraw() {
        hasPendingDraw = false

        data = data.filter { it.first >= range.first && it.first <= range.second }.sortedBy { it.first }
        if (data.size < 2) {
            return
        }
        data = data.map { dataToScreenSize(it) }

        if (vertexType == Vertex.CURVED) {
            polygonData = data.mapIndexed { index, pair ->
                when (index) {
                    0 -> pair
                    else -> (pair.first + data[index - 1].first) / 2 to (pair.second + data[index - 1].second) / 2
                }
            }
        }
        invalidate()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        if (width == 0 || height == 0) return
        this.canvasWidth = width - polygonStrokePaint.strokeWidth.toInt() * 2
        this.canvasHeight = height - polygonStrokePaint.strokeWidth.toInt() * 2
        if (hasPendingDraw) {
            prepareDraw()
        }
        super.onSizeChanged(width, height, oldWidth, oldHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvasWidth == 0 || canvasHeight == 0) {
            return
        }

        polygonPath.reset()

        val firstData = data.first()
        val lastData = data.last()

        if (vertexType == Vertex.SHARP) {
            data.forEachIndexed { index, pair ->
                when (index) {
                    0 -> polygonPath.moveTo(pair)
                    else -> polygonPath.lineTo(pair)
                }
            }
        } else {
            polygonData?.forEachIndexed { index, pair ->
                when (index) {
                    0 -> polygonPath.moveTo(pair)
                    else -> polygonPath.quadTo(data[index - 1], pair)
                }
            }
            polygonPath.lineTo(lastData)
        }
        polygonPath.lineTo(lastData.first, canvasHeight.toFloat())
        polygonPath.lineTo(firstData.first, canvasHeight.toFloat())

        if (firstData.second != 0F) {
            polygonPath.lineTo(firstData)
        }

        canvas?.drawPath(polygonPath, polygonPaint)
        drawHighlightPath(canvas)
        if (hasStroke) {
            canvas?.drawPath(polygonPath, polygonStrokePaint)
        }
    }

    private fun drawHighlightPath(canvas: Canvas?) {
        if (highlight != null) {
            val startX = dataToScreenSize(highlight!!.first to 0F).first
            val endX = dataToScreenSize(highlight!!.second to 0F).first
            highlightPath.reset()
            highlightPath.moveTo(startX, 0F)
            highlightPath.lineTo(endX, 0F)
            highlightPath.lineTo(endX, canvasHeight.toFloat())
            highlightPath.lineTo(startX, canvasHeight.toFloat())
            highlightPath.lineTo(startX, 0F)
            if (highlightPath.op(polygonPath, Path.Op.INTERSECT)) {
                canvas?.drawPath(highlightPath, highlightPaint)
                if (hasHighlightStroke) {
                    canvas?.drawPath(highlightPath, highlightStrokePaint)
                }
            }
        }
    }
}