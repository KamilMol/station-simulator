package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v20.message.Component;
import com.evbox.everon.ocpp.v20.message.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.SetVariableStatusEnum;
import com.evbox.everon.ocpp.v20.message.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.mock.constants.VariableConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TxStopPointVariableAccessorTest {

    private static final String TX_STOP_POINT_VALUE = "EVConnected,Authorized";

    private static final CiString.CiString1000 TX_STOP_POINT_ATTRIBUTE = new CiString.CiString1000(TX_STOP_POINT_VALUE);

    @Mock
    Station stationMock;

    @InjectMocks
    TxStopPointVariableAccessor txStoptPointVariableAccessor;

    @Test
    void shouldUpdateTxStopPoint() {
        VariableSetter variableSetter = txStoptPointVariableAccessor.getVariableSetters().get(AttributeType.ACTUAL);

        variableSetter.set(attributePath(), TX_STOP_POINT_ATTRIBUTE);

        verify(stationMock).updateTxStopPointValues(argThat(arg -> arg.contains(TxStartStopPointVariableValues.AUTHORIZED) && arg.contains(TxStartStopPointVariableValues.EV_CONNECTED)));
    }

    @Test
    void expectValidationToPass() {
        SetVariableValidator setVariableValidator = txStoptPointVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        SetVariableResult result = setVariableValidator.validate(attributePath(), TX_STOP_POINT_ATTRIBUTE);

        assertThat(result.getComponent().getName().toString()).isEqualTo(TRANSACTION_COMPONENT_NAME);
        assertThat(result.getVariable().getName().toString()).isEqualTo(TX_STOP_POINT_VARIABLE_NAME);
        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatusEnum.ACCEPTED);
    }

    @Test
    void expectValidationToPassEmpty() {
        SetVariableValidator setVariableValidator = txStoptPointVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        SetVariableResult result = setVariableValidator.validate(attributePath(), new CiString.CiString1000(""));

        assertThat(result.getComponent().getName().toString()).isEqualTo(TRANSACTION_COMPONENT_NAME);
        assertThat(result.getVariable().getName().toString()).isEqualTo(TX_STOP_POINT_VARIABLE_NAME);
        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatusEnum.ACCEPTED);
    }

    @Test
    void expectValidationToFailOnInvalidValues() {
        SetVariableValidator setVariableValidator = txStoptPointVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        CiString.CiString1000 invalidValues = new CiString.CiString1000("Authorized,Random");

        SetVariableResult result = setVariableValidator.validate(attributePath(), invalidValues);

        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE);
    }

    private AttributePath attributePath() {
        return AttributePath.builder()
                .component(new Component().withName(new CiString.CiString50(TRANSACTION_COMPONENT_NAME)))
                .variable(new Variable().withName(new CiString.CiString50(TX_STOP_POINT_VARIABLE_NAME)))
                .attributeType(AttributeType.ACTUAL)
                .build();
    }
}
