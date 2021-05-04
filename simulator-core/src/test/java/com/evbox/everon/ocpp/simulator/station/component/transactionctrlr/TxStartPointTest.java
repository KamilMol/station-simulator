package com.evbox.everon.ocpp.simulator.station.component.transactionctrlr;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.simulator.station.evse.states.AvailableState;
import com.evbox.everon.ocpp.simulator.station.evse.states.WaitingForAuthorizationState;
import com.evbox.everon.ocpp.simulator.station.evse.states.WaitingForPlugState;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v20.message.AuthorizationStatusEnum;
import com.evbox.everon.ocpp.v20.message.AuthorizeRequest;
import com.evbox.everon.ocpp.v20.message.AuthorizeResponse;
import com.evbox.everon.ocpp.v20.message.ChargingStateEnum;
import com.evbox.everon.ocpp.v20.message.IdTokenInfo;
import com.evbox.everon.ocpp.v20.message.StatusNotificationRequest;
import com.evbox.everon.ocpp.v20.message.StatusNotificationResponse;
import com.evbox.everon.ocpp.v20.message.TriggerReasonEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_TOKEN_ID;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TxStartPointTest {

    @Mock
    Connector connectorMock;

    @Mock
    Evse evseMock;

    @Mock
    StationStore stationStoreMock;

    @Mock
    StationMessageSender stationMessageSenderMock;

    @Mock
    StateManager stateManagerMock;

    @Captor
    ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> statusNotificationCaptor;

    @Captor
    ArgumentCaptor<Subscriber<AuthorizeRequest, AuthorizeResponse>> authorizeCaptor;

    @BeforeEach
    void setUp() {
        this.stateManagerMock = new StateManager(null, stationStoreMock, stationMessageSenderMock);
        when(evseMock.getEvseState()).thenReturn(new AvailableState());
    }

    @Test
    void verifyStartOnlyOnAuthorizedPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReasonEnum.CABLE_PLUGGED_IN, ChargingStateEnum.EV_CONNECTED);
        verify(evseMock, times(0)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnlyOnAuthorizedAuthAction() {
        when(evseMock.getId()).thenReturn(DEFAULT_EVSE_ID);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.AUTHORIZED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatusEnum.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReasonEnum.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(1)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnlyOnEVConnectedPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.EV_CONNECTED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReasonEnum.CABLE_PLUGGED_IN, ChargingStateEnum.EV_CONNECTED);
        verify(evseMock, times(1)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnlyOnEVConnectedAuthAction() {
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.EV_CONNECTED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatusEnum.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReasonEnum.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(0)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnlyOnPowerPathClosedPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReasonEnum.CABLE_PLUGGED_IN, ChargingStateEnum.EV_CONNECTED);
        verify(evseMock, times(0)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnlyOnPowerPathClosedAuthAction() {
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatusEnum.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReasonEnum.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(0)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnlyOnPowerPathClosedAuthPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatusEnum.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReasonEnum.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(0)).createTransaction(anyString());
        verify(evseMock).setEvseState(argThat(s -> s.getStateName().equals(WaitingForPlugState.NAME)));

        when(evseMock.getEvseState()).thenReturn(new WaitingForPlugState());

        stateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReasonEnum.CABLE_PLUGGED_IN, ChargingStateEnum.EV_CONNECTED);
        verify(evseMock, times(1)).createTransaction(anyString());
        verify(evseMock, times(1)).startCharging();
    }

    @Test
    void verifyStartOnlyOnPowerPathClosedPlugAuthAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Collections.singletonList(TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReasonEnum.CABLE_PLUGGED_IN, ChargingStateEnum.EV_CONNECTED);
        verify(evseMock, times(0)).createTransaction(anyString());
        verify(evseMock).setEvseState(argThat(s -> s.getStateName().equals(WaitingForAuthorizationState.NAME)));

        when(evseMock.getEvseState()).thenReturn(new WaitingForAuthorizationState());

        stateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatusEnum.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReasonEnum.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(1)).createTransaction(anyString());
        verify(evseMock, times(1)).startCharging();
    }

    @Test
    void verifyStartOnAuthorizedAndEVConnectedAuthAction() {
        when(evseMock.getId()).thenReturn(DEFAULT_EVSE_ID);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatusEnum.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReasonEnum.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(1)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnAuthorizedAndEVConnectedPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReasonEnum.CABLE_PLUGGED_IN, ChargingStateEnum.EV_CONNECTED);
        verify(evseMock, times(1)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnAuthorizedAndPowerPathClosedAuthPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatusEnum.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReasonEnum.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(0)).createTransaction(anyString());
        verify(evseMock).setEvseState(argThat(s -> s.getStateName().equals(WaitingForPlugState.NAME)));

        when(evseMock.getEvseState()).thenReturn(new WaitingForPlugState());

        stateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReasonEnum.CABLE_PLUGGED_IN, ChargingStateEnum.EV_CONNECTED);
        verify(evseMock, times(1)).createTransaction(anyString());
        verify(evseMock, times(1)).startCharging();
    }

    @Test
    void verifyStartOnEVConnectedAndPowerPathClosedPlugAuthAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.EV_CONNECTED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReasonEnum.CABLE_PLUGGED_IN, ChargingStateEnum.EV_CONNECTED);
        verify(evseMock, times(0)).createTransaction(anyString());
        verify(evseMock).setEvseState(argThat(s -> s.getStateName().equals(WaitingForAuthorizationState.NAME)));

        when(evseMock.getEvseState()).thenReturn(new WaitingForAuthorizationState());

        stateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatusEnum.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(1)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReasonEnum.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(1)).createTransaction(anyString());
        verify(evseMock, times(1)).startCharging();
    }

    @Test
    void verifyStartOnAuthorizedAndEVConnectedAndPowerPathClosedPlugAction() {
        when(connectorMock.getCableStatus()).thenReturn(CableStatus.UNPLUGGED);
        when(evseMock.findConnector(anyInt())).thenReturn(connectorMock);
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.cablePlugged(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID);
        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(any(), any(), statusNotificationCaptor.capture());
        statusNotificationCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID, TriggerReasonEnum.CABLE_PLUGGED_IN, ChargingStateEnum.EV_CONNECTED);
        verify(evseMock, times(0)).createTransaction(anyString());
    }

    @Test
    void verifyStartOnAuthorizedAndEVConnectedAndPowerPathClosedAuthAction() {
        when(stationStoreMock.getTxStartPointValues()).thenReturn(new OptionList<>(Arrays.asList(TxStartStopPointVariableValues.AUTHORIZED, TxStartStopPointVariableValues.EV_CONNECTED, TxStartStopPointVariableValues.POWER_PATH_CLOSED)));
        when(stationStoreMock.findEvse(anyInt())).thenReturn(evseMock);

        stateManagerMock.authorized(DEFAULT_EVSE_ID, DEFAULT_TOKEN_ID);
        verify(stationMessageSenderMock).sendAuthorizeAndSubscribe(anyString(), anyList(), authorizeCaptor.capture());
        authorizeCaptor.getValue().onResponse(new AuthorizeRequest(), new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatusEnum.ACCEPTED).withEvseId(Collections.singletonList(DEFAULT_EVSE_ID))));

        verify(stationMessageSenderMock, times(0)).sendTransactionEventStart(DEFAULT_EVSE_ID, TriggerReasonEnum.AUTHORIZED, DEFAULT_TOKEN_ID);
        verify(evseMock, times(0)).createTransaction(anyString());
    }

}
