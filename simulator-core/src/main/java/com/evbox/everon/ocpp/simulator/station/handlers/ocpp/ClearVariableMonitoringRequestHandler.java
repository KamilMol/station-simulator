package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v20.message.ClearMonitoringResult;
import com.evbox.everon.ocpp.v20.message.ClearMonitoringStatusEnum;
import com.evbox.everon.ocpp.v20.message.ClearVariableMonitoringRequest;
import com.evbox.everon.ocpp.v20.message.ClearVariableMonitoringResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for {@link ClearVariableMonitoringRequest} request.
 */
@Slf4j
public class ClearVariableMonitoringRequestHandler implements OcppRequestHandler<ClearVariableMonitoringRequest> {

    private StationMessageSender stationMessageSender;
    private StationComponentsHolder stationComponentsHolder;

    public ClearVariableMonitoringRequestHandler(StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
        this.stationComponentsHolder = stationComponentsHolder;
    }

    /**
     * Handle {@link ClearVariableMonitoringRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, ClearVariableMonitoringRequest request) {
        List<ClearMonitoringResult> results = request.getId().stream()
                .map(id -> new ClearMonitoringResult().withId(id).withStatus(tryClearMonitor(id)))
                .collect(Collectors.toList());
        stationMessageSender.sendCallResult(callId, new ClearVariableMonitoringResponse().withClearMonitoringResult(results));
    }

    private ClearMonitoringStatusEnum tryClearMonitor(int id) {
        if (stationComponentsHolder.clearMonitor(id)) {
            return ClearMonitoringStatusEnum.ACCEPTED;
        }
        return ClearMonitoringStatusEnum.NOT_FOUND;
    }

}
