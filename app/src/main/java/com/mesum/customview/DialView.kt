package com.mesum.customview

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import androidx.core.content.withStyledAttributes
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import java.lang.Integer.min
import java.lang.StrictMath.sin
import java.util.jar.Attributes
import kotlin.math.cos

private enum class PowerSwitch (val label : Int){
    OFF(R.string.power_off),
    ON(R.string.power_on);

    fun next() = when (this) {
        OFF -> ON
        ON -> OFF

    }
}

private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35

class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var radius = 0.0f                    // Radius of the circle
    private var powerspeed = PowerSwitch.OFF         // The active selection.
// position variable which will be used to draw label and indicator circle position
    private val  pointPosition : PointF = PointF(0.0f, 0.0f)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }
    private var powerOf = 0
    private var powerOn = 0

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.DialView){
            powerOf = getColor(R.styleable.DialView_powerColor1, 0)
            powerOn = getColor(R.styleable.DialView_powerColor2, 0)
        }
        updateContentDescription()
        ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View,
                                                           info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val customClick = AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfo.ACTION_CLICK,
                    context.getString(if (powerspeed !=  PowerSwitch.ON)
                        R.string.change else R.string.reset)
                )
                info.addAction(customClick)
            }
        })
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true
        powerspeed = powerspeed.next()
        contentDescription = resources.getString(powerspeed.label)
        updateContentDescription()
        invalidate()
        return true

    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }
    private fun PointF.computeXYForSpeed(pos: PowerSwitch, radius: Float) {
        // Angles are in radians.
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }

    override fun onDraw(canvas: Canvas?) {
    // Set dial background color to green if selection not off.
      paint.color = when(powerspeed){
          PowerSwitch.OFF -> Color.GRAY
          PowerSwitch.ON -> Color.GREEN
       }
        // Draw the dial.
        canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)
       // canvas?.drawRect((width/2).toFloat(), (height/2).toFloat(),(width/2).toFloat(), (height/2).toFloat(), paint )
        // Draw the indicator circle.
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(powerspeed, markerRadius)
        paint.color = Color.BLACK
        canvas?.drawCircle(pointPosition.x, pointPosition.y, radius/12, paint)


        // Draw the text labels.
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in PowerSwitch.values()){
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas?.drawText(label, pointPosition.x, pointPosition.y, paint)
        }



    }


    fun updateContentDescription(){
        contentDescription = resources.getString(powerspeed.label)
    }

}