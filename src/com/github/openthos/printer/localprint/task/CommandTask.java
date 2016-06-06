package com.github.openthos.printer.localprint.task;

import android.util.Log;

import com.github.openthos.printer.localprint.APP;
import com.github.openthos.printer.localprint.util.FileUtils;
import com.github.openthos.printer.localprint.util.LogUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bboxh on 2016/5/14.
 */
public abstract class CommandTask<Params, Progress, Result> extends BaseTask<Params, Progress, Result> {

    private boolean RUN_AGAIN = true;
    private List<String> stdOut = new ArrayList<String>();
    private List<String> stdErr = new ArrayList<String>();
    protected  String ERROR = "";
    private String[] cmd = null;
    private Thread cupsdThread = null;

    @Override
    protected final Result doInBackground(Params... params) {
        boolean flag = beforeCommand();
        if(!flag)
            return null;

        cmd = setCmd(params);

        Result result = null;
        while(RUN_AGAIN) {
            RUN_AGAIN = false;
            runCommand(cmd);
            result = handleCommand(stdOut, stdErr);
        }
        return result;
    }

    protected final void runCommandAgain(){
        if(cmd != null)
            RUN_AGAIN = true;
    }

    protected boolean beforeCommand() {
        return true;
    }

    protected abstract String[] setCmd(Params... params);

    private void runCommand(String[] cmd) {

        if(cmd != null && cmd.length == 0) {
            return;
        }
        LogUtils.d(TAG, "cmd => " + Arrays.toString(cmd));

        stdOut.clear();
        stdErr.clear();

        try {
            File file = new File(getWorkPath());
            final Process p = Runtime.getRuntime().exec(cmd, null, file);

            final Lock lock_in = new Lock();
            final Lock lock_error = new Lock();

            Runnable taskIn = new Runnable() {
                @Override
                public void run() {

                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = null;
                    try {
                        while ((line = in.readLine()) != null) {
                            stdOut.add(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    synchronized (lock_in) {
                        lock_in.notify();
                        lock_in.setFinish(true);
                    }
                }
            };

            Runnable taskError = new Runnable() {
                @Override
                public void run() {

                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    String line = null;
                    try {
                        while((line = in.readLine()) != null) {
                            stdErr.add(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    synchronized (lock_error) {
                        lock_error.notify();
                        lock_error.setFinish(true);
                    }
                }
            };

            new Thread(taskIn).start();
            new Thread(taskError).start();

            synchronized (lock_in) {
                if(!lock_in.isFinish()) {
                    lock_in.wait();
                }
            }

            synchronized (lock_error) {
                if(!lock_error.isFinish()) {
                    lock_error.wait();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG,"stdOut " + stdOut.toString());
        Log.d(TAG,"stdErr " + stdErr.toString());
    }

    protected abstract Result handleCommand(List<String> stdOut, List<String> stdErr);

    protected String getWorkPath() {
        return FileUtils.getComponentPath();
    }

    class Lock {
        boolean finish = false;

        public boolean isFinish() {
            return finish;
        }

        public void setFinish(boolean finish) {
            this.finish = finish;
        }
    }

    protected boolean cupsIsRunning() {
        boolean flag = false;

        runCommand(new String[] {"sh", "proot.sh", "lpstat", "-r"});
        for(String line: stdOut) {
            if(line.contains("scheduler is running")) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    protected boolean cupsIsRunning1() {
        boolean flag = false;
        runCommand(new String[]{"sh", "proot.sh", "ps", "|", "grep", "cupsd"});
        for(String line: stdOut) {
            if(line.contains("cupsd.conf")) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    protected boolean startCups() {

        if(cupsIsRunning()) {
            return true;
        }

        File file = new File(getWorkPath());
        try {
            APP.cupsdProcess = Runtime.getRuntime().exec(new String[]{"sh", "proot.sh" ,"cupsd"}, null, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return cupsIsRunning();
    }

    protected void killCups(){
        /*if(cupsdProcess != null) {
            cupsdProcess.destroy();
        }*/
    }
}
