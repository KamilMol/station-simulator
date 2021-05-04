package com.evbox.everon.ocpp.simulator.station.component.ocppcommctrlr;

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
import com.evbox.everon.ocpp.v20.message.GetVariableResult;
import com.evbox.everon.ocpp.v20.message.GetVariableStatusEnum;
import com.evbox.everon.ocpp.v20.message.ReportData;
import com.evbox.everon.ocpp.v20.message.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.SetVariableStatusEnum;
import com.evbox.everon.ocpp.v20.message.Variable;
import com.evbox.everon.ocpp.v20.message.VariableAttribute;
import com.evbox.everon.ocpp.v20.message.VariableCharacteristics;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.v20.message.DataEnum.INTEGER;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class HeartbeatIntervalVariableAccessor extends VariableAccessor {

    public static final String NAME = "HeartbeatInterval";
    private final Map<AttributeType, VariableGetter> variableGetters = ImmutableMap.<AttributeType, VariableGetter>builder()
            .put(AttributeType.ACTUAL, this::getActualValue)
            .build();

    private final Map<AttributeType, SetVariableValidator> variableValidators = ImmutableMap.<AttributeType, SetVariableValidator>builder()
            .put(AttributeType.ACTUAL, this::validateActualValue)
            .build();

    private final Map<AttributeType, VariableSetter> variableSetters = ImmutableMap.<AttributeType, VariableSetter>builder()
            .put(AttributeType.ACTUAL, this::setActualValue)
            .build();

    public HeartbeatIntervalVariableAccessor(Station station, StationStore stationStore) {
        super(station, stationStore);
    }

    @Override
    public String getVariableName() {
        return NAME;
    }

    @Override
    public Map<AttributeType, VariableGetter> getVariableGetters() {
        return variableGetters;
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

        int heartbeatInterval = getStationStore().getHeartbeatInterval();
        VariableAttribute variableAttribute = new VariableAttribute()
                .withValue(new CiString.CiString2500(String.valueOf(heartbeatInterval)))
                .withPersistent(false)
                .withConstant(false);

        VariableCharacteristics variableCharacteristics = new VariableCharacteristics()
                .withDataType(INTEGER)
                .withSupportsMonitoring(false);

        ReportData ReportData = new ReportData()
                .withComponent(component)
                .withVariable(new Variable().withName(new CiString.CiString50(NAME)))
                .withVariableCharacteristics(variableCharacteristics)
                .withVariableAttribute(singletonList(variableAttribute));

        return singletonList(ReportData);
    }

    @Override
    public boolean isMutable() { return true; }

    private SetVariableResult validateActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        SetVariableResult setVariableResult = new SetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(AttributeEnum.fromValue(attributePath.getAttributeType().getName()));

        if (!isNumeric(attributeValue.toString())) {
            return setVariableResult.withAttributeStatus(SetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE);
        } else {
            return setVariableResult.withAttributeStatus(SetVariableStatusEnum.ACCEPTED);
        }
    }

    public void setActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        Station station = getStation();
        station.updateHeartbeat(Integer.parseInt(attributeValue.toString()));
    }

    private GetVariableResult getActualValue(AttributePath attributePath) {
        int heartbeatInterval = getStationStore().getHeartbeatInterval();

        return new GetVariableResult()
                .withAttributeStatus(GetVariableStatusEnum.ACCEPTED)
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(AttributeEnum.fromValue(attributePath.getAttributeType().getName()))
                .withAttributeValue(new CiString.CiString2500(String.valueOf(heartbeatInterval)));
    }
}
