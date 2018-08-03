package com.dylan.dyn3rdparts.selectordialog;

import android.app.Dialog;
import android.widget.Button;

public interface DialogOnItemClickListener {
    void onItemClick(Dialog dialog, Button button, int position);
}
