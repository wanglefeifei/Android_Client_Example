package network.b.bnet.utils;

import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;

/**
 * Created by jack.ma on 2017/8/16.
 */

public class ShapeUtils {
    public static void setFillColorCornert(View view, int fillColor, float corner, boolean shadow) {

        setFillColorCornert(view, fillColor, corner, 0, 0,shadow);
    }

    public static void setFillColorCornert(final View view, final int fillColor, final float corner, final int sideColor, final int sideWidth, final boolean shadow) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                GradientDrawable gd = new GradientDrawable();
                //        int mw = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                //        int mh = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                //                final int mw = view.getMeasuredWidth();
                //                final int mh = view.getMeasuredHeight();
                //                view.measure(mw, mh);
                final int shade = 40;
                gd.setColor(fillColor);
                float vcorner = corner;
                if (corner == 0f) {

                    int h = view.getMeasuredHeight();
                    vcorner = h;
                }
                if (sideColor != 0) {
                    gd.setStroke(sideWidth, sideColor);
                }
                gd.setCornerRadii(new float[]{vcorner, vcorner, vcorner, vcorner, vcorner, vcorner, vcorner, vcorner});
                //                gd.setBounds(0,0,mw,mh+200);
                if (shadow && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ViewOutlineProvider vop = new ViewOutlineProvider() {
                        @Override
                        public void getOutline(View view, Outline outline) {
                            int size = 20;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight() - shade,10f);
//                                outline.setOval(0, 0, view.getWidth(), view.getHeight());
                            }
                        }
                    };
                    view.setOutlineProvider(vop);
                    view.setElevation(shade);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.setBackground(gd);
                }
                return false;
            }
        });

    }

}

