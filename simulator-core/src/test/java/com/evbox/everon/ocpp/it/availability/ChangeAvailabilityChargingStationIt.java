package com.evbox.everon.ocpp.it.availability;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.StatusNotification;
import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.evse.Evse.EvseView;
import com.evbox.everon.ocpp.simulator.station.evse.EvseStatus;
import com.evbox.everon.ocpp.v20.message.ChangeAvailabilityRequest;
import com.evbox.everon.ocpp.v20.message.EVSE;
import com.evbox.everon.ocpp.v20.message.OperationalStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.CHANGE_AVAILABILITY_ACTION;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_MESSAGE_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.EVSE_CONNECTORS_TWO;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.EVSE_COUNT_TWO;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.EVSE_ID_ZERO;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.OCPP_SERVER_URL;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.mock.expect.ExpectedCount.times;
import static com.evbox.everon.ocpp.mock.factory.JsonMessageTypeFactory.createCall;
import static com.evbox.everon.ocpp.mock.factory.SimulatorConfigCreator.createSimulatorConfiguration;
import static com.evbox.everon.ocpp.mock.factory.SimulatorConfigCreator.createStationConfiguration;
import static com.evbox.everon.ocpp.v20.message.ConnectorStatusEnum.AVAILABLE;
import static com.evbox.everon.ocpp.v20.message.ConnectorStatusEnum.UNAVAILABLE;
import static com.evbox.everon.ocpp.v20.message.OperationalStatusEnum.INOPERATIVE;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ChangeAvailabilityChargingStationIt extends StationSimulatorSetUp {


    @BeforeEach
    void changeStationAvailabilitySetUp() {
        SimulatorConfiguration.StationConfiguration stationConfiguration = createStationConfiguration(STATION_ID, EVSE_COUNT_TWO, EVSE_CONNECTORS_TWO);
        SimulatorConfiguration simulatorConfiguration = createSimulatorConfiguration(stationConfiguration);
        stationSimulatorRunner = new StationSimulatorRunner(OCPP_SERVER_URL, simulatorConfiguration);
    }

    @Test
    void shouldChangeStationStatusToUnavailable() {

        ocppMockServer
                .when(StatusNotification.request(AVAILABLE), times(4))
                .thenReturn(StatusNotification.response());

        ocppMockServer
                .when(StatusNotification.request(UNAVAILABLE), times(4))
                .thenReturn(StatusNotification.response());

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        ocppServerClient.findStationSender(STATION_ID).sendMessage(changeAvailabilityRequestWithStatus(INOPERATIVE));

        await().untilAsserted(() -> {
            Station station = stationSimulatorRunner.getStation(STATION_ID);

            List<EvseStatus> evseStatuses = station.getStateView().getEvses().stream().map(EvseView::getEvseStatus).collect(toList());

            assertThat(evseStatuses).containsOnly(EvseStatus.UNAVAILABLE);

            ocppMockServer.verify();
        });
    }

    String changeAvailabilityRequestWithStatus(OperationalStatusEnum operationalStatus) {
        ChangeAvailabilityRequest changeAvailabilityRequest = new ChangeAvailabilityRequest()
                .withEvse(new EVSE().withId(EVSE_ID_ZERO))
                .withOperationalStatus(operationalStatus);

        return createCall()
                .withMessageId(DEFAULT_MESSAGE_ID)
                .withAction(CHANGE_AVAILABILITY_ACTION)
                .withPayload(changeAvailabilityRequest)
                .toJson();
    }
}
