package com.github.openthos.printer.localprint.task;

import android.os.AsyncTask;

/**
 * Created by bboxh on 2016/5/14.
 */
public abstract class BaseTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected String TAG = "BaseTask";

    public BaseTask() {
        super();
        String TAG = bindTAG();
        if(TAG != null && !TAG.equals("")) {
            this.TAG = TAG;
        }
    }

    protected abstract String bindTAG();

    public AsyncTask<Params, Progress, Result> start(Params... params){
        return this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

}
