package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v20.message.ComponentVariable;
import com.evbox.everon.ocpp.v20.message.GenericDeviceModelStatusEnum;
import com.evbox.everon.ocpp.v20.message.GetMonitoringReportRequest;
import com.evbox.everon.ocpp.v20.message.GetMonitoringReportResponse;
import com.evbox.everon.ocpp.v20.message.MonitorEnum;
import com.evbox.everon.ocpp.v20.message.MonitoringCriterionEnum;
import com.evbox.everon.ocpp.v20.message.SetMonitoringData;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.evbox.everon.ocpp.v20.message.MonitorEnum.PERIODIC;
import static com.evbox.everon.ocpp.v20.message.MonitorEnum.PERIODIC_CLOCK_ALIGNED;

public class GetMonitoringReportRequestHandler implements OcppRequestHandler<GetMonitoringReportRequest> {

    private StationMessageSender stationMessageSender;
    private StationComponentsHolder stationComponentsHolder;

    public GetMonitoringReportRequestHandler(StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender) {
        this.stationMessageSender = stationMessageSender;
        this.stationComponentsHolder = stationComponentsHolder;
    }

    /**
     * Handle {@link GetMonitoringReportRequest} request.
     *
     * @param callId identity of the message
     * @param request incoming request from the server
     */
    @Override
    public void handle(String callId, GetMonitoringReportRequest request) {
        if (request.getMonitoringCriteria() != null && request.getMonitoringCriteria().contains(MonitoringCriterionEnum.PERIODIC_MONITORING)) {
            stationMessageSender.sendCallResult(callId, new GetMonitoringReportResponse().withStatus(GenericDeviceModelStatusEnum.NOT_SUPPORTED));
            return;
        }

        Map<ComponentVariable, List<SetMonitoringData>> monitoredComponents;
        if (request.getComponentVariable() != null && !request.getComponentVariable().isEmpty()) {
            monitoredComponents = stationComponentsHolder.getByComponentAndVariable(request.getComponentVariable());
        } else {
            monitoredComponents = stationComponentsHolder.getAllMonitoredComponents();
        }

        // Filter by type of monitor
        Set<MonitorEnum> requestedType = convertCriteriaToMonitorType(Optional.ofNullable(request.getMonitoringCriteria()).orElseGet(ArrayList::new));
        monitoredComponents.replaceAll((k, v) -> v.stream().filter(d -> requestedType.contains(d.getType())).collect(Collectors.toList()));

        if (monitoredComponents.isEmpty() || monitoredComponents.values().stream().allMatch(List::isEmpty)) {
            stationMessageSender.sendCallResult(callId, new GetMonitoringReportResponse().withStatus(GenericDeviceModelStatusEnum.REJECTED));
        } else {
            stationMessageSender.sendCallResult(callId, new GetMonitoringReportResponse().withStatus(GenericDeviceModelStatusEnum.ACCEPTED));
            stationMessageSender.sendNotifyMonitoringReport(request.getRequestId(), monitoredComponents);
        }
    }

    private EnumSet<MonitorEnum> convertCriteriaToMonitorType(List<MonitoringCriterionEnum> criterion) {
        EnumSet<MonitorEnum> criteriaToRemove = EnumSet.of(PERIODIC, PERIODIC_CLOCK_ALIGNED);

        if (!criterion.isEmpty()) {
            if (!criterion.contains(MonitoringCriterionEnum.THRESHOLD_MONITORING)) {
                criteriaToRemove.add(MonitorEnum.UPPER_THRESHOLD);
                criteriaToRemove.add(MonitorEnum.LOWER_THRESHOLD);
            }

            if (!criterion.contains(MonitoringCriterionEnum.DELTA_MONITORING)) {
                criteriaToRemove.add(MonitorEnum.DELTA);
            }
        }

        return EnumSet.complementOf(criteriaToRemove);
    }
}
