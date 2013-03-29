package com.trapster.innovation.monitor;

import com.trapster.innovation.comms.ELMCommunicator;

public abstract class OBDMonitor implements Runnable
{
    protected ELMCommunicator communicator;
    protected OBDMonitorCallback callback;

    public OBDMonitor(ELMCommunicator communicator, OBDMonitorCallback callback)
    {
        this.communicator = communicator;
    }

    public abstract double getResult();

    public interface OBDMonitorCallback
    {
        public void onResult(double result);
    }
}
