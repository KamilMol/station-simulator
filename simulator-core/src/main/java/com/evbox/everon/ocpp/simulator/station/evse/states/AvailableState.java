package com.evbox.everon.ocpp.simulator.station.evse.states;

import com.evbox.everon.ocpp.common.OptionList;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationMessage;
import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.simulator.station.actions.system.CancelRemoteStartTransaction;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessageResult;
import com.evbox.everon.ocpp.simulator.station.component.transactionctrlr.TxStartStopPointVariableValues;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.support.TransactionIdGenerator;
import com.evbox.everon.ocpp.v20.message.AuthorizationStatusEnum;
import com.evbox.everon.ocpp.v20.message.ChargingStateEnum;
import com.evbox.everon.ocpp.v20.message.ConnectorStatusEnum;
import com.evbox.everon.ocpp.v20.message.TriggerReasonEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.evbox.everon.ocpp.v20.message.TriggerReasonEnum.REMOTE_START;
import static java.util.Collections.singletonList;

/**
 * When the station is ready for an authorize or a plug
 */
@Slf4j
public class AvailableState extends AbstractEvseState {

    public static final String NAME = "AVAILABLE";

    @Override
    public String getStateName() {
        return NAME;
    }

    @Override
    public CompletableFuture<UserMessageResult> onPlug(int evseId, int connectorId) {
        Evse evse = stateManager.getStationStore().findEvse(evseId);

        if (evse.findConnector(connectorId).getCableStatus() != CableStatus.UNPLUGGED) {
            log.error(String.format("Connector is not available: %d %d", evseId, connectorId));
            return CompletableFuture.completedFuture(UserMessageResult.FAILED);
        }

        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();

        CompletableFuture<UserMessageResult> future = new CompletableFuture<>();
        evse.plug(connectorId);
        stationMessageSender.sendStatusNotificationAndSubscribe(evse, evse.findConnector(connectorId), (statusNotificationRequest, statusNotificationResponse) -> {
            OptionList<TxStartStopPointVariableValues> startPoints = stateManager.getStationStore().getTxStartPointValues();
            if (startPoints.contains(TxStartStopPointVariableValues.EV_CONNECTED) && !startPoints.contains(TxStartStopPointVariableValues.POWER_PATH_CLOSED)) {
                String transactionId = TransactionIdGenerator.getInstance().generateTransactionId();
                evse.createTransaction(transactionId);

                stationMessageSender.sendTransactionEventStart(evseId, connectorId, TriggerReasonEnum.CABLE_PLUGGED_IN, ChargingStateEnum.EV_CONNECTED);
            }
            future.complete(UserMessageResult.SUCCESSFUL);
        });

        stateManager.setStateForEvse(evseId, new WaitingForAuthorizationState());
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
//                List<Evse> authorizedEvses = hasEvses(response) ? getEvseList(response, stationStore) : singletonList(stationStore.getDefaultEvse());
                List<Evse> authorizedEvses = singletonList(stationStore.findEvse(evseId));

                authorizedEvses.forEach(evse -> evse.setToken(tokenId));

                OptionList<TxStartStopPointVariableValues> startPoints = stationStore.getTxStartPointValues();
                if (startPoints.contains(TxStartStopPointVariableValues.AUTHORIZED) && !startPoints.contains(TxStartStopPointVariableValues.POWER_PATH_CLOSED)) {
                    String transactionId = TransactionIdGenerator.getInstance().generateTransactionId();
                    authorizedEvses.forEach(evse -> evse.createTransaction(transactionId));

                    authorizedEvses.forEach(evse -> stationMessageSender.sendTransactionEventStart(evse.getId(), TriggerReasonEnum.AUTHORIZED, tokenId));
                }
                stateManager.setStateForEvse(evseId, new WaitingForPlugState());

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

        StationStore stationStore = stateManager.getStationStore();
        StationMessageSender stationMessageSender = stateManager.getStationMessageSender();

        Evse evse = stationStore.findEvse(evseId);

        String transactionId = TransactionIdGenerator.getInstance().generateTransactionId();
        evse.createTransaction(transactionId);

        evse.setToken(tokenId);

        stationMessageSender.sendStatusNotification(evse.getId(), connector.getId(), ConnectorStatusEnum.OCCUPIED);
        stationMessageSender.sendTransactionEventStart(evse.getId(), connector.getId(), remoteStartId, REMOTE_START);

        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            Station station = stateManager.getStation();
            station.sendMessage(new StationMessage(station.getConfiguration().getId(), StationMessage.Type.SYSTEM_ACTION, new CancelRemoteStartTransaction(evseId, connector.getId())));
        }, stationStore.getEVConnectionTimeOut(), TimeUnit.SECONDS);

        stateManager.setStateForEvse(evseId, new WaitingForPlugState());
    }

    @Override
    public void onRemoteStop(int evseId) {
        // NOP
    }
//
//    private List<Evse> getEvseList(AuthorizeResponse response, StationStore stationStore) {
//        return response.getEvseId().stream().map(stationStore::findEvse).collect(toList());
//    }
//
//    private boolean hasEvses(AuthorizeResponse response) {
//        return response.getEvseId() != null && !response.getEvseId().isEmpty();
//    }
}
