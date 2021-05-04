package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.v20.message.SendLocalListRequest;
import com.evbox.everon.ocpp.v20.message.SendLocalListResponse;
import com.evbox.everon.ocpp.v20.message.SendLocalListStatusEnum;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SendLocalListRequestHandler implements OcppRequestHandler<SendLocalListRequest> {

    private final StationMessageSender stationMessageSender;

    @Override
    public void handle(String callId, SendLocalListRequest request) {
        stationMessageSender.sendCallResult(callId, new SendLocalListResponse().withStatus(SendLocalListStatusEnum.ACCEPTED));
    }
}
