package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.v20.message.AttributeEnum;
import com.evbox.everon.ocpp.v20.message.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.SetVariableStatusEnum;

/**
 * Utils class holds shared methods between txStart and txStop variables
 */
class TxStartStopPointUtils {
    private TxStartStopPointUtils () {
        // NOP
    }

    static SetVariableResult validateActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        SetVariableResult setVariableResult = new SetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(AttributeEnum.fromValue(attributePath.getAttributeType().getName()));

        if (attributeValue.toString().isEmpty() || TxStartStopPointVariableValues.validateStringOfValues(attributeValue.toString())) {
            return setVariableResult.withAttributeStatus(SetVariableStatusEnum.ACCEPTED);
        } else {
            return setVariableResult.withAttributeStatus(SetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE);
        }
    }

}
