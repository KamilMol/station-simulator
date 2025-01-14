package com.evbox.everon.ocpp.it.security;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.simulator.StationSimulatorRunner;
import com.evbox.everon.ocpp.simulator.configuration.SimulatorConfiguration;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v20.message.AttributeEnum;
import com.evbox.everon.ocpp.v20.message.SetVariablesRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.BASIC_AUTH_PASSWORD;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CALL_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.EVSE_CONNECTORS_TWO;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.EVSE_COUNT_TWO;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.OCPP_SERVER_URL;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.mock.constants.VariableConstants.BASIC_AUTH_PASSWORD_VARIABLE_NAME;
import static com.evbox.everon.ocpp.mock.constants.VariableConstants.SECURITY_COMPONENT_NAME;
import static com.evbox.everon.ocpp.mock.factory.SetVariablesCreator.createSetVariablesRequest;
import static com.evbox.everon.ocpp.mock.factory.SimulatorConfigCreator.createSimulatorConfiguration;
import static com.evbox.everon.ocpp.mock.factory.SimulatorConfigCreator.createStationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;

public class BasicAuthenticationIt extends StationSimulatorSetUp {

    void setUp(String password) {
        SimulatorConfiguration.StationConfiguration stationConfiguration = createStationConfiguration(
                STATION_ID,
                EVSE_COUNT_TWO,
                EVSE_CONNECTORS_TWO,
                password
        );
        SimulatorConfiguration simulatorConfiguration = createSimulatorConfiguration(stationConfiguration);
        stationSimulatorRunner = new StationSimulatorRunner(OCPP_SERVER_URL, simulatorConfiguration);
    }

    @Test
    void expectToFailAuth() {

        String invalidPassword = "0000";
        setUp(invalidPassword);

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilAuthorized();


        await().untilAsserted(() -> {
            Map<String, String> receivedCredentials = ocppMockServer.getReceivedCredentials();

            assertAll(
                    () -> assertThat(receivedCredentials).hasSize(1),
                    () -> assertThat(receivedCredentials.get(STATION_ID)).isEqualTo(invalidPassword),
                    () -> assertThat(ocppServerClient.isConnected()).isFalse()
            );
        });
    }

    @Test
    void expectSuccessfulAuth() {

        setUp(BASIC_AUTH_PASSWORD);

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilAuthorized();


        await().untilAsserted(() -> {
            Map<String, String> receivedCredentials = ocppMockServer.getReceivedCredentials();

            assertAll(
                    () -> assertThat(receivedCredentials).hasSize(1),
                    () -> assertThat(receivedCredentials.get(STATION_ID)).isEqualTo(BASIC_AUTH_PASSWORD),
                    () -> assertThat(ocppServerClient.isConnected()).isTrue()
            );
        });
    }

    @Test
    void shouldReconnectWithNewPassword() {

        setUp(BASIC_AUTH_PASSWORD);

        stationSimulatorRunner.run();

        ocppMockServer.waitUntilConnected();

        String newPassword = "aabbcc";

        SetVariablesRequest setVariablesRequest = createSetVariablesRequest(
                SECURITY_COMPONENT_NAME,
                BASIC_AUTH_PASSWORD_VARIABLE_NAME,
                newPassword,
                AttributeEnum.ACTUAL);

        Call call = new Call(DEFAULT_CALL_ID, ActionType.SET_VARIABLES, setVariablesRequest);

        ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson());

        ocppMockServer.setNewPassword(newPassword);

        await().untilAsserted(() -> {

            Map<String, String> receivedCredentials = ocppMockServer.getReceivedCredentials();

            assertAll(
                    () -> assertThat(receivedCredentials).hasSize(1),
                    () -> assertThat(receivedCredentials.get(STATION_ID)).isEqualTo(newPassword),
                    () -> assertThat(ocppMockServer.connectionAttempts()).isEqualTo(2)
            );

            ocppMockServer.verify();
        });

    }
}
