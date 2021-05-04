package com.evbox.everon.ocpp.mock.factory;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.v20.message.AttributeEnum;
import com.evbox.everon.ocpp.v20.message.AuthorizeRequest;
import com.evbox.everon.ocpp.v20.message.Component;
import com.evbox.everon.ocpp.v20.message.EVSE;
import com.evbox.everon.ocpp.v20.message.GetVariableData;
import com.evbox.everon.ocpp.v20.message.GetVariableResult;
import com.evbox.everon.ocpp.v20.message.GetVariableStatusEnum;
import com.evbox.everon.ocpp.v20.message.GetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.GetVariablesResponse;
import com.evbox.everon.ocpp.v20.message.IdToken;
import com.evbox.everon.ocpp.v20.message.IdTokenEnum;
import com.evbox.everon.ocpp.v20.message.MeterValue;
import com.evbox.everon.ocpp.v20.message.RequestStartTransactionRequest;
import com.evbox.everon.ocpp.v20.message.ResetEnum;
import com.evbox.everon.ocpp.v20.message.ResetRequest;
import com.evbox.everon.ocpp.v20.message.SampledValue;
import com.evbox.everon.ocpp.v20.message.SetVariableData;
import com.evbox.everon.ocpp.v20.message.SetVariableResult;
import com.evbox.everon.ocpp.v20.message.SetVariableStatusEnum;
import com.evbox.everon.ocpp.v20.message.SetVariablesRequest;
import com.evbox.everon.ocpp.v20.message.SetVariablesResponse;
import com.evbox.everon.ocpp.v20.message.Transaction;
import com.evbox.everon.ocpp.v20.message.TransactionEventEnum;
import com.evbox.everon.ocpp.v20.message.TransactionEventRequest;
import com.evbox.everon.ocpp.v20.message.TriggerReasonEnum;
import com.evbox.everon.ocpp.v20.message.Variable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;

import static java.util.Collections.singletonList;

public class OcppMessageFactory {

    public static GetVariablesRequestBuilder createGetVariablesRequest() {
        return new GetVariablesRequestBuilder();
    }

    public static SetVariablesRequestBuilder createSetVariablesRequest() {
        return new SetVariablesRequestBuilder();
    }

    public static ResetRequestBuilder createResetRequest() {
        return new ResetRequestBuilder();
    }
    public static RequestStartTransactionRequestBuilder createRequestStartTransactionBuilder() {
        return new RequestStartTransactionRequestBuilder();
    }

    public static GetVariablesResponseBuilder createGetVariablesResponse() {
        return new GetVariablesResponseBuilder();
    }

    public static SetVariablesResponseBuilder createSetVariablesResponse() {
        return new SetVariablesResponseBuilder();
    }

    public static TransactionEventBuilder createTransactionEventRequest() {
        return new TransactionEventBuilder();
    }

    public static AuthorizeRequestBuilder createAuthorizeRequest() {
        return new AuthorizeRequestBuilder();
    }

    public static class GetVariablesRequestBuilder extends SkeletonBuilder<GetVariablesRequestBuilder> {

        public GetVariablesRequest build() {

            GetVariableData getVariableDatum = new GetVariableData();
            getVariableDatum.setComponent(component);
            getVariableDatum.setVariable(variable);
            GetVariablesRequest getVariablesRequest = new GetVariablesRequest();
            getVariablesRequest.setGetVariableData(singletonList(getVariableDatum));

            return getVariablesRequest;
        }
    }

    public static class SetVariablesRequestBuilder extends SkeletonBuilder<SetVariablesRequestBuilder> {

        private CiString.CiString1000 attributeValue;
        private AttributeEnum attributeType = AttributeEnum.ACTUAL;

        public SetVariablesRequestBuilder withAttributeValue(String attributeValue) {
            this.attributeValue = new CiString.CiString1000(attributeValue);
            return this;
        }

        public SetVariablesRequestBuilder withAttributeType(AttributeEnum attributeType) {
            this.attributeType = attributeType;
            return this;
        }

        public SetVariablesRequest build() {
            SetVariableData SetVariableData = new SetVariableData();
            SetVariableData.setComponent(component);
            SetVariableData.setVariable(variable);
            SetVariableData.setAttributeValue(attributeValue);
            SetVariableData.setAttributeType(attributeType);
            SetVariablesRequest setVariablesRequest = new SetVariablesRequest();
            setVariablesRequest.setSetVariableData(singletonList(SetVariableData));

            return setVariablesRequest;
        }
    }

    public static class ResetRequestBuilder {

        private ResetEnum type;

        public ResetRequestBuilder withType(ResetEnum type) {
            this.type = type;
            return this;
        }

        public ResetRequest build() {
            ResetRequest resetRequest = new ResetRequest();
            resetRequest.setType(type);
            return resetRequest;
        }
    }

    public static class RequestStartTransactionRequestBuilder {

        private Integer evseId;
        private Integer remoteStartId;
        private IdToken idToken;

        public RequestStartTransactionRequestBuilder withEvseId(Integer evseId) {
            this.evseId = evseId;
            return this;
        }

        public RequestStartTransactionRequestBuilder withRemoteStartId(Integer remoteStartId) {
            this.remoteStartId = remoteStartId;
            return this;
        }

        public RequestStartTransactionRequestBuilder withIdToken(IdToken idToken) {
            this.idToken = idToken;
            return this;
        }

        public RequestStartTransactionRequest build() {
            RequestStartTransactionRequest request = new RequestStartTransactionRequest();
            request.setEvseId(evseId);
            request.setIdToken(idToken);
            request.setRemoteStartId(remoteStartId);
            return request;
        }
    }

    public static class GetVariablesResponseBuilder extends SkeletonBuilder<GetVariablesResponseBuilder> {

        private GetVariableStatusEnum attributeStatus;
        private CiString.CiString2500 attributeValue;

        public GetVariablesResponseBuilder withAttributeStatus(GetVariableStatusEnum attributeStatus) {
            this.attributeStatus = attributeStatus;
            return this;
        }

        public GetVariablesResponseBuilder withAttributeValue(String attributeValue) {
            this.attributeValue = new CiString.CiString2500(attributeValue);
            return this;
        }

        public GetVariablesResponse build() {

            return new GetVariablesResponse().withGetVariableResult(
                    singletonList(new GetVariableResult()
                            .withComponent(component)
                            .withVariable(variable)
                            .withAttributeStatus(attributeStatus)
                            .withAttributeValue(attributeValue)
                    )
            );

        }

    }

    public static class SetVariablesResponseBuilder extends SkeletonBuilder<SetVariablesResponseBuilder> {

        private SetVariableStatusEnum attributeStatus;

        public SetVariablesResponseBuilder withAttributeStatus(SetVariableStatusEnum attributeStatus) {
            this.attributeStatus = attributeStatus;
            return this;
        }

        public SetVariablesResponse build() {

            return new SetVariablesResponse().withSetVariableResult(
                    singletonList(new SetVariableResult()
                            .withComponent(component)
                            .withVariable(variable)
                            .withAttributeStatus(attributeStatus))
            );

        }

    }

    public static class TransactionEventBuilder {

        private TransactionEventEnum eventType;
        private int sampledValue;
        private Instant meterValueTimestamp;
        private Instant timestamp;
        private TriggerReasonEnum triggerReason;
        private int seqNo;
        private String transactionId;
        private int evseId;
        private String tokenId;
        private IdTokenEnum tokenType;

        public TransactionEventBuilder withEventType(TransactionEventEnum eventType) {
            this.eventType = eventType;
            return this;
        }

        public TransactionEventBuilder withSampledValue(int sampledValue) {
            this.sampledValue = sampledValue;
            return this;
        }

        public TransactionEventBuilder withMeterValueTimestamp(Instant meterValueTimestamp) {
            this.meterValueTimestamp = meterValueTimestamp;
            return this;
        }

        public TransactionEventBuilder withTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public TransactionEventBuilder withTriggerReason(TriggerReasonEnum triggerReason) {
            this.triggerReason = triggerReason;
            return this;
        }

        public TransactionEventBuilder withSeqNo(int seqNo) {
            this.seqNo = seqNo;
            return this;
        }

        public TransactionEventBuilder withTransactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public TransactionEventBuilder withEvseId(int evseId) {
            this.evseId = evseId;
            return this;
        }

        public TransactionEventBuilder withTokenId(String tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        public TransactionEventBuilder withTokenType(IdTokenEnum tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public TransactionEventRequest build() {

            MeterValue meterValue = new MeterValue()
                    .withSampledValue(singletonList(new SampledValue().withValue(BigDecimal.valueOf(sampledValue))))
                    .withTimestamp(meterValueTimestamp.atZone(ZoneOffset.UTC));

            IdToken idToken = new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(tokenType);

            return new TransactionEventRequest()
                    .withEventType(eventType)
                    .withMeterValue(Collections.singletonList(meterValue))
                    .withTimestamp(timestamp.atZone(ZoneOffset.UTC))
                    .withTriggerReason(triggerReason)
                    .withSeqNo(seqNo)
                    .withTransactionInfo(new Transaction().withTransactionId(new CiString.CiString36(transactionId)))
                    .withEvse(new EVSE().withId(evseId))
                    .withIdToken(idToken);

        }

    }

    public static class AuthorizeRequestBuilder {

        private String tokenId;
        private int evseId;

        public AuthorizeRequestBuilder withTokenId(String tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        public AuthorizeRequestBuilder withEvseId(int evseId) {
            this.evseId = evseId;
            return this;
        }

        public AuthorizeRequest build() {
            return new AuthorizeRequest()
                    .withIdToken(new IdToken().withIdToken(new CiString.CiString36(tokenId)).withType(IdTokenEnum.ISO_14443))
//                    .withEvseId(Collections.singletonList(evseId))
                    ;
        }
    }

    private static class SkeletonBuilder<T> {

        Component component;
        Variable variable;

        public T withComponent(String name) {
            this.component = new Component().withName(new CiString.CiString50(name));
            return (T) this;
        }

        public T withVariable(String name) {
            this.variable = new Variable().withName(new CiString.CiString50(name));
            return (T) this;
        }
    }
}
