package com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.reportbase;

import com.evbox.everon.ocpp.simulator.station.StationMessageSender;
import com.evbox.everon.ocpp.simulator.station.component.StationComponentsHolder;
import com.evbox.everon.ocpp.v20.message.GetBaseReportRequest;
import com.evbox.everon.ocpp.v20.message.GetBaseReportResponse;
import com.evbox.everon.ocpp.v20.message.ReportData;

import java.time.Clock;
import java.util.List;

import static com.evbox.everon.ocpp.v20.message.GenericDeviceModelStatusEnum.ACCEPTED;

public class ConfigurationInventoryReportAbstract extends AbstractBaseReport {

    private final StationComponentsHolder stationComponentsHolder;

    public ConfigurationInventoryReportAbstract(StationComponentsHolder stationComponentsHolder, StationMessageSender stationMessageSender, Clock clock) {
        super(stationMessageSender, clock);
        this.stationComponentsHolder = stationComponentsHolder;
    }

    @Override
    public void generateAndRespond(String callId, GetBaseReportRequest request) {
        stationMessageSender.sendCallResult(callId, new GetBaseReportResponse().withStatus(ACCEPTED));
        List<ReportData> reportData = stationComponentsHolder.generateReportData(true);
        sendNotifyReportRequests(reportData, request);
    }
}
