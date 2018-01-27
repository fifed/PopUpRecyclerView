package com.fifed.popuprecyclerview.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.fifed.popuprecyclerview.interfaces.OnRecyclerViewChangeStateListener;

public class PopUpRecyclerView extends FrameLayout implements PopupWindow.OnDismissListener, View.OnTouchListener {
    private boolean isDroppedDown;
    private boolean isOverKeyboard;
    private boolean isAutoOpenOnTouch = true;
    private int offset;
    private RecyclerView rv;
    private CardView dropDownContainer;
    private PopupWindow popupWindow;
    private OnRecyclerViewChangeStateListener listener;

    public PopUpRecyclerView(Context context) {
        super(context);
    }

    public PopUpRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PopUpRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnRecyclerViewChangeStateListener(OnRecyclerViewChangeStateListener listener){
        this.listener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(getChildCount() != 1){
            throw new RuntimeException("PopUpRecyclerView must contain one child!");
        }
        initView();
    }

    private void initView(){
        rv = new RecyclerView(getContext());
        initDropDownContainer();
        initPopUpWindow();

    }

    private void initDropDownContainer(){
        dropDownContainer = new CardView(getContext()){
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return onPopUpWindowTouch(ev);
            }
        };
        dropDownContainer.addView(rv);
    }

    private void initPopUpWindow(){
        popupWindow = new PopupWindow(getContext());
        popupWindow.setOnDismissListener(this);
        popupWindow.setTouchInterceptor(this);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(dropDownContainer);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void showRV() {
        if(!isDroppedDown){
            popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
            popupWindow.setWidth(getMeasuredWidth());
            popupWindow.showAsDropDown(this, 0, offset);
            isDroppedDown = true;
            if(listener != null){
                listener.onRecyclerViewShow();;
            }
        }
    }


    private void hideRV(){
        popupWindow.dismiss();
    }


    public void setAdapter(RecyclerView.Adapter adaper){
        rv.setAdapter(adaper);
    }

    public void setLayoutManager(RecyclerView.LayoutManager manager){
        rv.setLayoutManager(manager);
    }

    public void dropDownRecyclerView(){
        if(!isDroppedDown){
            if(rv.getAdapter() != null && rv.getAdapter().getItemCount() > 0) {
                showRV();
            }
        }
    }

    public void hideRecyclerView(){
        if(isDroppedDown) {
            hideRV();
        }
    }

    public void update(){
        if (popupWindow != null){
            popupWindow.update();
        }
    }

    public void setVerticalOffset(int offsetInDp){
        offset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, offsetInDp, getResources().getDisplayMetrics());
    }

    public void enabledAutoOpenOnTouch(boolean enabled){
        isAutoOpenOnTouch = enabled;
    }

    private boolean onPopUpWindowTouch(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN && !isOverKeyboard) {
            if (isKeyboardActive()) {
                popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
                popupWindow.update();
                isOverKeyboard = true;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v == this){
            if(event.getAction() == MotionEvent.ACTION_DOWN && !isDroppedDown && isAutoOpenOnTouch){
                if(rv.getAdapter() != null && rv.getAdapter().getItemCount() > 0) {
                    showRV();
                }
            }
        } else if(event.getAction() == MotionEvent.ACTION_OUTSIDE && isDroppedDown) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (x > 0 && x <= getMeasuredWidth() && y < 0 && y >= -getMeasuredHeight()) {
                return true;
            } else if(x == 0 && y == 0){
                popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
                popupWindow.update();
                isOverKeyboard = false;
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getAction()!= MotionEvent.ACTION_MOVE && ev.getAction() != MotionEvent.ACTION_UP){
            onTouch(this, ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    public RecyclerView getRecyclerView(){
        return rv;
    }

    @Override
    public void onDismiss() {
        isDroppedDown = false;
        isOverKeyboard = false;
        if(listener != null){
            listener.onRecyclerViewHide();;
        }
    }

    private boolean isKeyboardActive(){
        Rect r = new Rect();
        getRootView().getWindowVisibleDisplayFrame(r);
        int screenHeight = getRootView().getRootView().getHeight();
        int keypadHeight = screenHeight - r.bottom;
        return  (keypadHeight > screenHeight * 0.15);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if(isOverKeyboard){
            popupWindow.dismiss();
            return true;
        } else if(isKeyboardActive()) {
            return false;
        } else if(isDroppedDown) {
            popupWindow.dismiss();
            return true;
        } else {
            return false;
        }
    }
}
