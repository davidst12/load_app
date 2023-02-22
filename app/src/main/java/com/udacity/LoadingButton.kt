package com.udacity

import android.animation.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.animation.addListener
import androidx.core.animation.addPauseListener
import androidx.core.animation.doOnEnd
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.view.*
import timber.log.Timber
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var text = context.getString(R.string.download_button_text)
    private var radius = 40f;
    private var progressWidth = 0f
    private var progressAngle = 0f

    private var buttonAnimator = ValueAnimator()
    private var circleAnimator = ValueAnimator()
    private var set = AnimatorSet()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 80.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when(new){
            ButtonState.Clicked ->{}
            ButtonState.Loading ->{
                text = context.getString(R.string.downloading_button_text)
                setUpAnimations(0f, 0f)
                set.apply {
                    playTogether(buttonAnimator, circleAnimator)
                    duration = 5000
                    interpolator = DecelerateInterpolator()
                }
                set.start()
            }
            ButtonState.Completed -> {
                // Set up a new animation that starts base on the progress of the button
                setUpAnimations(progressWidth, progressAngle)
                set.end()
                text = context.getString(R.string.downloading_button_text)
                //Start a new faster animation
                set.apply {
                    playTogether(buttonAnimator, circleAnimator)
                    duration = 1000
                    interpolator = AccelerateInterpolator()
                }
                set.start()
            }
        }
    }

    init {
        isClickable = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw main rectangle
        paint.color = Color.CYAN
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Draw progress bar
        paint.color = Color.BLUE
        canvas.drawRect(0f, 0f, progressWidth, height.toFloat(), paint)

        //Draw text
        paint.color = Color.WHITE
        canvas.drawText(text, width/2f, (height.toFloat()-paint.descent()-paint.ascent())/2 , paint)

        //Draw progress circle
        paint.color = Color.YELLOW
        canvas.drawArc(width.toFloat()*0.9f - radius, height.toFloat()/2 - radius,width.toFloat()*0.9f + radius, height.toFloat()/2 + radius, 0f, progressAngle, true, paint)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        widthSize = width
        heightSize = height
    }

    private fun setUpAnimations(currentWidth: Float, currentAngle: Float){
        buttonAnimator = ValueAnimator.ofFloat(currentWidth, widthSize.toFloat())
            .apply {
                addUpdateListener {
                    progressWidth = animatedValue as Float
                    invalidate()
                }
            }
        circleAnimator = ValueAnimator.ofFloat(currentAngle, 360f)
            .apply {
                addUpdateListener {
                    progressAngle = animatedValue as Float
                    invalidate()
                }
            }
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if(buttonState==ButtonState.Completed){
                    progressWidth=0f
                    progressAngle=0f
                    text = context.getString(R.string.download_button_text)
                }
            }
        })
    }
}


