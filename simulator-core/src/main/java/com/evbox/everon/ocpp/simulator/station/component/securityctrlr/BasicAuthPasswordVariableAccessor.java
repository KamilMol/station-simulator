package com.evbox.everon.ocpp.simulator.station.component.securityctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v20.message.AttributeEnum;
import com.evbox.everon.ocpp.v20.message.Component;
import com.evbox.everon.ocpp.v20.message.MutabilityEnum;
import com.evbox.everon.ocpp.v20.message.ReportData;
import com.evbox.everon.ocpp.v20.message.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.SetVariableStatusEnum;
import com.evbox.everon.ocpp.v20.message.Variable;
import com.evbox.everon.ocpp.v20.message.VariableAttribute;
import com.evbox.everon.ocpp.v20.message.VariableCharacteristics;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.simulator.station.support.HexUtils.isNotHex;
import static com.evbox.everon.ocpp.v20.message.DataEnum.STRING;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;

public class BasicAuthPasswordVariableAccessor extends VariableAccessor {

    private static final String NAME = "BasicAuthPassword";
    private static final long RECONNECT_TIMEOUT = 5;

    private final Map<AttributeType, SetVariableValidator> variableValidators = ImmutableMap.<AttributeType, SetVariableValidator>builder()
            .put(AttributeType.ACTUAL, this::validateActualValue)
            .build();

    private final Map<AttributeType, VariableSetter> variableSetters = ImmutableMap.<AttributeType, VariableSetter>builder()
            .put(AttributeType.ACTUAL, this::setActualValue)
            .build();

    public BasicAuthPasswordVariableAccessor(Station station, StationStore stationStore) {
        super(station, stationStore);
    }

    @Override
    public String getVariableName() {
        return NAME;
    }

    @Override
    public Map<AttributeType, VariableGetter> getVariableGetters() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public Map<AttributeType, VariableSetter> getVariableSetters() {
        return variableSetters;
    }

    @Override
    public Map<AttributeType, SetVariableValidator> getVariableValidators() {
        return variableValidators;
    }

    @Override
    public List<ReportData> generateReportData(String componentName) {
        Component component = new Component()
                .withName(new CiString.CiString50(componentName));

        // basicAuthPassword must not be exposed
        VariableAttribute variableAttribute = new VariableAttribute()
                .withValue(new CiString.CiString2500(""))
                .withPersistent(true)
                .withConstant(true)
                .withMutability(MutabilityEnum.WRITE_ONLY);

        VariableCharacteristics variableCharacteristics = new VariableCharacteristics()
                .withDataType(STRING)
                .withSupportsMonitoring(false);

        ReportData ReportData = new ReportData()
                .withComponent(component)
                .withVariable(new Variable().withName(new CiString.CiString50(NAME)))
                .withVariableCharacteristics(variableCharacteristics)
                .withVariableAttribute(singletonList(variableAttribute));

        return singletonList(ReportData);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    private SetVariableResult validateActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        SetVariableResult setVariableResult = new SetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(AttributeEnum.fromValue(attributePath.getAttributeType().getName()));

        if (invalidLength(attributeValue) || isNotHex(attributeValue.toString()) || isOdd(attributeValue)) {
            return setVariableResult.withAttributeStatus(SetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE);
        }

        return setVariableResult.withAttributeStatus(SetVariableStatusEnum.ACCEPTED);

    }

    private boolean isOdd(CiString.CiString1000 attributeValue) {
        return (attributeValue.toString().length() & 0x1) == 1;
    }


    private boolean invalidLength(CiString.CiString1000 attributeValue) {
        return attributeValue.toString().length() > 40;
    }

    private void setActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        getStation().getConfiguration().getComponentsConfiguration().getSecurityCtrlr().setBasicAuthPassword(attributeValue.toString());
        getStation().reconnect(RECONNECT_TIMEOUT, SECONDS);
    }
}
