package com.evbox.everon.ocpp.simulator.station.component.variable;

import com.evbox.everon.ocpp.v20.message.SetVariableData;
import com.evbox.everon.ocpp.v20.message.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.SetVariableStatusEnum;
import lombok.Value;

/**
 * Contains initial request for variable modification and result of its validation.
 */
@Value
public class SetVariableValidationResult {

    private SetVariableData setVariableData;
    private SetVariableResult result;

    public boolean isAccepted() {
        return result.getAttributeStatus() == SetVariableStatusEnum.ACCEPTED;
    }
}
