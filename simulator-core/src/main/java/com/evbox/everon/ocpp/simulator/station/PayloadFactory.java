package com.evbox.everon.ocpp.simulator.station;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.v20.message.AuthorizeRequest;
import com.evbox.everon.ocpp.v20.message.BootNotificationRequest;
import com.evbox.everon.ocpp.v20.message.BootReasonEnum;
import com.evbox.everon.ocpp.v20.message.CertificateSigningUseEnum;
import com.evbox.everon.ocpp.v20.message.ChargingNeeds;
import com.evbox.everon.ocpp.v20.message.ChargingStateEnum;
import com.evbox.everon.ocpp.v20.message.ChargingStation;
import com.evbox.everon.ocpp.v20.message.ConnectorStatusEnum;
import com.evbox.everon.ocpp.v20.message.DataTransferRequest;
import com.evbox.everon.ocpp.v20.message.EnergyTransferModeEnum;
import com.evbox.everon.ocpp.v20.message.IdToken;
import com.evbox.everon.ocpp.v20.message.IdTokenEnum;
import com.evbox.everon.ocpp.v20.message.MeasurandEnum;
import com.evbox.everon.ocpp.v20.message.MeterValue;
import com.evbox.everon.ocpp.v20.message.Modem;
import com.evbox.everon.ocpp.v20.message.NotifyEVChargingNeedsRequest;
import com.evbox.everon.ocpp.v20.message.NotifyReportRequest;
import com.evbox.everon.ocpp.v20.message.ReadingContextEnum;
import com.evbox.everon.ocpp.v20.message.ReasonEnum;
import com.evbox.everon.ocpp.v20.message.ReportData;
import com.evbox.everon.ocpp.v20.message.SampledValue;
import com.evbox.everon.ocpp.v20.message.SignCertificateRequest;
import com.evbox.everon.ocpp.v20.message.SignedMeterValue;
import com.evbox.everon.ocpp.v20.message.StatusNotificationRequest;
import com.evbox.everon.ocpp.v20.message.Transaction;
import com.evbox.everon.ocpp.v20.message.TransactionEventEnum;
import com.evbox.everon.ocpp.v20.message.TransactionEventRequest;
import com.evbox.everon.ocpp.v20.message.TriggerReasonEnum;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.evbox.everon.ocpp.simulator.message.ObjectMapperHolder.JSON_OBJECT_MAPPER;
import static com.evbox.everon.ocpp.v20.message.TransactionEventEnum.ENDED;
import static com.evbox.everon.ocpp.v20.message.TransactionEventEnum.STARTED;
import static com.evbox.everon.ocpp.v20.message.TransactionEventEnum.UPDATED;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toList;

public class PayloadFactory {

    private static final String EICHRECHT = "EICHRECHT";
    private static final String SIGNED_METER_START = "AP;0;3;ALCV3ABBBISHMA2RYEGAZE3HV5YQBQRQAEHAR2MN;BIHEIWSHAAA2W2V7OYYDCNQAAAFACRC2I4ADGAETI4AAAABAOOJYUAGMXEGV4AIAAEEAB7Y6AAO3EAIAAAAAAABQGQ2UINJZGZATGMJTGQ4DAAAAAAAAAACXAAAABKYAAAAA====;R7KGQ3CEYTZI6AWKPOA42MXJTGBW27EUE2E6X7J77J5WMQXPSOM3E27NMVM2D77DPTMO3YACIPTRI===;";
    private static final String SIGNED_METER_STOP = "AP;1;3;ALCV3ABBBISHMA2RYEGAZE3HV5YQBQRQAEHAR2MN;BIHEIWSHAAA2W2V7OYYDCNQAAAFACRC2I4ADGAETI4AAAAAQHCKIUAETXIGV4AIAAEEAB7Y6ACT3EAIAAAAAAABQGQ2UINJZGZATGMJTGQ4DAAAAAAAAAACXAAAABLAAAAAA====;HIWX6JKGDO3JARYVAQYKJO6XB7HFLMLNUUCDBOTWJFFC7IY3VWZGN7UPIVA26TMTK4S2GVXJ3BD4S===;";
    private static final String V2G = "V2G";
    private static final long V2G_START = 100000000L;
    public static final String PUBLIC_KEY = "30 5A 30 14 06 07 2A 86 48 CE 3D 02 01 06 09 2B 24 03 03 02 08 01 01 07 03 42 00 04 6F 04 84 E2 06 4D 66 2C 66 0D B5 FE 7F AA 29 2A 42 B6 AF 02 81 B1 96 1C 87 CA 57 E1 43 8E C7 35 26 DA D8 48 6D C9 FB 5A 56 B7 35 A5 41 DD 69 A2 96 8D 10 82 7B 4F 98 9F C2 74 26 A2 24 AF 9E 0F";
    public static final CiString.CiString255 GENERAL_CONFIGURATION = new CiString.CiString255("generalConfiguration");
    public static final CiString.CiString50 SET_METER_CONFIGURATION = new CiString.CiString50("setMeterConfiguration");

    SignCertificateRequest createSignCertificateRequest(String csr) {
        return new SignCertificateRequest()
                .withCsr(new CiString.CiString5500(csr))
                .withCertificateType(CertificateSigningUseEnum.CHARGING_STATION_CERTIFICATE);
    }

    AuthorizeRequest createAuthorizeRequest(String tokenId, List<Integer> evseIds) {
        AuthorizeRequest payload = new AuthorizeRequest();

//        if (!evseIds.isEmpty()) {
//            payload.setEvseId(evseIds);
//        }

        IdToken idToken = new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(IdTokenEnum.ISO_14443);
        payload.setIdToken(idToken);

        return payload;
    }

    StatusNotificationRequest createStatusNotification(int evseId, int connectorId, CableStatus cableStatus, Instant currentTime) {
        if (cableStatus == CableStatus.UNPLUGGED) {
            return createStatusNotification(evseId, connectorId, ConnectorStatusEnum.AVAILABLE, currentTime);
        }

        return createStatusNotification(evseId, connectorId, ConnectorStatusEnum.OCCUPIED, currentTime);
    }

    StatusNotificationRequest createStatusNotification(int evseId, int connectorId, ConnectorStatusEnum connectorStatus, Instant currentTime) {
        return new StatusNotificationRequest()
                    .withEvseId(evseId)
                    .withConnectorId(connectorId)
                    .withConnectorStatus(connectorStatus)
                    .withTimestamp(currentTime.atZone(ZoneOffset.UTC));
    }

    StatusNotificationRequest createStatusNotification(Evse evse, Connector connector, Instant currentTime) {
        StatusNotificationRequest payload = new StatusNotificationRequest();
        payload.setEvseId(evse.getId());
        payload.setConnectorId(connector.getId());
        payload.setConnectorStatus(connector.getConnectorStatus());
        payload.setTimestamp(currentTime.atZone(ZoneOffset.UTC));

        return payload;
    }

    BootNotificationRequest createBootNotification(BootReasonEnum reason) {
        Modem modem = new Modem();
        modem.setIccid(new CiString.CiString20(StationHardwareData.MODEM_ICCID));
        modem.setImsi(new CiString.CiString20(StationHardwareData.MODEM_IMSI));

        ChargingStation chargingStation = new ChargingStation()
                .withModem(modem);

        chargingStation.setVendorName(new CiString.CiString50(StationHardwareData.VENDOR_NAME));
        chargingStation.setModel(new CiString.CiString20(StationHardwareData.MODEL));
        chargingStation.setSerialNumber(new CiString.CiString25(StationHardwareData.SERIAL_NUMBER));
        chargingStation.setFirmwareVersion(new CiString.CiString50(StationHardwareData.FIRMWARE_VERSION));

        return new BootNotificationRequest()
                .withReason(reason)
                .withChargingStation(chargingStation);
    }

    TransactionEventRequest createTransactionEventStart(String stationId, Evse evse, Integer connectorId, TriggerReasonEnum reason, String tokenId,
                                                        ChargingStateEnum chargingState, Integer remoteStartId,  Instant currentDateTime) {

        Transaction transactionData = new Transaction()
                .withTransactionId(new CiString.CiString36(evse.getTransaction().toString()))
                .withRemoteStartId(remoteStartId)
                .withChargingState(chargingState);
        TransactionEventRequest payload = createTransactionEvent(stationId, evse.getId(), connectorId, reason, transactionData, STARTED, currentDateTime, evse.getSeqNoAndIncrement(), 0L);

        if (tokenId != null) {
            payload.setIdToken(new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(IdTokenEnum.ISO_14443));
        }

        return payload;
    }

    NotifyEVChargingNeedsRequest createNotifyEVChargingNeeds(Integer evseId){
        return new NotifyEVChargingNeedsRequest()
                .withEvseId(evseId)
                .withChargingNeeds(new ChargingNeeds().withRequestedEnergyTransfer(EnergyTransferModeEnum.AC_SINGLE_PHASE));

    }

    TransactionEventRequest createTransactionEventUpdate(String stationId, Evse evse, Integer connectorId, TriggerReasonEnum reason, String tokenId,
                                                         ChargingStateEnum chargingState, Instant currentDateTime, long powerConsumed) {

        Transaction transactionData = new Transaction()
                .withTransactionId(new CiString.CiString36(evse.getTransaction().getTransactionId()))
                .withChargingState(chargingState);

        TransactionEventRequest payload = createTransactionEvent(stationId, evse.getId(), connectorId, reason, transactionData, UPDATED, currentDateTime, evse.getSeqNoAndIncrement(), powerConsumed);

        if (tokenId != null) {
            payload.setIdToken(new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(IdTokenEnum.ISO_14443));
        }

        return payload;
    }

    TransactionEventRequest createTransactionEventEnded(String stationId, Evse evse, Integer connectorId, TriggerReasonEnum reason,
                                                        ReasonEnum stoppedReason, Instant currentDateTime, long powerConsumed) {

        Transaction transactionData = new Transaction().withTransactionId(new CiString.CiString36(evse.getTransaction().toString()))
                .withChargingState(ChargingStateEnum.SUSPENDED_EVSE)
                .withStoppedReason(stoppedReason);

        return createTransactionEvent(stationId, evse.getId(), connectorId, reason, transactionData, TransactionEventEnum.ENDED, currentDateTime, evse.getSeqNoAndIncrement(), powerConsumed);
    }

    NotifyReportRequest createNotifyReportRequest(@Nullable Integer requestId, boolean tbc, int seqNo, ZonedDateTime generatedAt, List<ReportData> reportData) {
        NotifyReportRequest notifyReportRequest = new NotifyReportRequest()
                .withGeneratedAt(generatedAt)
                .withReportData(reportData)
                .withSeqNo(seqNo)
                .withTbc(tbc);

        notifyReportRequest.setRequestId(requestId);

        return notifyReportRequest;
    }

    private TransactionEventRequest createTransactionEvent(String stationId, Integer evseId, Integer connectorId, TriggerReasonEnum reason, Transaction transactionData,
                                                           TransactionEventEnum eventType, Instant currentDateTime, Integer seqNo, long powerConsumed) {
        SampledValue sampledValue = new SampledValue()
//                .withSignedMeterValue(createSignedMeterValues(stationId, eventType, powerConsumed))
                .withValue(getPowerConsumed(stationId, eventType, powerConsumed))
                                        .withMeasurand(MeasurandEnum.ENERGY_ACTIVE_IMPORT_REGISTER);

        if(STARTED.equals(eventType)) {
            sampledValue.withContext(ReadingContextEnum.TRANSACTION_BEGIN);
        }

        if (ENDED.equals(eventType)) {
            sampledValue.withContext(ReadingContextEnum.TRANSACTION_END);
        }

        MeterValue meterValue = new MeterValue()
                .withTimestamp(ZonedDateTime.now(ZoneOffset.UTC))
                .withSampledValue(Collections.singletonList(sampledValue));
        List<MeterValue> meterValues = Collections.singletonList(meterValue);
        return createTransactionEvent(evseId, connectorId, reason, transactionData, eventType, currentDateTime, seqNo, meterValues);
    }

    private BigDecimal getPowerConsumed(String stationId, TransactionEventEnum eventType, long powerConsumed) {
        if (isV2GTransaction(stationId)) {
            return eventType == STARTED ? new BigDecimal(V2G_START) : new BigDecimal(V2G_START - powerConsumed);
        }
        return eventType == STARTED ? ZERO : new BigDecimal(powerConsumed);
    }

    private boolean isV2GTransaction(String stationId) {
        return stationId.toUpperCase().startsWith(V2G);
    }

    private TransactionEventRequest createTransactionEvent(Integer evseId, Integer connectorId, TriggerReasonEnum reason, Transaction transactionData,
                                                           TransactionEventEnum eventType, Instant currentDateTime, Integer seqNo, List<MeterValue> meterValues) {
        TransactionEventRequest transaction = new TransactionEventRequest();
        transaction.setEventType(eventType);
        transaction.setTimestamp(currentDateTime.atZone(ZoneOffset.UTC));
        transaction.setTriggerReason(reason);
        transaction.setSeqNo(seqNo);
        transaction.setTransactionInfo(transactionData);
        transaction.setEvse(new com.evbox.everon.ocpp.v20.message.EVSE().withId(evseId).withConnectorId(connectorId));
        transaction.setMeterValue(meterValues);
        return transaction;
    }

//    private SignedMeterValue createSignedMeterValues(String stationId, TransactionEventEnum eventType, Long powerConsumed) {
//        if (stationId.toUpperCase().contains(EICHRECHT) && eventType != UPDATED) {
//            SignedMeterValue signedMeterValue =  new SignedMeterValue()
//                                                    .withEncodingMethod(new CiString.CiString50("Other"))//SignedMeterValue.EncodingMethod.OTHER.toString()
////                                                     .withEncodedMeterValue(new CiString.CiString512(powerConsumed.toString()))
//                                                    .withSigningMethod(new CiString.CiString50("ECDSA192SHA256"));//SignedMeterValue.SignatureMethod.ECDSA_192_SHA_256);
//
//            if (eventType == STARTED) {
//                signedMeterValue.withMeterValueSignature(new CiString.CiString2500(SIGNED_METER_START));
//            } else {
//                signedMeterValue.withMeterValueSignature(new CiString.CiString2500(SIGNED_METER_STOP));
//            }
//
//            return signedMeterValue;
//        }
//        return null;
//    }

    DataTransferRequest createPublicKeyDataTransfer(List<Integer> evseIds) {
        return new DataTransferRequest().withVendorId(GENERAL_CONFIGURATION)
                .withMessageId(SET_METER_CONFIGURATION)
                .withData(buildPubLicKeyMeterConfig(evseIds));
    }

    @SneakyThrows
    private Object buildPubLicKeyMeterConfig(List<Integer> evseIds) {
        Map<String, List<MeterConfig>> config = Map.of("meters", evseIds.stream().map(MeterConfig::new).collect(toList()));
        return JSON_OBJECT_MAPPER.writeValueAsString(config);
    }

    @Getter
    public static class MeterConfig {
        int connectorId;
        String meterSerial;
        String type;
        String publicKey;

        public MeterConfig(int evseId) {
            this.connectorId = evseId;
            this.meterSerial = "MeterSerial" + connectorId;
            this.type = "SIGNATURE";
            this.publicKey = PUBLIC_KEY;
        }
    }
}
