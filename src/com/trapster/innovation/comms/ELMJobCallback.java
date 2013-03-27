package com.trapster.innovation.comms;

import com.trapster.innovation.comms.obd.OBDCommand;

public interface ELMJobCallback
{
    public void onError(String error);
    public void onComplete(OBDCommand command);
    public void onProgressUpdate(String progress);
}
