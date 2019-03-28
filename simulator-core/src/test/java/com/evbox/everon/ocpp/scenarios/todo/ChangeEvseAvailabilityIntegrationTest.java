package com.evbox.everon.ocpp.scenarios.todo;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.evse.EvseStatus;
import com.evbox.everon.ocpp.testutil.StationSimulatorSetUp;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest;
import org.junit.jupiter.api.Test;

import static com.evbox.everon.ocpp.testutil.expect.ExpectedCount.twice;
import static com.evbox.everon.ocpp.testutil.ocpp.ExpectedRequests.bootNotificationRequest;
import static com.evbox.everon.ocpp.testutil.ocpp.ExpectedRequests.statusNotificationRequestWithStatus;
import static com.evbox.everon.ocpp.testutil.constants.StationConstants.*;
import static com.evbox.everon.ocpp.testutil.factory.JsonMessageTypeFactory.createCall;
import static com.evbox.everon.ocpp.testutil.ocpp.MockedResponses.*;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus.INOPERATIVE;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus.OPERATIVE;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.AVAILABLE;
import static com.evbox.everon.ocpp.v20.message.station.StatusNotificationRequest.ConnectorStatus.UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


public class ChangeEvseAvailabilityIntegrationTest extends StationSimulatorSetUp {

    @Test
    void shouldChangeEvseStatusToUnavailable() {

        // given
        ocppMockServer
                .when(bootNotificationRequest())
                .thenReturn(bootNotificationResponseMock());

        ocppMockServer
                .when(statusNotificationRequestWithStatus(AVAILABLE))
                .thenReturn(emptyResponse());

        ocppMockServer
                .when(statusNotificationRequestWithStatus(UNAVAILABLE))
                .thenReturn(emptyResponse());

        // when
        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(changeAvailabilityRequestWithStatus(INOPERATIVE));

        // then
        await().untilAsserted(() -> {
            Station station = stationSimulatorRunner.getStation(STATION_ID);

            assertThat(station.getState().findEvse(DEFAULT_EVSE_ID).getEvseStatus()).isEqualTo(EvseStatus.UNAVAILABLE);

            ocppMockServer.verify();
        });

    }

    @Test
    void shouldChangeEvseStatusToAvailable() {

        // given
        ocppMockServer
                .when(bootNotificationRequest())
                .thenReturn(bootNotificationResponseMock());

        ocppMockServer
                .when(statusNotificationRequestWithStatus(AVAILABLE), twice())
                .thenReturn(emptyResponse());

        ocppMockServer
                .when(statusNotificationRequestWithStatus(UNAVAILABLE))
                .thenReturn(emptyResponse());

        // when
        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(changeAvailabilityRequestWithStatus(INOPERATIVE));
        ocppServerClient.findStationSender(STATION_ID).sendMessage(changeAvailabilityRequestWithStatus(OPERATIVE));

        // then
        await().untilAsserted(() -> {
            Station station = stationSimulatorRunner.getStation(STATION_ID);

            assertThat(station.getState().findEvse(DEFAULT_EVSE_ID).getEvseStatus()).isEqualTo(EvseStatus.AVAILABLE);

            ocppMockServer.verify();
        });

    }

    String changeAvailabilityRequestWithStatus(ChangeAvailabilityRequest.OperationalStatus operationalStatus) {
        ChangeAvailabilityRequest changeAvailabilityRequest = new ChangeAvailabilityRequest()
                .withEvseId(DEFAULT_EVSE_ID)
                .withOperationalStatus(operationalStatus);

        return createCall()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withAction(CHANGE_AVAILABILITY_ACTION)
                .withPayload(changeAvailabilityRequest)
                .toJson();
    }
}
