package com.evbox.everon.ocpp.it.remotecontrol;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.Authorize;
import com.evbox.everon.ocpp.mock.csms.exchange.StatusNotification;
import com.evbox.everon.ocpp.mock.csms.exchange.TransactionEvent;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.actions.user.Plug;
import com.evbox.everon.ocpp.simulator.station.actions.user.Unplug;
import com.evbox.everon.ocpp.simulator.station.evse.EvseStatus;
import com.evbox.everon.ocpp.v20.message.ChargingStateEnum;
import com.evbox.everon.ocpp.v20.message.ConnectorStatusEnum;
import com.evbox.everon.ocpp.v20.message.ReasonEnum;
import com.evbox.everon.ocpp.v20.message.RequestStartStopStatusEnum;
import com.evbox.everon.ocpp.v20.message.RequestStopTransactionRequest;
import com.evbox.everon.ocpp.v20.message.RequestStopTransactionResponse;
import com.evbox.everon.ocpp.v20.message.TransactionEventEnum;
import com.evbox.everon.ocpp.v20.message.TriggerReasonEnum;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CALL_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_TOKEN_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_TRANSACTION_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.v20.message.IdTokenEnum.ISO_14443;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class RemoteStopTransactionIt extends StationSimulatorSetUp {

    @Test
    void shouldRemotelyStopTransaction() {

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(TransactionEvent.request(TransactionEventEnum.UPDATED, ChargingStateEnum.EV_CONNECTED, DEFAULT_TRANSACTION_ID, TriggerReasonEnum.REMOTE_STOP))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatusEnum.AVAILABLE), times(2))
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(TransactionEvent.request(TransactionEventEnum.ENDED, ReasonEnum.REMOTE))
                .thenReturn(TransactionEvent.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.user.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isTrue());

        Call call = new Call(DEFAULT_CALL_ID, ActionType.REQUEST_STOP_TRANSACTION, new RequestStopTransactionRequest().withTransactionId(new CiString.CiString36(DEFAULT_TRANSACTION_ID)));
        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isFalse());

        triggerUserAction(STATION_ID, new Unplug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        await().untilAsserted(() -> {
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().findEvse(DEFAULT_EVSE_ID).getEvseStatus()).isEqualTo(EvseStatus.AVAILABLE);
            ocppMockServer.verify();
        });

    }

    @Test
    void shouldRejectRemoteStopTransaction() {

        final String RANDOM_TRANSACTION_ID = "TT_1234";

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatusEnum.AVAILABLE))
                .thenReturn(Authorize.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));
        triggerUserAction(STATION_ID, new com.evbox.everon.ocpp.simulator.station.actions.user.Authorize(DEFAULT_TOKEN_ID, DEFAULT_EVSE_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isTrue());

        Call call = new Call(DEFAULT_CALL_ID, ActionType.REQUEST_STOP_TRANSACTION, new RequestStopTransactionRequest().withTransactionId(new CiString.CiString36(RANDOM_TRANSACTION_ID)));
        RequestStopTransactionResponse response = ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson(), RequestStopTransactionResponse.class);

        await().untilAsserted(() -> {

            assertThat(response.getStatus()).isEqualTo(RequestStartStopStatusEnum.REJECTED);
            assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isTrue();
            ocppMockServer.verify();
        });

    }
}
