package com.trapster.innovation.comms.obd.protocol;

import com.trapster.innovation.comms.ELMJobCallback;
import com.trapster.innovation.comms.obd.OBDCommand;

public class SelectAutoProtocolCommand extends OBDCommand
{
    public SelectAutoProtocolCommand(ELMJobCallback callback)
    {
        super("AT SP 0", callback);
    }

    @Override
    protected void processBuffer()
    {

    }

    @Override
    public String getResult()
    {
        return rawData;
    }

    @Override
    public double getValue()
    {
        return -1;
    }
}
