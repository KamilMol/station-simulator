package com.evbox.everon.ocpp.simulator.station.evse.states.helpers;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.StateManager;
import com.evbox.everon.ocpp.v20.message.ChargingStateEnum;
import com.evbox.everon.ocpp.v20.message.ReasonEnum;
import com.evbox.everon.ocpp.v20.message.TriggerReasonEnum;

import java.util.Optional;

public final class AuthorizeHelper {

    private AuthorizeHelper() {
        //NOP
    }

    public static void handleFailedAuthorizeResponse(StateManager stateManager, Evse evse) {
        if (evse.hasOngoingTransaction()) {
            StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
            StationStore stationStore = stateManager.getStationStore();
            OptionList<TxStartStopPointVariableValues> stopPoints = stationStore.getTxStopPointValues();
            Optional<Connector> connector = evse.tryFindPluggedConnector();

            evse.clearToken();
            evse.stopCharging();

            if (!stopPoints.contains(TxStartStopPointVariableValues.AUTHORIZED)) {
                stationMessageSender.sendTransactionEventUpdate(evse.getId(),
                        connector.map(Connector::getId).orElse(null),
                        TriggerReasonEnum.DEAUTHORIZED,
                        ChargingStateEnum.SUSPENDED_EVSE,
                        evse.getTotalConsumedWattHours());
            } else {
                stationMessageSender.sendTransactionEventEnded(evse.getId(),
                        connector.map(Connector::getId).orElse(null),
                        TriggerReasonEnum.DEAUTHORIZED,
                        ReasonEnum.DE_AUTHORIZED,
                        evse.getWattConsumedLastSession());
            }
        }
    }
}
