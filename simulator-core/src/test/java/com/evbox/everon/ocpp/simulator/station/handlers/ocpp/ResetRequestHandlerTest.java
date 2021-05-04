package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.mock.factory.JsonMessageTypeFactory;
import com.evbox.everon.ocpp.mock.factory.OcppMessageFactory;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.subscription.Subscriber;
import com.evbox.everon.ocpp.simulator.websocket.AbstractWebSocketClientInboxMessage;
import com.evbox.everon.ocpp.v20.message.BootReasonEnum;
import com.evbox.everon.ocpp.v20.message.ReasonEnum;
import com.evbox.everon.ocpp.v20.message.ResetEnum;
import com.evbox.everon.ocpp.v20.message.ResetRequest;
import com.evbox.everon.ocpp.v20.message.ResetResponse;
import com.evbox.everon.ocpp.v20.message.ResetStatusEnum;
import com.evbox.everon.ocpp.v20.message.TriggerReasonEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_MESSAGE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResetRequestHandlerTest {

    @Mock
    StationMessageSender stationMessageSender;
    @Mock
    StationStore stationStore;

    @InjectMocks
    ResetRequestHandler resetRequestHandler;

    @Test
    void verifyMessageOnImmediateResetRequestType() throws JsonProcessingException {
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(ResetEnum.IMMEDIATE)
                .build();

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        ResetResponse payload = new ResetResponse().withStatus(ResetStatusEnum.ACCEPTED);

        ArgumentCaptor<AbstractWebSocketClientInboxMessage> messageCaptor = ArgumentCaptor.forClass(AbstractWebSocketClientInboxMessage.class);

        verify(stationMessageSender).sendMessage(messageCaptor.capture());

        String expectedCallResult = JsonMessageTypeFactory.createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(payload)
                .toJson();

        assertThat(messageCaptor.getValue().getData().get()).isEqualTo(expectedCallResult);
    }

    @Test
    void verifyEventSending() {
        Evse evse = mock(Evse.class);
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(ResetEnum.IMMEDIATE)
                .build();

        when(stationStore.getEvseIds()).thenReturn(Collections.singletonList(1));
        when(stationStore.hasOngoingTransaction(anyInt())).thenReturn(true);
        when(stationStore.findEvse(anyInt())).thenReturn(evse);
        when(evse.getWattConsumedLastSession()).thenReturn(0L);

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationStore).stopCharging(anyInt());
        verify(stationMessageSender).sendTransactionEventEndedAndSubscribe(anyInt(), anyInt(),
                any(TriggerReasonEnum.class), any(ReasonEnum.class), anyLong(), any(Subscriber.class));

    }

    @Test
    void verifyRebooting() {
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(ResetEnum.IMMEDIATE)
                .build();

        when(stationStore.getEvseIds()).thenReturn(Collections.singletonList(1));
        when(stationStore.hasOngoingTransaction(anyInt())).thenReturn(false);

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(stationStore).clearTokens();
        verify(stationStore).clearTransactions();
        verify(stationMessageSender, times(3)).sendMessage(any(AbstractWebSocketClientInboxMessage.class));
        verify(stationMessageSender).sendBootNotification(any(BootReasonEnum.class));

    }

    @Test
    void verifyMessageOnIdleResetRequestType() throws JsonProcessingException {
        ResetRequest request = OcppMessageFactory.createResetRequest()
                .withType(ResetEnum.ON_IDLE)
                .build();

        resetRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        ResetResponse payload = new ResetResponse().withStatus(ResetStatusEnum.ACCEPTED);

        ArgumentCaptor<AbstractWebSocketClientInboxMessage> messageCaptor = ArgumentCaptor.forClass(AbstractWebSocketClientInboxMessage.class);

        verify(stationMessageSender).sendMessage(messageCaptor.capture());

        String expectedCallResult = JsonMessageTypeFactory.createCallResult()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withPayload(payload)
                .toJson();

        assertThat(messageCaptor.getValue().getData().get()).isEqualTo(expectedCallResult);
    }

}
