/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.services.connectivity.messaging.persistence;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.ditto.model.connectivity.ConnectionId;

/**
 * provides the priority of a given connection.
 */
@FunctionalInterface
public interface ConnectionPriorityProvider {

    /**
     * provides the priority of a given connection.
     *
     * @param connectionId the ID of the connection for which the priority should be returned.
     * @param correlationId the correlation ID.
     * @return the priority.
     */
    CompletionStage<Optional<Integer>> getPriorityFor(ConnectionId connectionId, String correlationId);

    default ConnectionPriorityProvider withFallBack(final ConnectionPriorityProvider fallBack) {
        return (connectionId, correlationId) -> this.getPriorityFor(connectionId, correlationId)
                .thenCompose(result -> {
                    if (result.isPresent()) {
                        return CompletableFuture.completedFuture(result);
                    } else {
                        return fallBack.getPriorityFor(connectionId, correlationId);
                    }
                });
    }

}
