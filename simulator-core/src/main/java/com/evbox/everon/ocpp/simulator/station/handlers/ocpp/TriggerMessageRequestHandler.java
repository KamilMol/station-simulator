package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.v20.message.MessageTriggerEnum;
import com.evbox.everon.ocpp.v20.message.TriggerMessageRequest;
import com.evbox.everon.ocpp.v20.message.TriggerMessageResponse;
import com.evbox.everon.ocpp.v20.message.TriggerMessageStatusEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Handler for {@link TriggerMessageRequest} request.
 */
@Slf4j
public class TriggerMessageRequestHandler implements OcppRequestHandler<TriggerMessageRequest> {

    private static final Executor executor = Executors.newSingleThreadExecutor();

    private StationStore stationStore;
    private StationMessageSender stationMessageSender;

    public TriggerMessageRequestHandler(StationStore stationStore, StationMessageSender stationMessageSender) {
        this.stationStore = stationStore;
        this.stationMessageSender = stationMessageSender;
    }

    @Override
    public void handle(String callId, TriggerMessageRequest request) {
        if (request.getRequestedMessage() == MessageTriggerEnum.SIGN_CHARGING_STATION_CERTIFICATE) {
            executor.execute(new SignCertificateRequestHandler(stationStore, stationMessageSender));
            stationMessageSender.sendCallResult(callId, new TriggerMessageResponse().withStatus(TriggerMessageStatusEnum.ACCEPTED));
        } else {
            stationMessageSender.sendCallResult(callId, new TriggerMessageResponse().withStatus(TriggerMessageStatusEnum.REJECTED));
        }
    }
}
