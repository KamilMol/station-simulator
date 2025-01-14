package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponent;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v20.message.Component;
import com.evbox.everon.ocpp.v20.message.MonitorEnum;
import com.evbox.everon.ocpp.v20.message.SetMonitoringData;
import com.evbox.everon.ocpp.v20.message.SetMonitoringStatusEnum;
import com.evbox.everon.ocpp.v20.message.SetVariableMonitoringRequest;
import com.evbox.everon.ocpp.v20.message.SetVariableMonitoringResponse;
import com.evbox.everon.ocpp.v20.message.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetVariableMonitoringRequestHandlerTest {

    @Mock
    StationComponent stationComponent;
    @Mock
    StationComponentsHolder stationComponentsHolder;
    @Mock
    StationMessageSender stationMessageSender;
    @InjectMocks
    SetVariableMonitoringRequestHandler handler;

    @Captor
    ArgumentCaptor<SetVariableMonitoringResponse> responseCaptor = ArgumentCaptor.forClass(SetVariableMonitoringResponse.class);

    @Test
    void verifyCorrectNumberOfResponses() {
        final int size = 5;
        List<SetMonitoringData> datums = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            datums.add(createdMonitoringDatum("component" + i, "variable"+i));
        }
        SetVariableMonitoringRequest request = new SetVariableMonitoringRequest().withSetMonitoringData(datums);

        handler.handle("1", request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        SetVariableMonitoringResponse response = responseCaptor.getValue();
        assertThat(response.getSetMonitoringResult().size()).isEqualTo(size);
        assertTrue(response.getSetMonitoringResult().stream().allMatch(r -> r.getStatus() == SetMonitoringStatusEnum.UNKNOWN_COMPONENT));
        assertTrue(response.getSetMonitoringResult().stream().allMatch(r -> r.getId() == null));
    }

    @Test
    void verifyUnknownVariable() {
        when(stationComponent.getVariableNames()).thenReturn(Collections.emptySet());
        when(stationComponentsHolder.getComponent(any())).thenReturn(Optional.of(stationComponent));

        List<SetMonitoringData> datums = new ArrayList<>();
        datums.add(createdMonitoringDatum("component", "variable"));
        SetVariableMonitoringRequest request = new SetVariableMonitoringRequest().withSetMonitoringData(datums);

        handler.handle("1", request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        SetVariableMonitoringResponse response = responseCaptor.getValue();
        assertThat(response.getSetMonitoringResult().get(0).getStatus()).isEqualTo(SetMonitoringStatusEnum.UNKNOWN_VARIABLE);
        assertTrue(response.getSetMonitoringResult().stream().allMatch(r -> r.getId() == null));
    }

    @Test
    void verifyVariableIsMonitored() {
        when(stationComponent.getVariableNames()).thenReturn(new HashSet<>(Arrays.asList("variable1", "variable2")));
        when(stationComponentsHolder.getComponent(any())).thenReturn(Optional.of(stationComponent));

        List<SetMonitoringData> datums = new ArrayList<>();
        datums.add(createdMonitoringDatum("component", "variable1"));
        datums.add(createdMonitoringDatum("component", "variable2"));
        SetVariableMonitoringRequest request = new SetVariableMonitoringRequest().withSetMonitoringData(datums);

        handler.handle("1", request);

        verify(stationMessageSender).sendCallResult(anyString(), responseCaptor.capture());
        SetVariableMonitoringResponse response = responseCaptor.getValue();
        assertTrue(response.getSetMonitoringResult().stream().allMatch(r -> r.getStatus() == SetMonitoringStatusEnum.ACCEPTED));
        assertTrue(response.getSetMonitoringResult().stream().allMatch(r -> r.getId() == 1));

        verify(stationComponentsHolder).monitorComponent(eq(1), argThat(cv -> "component".equals(cv.getComponent().getName().toString()) && "variable1".equals(cv.getVariable().getName().toString())), eq(datums.get(0)));
        verify(stationComponentsHolder).monitorComponent(eq(1), argThat(cv -> "component".equals(cv.getComponent().getName().toString()) && "variable2".equals(cv.getVariable().getName().toString())), eq(datums.get(1)));
    }

    private SetMonitoringData createdMonitoringDatum(String componentName, String variableName) {
        return new SetMonitoringData()
                .withId(1)
                .withType(MonitorEnum.LOWER_THRESHOLD)
                .withSeverity(0)
                .withComponent(new Component().withName(new CiString.CiString50(componentName)))
                .withVariable(new Variable().withName(new CiString.CiString50(variableName)));
    }

}
