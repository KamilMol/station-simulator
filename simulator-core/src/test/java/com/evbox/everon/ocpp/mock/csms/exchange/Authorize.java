package com.evbox.everon.ocpp.mock.csms.exchange;

import com.evbox.everon.ocpp.mock.factory.JsonMessageTypeFactory;
import com.evbox.everon.ocpp.simulator.message.Call;
import com.evbox.everon.ocpp.v20.message.AuthorizationStatusEnum;
import com.evbox.everon.ocpp.v20.message.AuthorizeRequest;
import com.evbox.everon.ocpp.v20.message.AuthorizeResponse;
import com.evbox.everon.ocpp.v20.message.IdTokenEnum;
import com.evbox.everon.ocpp.v20.message.IdTokenInfo;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.evbox.everon.ocpp.mock.csms.exchange.Common.equalsType;
import static com.evbox.everon.ocpp.simulator.message.ActionType.AUTHORIZE;

public class Authorize {

    /**
     * Authorize request with given tokenId and tokenType.
     *
     * @param type    type of id token
     * @param tokenId auth token id
     * @return checks whether an incoming request is AuthorizeRequest or not.
     */
    public static Predicate<Call> request(String tokenId, IdTokenEnum type) {
        return request -> equalsType(request, AUTHORIZE) && equalsTokenId(request, tokenId) && equalsTokenType(request, type);
    }

    /**
     * Create AuthorizeResponse with default configuration.
     *
     * @return response in json.
     */
    public static Function<Call, String> response() {
        return request -> JsonMessageTypeFactory.createCallResult()
                .withMessageId(request.getMessageId())
                .withPayload(new AuthorizeResponse().withIdTokenInfo(new IdTokenInfo().withStatus(AuthorizationStatusEnum.ACCEPTED)))
                .toJson();
    }

    private static boolean equalsTokenId(Call request, String tokenId) {
        return ((AuthorizeRequest) request.getPayload()).getIdToken().getIdToken().toString().equals(tokenId);
    }

    private static boolean equalsTokenType(Call request, IdTokenEnum type) {
        return ((AuthorizeRequest) request.getPayload()).getIdToken().getType() == type;
    }
}
