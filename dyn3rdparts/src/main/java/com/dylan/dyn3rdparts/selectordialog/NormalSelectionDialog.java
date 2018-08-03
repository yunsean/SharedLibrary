package com.dylan.dyn3rdparts.selectordialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dylan.dyn3rdparts.R;

import java.util.ArrayList;

public class NormalSelectionDialog {
    private static Context mContext;
    private Dialog mDialog;
    private View dialogView;
    private TextView title;
    private Button bottomBtn;
    private LinearLayout linearLayout;

    private Builder mBuilder;
    private ArrayList<String> datas;
    private int selectPosition;

    public NormalSelectionDialog(Builder builder) {
        this.mBuilder = builder;
        mDialog = new Dialog(mContext, R.style.selector_bottomDialogStyle);
        dialogView = View.inflate(mContext, R.layout.selector_widget_bottom_dialog, null);
        mDialog.setContentView(dialogView);

        //设置dialog的宽
        Window dialogWindow = mDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (MDAlertDialog.getScreenWidth(mContext) * builder.getItemWidth());
        lp.gravity = Gravity.BOTTOM;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);


        title = (TextView) dialogView.findViewById(R.id.action_dialog_title);
        linearLayout = (LinearLayout) dialogView.findViewById(R.id.action_dialog_linearlayout);
        bottomBtn = (Button) dialogView.findViewById(R.id.action_dialog_botbtn);
        bottomBtn.setText(builder.getCancleButtonText());
        bottomBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mDialog.dismiss();
            }
        });
        mDialog.setCanceledOnTouchOutside(builder.isTouchOutside());
    }

    private void loadItem() {
        if (mBuilder.getTitleVisible()) {
            title.setVisibility(View.VISIBLE);
            title.setText(mBuilder.getTitleText());
            title.setTextColor(mBuilder.getTitleTextColor());
            title.setTextSize(mBuilder.getTitleTextSize());
            LinearLayout.LayoutParams l = (LinearLayout.LayoutParams) title.getLayoutParams();
            l.height = MDAlertDialog.dp2px(mContext, mBuilder.getTitleHeight());
            title.setLayoutParams(l);
            if (datas.size() != 0) {
                title.setBackgroundResource(R.drawable.selector_widget_actiondialog_top);
            } else {
                title.setBackgroundResource(R.drawable.selector_widget_actiondialog_single);
            }
        } else {
            title.setVisibility(View.GONE);
        }

        bottomBtn.setTextColor(mBuilder.getItemTextColor());
        bottomBtn.setTextSize(mBuilder.getItemTextSize());
        LinearLayout.LayoutParams btnlp = new LinearLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mBuilder.getItemHeight());
        btnlp.topMargin = 10;
        bottomBtn.setLayoutParams(btnlp);

        if (datas.size() == 1) {
            Button button = getButton(datas.get(0), 0);
            if (mBuilder.getTitleVisible())
                button.setBackgroundResource(R.drawable.selector_widget_actiondialog_bottom);
            else button.setBackgroundResource(R.drawable.selector_widget_actiondialog_single);
            linearLayout.addView(button);
        } else if (datas.size() > 1) {
            for (int i = 0; i < datas.size(); i++) {
                Button button = getButton(datas.get(i), i);
                if (!mBuilder.getTitleVisible() && i == 0) {
                    button.setBackgroundResource(R.drawable.selector_widget_actiondialog_top);
                } else {
                    if (i != datas.size() - 1)
                        button.setBackgroundResource(R.drawable.selector_widget_actiondialog_middle);
                    else
                        button.setBackgroundResource(R.drawable.selector_widget_actiondialog_bottom);
                }
                linearLayout.addView(button);
            }
        }
    }

    private Button getButton(String text, int position) {
        final Button button = new Button(mContext);
        button.setText(text);
        button.setTag(position);
        button.setTextColor(mBuilder.getItemTextColor());
        button.setTextSize(mBuilder.getItemTextSize());
        button.setLayoutParams(new LinearLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mBuilder.getItemHeight()));
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mBuilder.getOnItemListener() != null) {
                    selectPosition = Integer.parseInt(button.getTag().toString());
                    mBuilder.getOnItemListener().onItemClick(mDialog, button, selectPosition);
                }
            }
        });
        return button;
    }

    public NormalSelectionDialog setDataList(ArrayList<String> datas) {
        int count = linearLayout.getChildCount();
        if(count>1){
            linearLayout.removeViewsInLayout(1,count-1);
        }
        this.datas = (datas == null ? new ArrayList<String>() : datas);
        loadItem();
        return this;
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }

    public Dialog show() {
        mDialog.show();
        return mDialog;
    }
    public void dismiss() {
        mDialog.dismiss();
    }

    public static class Builder {
        private boolean boolTitle;
        private int titleHeight;
        private String titleText;
        private int titleTextColor;
        private float titleTextSize;
        private DialogOnItemClickListener onItemListener;
        private int itemHeight;
        private float itemWidth;
        private int itemTextColor;
        private float itemTextSize;
        private String cancleButtonText;
        private boolean isTouchOutside;

        public Builder(Context context) {
            mContext = context;
            boolTitle = false;
            titleHeight = 65;
            titleText = "请选择";
            titleTextColor = ContextCompat.getColor(context, R.color.selector_black_light);
            titleTextSize = 13;
            onItemListener = null;
            itemHeight = MDAlertDialog.dp2px(context, 45);
            itemWidth = 0.92f;
            itemTextColor = ContextCompat.getColor(mContext, R.color.selector_black_light);
            itemTextSize = 14;
            cancleButtonText = "取消";
            isTouchOutside = true;
        }

        public boolean getTitleVisible() {
            return boolTitle;
        }
        public Builder setlTitleVisible(boolean isVisible) {
            this.boolTitle = isVisible;
            return this;
        }

        public int getTitleHeight() {
            return titleHeight;
        }
        public Builder setTitleHeight(int dp) {
            this.titleHeight = dp;
            return this;
        }

        public String getTitleText() {
            return titleText;
        }
        public Builder setTitleText(String titleText) {
            this.titleText = titleText;
            return this;
        }

        public int getTitleTextColor() {
            return titleTextColor;
        }
        public Builder setTitleTextColor(@ColorRes int titleTextColor) {
            this.titleTextColor = ContextCompat.getColor(mContext, titleTextColor);
            return this;
        }

        public float getTitleTextSize() {
            return titleTextSize;
        }
        public Builder setTitleTextSize(int sp) {

            this.titleTextSize = sp;
            return this;
        }

        public DialogOnItemClickListener getOnItemListener() {
            return onItemListener;
        }
        public Builder setOnItemListener(DialogOnItemClickListener onItemListener) {
            this.onItemListener = onItemListener;
            return this;
        }

        public int getItemHeight() {
            return itemHeight;
        }
        public Builder setItemHeight(int dp) {
            this.itemHeight = MDAlertDialog.dp2px(mContext, dp);
            return this;
        }


        public float getItemWidth() {
            return itemWidth;
        }
        public Builder setItemWidth(float itemWidth) {
            this.itemWidth = itemWidth;
            return this;
        }

        public int getItemTextColor() {
            return itemTextColor;
        }
        public Builder setItemTextColor(@ColorRes int itemTextColor) {
            this.itemTextColor = ContextCompat.getColor(mContext, itemTextColor);
            return this;
        }

        public float getItemTextSize() {
            return itemTextSize;
        }
        public Builder setItemTextSize(int sp) {
            this.itemTextSize = sp;
            return this;
        }

        public String getCancleButtonText() {
            return cancleButtonText;
        }
        public Builder setCancleButtonText(String cancleButtonText) {
            this.cancleButtonText = cancleButtonText;
            return this;
        }

        public boolean isTouchOutside() {
            return isTouchOutside;
        }
        public Builder setCanceledOnTouchOutside(boolean isTouchOutside) {
            this.isTouchOutside = isTouchOutside;
            return this;
        }

        public NormalSelectionDialog build() {
            return new NormalSelectionDialog(this);
        }
    }
}