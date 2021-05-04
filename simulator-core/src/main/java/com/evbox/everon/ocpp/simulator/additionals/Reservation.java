package com.evbox.everon.ocpp.simulator.additionals;

import com.evbox.everon.ocpp.v20.message.ConnectorEnum;

import java.time.ZonedDateTime;

/**
 * Classes similar to 'Reservation' class from 2.0 OCPP protocol version (unfortunately it was deleted in 2.0.1 version)
 * It is used to hold information about connectors/evses reservations.
 */
public class Reservation {

    private Integer id;
    private Integer evseId;
    private Integer connectorId;
    private ConnectorEnum connectorType;
    private ZonedDateTime expiryDateTime;

    public Integer getId() {
        return id;
    }

    public Integer getEvseId() {
        return evseId;
    }

    public ConnectorEnum getConnectorType() {
        return connectorType;
    }

    public ZonedDateTime getExpiryDateTime() {
        return expiryDateTime;
    }

    public Integer getConnectorId() {
        return connectorId;
    }

    public Reservation withId(Integer id) {
        this.id = id;
        return this;
    }

    public Reservation withEvseId(Integer evseId) {
        this.evseId = evseId;
        return this;
    }

    public Reservation withConnectorId(Integer connectorId) {
        this.connectorId = connectorId;
        return this;
    }


}
