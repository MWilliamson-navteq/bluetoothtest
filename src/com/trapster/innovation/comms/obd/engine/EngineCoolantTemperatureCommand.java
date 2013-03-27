package com.trapster.innovation.comms.obd.engine;

import com.trapster.innovation.comms.ELMJobCallback;
import com.trapster.innovation.comms.obd.OBDCommand;

public class EngineCoolantTemperatureCommand extends OBDCommand
{
    public EngineCoolantTemperatureCommand(ELMJobCallback callback)
    {
        super("01 05", callback);
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
}
