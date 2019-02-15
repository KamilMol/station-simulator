package com.evbox.everon.ocpp.simulator.station.actions;

import com.evbox.everon.ocpp.simulator.station.Evse;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationState;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest;
import com.evbox.everon.ocpp.v20.message.station.StatusNotificationResponse;
import com.evbox.everon.ocpp.v20.message.station.TransactionData;
import com.evbox.everon.ocpp.v20.message.station.TransactionEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.simulator.support.StationConstants.DEFAULT_CONNECTOR_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlugTest {

    @Mock
    StationState stationStateMock;
    @Mock
    StationMessageSender stationMessageSenderMock;

    Plug plug;

    @BeforeEach
    void setUp() {
        this.plug = new Plug(DEFAULT_CONNECTOR_ID);
    }

    @Test
    void shouldThrowExceptionOnIllegalState() {

        when(stationStateMock.getConnectorState(anyInt())).thenReturn(Evse.ConnectorState.LOCKED);

        assertThrows(IllegalStateException.class, () -> plug.perform(stationStateMock, stationMessageSenderMock));

    }

    @Test
    void verifyTransactionEventUpdate() {

        when(stationStateMock.getConnectorState(anyInt())).thenReturn(Evse.ConnectorState.AVAILABLE);

        when(stationStateMock.hasOngoingTransaction(anyInt())).thenReturn(true);

        plug.perform(stationStateMock, stationMessageSenderMock);

        ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(anyInt(), anyInt(), subscriberCaptor.capture());

        when(stationStateMock.hasAuthorizedToken(anyInt())).thenReturn(true);

        subscriberCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock).sendTransactionEventUpdateAndSubscribe(anyInt(), anyInt(), any(TransactionEventRequest.TriggerReason.class),
                isNull(), any(TransactionData.ChargingState.class), any(Subscriber.class));

    }

    @Test
    void verifyTransactionEventStart() {

        when(stationStateMock.getConnectorState(anyInt())).thenReturn(Evse.ConnectorState.AVAILABLE);

        when(stationStateMock.hasOngoingTransaction(anyInt())).thenReturn(true);

        plug.perform(stationStateMock, stationMessageSenderMock);

        ArgumentCaptor<Subscriber<StatusNotificationRequest, StatusNotificationResponse>> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);

        verify(stationMessageSenderMock).sendStatusNotificationAndSubscribe(anyInt(), anyInt(), subscriberCaptor.capture());

        when(stationStateMock.hasAuthorizedToken(anyInt())).thenReturn(false);

        subscriberCaptor.getValue().onResponse(new StatusNotificationRequest(), new StatusNotificationResponse());

        verify(stationMessageSenderMock).sendTransactionEventStart(anyInt(), anyInt(), any(TransactionEventRequest.TriggerReason.class),
                any(TransactionData.ChargingState.class));

    }
}