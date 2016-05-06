package ayalma.ir.sample;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;


import ayalma.ir.ripplecompat.EmptyDrawable;
import ayalma.ir.ripplecompat.RippleDrawable;
import ayalma.ir.ripplecompat.RippleDrawableFroyo;
import ayalma.ir.ripplecompat.RippleDrawableLollipop;
import ayalma.ir.ripplecompat.RippleDrawableMarshmallow;
import ayalma.ir.ripplecompat.RippleView;

import static com.nineoldandroids.view.animation.AnimatorProxy.NEEDS_PROXY;
import static com.nineoldandroids.view.animation.AnimatorProxy.wrap;

/**
 * Created by Marcin on 2014-11-07.
 * <p/>
 * Carbon version of android.widget.Button. Supports shadows, ripples, animations and all other material features.
 */
public class Button extends android.widget.Button implements RippleView {
    protected Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    public Button(Context context) {
        super(context, null);
        initButton(null, android.R.attr.buttonStyle);
    }

    /**
     * XML constructor. Gets default parameters from android.R.attr.buttonStyle.
     *
     * @param context
     * @param attrs
     */
    public Button(Context context, AttributeSet attrs) {
        super(context, attrs);
        initButton(attrs, android.R.attr.buttonStyle);
    }

    public Button(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initButton(attrs, defStyle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Button(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initButton(attrs, defStyleAttr);
    }

    public static void initRippleDrawable(RippleView rippleView, AttributeSet attrs, int defStyleAttr) {
        View view = (View) rippleView;
        if (view.isInEditMode())
            return;

        TypedArray a = view.getContext().obtainStyledAttributes(attrs, R.styleable.Carbon, defStyleAttr, 0);
        ColorStateList color =  a.getColorStateList(R.styleable.Carbon_carbon_rippleColor);


        if (color != null) {
            RippleDrawable.Style style = RippleDrawable.Style.values()[a.getInt(R.styleable.Carbon_carbon_rippleStyle, RippleDrawable.Style.Background.ordinal())];
            boolean useHotspot = a.getBoolean(R.styleable.Carbon_carbon_rippleHotspot, true);
            int radius = (int) a.getDimension(R.styleable.Carbon_carbon_rippleRadius, -1);

            rippleView.setRippleDrawable(createRippleDrawable(color, style, view, useHotspot, 500));
        }

        a.recycle();
    }

    private void initButton(AttributeSet attrs, int defStyleAttr) {
        if (isInEditMode())
            return;

       initRippleDrawable(this,attrs,defStyleAttr);

    }

    public static RippleDrawable createRippleDrawable(ColorStateList color, RippleDrawable.Style style, View view, boolean useHotspot, int radius)
    {
        RippleDrawable rippleDrawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rippleDrawable = new RippleDrawableMarshmallow(color, style == RippleDrawable.Style.Background ? view.getBackground() : null, style);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rippleDrawable = new RippleDrawableLollipop(color, style == RippleDrawable.Style.Background ? view.getBackground() : null, style);
        } else {
            rippleDrawable = new RippleDrawableFroyo(color, style == RippleDrawable.Style.Background ? view.getBackground() : null, style);
        }
        rippleDrawable.setCallback(view);
        rippleDrawable.setHotspotEnabled(useHotspot);
        rippleDrawable.setRadius(radius);
        return rippleDrawable;
    }

    public static RippleDrawable createRippleDrawable(ColorStateList color, RippleDrawable.Style style, View view, Drawable background, boolean useHotspot, int radius) {
        RippleDrawable rippleDrawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rippleDrawable = new RippleDrawableMarshmallow(color, background, style);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rippleDrawable = new RippleDrawableLollipop(color, background, style);
        } else {
            rippleDrawable = new RippleDrawableFroyo(color, background, style);
        }
        rippleDrawable.setCallback(view);
        rippleDrawable.setHotspotEnabled(useHotspot);
        rippleDrawable.setRadius(radius);
        return rippleDrawable;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

            super.draw(canvas);
            if (rippleDrawable != null && rippleDrawable.getStyle() == RippleDrawable.Style.Over)
                rippleDrawable.draw(canvas);
    }


    // -------------------------------
    // ripple
    // -------------------------------

    private RippleDrawable rippleDrawable;
    private EmptyDrawable emptyBackground = new EmptyDrawable();
    private Transformation t = new Transformation();

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        Animation a = getAnimation();
        if (a != null) {
            a.getTransformation(event.getEventTime(), t);
            float[] loc = new float[]{event.getX(), event.getY()};
            t.getMatrix().mapPoints(loc);
            event.setLocation(loc[0], loc[1]);
        }
        if (rippleDrawable != null && event.getAction() == MotionEvent.ACTION_DOWN)
            rippleDrawable.setHotspot(event.getX(), event.getY());
        return super.dispatchTouchEvent(event);
    }

    @Override
    public RippleDrawable getRippleDrawable() {
        return rippleDrawable;
    }

    @Override
    public void setRippleDrawable(RippleDrawable newRipple) {
        if (rippleDrawable != null) {
            rippleDrawable.setCallback(null);
            if (rippleDrawable.getStyle() == RippleDrawable.Style.Background)
                super.setBackgroundDrawable(rippleDrawable.getBackground() == null ? emptyBackground : rippleDrawable.getBackground());
        }

        if (newRipple != null) {
            newRipple.setCallback(this);
            newRipple.setBounds(0, 0, getWidth(), getHeight());
            if (newRipple.getStyle() == RippleDrawable.Style.Background)
                super.setBackgroundDrawable((Drawable) newRipple);
        }

        rippleDrawable = newRipple;
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || rippleDrawable == who;
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
        super.invalidateDrawable(drawable);
        if (getParent() == null || !(getParent() instanceof View))
            return;

        if (rippleDrawable != null && rippleDrawable.getStyle() == RippleDrawable.Style.Borderless)
            ((View) getParent()).invalidate();

    }

    @Override
    public void invalidate(@NonNull Rect dirty) {
        super.invalidate(dirty);
        if (getParent() == null || !(getParent() instanceof View))
            return;

        if (rippleDrawable != null && rippleDrawable.getStyle() == RippleDrawable.Style.Borderless)
            ((View) getParent()).invalidate(dirty);


    }

    @Override
    public void invalidate(int l, int t, int r, int b) {
        super.invalidate(l, t, r, b);
        if (getParent() == null || !(getParent() instanceof View))
            return;

        if (rippleDrawable != null && rippleDrawable.getStyle() == RippleDrawable.Style.Borderless)
            ((View) getParent()).invalidate(l, t, r, b);

    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (getParent() == null || !(getParent() instanceof View))
            return;

        if (rippleDrawable != null && rippleDrawable.getStyle() == RippleDrawable.Style.Borderless)
            ((View) getParent()).invalidate();

    }

    @Override
    public void postInvalidateDelayed(long delayMilliseconds) {
        super.postInvalidateDelayed(delayMilliseconds);
        if (getParent() == null || !(getParent() instanceof View))
            return;

        if (rippleDrawable != null && rippleDrawable.getStyle() == RippleDrawable.Style.Borderless)
            ((View) getParent()).postInvalidateDelayed(delayMilliseconds);

    }

    @Override
    public void postInvalidateDelayed(long delayMilliseconds, int left, int top, int right, int bottom) {
        super.postInvalidateDelayed(delayMilliseconds, left, top, right, bottom);
        if (getParent() == null || !(getParent() instanceof View))
            return;

        if (rippleDrawable != null && rippleDrawable.getStyle() == RippleDrawable.Style.Borderless)
            ((View) getParent()).postInvalidateDelayed(delayMilliseconds, left, top, right, bottom);

    }

    @Override
    public void postInvalidate() {
        super.postInvalidate();
        if (getParent() == null || !(getParent() instanceof View))
            return;

        if (rippleDrawable != null && rippleDrawable.getStyle() == RippleDrawable.Style.Borderless)
            ((View) getParent()).postInvalidate();

    }

    @Override
    public void postInvalidate(int left, int top, int right, int bottom) {
        super.postInvalidate(left, top, right, bottom);
        if (getParent() == null || !(getParent() instanceof View))
            return;

        if (rippleDrawable != null && rippleDrawable.getStyle() == RippleDrawable.Style.Borderless)
            ((View) getParent()).postInvalidate(left, top, right, bottom);

    }

    @Override
    public void setBackground(Drawable background) {
        setBackgroundDrawable(background);
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        if (background instanceof RippleDrawable) {
            setRippleDrawable((RippleDrawable) background);
            return;
        }

        if (rippleDrawable != null && rippleDrawable.getStyle() == RippleDrawable.Style.Background) {
            rippleDrawable.setCallback(null);
            rippleDrawable = null;
        }
        super.setBackgroundDrawable(background == null ? emptyBackground : background);
    }


}