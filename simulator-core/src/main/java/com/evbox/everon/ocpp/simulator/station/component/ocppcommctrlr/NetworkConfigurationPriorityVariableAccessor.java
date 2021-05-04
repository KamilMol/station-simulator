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

import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.v20.message.DataEnum.SEQUENCE_LIST;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class NetworkConfigurationPriorityVariableAccessor extends VariableAccessor {

    public static final String NAME = "NetworkConfigurationPriority";

    NetworkConfigurationPriorityVariableAccessor(Station station, StationStore stationStore) {
        super(station, stationStore);
    }

    private final Map<AttributeType, VariableGetter> variableGetters = Map.of(AttributeType.ACTUAL, this::getActualValue);

    private final Map<AttributeType, SetVariableValidator> variableValidators = Map.of(AttributeType.ACTUAL, this::validateActualValue);

    private final Map<AttributeType, VariableSetter> variableSetters = Map.of(AttributeType.ACTUAL, this::setActualValue);

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

        VariableAttribute variableAttribute = new VariableAttribute()
                .withValue(new CiString.CiString2500(String.valueOf(getStationStore().getNetworkConfigurationPriority())))
                .withPersistent(false)
                .withConstant(false);

        VariableCharacteristics variableCharacteristics = new VariableCharacteristics()
                .withDataType(SEQUENCE_LIST)
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

        if (!isNumeric(attributeValue.toString())) {
            return setVariableResult.withAttributeStatus(SetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE);
        }

        if (!getStationStore().getNetworkConnectionProfiles().containsKey(Integer.parseInt(attributeValue.toString()))) {
            return setVariableResult.withAttributeStatus(SetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE);
        }

        return setVariableResult.withAttributeStatus(SetVariableStatusEnum.ACCEPTED);
    }

    private GetVariableResult getActualValue(AttributePath attributePath) {

        List<Integer> networkNetworkConfigurationPriority = getStationStore().getNetworkConfigurationPriority();

        return new GetVariableResult()
                .withAttributeStatus(GetVariableStatusEnum.ACCEPTED)
                .withComponent(attributePath.getComponent())
                .withVariable(attributePath.getVariable())
                .withAttributeType(AttributeEnum.fromValue(attributePath.getAttributeType().getName()))
                .withAttributeValue(new CiString.CiString2500(String.valueOf(networkNetworkConfigurationPriority)));
    }

    private void setActualValue(AttributePath attributePath, CiString.CiString1000 attributeValue) {
        getStation().updateNetworkConfigurationPriorityValues(Integer.parseInt(attributeValue.toString()));
    }
}
