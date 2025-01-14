package com.evbox.everon.ocpp.it.remotecontrol;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.Authorize;
import com.evbox.everon.ocpp.mock.csms.exchange.StatusNotification;
import com.evbox.everon.ocpp.mock.csms.exchange.TransactionEvent;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.simulator.station.actions.user.Plug;
import com.evbox.everon.ocpp.simulator.station.evse.EvseStatus;
import com.evbox.everon.ocpp.v20.message.ConnectorStatusEnum;
import com.evbox.everon.ocpp.v20.message.IdToken;
import com.evbox.everon.ocpp.v20.message.ReasonEnum;
import com.evbox.everon.ocpp.v20.message.RequestStartTransactionRequest;
import com.evbox.everon.ocpp.v20.message.TransactionEventEnum;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CALL_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_TOKEN_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.v20.message.IdTokenEnum.ISO_14443;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.waitAtMost;

public class RemoteStartTransactionIt extends StationSimulatorSetUp {

    private final int EV_CONNECTION_TIMEOUT = 2;

    @Test
    void shouldRemotelyStartTransactionRemoteStartFirst() {

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(TransactionEvent.request(TransactionEventEnum.STARTED))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(TransactionEventEnum.UPDATED))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatusEnum.AVAILABLE))
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatusEnum.OCCUPIED))
                .thenReturn(StatusNotification.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        IdToken token = new IdToken().withIdToken(new CiString.CiString36(DEFAULT_TOKEN_ID));
        Call call = new Call(DEFAULT_CALL_ID, ActionType.REQUEST_START_TRANSACTION, new RequestStartTransactionRequest().withEvseId(DEFAULT_EVSE_ID)
                .withIdToken(token).withRemoteStartId(1));
        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().hasOngoingTransaction(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isFalse());

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().hasOngoingTransaction(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().getDefaultEvse().getTokenId()).isEqualTo(DEFAULT_TOKEN_ID));

    }

    @Test
    void shouldRemotelyStartTransactionPlugFirst() {

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(TransactionEvent.request(TransactionEventEnum.STARTED))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(TransactionEventEnum.UPDATED))
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

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().hasOngoingTransaction(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isFalse());

        IdToken token = new IdToken().withIdToken(new CiString.CiString36(DEFAULT_TOKEN_ID));
        Call call = new Call(DEFAULT_CALL_ID, ActionType.REQUEST_START_TRANSACTION, new RequestStartTransactionRequest().withEvseId(DEFAULT_EVSE_ID).withIdToken(token).withRemoteStartId(1));
        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().hasOngoingTransaction(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().getDefaultEvse().getTokenId()).isEqualTo(DEFAULT_TOKEN_ID));

    }

    @Test
    void shouldTriggerTimeOut() {

        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443))
                .thenReturn(Authorize.response());

        ocppMockServer
                .when(TransactionEvent.request(TransactionEventEnum.STARTED))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(TransactionEventEnum.ENDED, ReasonEnum.TIMEOUT))
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatusEnum.AVAILABLE), times(2))
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(StatusNotification.request(ConnectorStatusEnum.OCCUPIED))
                .thenReturn(StatusNotification.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        IdToken token = new IdToken().withIdToken(new CiString.CiString36(DEFAULT_TOKEN_ID));
        Call call = new Call(DEFAULT_CALL_ID, ActionType.REQUEST_START_TRANSACTION, new RequestStartTransactionRequest().withEvseId(DEFAULT_EVSE_ID).withIdToken(token).withRemoteStartId(1));
        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().hasOngoingTransaction(DEFAULT_EVSE_ID)).isTrue());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().isCharging(DEFAULT_EVSE_ID)).isFalse());

        waitAtMost(EV_CONNECTION_TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().hasOngoingTransaction(DEFAULT_EVSE_ID)).isFalse());
        await().untilAsserted(() -> assertThat(stationSimulatorRunner.getStation(STATION_ID).getStateView().findEvse(DEFAULT_EVSE_ID).getEvseStatus()).isEqualTo(EvseStatus.AVAILABLE));

    }

    @Override
    protected int getEVConnectionTimeOutSec() {
        return EV_CONNECTION_TIMEOUT;
    }
}
