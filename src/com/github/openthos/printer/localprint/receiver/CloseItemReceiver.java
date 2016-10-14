package com.github.openthos.printer.localprint.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.github.openthos.printer.localprint.APP;
import com.android.systemui.statusbar.phone.PrinterJobStatus;
import com.github.openthos.printer.localprint.task.JobCancelTask;

public class CloseItemReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String strCloseInfo = intent.getAction();
        PrinterJobStatus valueCloseInfo = (PrinterJobStatus)intent.
                                          getParcelableExtra("CloseItem");
        if (strCloseInfo.equals(APP.NOTIFICATION_CLOSE_ITEM)) {
            removeJob(valueCloseInfo);
        }
    }

    // Execute remove
    private void removeJob(PrinterJobStatus jobItem) {
        JobCancelTask<Void> task = new JobCancelTask<Void>() {
            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean) {
                    //Toast.makeText(mContext, R.string.canceled,
                                              //Toast.LENGTH_SHORT).show();
                } else {
                    // Toast.makeText(mContext, R.string.cancel_error,
                                               //Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.start(jobItem);
    }
}
