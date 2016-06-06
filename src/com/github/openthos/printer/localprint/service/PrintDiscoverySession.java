package com.github.openthos.printer.localprint.service;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrinterDiscoverySession;
import android.widget.Toast;


import com.github.openthos.printer.localprint.R;
import com.github.openthos.printer.localprint.model.PrinterItem;
import com.github.openthos.printer.localprint.task.ListAddedTask;
import com.github.openthos.printer.localprint.task.StateTask;
import com.github.openthos.printer.localprint.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by bboxh on 2016/4/12.
 */
public class PrintDiscoverySession extends PrinterDiscoverySession {

    private static final String TAG = "PrintDiscoverySession";
    private final OpenthosPrintService openthosPrintService;

    public PrintDiscoverySession(OpenthosPrintService openthosPrintService) {
        this.openthosPrintService = openthosPrintService;
    }

    @Override
    public void onStartPrinterDiscovery(final List<PrinterId> priorityList) {
        LogUtils.d(TAG, "onStartPrinterDiscovery()");
        final List<PrinterInfo> printers = this.getPrinters();

        ListAddedTask<Void, Void> task = new ListAddedTask<Void, Void>() {
            @Override
            protected void onPostExecute(List<PrinterItem> list) {
                List<PrinterId> old_list = new ArrayList<>();
                old_list.addAll(priorityList);

                if (list != null) {
                    for (PrinterItem printerItem : list) {

                        PrinterId id = openthosPrintService.generatePrinterId(String.valueOf(printerItem.getNickName()));

                        if(priorityList.contains(id)) {
                            old_list.remove(id);
                            continue;
                        }

                        PrinterInfo.Builder builder =
                                new PrinterInfo.Builder(id, printerItem.getNickName(), PrinterInfo.STATUS_IDLE);
                        PrinterInfo myprinter = builder.build();
                        printers.add(myprinter);
                    }
                    addPrinters(printers);
                } else {
                    Toast.makeText(openthosPrintService, openthosPrintService.getResources().getString(R.string.query_error) + " " + ERROR, Toast.LENGTH_SHORT).show();
                }

                removePrinters(old_list);
            }
        };

        task.start();

    }

    @Override
    public void onStopPrinterDiscovery() {
    }

    @Override
    public void onValidatePrinters(List<PrinterId> printerIds) {
        // TODO: 2016/5/10  onValidatePrinters ?
    }

    @Override
    public void onStartPrinterStateTracking(final PrinterId printerId) {
        LogUtils.d(TAG, "onStartPrinterStateTracking()");

        StateTask<Void> task = new StateTask<Void>() {
            @Override
            protected void onPostExecute(PrinterInfo printerInfo) {

                if(printerInfo == null) {
                    Toast.makeText(openthosPrintService, openthosPrintService.getResources().getString(R.string.query_error) + " " + ERROR, Toast.LENGTH_LONG).show();
                    PrinterInfo.Builder builder =
                            new PrinterInfo.Builder(printerId, printerId.getLocalId(), PrinterInfo.STATUS_UNAVAILABLE);
                    printerInfo = builder.build();
                }

                List<PrinterInfo> printers = new ArrayList<PrinterInfo>();
                printers.add(printerInfo);
                addPrinters(printers);
            }
        };
        task.start(printerId);

    }

    @Override
    public void onStopPrinterStateTracking(PrinterId printerId) {
    }

    @Override
    public void onDestroy() {

    }



}
