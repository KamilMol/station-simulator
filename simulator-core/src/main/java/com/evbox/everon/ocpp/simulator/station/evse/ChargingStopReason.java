package com.evbox.everon.ocpp.simulator.station.evse;

import com.evbox.everon.ocpp.v20.message.ReasonEnum;

/**
 * Reason why charging in EVSE stopped.
 *
 * LOCALLY_STOPPED corresponds to charge locally stopped by user/token
 * REMOTELY_STOPPED corresponds to charge stopped remotely
 */
public enum ChargingStopReason {
    NONE(null),
    LOCALLY_STOPPED(ReasonEnum.EV_DISCONNECTED),
    REMOTELY_STOPPED(ReasonEnum.REMOTE);

    ReasonEnum stoppedReason;

    ChargingStopReason(ReasonEnum stoppedReason) {
        this.stoppedReason = stoppedReason;
    }

    public ReasonEnum getStoppedReason() {
        return stoppedReason;
    }
}
