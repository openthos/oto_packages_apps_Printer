package com.github.openthos.printer.localprint.task;

import com.github.openthos.printer.localprint.model.JobItem;

import java.util.List;

/**
 * Query current jobs C2
 * Created by bboxh on 2016/6/5.
 */
public class JobQueryTask<Params, Progress> extends CommandTask<Params, Progress, Boolean> {
    private final List<JobItem> mList;

    public JobQueryTask(List<JobItem> list) {
        super();
        mList = list;
    }

    @Override
    protected String[] setCmd(Params... params) {
        return new String[]{"sh", "proot.sh", "sh", "/jobquery.sh"};
    }

    @Override
    protected Boolean handleCommand(List<String> stdOut, List<String> stdErr) {

        for (String line : stdErr) {

            if (line.startsWith("WARNING")) {
                continue;
            } else if (line.contains("Unable to connect to server")) {
                if (startCups()) {
                    runCommandAgain();
                    return null;
                } else {
                    ERROR = "Cups start failed.";
                    return null;
                }
            }
        }

        mList.clear();

        String statusLine = " ";
        int stat = 0;
        int id = -1;
        for (String line : stdOut) {
            if (line.equals("no entries") || line.startsWith("Rank")) {
                continue;
            }
            if (line.endsWith("bytes")) {
                String[] splitLine = line.split("\\s+");
                JobItem printTask = new JobItem();
                printTask.setJobId(Integer.parseInt(splitLine[2]));
                printTask.setSize(splitLine[splitLine.length - 2]
                                  + splitLine[splitLine.length - 1]);
                StringBuilder sb = new StringBuilder();
                for (int i = 3; i < (splitLine.length - 2); i++) {
                    sb.append(splitLine[i]);
                }
                printTask.setFileName(sb.toString());
                mList.add(printTask);
                continue;
            }

            if (line.startsWith("\tStatus")) {
                statusLine = line.replace("\tStatus:", "");
                continue;
            }

            if (line.startsWith("\tAlerts")) {

                String[] splitLine = line.split(":");
                if (splitLine[1].contains("none") && statusLine.equals("")) {
                    stat = JobItem.STATUS_READY;
                } else if (splitLine[1].contains("job-hold-until-specified")) {
                    stat = JobItem.STATUS_HOLDING;
                } else if (splitLine[1].contains("none")
                        || splitLine[1].contains("job-printing")
                        || splitLine[1].contains("printer-stopped")) {
                    if (statusLine.endsWith("failed")) {
                        stat = JobItem.STATUS_ERROR;
                    } else if (statusLine.endsWith("Waiting for printer to become available.")) {
                        stat = JobItem.STATUS_WAITING_FOR_PRINTER;
                    } else if (statusLine.contains("ing")) {
                        stat = JobItem.STATUS_PRINTING;
                    } else {
                        stat = JobItem.STATUS_READY;
                    }
                }
                continue;

            }

            if (line.startsWith("\tqueued")) {
                String[] splitLine = line.split("\\s+");
                for (int i = 0; i < mList.size(); i++) {

                    JobItem printTask = mList.get(i);
                    if (printTask.getJobId() == id) {
                        printTask.setPrinter(splitLine[3]);
                        printTask.setStatus(stat);
                        switch (stat) {
                            case JobItem.STATUS_ERROR:
                            case JobItem.STATUS_PRINTING:
                                printTask.setERROR(statusLine);
                                break;
                            default:
                                break;
                        }
                        break;
                    }

                }
                statusLine = "";
                continue;
            }

            String[] splitLine = line.split("\\s+");
            String[] info = splitLine[0].split("-");
            id = Integer.parseInt(info[info.length - 1]);


        }
        return true;
    }

    @Override
    protected String bindTAG() {
        return "JobQueryTask";
    }
}
