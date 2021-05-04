package com.evbox.everon.ocpp.mock.factory;

import com.evbox.everon.ocpp.mock.constants.StationConstants;
import com.evbox.everon.ocpp.simulator.station.evse.CableStatus;
import com.evbox.everon.ocpp.simulator.station.evse.Connector;
import com.evbox.everon.ocpp.simulator.station.evse.Evse;
import com.evbox.everon.ocpp.simulator.station.evse.EvseStatus;
import com.evbox.everon.ocpp.simulator.station.evse.EvseTransaction;
import com.evbox.everon.ocpp.v20.message.ConnectorStatusEnum;

import java.util.Collections;

public class EvseCreator {

    public static final Evse DEFAULT_EVSE_INSTANCE = createEvse()
            .withId(StationConstants.DEFAULT_EVSE_ID)
            .withStatus(EvseStatus.AVAILABLE)
            .withConnectorId(StationConstants.DEFAULT_CONNECTOR_ID)
            .withCableStatus(CableStatus.UNPLUGGED)
            .withConnectorStatus(ConnectorStatusEnum.AVAILABLE)
            .withTransaction(new EvseTransaction(StationConstants.DEFAULT_TRANSACTION_ID))
            .build();

    public static EvseBuilder createEvse() {
        return new EvseBuilder();
    }

    public static class EvseBuilder {

        private int id;
        private EvseStatus evseStatus;
        private int connectorId;
        private CableStatus cableStatus;
        private EvseTransaction evseTransaction;
        private ConnectorStatusEnum connectorStatus;

        public EvseBuilder withId(int id) {
            this.id = id;
            return this;
        }

        public EvseBuilder withStatus(EvseStatus evseStatus) {
            this.evseStatus = evseStatus;
            return this;
        }

        public EvseBuilder withConnectorId(int connectorId) {
            this.connectorId = connectorId;
            return this;
        }

        public EvseBuilder withCableStatus(CableStatus cableStatus) {
            this.cableStatus = cableStatus;
            return this;
        }

        public EvseBuilder withConnectorStatus(ConnectorStatusEnum connectorStatus) {
            this.connectorStatus = connectorStatus;
            return this;
        }

        public EvseBuilder withTransaction(EvseTransaction evseTransaction) {
            this.evseTransaction = evseTransaction;
            return this;
        }

        public Evse build() {
            return new Evse(id, evseStatus, evseTransaction, Collections.singletonList(new Connector(connectorId, cableStatus, connectorStatus)));
        }
    }
}
