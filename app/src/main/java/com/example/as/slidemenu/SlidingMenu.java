package com.example.as.slidemenu;

import android.app.Service;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

/**
 * Created by as on 2017/7/1.
 */

public class SlidingMenu extends HorizontalScrollView {
    private int menuRightMargin = 65;
    private int mMenuWidth = 0;
    private View mContentView;
    private View mMenuView;
    //手势处理类
    private GestureDetector mGestureDetector;
    private boolean isMenuOpen;
    private boolean mIsIntercept =false;

    public SlidingMenu(Context context) {
        this(context, null);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu);
        menuRightMargin = (int) typedArray.getDimension(R.styleable.SlidingMenu_menuRightMargin, dip5px(menuRightMargin));
        mMenuWidth = getScreenWidth(context) - menuRightMargin;
        typedArray.recycle();
        // 7.1 初始化手势处理类
        mGestureDetector = new GestureDetector(context, new GestureListener());
    }

    //布局解析完成后调用该方法
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ViewGroup container = (ViewGroup) getChildAt(0);
        int childCount = container.getChildCount();
        if (childCount != 2) {
            throw new RuntimeException("只能放置两个子View!");
        }

        //设置菜单宽度
        mMenuView = container.getChildAt(0);
        ViewGroup.LayoutParams menuLayoutParams = mMenuView.getLayoutParams();
        menuLayoutParams.width = mMenuWidth;
        mMenuView.setLayoutParams(menuLayoutParams);

        //设置内容宽度
        mContentView = container.getChildAt(1);
        ViewGroup.LayoutParams contentParams = mContentView.getLayoutParams();
        contentParams.width = getScreenWidth(getContext());
        mContentView.setLayoutParams(contentParams);
    }

    //dp转换成px
    public int dip5px(int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
    }

    //让布局默认移动到第一页  而不是菜单
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        scrollTo(mMenuWidth, 0);
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    private int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    //scroll  滑动监听回传事件
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //进行判断l是否为0
        if (l == 0) {
            return;
        }
        float scale = 1f * l / mMenuWidth;
        float rightScale = 0.7f + 0.3f * scale;
        ViewCompat.setPivotX(mContentView, 0);
        ViewCompat.setPivotY(mContentView, getMeasuredHeight() / 2);
        ViewCompat.setScaleX(mContentView, rightScale);
        ViewCompat.setScaleY(mContentView, rightScale);

        //设置Menu的透明度
        float menuAlpha = 0.5f + (1 - scale) * 0.5f;
        ViewCompat.setAlpha(mMenuView, menuAlpha);

        ViewCompat.setTranslationX(mMenuView, 0.15f * l);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mIsIntercept){
            return  true;
        }
        if (mGestureDetector.onTouchEvent(ev)) {
            return true;
        }
        //处理滑动一半手指抬起  菜单打开或者关闭
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            int currentScrollX = getScrollX();
            if (currentScrollX > mMenuWidth / 2) {
                //关闭菜单
                closeMenu();
                isMenuOpen = false;
            } else {

                //打开菜单
                openMenu();
                isMenuOpen = true;
                return true;
            }
            return true;
        }
        return super.onTouchEvent(ev);


    }

    //打开菜单
    private void openMenu() {
        //滑动带有动画
        smoothScrollTo(0, 0);
    }

    //关闭菜单
    private void closeMenu() {
        //滑动带有动画
        smoothScrollTo(mMenuWidth, 0);
    }

    //手势处理类
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        //快速打开事件
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isMenuOpen) {
                if (velocityX > 0) {
                    openMenu();
                    return true;
                } else {
                    if (velocityX < 0) {
                        closeMenu();
                        return false;
                    }
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    //事件拦截
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mIsIntercept =false;
        if(isMenuOpen){
            float fingerX =ev.getX();
            if(fingerX >mMenuWidth){
                //关闭菜单
                closeMenu();
                //虽然拦截了子View的点击事件但是它还是走自己的onTouch事件
                mIsIntercept =true;
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }
    
}
