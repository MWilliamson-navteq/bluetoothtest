package com.trapster.innovation.comms.obd.fuel;

import com.trapster.innovation.comms.ELMJobCallback;
import com.trapster.innovation.comms.obd.OBDCommand;

public class MAFAIrFlowRateCommand extends OBDCommand
{
    private double flowRate = -1; // grams/sec

    public MAFAIrFlowRateCommand(ELMJobCallback callback)
    {
        super("01 10", callback);
    }

    @Override
    protected void processBuffer()
    {
        // Response should be 41 10 XX, therefore our value is the 4th and 5th cells

        int firstValue = singleByteBuffer.get(0);
        int secondValue = singleByteBuffer.get(1);
        flowRate = ((firstValue * 256) + secondValue) / 100.0;

    }

    @Override
    public String getResult()
    {
        if (flowRate != -1)
            return flowRate + " grams per second.";
        else
            return null;
    }

    @Override
    public double getValue()
    {
        return flowRate;
    }
}
