package com.evbox.everon.ocpp.simulator.station.component.chargingstation;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationHardwareData;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v20.message.AttributeEnum;
import com.evbox.everon.ocpp.v20.message.Component;
import com.evbox.everon.ocpp.v20.message.GetVariableResult;
import com.evbox.everon.ocpp.v20.message.GetVariableStatusEnum;
import com.evbox.everon.ocpp.v20.message.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.SetVariableStatusEnum;
import com.evbox.everon.ocpp.v20.message.Variable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static com.evbox.everon.ocpp.mock.assertion.CiStringAssert.assertCiString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class SerialNumberVariableAccessorTest {

    private static final String COMPONENT_NAME = ChargingStationComponent.NAME;
    private static final String VARIABLE_NAME = ManufacturerVariableAccessor.NAME;

    private static final AttributePath ACTUAL_ATTRIBUTE = attributePathBuilder().attributeType(AttributeType.ACTUAL).build();
    private static final AttributePath MAX_SET_ATTRIBUTE = attributePathBuilder().attributeType(AttributeType.MAX_SET).build();
    private static final AttributePath MIN_SET_ATTRIBUTE = attributePathBuilder().attributeType(AttributeType.MIN_SET).build();
    private static final AttributePath TARGET_ATTRIBUTE = attributePathBuilder().attributeType(AttributeType.TARGET).build();

    @InjectMocks
    SerialNumberVariableAccessor variableAccessor;

    static Stream<Arguments> SetVariableDataProvider() {
        return Stream.of(
                arguments(ACTUAL_ATTRIBUTE, StationHardwareData.SERIAL_NUMBER, SetVariableStatusEnum.REJECTED),
                arguments(MAX_SET_ATTRIBUTE, StationHardwareData.SERIAL_NUMBER, SetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(MIN_SET_ATTRIBUTE, StationHardwareData.SERIAL_NUMBER, SetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE),
                arguments(TARGET_ATTRIBUTE, StationHardwareData.SERIAL_NUMBER, SetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE)
        );
    }

    static Stream<Arguments> getVariableDatumProvider() {
        return Stream.of(
                arguments(ACTUAL_ATTRIBUTE, GetVariableStatusEnum.ACCEPTED, StationHardwareData.SERIAL_NUMBER),
                arguments(MAX_SET_ATTRIBUTE, GetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(MIN_SET_ATTRIBUTE, GetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE, null),
                arguments(TARGET_ATTRIBUTE, GetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE, null)
        );
    }

    @ParameterizedTest
    @MethodSource("SetVariableDataProvider")
    void shouldValidateSetVariableData(AttributePath attributePath, String value, SetVariableStatusEnum expectedAttributeStatus) {
        //when
        SetVariableResult result = variableAccessor.validate(attributePath, new CiString.CiString1000(value));

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(attributePath.getComponent().getName());
        assertCiString(result.getVariable().getName()).isEqualTo(attributePath.getVariable().getName());
        assertThat(result.getAttributeType()).isEqualTo(AttributeEnum.fromValue(attributePath.getAttributeType().getName()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
    }

    @ParameterizedTest
    @MethodSource("getVariableDatumProvider")
    void shouldGetVariableDatum(AttributePath attributePath, GetVariableStatusEnum expectedAttributeStatus, String expectedValue) {
        //when
        GetVariableResult result = variableAccessor.get(attributePath);

        //then
        assertCiString(result.getComponent().getName()).isEqualTo(attributePath.getComponent().getName());
        assertCiString(result.getVariable().getName()).isEqualTo(attributePath.getVariable().getName());
        assertThat(result.getAttributeType()).isEqualTo(AttributeEnum.fromValue(attributePath.getAttributeType().getName()));
        assertThat(result.getAttributeStatus()).isEqualTo(expectedAttributeStatus);
        assertCiString(result.getAttributeValue()).isEqualTo(expectedValue);
    }

    static AttributePath.AttributePathBuilder attributePathBuilder() {
        return AttributePath.builder()
                .component(new Component().withName(new CiString.CiString50(COMPONENT_NAME)))
                .variable(new Variable().withName(new CiString.CiString50(VARIABLE_NAME)));
    }
}
