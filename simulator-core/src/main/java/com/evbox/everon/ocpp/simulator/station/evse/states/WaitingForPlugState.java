package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessageResult;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.support.TransactionIdGenerator;
import com.evbox.everon.ocpp.v20.message.AuthorizationStatusEnum;
import com.evbox.everon.ocpp.v20.message.ChargingStateEnum;
import com.evbox.everon.ocpp.v20.message.ReasonEnum;
import com.evbox.everon.ocpp.v20.message.TriggerReasonEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

import static com.evbox.everon.ocpp.v20.message.TriggerReasonEnum.CABLE_PLUGGED_IN;
import static java.util.Collections.singletonList;

/**
 * When the user has been authorized, but the cable is not plugged yet.
 */
@Slf4j
public class WaitingForPlugState extends AbstractEvseState {

    public static final String NAME = "WAITING_FOR_PLUG";

    @Override
    public String getStateName() {
        return NAME;
    }

    @Override
    public CompletableFuture<UserMessageResult> onPlug(int evseId, int connectorId) {
        StationStore stationStore = stateManager.getStationStore();

        Evse evse = stationStore.findEvse(evseId);
        if (evse.findConnector(connectorId).getCableStatus() != CableStatus.UNPLUGGED) {
            throw new IllegalStateException(String.format("Connector is not available: %d %d", evseId, connectorId));
        }

        evse.plug(connectorId);

        CompletableFuture<UserMessageResult> future = new CompletableFuture<>();
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
        stationMessageSender.sendStatusNotificationAndSubscribe(evse, evse.findConnector(connectorId), (statusNotificationRequest, statusNotificationResponse) -> {
            String tokenId = evse.getTokenId();
            log.info("Station has authorised token {}", tokenId);

            if (!evse.hasOngoingTransaction()) {
                String transactionId = TransactionIdGenerator.getInstance().generateTransactionId();
                evse.createTransaction(transactionId);

                stationMessageSender.sendTransactionEventStart(evseId, connectorId, CABLE_PLUGGED_IN, ChargingStateEnum.EV_CONNECTED);
            } else {
                stationMessageSender.sendTransactionEventUpdate(evseId, connectorId, TriggerReasonEnum.CABLE_PLUGGED_IN, tokenId, ChargingStateEnum.EV_CONNECTED);
            }

            startCharging(evse);

            stationMessageSender.sendTransactionEventUpdate(evseId, connectorId, TriggerReasonEnum.CHARGING_STATE_CHANGED, tokenId,
                    ChargingStateEnum.CHARGING);
            future.complete(UserMessageResult.SUCCESSFUL);
        });

        stateManager.setStateForEvse(evseId, new ChargingState());
        return future;
    }

    @Override
    public CompletableFuture<UserMessageResult> onAuthorize(int evseId, String tokenId) {
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();
        StationStore stationStore = stateManager.getStationStore();

        log.info("in authorizeToken {}", tokenId);

        CompletableFuture<UserMessageResult> future = new CompletableFuture<>();
        stationMessageSender.sendAuthorizeAndSubscribe(tokenId, singletonList(evseId), (request, response) -> {
            if (response.getIdTokenInfo().getStatus() == AuthorizationStatusEnum.ACCEPTED) {
                Evse evse = stationStore.findEvse(evseId);

                if (evse.hasOngoingTransaction()) {
                    evse.stopTransaction();
                    evse.clearToken();

                    stationMessageSender.sendTransactionEventEnded(evse.getId(), null, TriggerReasonEnum.EV_DEPARTED, ReasonEnum.DE_AUTHORIZED, evse.getWattConsumedLastSession());
                }
                stateManager.setStateForEvse(evseId, new AvailableState());
                future.complete(UserMessageResult.SUCCESSFUL);
            } else {
                future.complete(UserMessageResult.FAILED);
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<UserMessageResult> onUnplug(int evseId, int connectorId) {
        return CompletableFuture.completedFuture(UserMessageResult.NOT_EXECUTED);
    }

    @Override
    public void onRemoteStart(int evseId, int remoteStartId, String tokenId, Connector connector) {
        // NOP
    }

    @Override
    public void onRemoteStop(int evseId) {
        // NOP
    }

    private void startCharging(Evse evse) {
        evse.lockPluggedConnector();
        evse.startCharging();
    }

}
