package com.evbox.everon.ocpp.simulator.station.component.securityctrlr;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.common.CiString.CiString1000;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration.StationConfiguration;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.variable.SetVariableValidator;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableGetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.VariableSetter;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributePath;
import com.evbox.everon.ocpp.simulator.station.component.variable.attribute.AttributeType;
import com.evbox.everon.ocpp.v20.message.Component;
import com.evbox.everon.ocpp.v20.message.ReportData;
import com.evbox.everon.ocpp.v20.message.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.SetVariableStatusEnum;
import com.evbox.everon.ocpp.v20.message.Variable;
import com.evbox.everon.ocpp.v20.message.VariableAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.evbox.everon.ocpp.mock.constants.VariableConstants.BASIC_AUTH_PASSWORD_VARIABLE_NAME;
import static com.evbox.everon.ocpp.mock.constants.VariableConstants.SECURITY_COMPONENT_NAME;
import static com.evbox.everon.ocpp.v20.message.MutabilityEnum.WRITE_ONLY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BasicAuthPasswordVariableAccessorTest {

    private static final String BASIC_AUTH_PASSWORD_VALUE = "0123456789abcdefABCDEF";

    private static final CiString1000 BASIC_AUTH_PASSWORD_ATTRIBUTE = new CiString1000(BASIC_AUTH_PASSWORD_VALUE);

    @Mock
    StationConfiguration stationConfigurationMock;

    @Mock
    Station stationMock;

    @Mock
    SimulatorConfiguration.ComponentsConfiguration componentsConfigurationMock;

    @Mock
    SimulatorConfiguration.SecurityComponentConfiguration securityComponentConfigurationMock;

    @SuppressWarnings("unused")
    @Mock
    StationStore stationStoreMock;

    @InjectMocks
    BasicAuthPasswordVariableAccessor basicAuthPasswordVariableAccessor;

    @Test
    void shouldUpdateBasicAuthPassword() {
        when(stationMock.getConfiguration()).thenReturn(stationConfigurationMock);
        when(stationConfigurationMock.getComponentsConfiguration()).thenReturn(componentsConfigurationMock);
        when(componentsConfigurationMock.getSecurityCtrlr()).thenReturn(securityComponentConfigurationMock);

        VariableSetter variableSetter = basicAuthPasswordVariableAccessor.getVariableSetters().get(AttributeType.ACTUAL);

        variableSetter.set(attributePath(), BASIC_AUTH_PASSWORD_ATTRIBUTE);

        verify(securityComponentConfigurationMock).setBasicAuthPassword(eq(BASIC_AUTH_PASSWORD_VALUE));
    }

    @Test
    void shouldCallStationReconnect() {
        when(stationMock.getConfiguration()).thenReturn(stationConfigurationMock);
        when(stationConfigurationMock.getComponentsConfiguration()).thenReturn(componentsConfigurationMock);
        when(componentsConfigurationMock.getSecurityCtrlr()).thenReturn(securityComponentConfigurationMock);

        VariableSetter variableSetter = basicAuthPasswordVariableAccessor.getVariableSetters().get(AttributeType.ACTUAL);

        variableSetter.set(attributePath(), BASIC_AUTH_PASSWORD_ATTRIBUTE);

        verify(stationMock).reconnect(anyLong(), any(TimeUnit.class));
    }

    @Test
    void expectWriteOnlyReportData() {
        List<ReportData> reportData = basicAuthPasswordVariableAccessor.generateReportData(SECURITY_COMPONENT_NAME);

        ReportData ReportData = reportData.get(0);

        assertThat(ReportData.getComponent().getName().toString()).isEqualTo(SECURITY_COMPONENT_NAME);
        assertThat(ReportData.getVariable().getName().toString()).isEqualTo(BASIC_AUTH_PASSWORD_VARIABLE_NAME);

        VariableAttribute variableAttribute = ReportData.getVariableAttribute().get(0);
        assertThat(variableAttribute.getMutability()).isEqualTo(WRITE_ONLY);
    }

    @Test
    void expectEmptyPasswordInReportData() {
        List<ReportData> reportData = basicAuthPasswordVariableAccessor.generateReportData(SECURITY_COMPONENT_NAME);

        ReportData ReportData = reportData.get(0);

        VariableAttribute variableAttribute = ReportData.getVariableAttribute().get(0);
        assertThat(variableAttribute.getValue().toString()).isEmpty();
    }

    @Test
    void expectEmptyVariableGetters() {
        Map<AttributeType, VariableGetter> variableGetters = basicAuthPasswordVariableAccessor.getVariableGetters();

        assertThat(variableGetters).isEmpty();
    }

    @Test
    void expectValidationToPass() {
        SetVariableValidator setVariableValidator = basicAuthPasswordVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        SetVariableResult result = setVariableValidator.validate(attributePath(), BASIC_AUTH_PASSWORD_ATTRIBUTE);

        assertThat(result.getComponent().getName().toString()).isEqualTo(SECURITY_COMPONENT_NAME);
        assertThat(result.getVariable().getName().toString()).isEqualTo(BASIC_AUTH_PASSWORD_VARIABLE_NAME);
        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatusEnum.ACCEPTED);
    }

    @Test
    void expectValidationToFailOnInvalidLength() {
        SetVariableValidator setVariableValidator = basicAuthPasswordVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        CiString1000 invalidPassword = new CiString1000("01234567890123456789012345678901234567890");

        SetVariableResult result = setVariableValidator.validate(attributePath(), invalidPassword);

        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE);
    }

    @Test
    void expectValidationToFailOnOddNumberOfChars() {
        SetVariableValidator setVariableValidator = basicAuthPasswordVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        CiString1000 invalidPassword = new CiString1000("12345");

        SetVariableResult result = setVariableValidator.validate(attributePath(), invalidPassword);

        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE);
    }

    @Test
    void expectValidationToFailOnInvalidHex() {
        SetVariableValidator setVariableValidator = basicAuthPasswordVariableAccessor.getVariableValidators().get(AttributeType.ACTUAL);

        CiString1000 invalidPassword = new CiString1000("0123456789abcdefABCDEF!");

        SetVariableResult result = setVariableValidator.validate(attributePath(), invalidPassword);

        assertThat(result.getAttributeStatus()).isEqualTo(SetVariableStatusEnum.NOT_SUPPORTED_ATTRIBUTE_TYPE);
    }

    private AttributePath attributePath() {
        return AttributePath.builder()
                .component(new Component().withName(new CiString.CiString50(SECURITY_COMPONENT_NAME)))
                .variable(new Variable().withName(new CiString.CiString50(BASIC_AUTH_PASSWORD_VARIABLE_NAME)))
                .attributeType(AttributeType.ACTUAL)
                .build();
    }
}
