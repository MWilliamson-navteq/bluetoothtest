package com.trapster.innovation.comms.obd.engine;

import com.trapster.innovation.comms.ELMJobCallback;
import com.trapster.innovation.comms.obd.OBDCommand;

public class MAPCommand extends OBDCommand
{
    private double pressure;  // kpa

    public MAPCommand(ELMJobCallback callback)
    {
        super("01 0B", callback);
    }

    @Override
    protected void processBuffer()
    {
        pressure = singleByteBuffer.get(0);
    }

    @Override
    public String getResult()
    {
        return null;
    }

    @Override
    public double getValue()
    {
        return pressure;
    }
}
