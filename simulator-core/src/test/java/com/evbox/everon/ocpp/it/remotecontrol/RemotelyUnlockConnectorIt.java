package com.evbox.everon.ocpp.it.remotecontrol;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.Authorize;
import com.evbox.everon.ocpp.mock.csms.exchange.StatusNotification;
import com.evbox.everon.ocpp.mock.csms.exchange.TransactionEvent;
import com.evbox.everon.ocpp.mock.expect.ExpectedCount;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.actions.user.Plug;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.v20.message.ConnectorStatusEnum;
import com.evbox.everon.ocpp.v20.message.TransactionEventEnum;
import com.evbox.everon.ocpp.v20.message.UnlockConnectorRequest;
import com.evbox.everon.ocpp.v20.message.UnlockConnectorResponse;
import com.evbox.everon.ocpp.v20.message.UnlockStatusEnum;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CALL_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_TOKEN_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.v20.message.IdTokenEnum.ISO_14443;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class RemotelyUnlockConnectorIt extends StationSimulatorSetUp {

    @Test
    void shouldRemotelyUnlockConnector() {

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443), ExpectedCount.times(2))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(TransactionEvent.request(TransactionEventEnum.STARTED))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatusEnum.AVAILABLE))
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatusEnum.OCCUPIED))
                .thenReturn(StatusNotification.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.user.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().findEvse(DEFAULT_EVSE_ID).getConnectors().get(0).getCableStatus()).isEqualTo(CableStatus.LOCKED));
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().findEvse(DEFAULT_EVSE_ID).isCharging()).isTrue());

        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.user.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().findEvse(DEFAULT_EVSE_ID).isCharging()).isFalse());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().findEvse(DEFAULT_EVSE_ID).hasOngoingTransaction()).isFalse());

        Call call = new Call(DEFAULT_CALL_ID, ActionType.UNLOCK_CONNECTOR, new UnlockConnectorRequest().withEvseId(DEFAULT_EVSE_ID).withConnectorId(DEFAULT_CONNECTOR_ID));
        UnlockConnectorResponse response = ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson(), UnlockConnectorResponse.class);

        await().untilAsserted(() -> assertThat(response.getStatus()).isEqualTo(UnlockStatusEnum.UNLOCKED));
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().findEvse(DEFAULT_EVSE_ID).getConnectors().get(0).getCableStatus()).isEqualTo(CableStatus.PLUGGED));
    }

    @Test
    void shouldNotUnlockWithOngoingTransaction() {

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(TransactionEvent.request(TransactionEventEnum.STARTED))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatusEnum.AVAILABLE))
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatusEnum.OCCUPIED))
                .thenReturn(StatusNotification.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.user.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().findEvse(DEFAULT_EVSE_ID).getConnectors().get(0).getCableStatus()).isEqualTo(CableStatus.LOCKED));
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().findEvse(DEFAULT_EVSE_ID).hasOngoingTransaction()).isTrue());

        Call call = new Call(DEFAULT_CALL_ID, ActionType.UNLOCK_CONNECTOR, new UnlockConnectorRequest().withEvseId(DEFAULT_EVSE_ID).withConnectorId(DEFAULT_CONNECTOR_ID));
        UnlockConnectorResponse response = ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson(), UnlockConnectorResponse.class);

        await().untilAsserted(() -> assertThat(response.getStatus()).isEqualTo(UnlockStatusEnum.UNLOCK_FAILED));
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().findEvse(DEFAULT_EVSE_ID).getConnectors().get(0).getCableStatus()).isEqualTo(CableStatus.LOCKED));
    }
}