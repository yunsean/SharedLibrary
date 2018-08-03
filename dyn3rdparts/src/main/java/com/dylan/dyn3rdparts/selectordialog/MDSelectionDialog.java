package com.dylan.dyn3rdparts.selectordialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.dylan.dyn3rdparts.R;

import java.util.ArrayList;

public class MDSelectionDialog {


    private static Context mContext;
    private Dialog mDialog;
    private View dialogView;
    private LinearLayout linearLayout;
    private Builder mBuilder;
    private ArrayList<String> datas;
    private int selectPosition;

    public MDSelectionDialog(Builder builder) {

        this.mBuilder = builder;
        mDialog = new Dialog(mContext, R.style.selector_myDialogStyle);
        dialogView = View.inflate(mContext, R.layout.selector_widget_md_mid_dialog, null);
        linearLayout = (LinearLayout) dialogView.findViewById(R.id.md_mid_dialog_linear);
        mDialog.setContentView(dialogView);
        Window dialogWindow = mDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (MDAlertDialog.getScreenWidth(mContext) * builder.itemWidth);
        lp.gravity = Gravity.CENTER;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);
        mDialog.setCanceledOnTouchOutside(builder.isTouchOutside());
    }

    private void loadItem() {
        if (datas.size() == 1) {
            Button button = getButton(datas.get(0), 0);
            button.setBackgroundResource(R.drawable.selector_widget_md_single);
            linearLayout.addView(button);
        } else if (datas.size() > 1) {
            for (int i = 0; i < datas.size(); i++) {
                Button button = getButton(datas.get(i), i);
                if (i == 0) {
                    button.setBackgroundResource(R.drawable.selector_widget_md_top);
                } else if (i > 0 && i != datas.size() - 1) {
                    button.setBackgroundResource(R.drawable.selector_widget_md_middle);
                } else {
                    button.setBackgroundResource(R.drawable.selector_widget_md_bottom);                }
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
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams
                .MATCH_PARENT, mBuilder.getItemHeight());
        button.setLayoutParams(lp);
        button.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        button.setPadding(MDAlertDialog.dp2px(mContext,10), 0, MDAlertDialog.dp2px(mContext,10), 0);
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

    public void setDataList(ArrayList<String> datas) {
        linearLayout.removeAllViews();
        this.datas = (datas == null ? new ArrayList<String>() : datas);
        loadItem();
    }

    public void show() {
        mDialog.show();
    }
    public void dismiss() {
        mDialog.dismiss();
    }

    public static class Builder {
        private DialogOnItemClickListener onItemListener;
        private int itemHeight;
        private float itemWidth;
        private int itemTextColor;
        private float itemTextSize;
        private boolean isTouchOutside;

        public Builder(Context context) {
            mContext = context;
            onItemListener = null;
            itemHeight = MDAlertDialog.dp2px(context, 45);
            itemWidth = 0.75f;
            itemTextColor = ContextCompat.getColor(mContext, R.color.selector_black_light); // 默认字体颜色
            itemTextSize = 14;
            isTouchOutside = true;
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

        public boolean isTouchOutside() {
            return isTouchOutside;
        }
        public Builder setCanceledOnTouchOutside(boolean isTouchOutside) {
            this.isTouchOutside = isTouchOutside;
            return this;
        }

        public MDSelectionDialog build() {
            return new MDSelectionDialog(this);
        }
    }
}
