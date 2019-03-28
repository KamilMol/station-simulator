package com.evbox.everon.ocpp.testutil;

import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.testutil.constants.StationConstants;
import com.evbox.everon.ocpp.testutil.factory.SimulatorConfigCreator;
import com.evbox.everon.ocpp.testutil.ocpp.OcppMockServer;
import com.evbox.everon.ocpp.testutil.ocpp.OcppServerClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static com.evbox.everon.ocpp.testutil.constants.StationConstants.*;

public class StationSimulatorSetUp {

    protected OcppServerClient ocppServerClient = new OcppServerClient();

    protected OcppMockServer ocppMockServer = OcppMockServer.builder()
            .hostname(StationConstants.HOST)
            .port(StationConstants.PORT)
            .path(StationConstants.PATH)
            .ocppServerClient(ocppServerClient)
            .build();

    protected StationSimulatorRunner stationSimulatorRunner;

    @BeforeEach
    void setUp() {
        ocppMockServer.start();

        SimulatorConfiguration.StationConfiguration stationConfiguration = SimulatorConfigCreator.createStationConfiguration(STATION_ID, DEFAULT_EVSE_COUNT, DEFAULT_EVSE_CONNECTORS);
        SimulatorConfiguration simulatorConfiguration = SimulatorConfigCreator.createSimulatorConfiguration(stationConfiguration);

        stationSimulatorRunner = new StationSimulatorRunner(StationConstants.OCPP_SERVER_URL, simulatorConfiguration);
    }

    @AfterEach
    void tearDown() {
        ocppMockServer.stop();
        ocppMockServer.reset();
    }
}
