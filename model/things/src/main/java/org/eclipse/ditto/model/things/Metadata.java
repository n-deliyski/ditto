/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.model.things;

import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.base.json.Jsonifiable;

import javax.annotation.concurrent.Immutable;

/**
 * {@code Metadata} describe a {@code Thing} in more detail and can be of any type.
 */
@Immutable
public interface Metadata extends JsonObject, Jsonifiable.WithFieldSelectorAndPredicate<JsonField> {

    /**
     * Returns a new empty builder for a {@code Metadata}.
     *
     * @return the builder.
     */
    static MetadataBuilder newBuilder() {
        return MetadataModelFactory.newMetadataBuilder();
    }

    /**
     * Returns a mutable builder with a fluent API for immutable {@code Metadata}. The builder is initialised with the
     * entries of this instance.
     *
     * @return the new builder.
     */
    @Override
    default MetadataBuilder toBuilder() {
        return MetadataModelFactory.newMetadataBuilder(this);
    }

    @Override
    Metadata setValue(CharSequence key, int value);

    @Override
    Metadata setValue(CharSequence key, long value);

    @Override
    Metadata setValue(CharSequence key, double value);

    @Override
    Metadata setValue(CharSequence key, boolean value);

    @Override
    Metadata setValue(CharSequence key, String value);

    @Override
    Metadata setValue(CharSequence key, JsonValue value);

    @Override
    Metadata set(JsonField field);

    @Override
    Metadata setAll(Iterable<JsonField> jsonFields);

    @Override
    Metadata remove(CharSequence key);

    @Override
    default JsonObject toJson(final JsonSchemaVersion schemaVersion, final JsonFieldSelector fieldSelector) {
        return get(fieldSelector);
    }
}
