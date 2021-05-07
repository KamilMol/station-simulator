package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessageResult;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.ReasonEnum;
import com.evbox.everon.ocpp.v20.message.TriggerReasonEnum;

import java.util.concurrent.CompletableFuture;

public class RemotelyStoppedState extends StoppedState {

    public static final String NAME = "REMOTELY_STOPPED";

    @Override
    public String getStateName() {
        return NAME;
    }

    @Override
    public CompletableFuture<UserMessageResult> onUnplug(int evseId, int connectorId) {
        StationStore stationStore = stateManager.getStationStore();
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
        Evse evse = stationStore.findEvse(evseId);

        evse.unplug(connectorId);
        evse.stopTransaction();
        evse.clearToken();

//        stationMessageSender.sendStatusNotification(evse, evse.findConnector(connectorId));
//        stationMessageSender.sendTransactionEventEnded(evseId, connectorId,
//                                                        TriggerReasonEnum.EV_DEPARTED,
//                                                        ReasonEnum.REMOTE,
//                                                        evse.getWattConsumedLastSession());

        stationMessageSender.sendTransactionEventEndedAndSubscribe(evseId, connectorId,
                TriggerReasonEnum.EV_DEPARTED,
                ReasonEnum.REMOTE,
                evse.getWattConsumedLastSession(),
                (request, response) -> {
                    stationMessageSender.sendStatusNotification(evse, evse.findConnector(connectorId));
                });


        stateManager.setStateForEvse(evseId, new AvailableState());
        return CompletableFuture.completedFuture(UserMessageResult.SUCCESSFUL);
    }
}
