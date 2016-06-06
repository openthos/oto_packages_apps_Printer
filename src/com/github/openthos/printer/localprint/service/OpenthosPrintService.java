package com.github.openthos.printer.localprint.service;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.view.WindowManager;

import com.github.openthos.printer.localprint.APP;
import com.github.openthos.printer.localprint.R;
import com.github.openthos.printer.localprint.task.CancelPrintTask;
import com.github.openthos.printer.localprint.task.PrintTask;
import com.github.openthos.printer.localprint.task.StateTask;
import com.github.openthos.printer.localprint.ui.ManagementActivity;
import com.github.openthos.printer.localprint.util.FileUtils;
import com.github.openthos.printer.localprint.util.LogUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenthosPrintService extends PrintService {

    private static final String TAG = "OpenthosPrintService";

    @Override
    protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        return new PrintDiscoverySession(this);
    }

    @Override
    protected void onRequestCancelPrintJob(final PrintJob printJob) {
        // 取消打印任务

        CancelPrintTask<Void, Void> task = new CancelPrintTask<Void, Void>() {
            @Override
            protected void onPostExecute(Void aVoid) {
                printJob.cancel();
            }
        };

        String jobId = printJob.getTag();

        if(jobId == null) {
            printJob.cancel();
        } else {
            task.start(jobId);
        }

    }

    @Override
    protected void onPrintJobQueued(final PrintJob printJob) {
        LogUtils.d(TAG, "onPrintJobQueued()");

        final String docu_file_path = FileUtils.getDocuFilePath(printJob.getId().toString());

        Map<String, String> map = new HashMap<>();
        map.put(PrintTask.LP_PRINTER, printJob.getInfo().getPrinterId().getLocalId());
        map.put(PrintTask.LP_FILE, FileUtils.getDocuFileName(printJob.getId().toString()));
        map.put(PrintTask.LP_MEDIA, StateTask.Media2cups(printJob.getInfo().getAttributes().getMediaSize()));
        map.put(PrintTask.LP_COPIES, String.valueOf(printJob.getInfo().getCopies()));
        map.put(PrintTask.LP_LABEL, printJob.getDocument().getInfo().getName());

        boolean flag = FileUtils.copyFile(docu_file_path, printJob.getDocument().getData());
        if(!flag) {
            printJob.fail(getResources().getString(R.string.print_copy_file_failed));
            return;
        }

        PrintTask<Void> task = new PrintTask<Void>() {

            @Override
            protected void onPreExecute() {
                printJob.start();
            }

            @Override
            protected void onPostExecute(String jobId) {

                new File(docu_file_path).delete();

                if(jobId == null) {
                    printJob.fail(ERROR);
                } else {
                    printJob.setTag(String.valueOf(jobId));
                    printJob.complete();
                }
            }
        };

        task.start(map);

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent == null) {
            return START_STICKY;
        }

        int task = intent.getIntExtra(APP.TASK, APP.TASK_DEFAULT);

        LogUtils.d(TAG, "task -> " + task);

        switch(task) {
            case APP.TASK_DETECT_USB_PRINTER:
                detectPrinter();
                break;
            case APP.TASK_ADD_NEW_PRINTER:
                showAddPrinterDialog();
                break;
            case APP.TASK_JOB_RESULT:
                handleJobResult(intent.getBooleanExtra(APP.RESULT, false), intent.getStringExtra(APP.JOBID), intent.getStringExtra(APP.MESSAGE));
                break;
            case APP.TASK_DEFAULT:
                break;
        }

        return START_STICKY;
    }

    private void handleJobResult(boolean result, String jobId, String message) {

        PrintJob jobitem = null;

        List<PrintJob> jobs = getActivePrintJobs();
        for(PrintJob job: jobs) {
            if(job.getId().toString().equals(jobId)) {
                jobitem = job;
            }
        }

        if(jobitem == null) {
            LogUtils.d(TAG, "empty jobitem");
            return;
        }

        if(result) {
            jobitem.complete();
        } else {
            jobitem.fail(message);
        }

    }

    private void detectPrinter() {
        //TaskUtils.execute(new DetectPrinterTask(TAG));
    }

    private void showAddPrinterDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
                .setTitle(R.string.new_printer__notification)
                .setMessage(R.string.whether_add_new_printer)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(OpenthosPrintService.this, ManagementActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(APP.TASK, APP.TASK_ADD_NEW_PRINTER);
                        APP.getApplicatioContext().startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }
}
