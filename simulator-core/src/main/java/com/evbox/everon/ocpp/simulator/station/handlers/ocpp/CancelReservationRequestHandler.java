package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.additionals.Reservation;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.v20.message.CancelReservationRequest;
import com.evbox.everon.ocpp.v20.message.CancelReservationResponse;
import com.evbox.everon.ocpp.v20.message.CancelReservationStatusEnum;
import com.evbox.everon.ocpp.v20.message.ConnectorStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class CancelReservationRequestHandler implements OcppRequestHandler<CancelReservationRequest> {

    private final StationMessageSender stationMessageSender;
    private final StationStore stationStore;

    @Override
    public void handle(String callId, CancelReservationRequest request) {
        Optional<Reservation> reservationOpt = stationStore.tryFindReservationById(request.getReservationId());
        CancelReservationResponse cancelReservationResponse = reservationOpt.map(reservation -> new CancelReservationResponse().withStatus(CancelReservationStatusEnum.ACCEPTED))
                .orElseGet(() -> new CancelReservationResponse().withStatus(CancelReservationStatusEnum.REJECTED));

        reservationOpt.ifPresent(reservation -> {
            if(isConnectorReserved(reservation)) {
                makeConnectorAvailable(reservation);
            }
            stationStore.removeReservation(reservation);
         });
        stationMessageSender.sendCallResult(callId, cancelReservationResponse);
    }

    private boolean isConnectorReserved(Reservation reservation) {
        return reservation.getConnectorId() != null && stationStore.tryFindConnector(reservation.getEvseId(), reservation.getConnectorId())
                    .map(Connector::getConnectorStatus)
                    .map(ConnectorStatusEnum.RESERVED::equals)
                    .orElse(false);
    }

    private void makeConnectorAvailable(Reservation reservation) {
        stationStore.tryFindConnector(reservation.getEvseId(), reservation.getConnectorId())
            .ifPresent(connector -> connector.setConnectorStatus(ConnectorStatusEnum.AVAILABLE));

        stationMessageSender.sendStatusNotification(reservation.getEvseId(), reservation.getConnectorId(), ConnectorStatusEnum.AVAILABLE);
    }
}
