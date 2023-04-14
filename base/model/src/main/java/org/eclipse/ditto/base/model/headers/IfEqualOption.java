/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.base.model.headers;

import java.util.Arrays;
import java.util.Optional;

/**
 * Possible options for Ditto's {@code if-equal} header.
 *
 * @since 3.3.0
 */
public enum IfEqualOption {

    /**
     * Option which updates a value, even if the value is the same (via {@code equal()}) than the value before.
     * This is the default if omitted and for backwards compatibility.
     */
    UPDATE("update"),

    /**
     * Option which will skip the update of a twin if the new value is the same (via {@code equal()}) than the value
     * before.
     */
    SKIP("skip"),

    /**
     * Option which will skip the update of a twin if the new value is the same (via {@code equal()}) than the value
     * before, but which will update potentially provided metadata.
     */
    UPDATE_METADATA_ONLY("update-metadata-only");

    private final String option;

    IfEqualOption(final String option) {
        this.option = option;
    }

    @Override
    public String toString() {
        return option;
    }

    /**
     * Find an If-Equal option by a provided option string.
     *
     * @param option the option.
     * @return the option with the given option string if any exists.
     */
    public static Optional<IfEqualOption> forOption(final String option) {
        return Arrays.stream(values())
                .filter(strategy -> strategy.toString().equals(option))
                .findAny();
    }
}
