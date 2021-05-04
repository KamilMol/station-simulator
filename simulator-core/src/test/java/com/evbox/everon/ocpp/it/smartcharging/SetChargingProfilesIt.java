package com.evbox.everon.ocpp.it.smartcharging;

import com.evbox.everon.ocpp.mock.StationSimulatorSetUp;
import com.evbox.everon.ocpp.mock.csms.exchange.Authorize;
import com.evbox.everon.ocpp.simulator.message.ActionType;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v20.message.ChargingProfile;
import com.evbox.everon.ocpp.v20.message.ChargingProfileKindEnum;
import com.evbox.everon.ocpp.v20.message.ChargingProfilePurposeEnum;
import com.evbox.everon.ocpp.v20.message.ChargingProfileStatusEnum;
import com.evbox.everon.ocpp.v20.message.ChargingRateUnitEnum;
import com.evbox.everon.ocpp.v20.message.ChargingSchedule;
import com.evbox.everon.ocpp.v20.message.SetChargingProfileRequest;
import com.evbox.everon.ocpp.v20.message.SetChargingProfileResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_CALL_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.DEFAULT_TOKEN_ID;
import static com.evbox.everon.ocpp.mock.constants.StationConstants.STATION_ID;
import static com.evbox.everon.ocpp.v20.message.IdTokenEnum.ISO_14443;
import static org.assertj.core.api.Assertions.assertThat;

public class SetChargingProfilesIt extends StationSimulatorSetUp {

    @Test
    void shouldRejectSetChargingProfileRequests() {
        ocppMockServer
                .when(Authorize.request(DEFAULT_TOKEN_ID, ISO_14443))
                .thenReturn(Authorize.response());

        stationSimulatorRunner.run();
        ocppMockServer.waitUntilConnected();

        ChargingProfile chargingProfile = new ChargingProfile()
                                                .withId(1)
                                                .withStackLevel(2)
                                                .withChargingProfilePurpose(ChargingProfilePurposeEnum.TX_DEFAULT_PROFILE)
                                                .withChargingProfileKind(ChargingProfileKindEnum.ABSOLUTE)
                                                .withChargingSchedule(List.of(new ChargingSchedule().withChargingRateUnit(ChargingRateUnitEnum.A)));
        SetChargingProfileRequest chargingProfileRequest = new SetChargingProfileRequest()
                                                                .withEvseId(1)
                                                                .withChargingProfile(chargingProfile);

        Call call = new Call(DEFAULT_CALL_ID, ActionType.SET_CHARGING_PROFILE, chargingProfileRequest);
        SetChargingProfileResponse response = ocppServerClient.findStationSender(STATION_ID).sendMessage(call.toJson(), SetChargingProfileResponse.class);
        assertThat(response.getStatus()).isEqualTo(ChargingProfileStatusEnum.REJECTED);
    }
}
