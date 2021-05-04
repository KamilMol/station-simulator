package com.evbox.everon.ocpp.simulator.station.component.connector;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableAccessor;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.AttributeEnum;
import com.evbox.everon.ocpp.v20.message.Component;
import com.evbox.everon.ocpp.v20.message.DataEnum;
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

import static java.util.Collections.singletonList;

public class EnabledVariableAccessor extends VariableAccessor {

    public static final String NAME = "Enabled";
    public static final String CONNECTOR_STATUS = Boolean.TRUE.toString();

    private final Map<AttributeType, VariableGetter> variableGetters = ImmutableMap.<AttributeType, VariableGetter>builder()
            .put(AttributeType.ACTUAL, this::getActualValue)
            .build();

    private final Map<AttributeType, SetVariableValidator> variableValidators = ImmutableMap.<AttributeType, SetVariableValidator>builder()
            .put(AttributeType.ACTUAL, this::validateActualValue)
            .build();

    public EnabledVariableAccessor(Station station, StationStore stationStore) {
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

        for (Evse evse : getStationStore().getEvses()) {
            for (Connector connector : evse.getConnectors()) {
                com.evbox.everon.ocpp.v20.message.EVSE componentEvse = new com.evbox.everon.ocpp.v20.message.EVSE()
                        .withConnectorId(connector.getId())
                        .withId(evse.getId());

                Component component = new Component()
                        .withName(new CiString.CiString50(componentName))
                        .withEvse(componentEvse);

                VariableAttribute variableAttribute = new VariableAttribute()
                        .withValue(new CiString.CiString2500(CONNECTOR_STATUS))
                        .withPersistent(true)
                        .withConstant(true)
                        .withMutability(MutabilityEnum.READ_ONLY);

                VariableCharacteristics variableCharacteristics = new VariableCharacteristics()
                        .withDataType(DataEnum.BOOLEAN)
                        .withSupportsMonitoring(false);

                ReportData ReportData = new ReportData()
                        .withComponent(component)
                        .withVariable(new Variable().withName(new CiString.CiString50(NAME)))
                        .withVariableCharacteristics(variableCharacteristics)
                        .withVariableAttribute(singletonList(variableAttribute));

                reportData.add(ReportData);
            }
        }

        return reportData;
    }

    @Override
    public boolean isMutable() { return false; }

    private SetVariableResult validateActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        return RESULT_CREATOR.createResult(attributePath, attributeValue, SetVariableStatusEnum.REJECTED);
    }

    private GetVariableResult getActualValue(AttributePath attributePath) {
        Integer evseId = attributePath.getComponent().getEvse().getId();
        Integer connectorId = attributePath.getComponent().getEvse().getConnectorId();

        boolean connectorExists = getStationStore().tryFindConnector(evseId, connectorId).isPresent();

        GetVariableResult getVariableResult = new GetVariableResult()
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(AttributeEnum.fromValue(attributePath.getAttributeType().getName()));

        if (!connectorExists) {
            return getVariableResult
                    .withAttributeStatus(GetVariableStatusEnum.REJECTED);
        } else {
            return getVariableResult
                    .withAttributeValue(new CiString.CiString2500(CONNECTOR_STATUS))
                    .withAttributeStatus(GetVariableStatusEnum.ACCEPTED);
        }
    }
}
