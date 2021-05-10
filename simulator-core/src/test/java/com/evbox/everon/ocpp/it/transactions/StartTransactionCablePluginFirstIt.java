package com.evbox.everon.ocpp.it.transactions;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.StatusNotification;
import com.evbox.everon.ocpp.mock.csms.exchange.TransactionEvent;
import com.evbox.everon.ocpp.simulator.station.actions.user.Plug;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CONNECTOR_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_EVSE_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_SEQ_NUMBER;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_TRANSACTION_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.atLeastOnce;
import static com.evbox.everon.ocpp.v20.message.ConnectorStatusEnum.OCCUPIED;
import static com.evbox.everon.ocpp.v20.message.TransactionEventEnum.STARTED;
import static org.awaitility.Awaitility.await;

public class StartTransactionCablePluginFirstIt extends StationSimulatorSetUp {

    @Test
    void shouldSendConnectorStatusAndTransactionStartedWhenCablePluggedIn() {

        ocppMockServer
                .when(StatusNotification.request(OCCUPIED), atLeastOnce())
                .thenReturn(TransactionEvent.response());

        ocppMockServer
                .when(TransactionEvent.request(STARTED, DEFAULT_SEQ_NUMBER, DEFAULT_EVSE_ID))
                .thenReturn(TransactionEvent.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        triggerUserAction(STATION_ID, new Plug(DEFAULT_EVSE_ID, DEFAULT_CONNECTOR_ID));

        await().untilAsserted(() -> ocppMockServer.verify());
    }
}
