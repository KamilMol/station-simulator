package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.additionals.Reservation;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.v20.message.CancelReservationRequest;
import com.evbox.everon.ocpp.v20.message.CancelReservationResponse;
import com.evbox.everon.ocpp.v20.message.CancelReservationStatusEnum;
import com.evbox.everon.ocpp.v20.message.ConnectorStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CancelReservationRequestHandlerTest {

    private static final String CALL_ID = "123";
    private static final int RESERVATION_ID = 123;
    private static final int CONNECTOR_ID = 1;
    private static final int EVSE_ID = 2;

    @Mock
    private StationMessageSender stationMessageSender;

    @Mock
    private StationStore stationStore;

    @InjectMocks
    private CancelReservationRequestHandler handler;

    @Captor
    private ArgumentCaptor<CancelReservationResponse> cancelReservationResponseArgumentCaptor = ArgumentCaptor.forClass(CancelReservationResponse.class);

    @Test
    public void testCancelReservationRejected() {
        assertCancelReservationStatus(CancelReservationStatusEnum.REJECTED);
    }

    @Test
    public void testCancelReservationAcceptedWithNoConnectorId() {
        Reservation reservation = buildReservation(EVSE_ID, RESERVATION_ID, null);

        when(stationStore.tryFindReservationById(anyInt())).thenReturn(Optional.of(reservation));

        assertCancelReservationStatus(CancelReservationStatusEnum.ACCEPTED);
    }

    @Test
    public void testCancelReservationAcceptedWithConnectorId() {
        Reservation reservation = buildReservation(EVSE_ID, RESERVATION_ID, CONNECTOR_ID);
        Connector connector = new Connector(CONNECTOR_ID, CableStatus.PLUGGED, ConnectorStatusEnum.RESERVED);

        when(stationStore.tryFindReservationById(anyInt())).thenReturn(Optional.of(reservation));
        when(stationStore.tryFindConnector(anyInt(), anyInt())).thenReturn(Optional.of(connector));

        assertCancelReservationStatus(CancelReservationStatusEnum.ACCEPTED);
        verify(stationMessageSender).sendStatusNotification(EVSE_ID, CONNECTOR_ID, ConnectorStatusEnum.AVAILABLE);
        assertThat(connector.getConnectorStatus()).isEqualTo(ConnectorStatusEnum.AVAILABLE);
    }

    private void assertCancelReservationStatus(CancelReservationStatusEnum status) {
        handler.handle(CALL_ID, new CancelReservationRequest().withReservationId(RESERVATION_ID));

        verify(stationMessageSender).sendCallResult(anyString(), cancelReservationResponseArgumentCaptor.capture());

        CancelReservationResponse response = cancelReservationResponseArgumentCaptor.getValue();

        assertThat(response.getStatus()).isEqualTo(status);
//        assertThat(response.getAdditionalProperties()).isEmpty();
    }

    private Reservation buildReservation(Integer evseId, Integer reservationId, Integer connectorId) {
        return new Reservation().withId(reservationId).withEvseId(evseId).withConnectorId(connectorId);
    }
}
