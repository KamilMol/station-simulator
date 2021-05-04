package com.evbox.everon.ocpp.simulator.station.component.chargingstation;

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
import com.evbox.everon.ocpp.v20.message.MutabilityEnum;
import com.evbox.everon.ocpp.v20.message.ReportData;
import com.evbox.everon.ocpp.v20.message.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.SetVariableStatusEnum;
import com.evbox.everon.ocpp.v20.message.Variable;
import com.evbox.everon.ocpp.v20.message.VariableAttribute;
import com.evbox.everon.ocpp.v20.message.VariableCharacteristics;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.v20.message.DataEnum.SEQUENCE_LIST;
import static java.util.Collections.singletonList;

public class AvailabilityStateVariableAccessor extends VariableAccessor {

    public static final String NAME = "AvailabilityState";
    public static final String STATION_AVAILABILITY = "Available";

    private final Map<AttributeType, VariableGetter> variableGetters = ImmutableMap.<AttributeType, VariableGetter>builder()
            .put(AttributeType.ACTUAL, this::getActualValue)
            .build();

    private final Map<AttributeType, SetVariableValidator> variableValidators = ImmutableMap.<AttributeType, SetVariableValidator>builder()
            .put(AttributeType.ACTUAL, this::rejectVariable)
            .build();

    public AvailabilityStateVariableAccessor(Station station, StationStore stationStore) {
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
        return Collections.emptyMap();
    }

    @Override
    public Map<AttributeType, SetVariableValidator> getVariableValidators() {
        return variableValidators;
    }

    @Override
    public List<ReportData> generateReportData(String componentName) {
        List<ReportData> reportData = new ArrayList<>();
        Component component = new Component().withName(new CiString.CiString50(componentName));

        VariableAttribute variableAttribute = new VariableAttribute()
                .withValue(new CiString.CiString2500(STATION_AVAILABILITY))
                .withPersistent(true)
                .withConstant(true)
                .withMutability(MutabilityEnum.READ_ONLY);

        VariableCharacteristics variableCharacteristics = new VariableCharacteristics()
                .withDataType(SEQUENCE_LIST)
                .withSupportsMonitoring(false);

        ReportData ReportData = new ReportData()
                .withComponent(component)
                .withVariable(new Variable().withName(new CiString.CiString50(NAME)))
                .withVariableCharacteristics(variableCharacteristics)
                .withVariableAttribute(singletonList(variableAttribute));

        reportData.add(ReportData);

        return reportData;
    }

    @Override
    public boolean isMutable() { return false; }

    private GetVariableResult getActualValue(AttributePath attributePath) {
        Integer evseId = attributePath.getComponent().getEvse().getId();

        GetVariableResult getVariableResult = new GetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(AttributeEnum.fromValue(attributePath.getAttributeType().getName()));

        boolean evseExists = getStationStore().hasEvse(evseId);

        if (evseExists) {
            return getVariableResult
                    .withAttributeValue(new CiString.CiString2500(STATION_AVAILABILITY))
                    .withAttributeStatus(GetVariableStatusEnum.ACCEPTED);
        } else {
            return getVariableResult.withAttributeStatus(GetVariableStatusEnum.REJECTED);
        }
    }

    private SetVariableResult rejectVariable(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        return RESULT_CREATOR.createResult(attributePath, attributeValue, SetVariableStatusEnum.REJECTED);
    }
}