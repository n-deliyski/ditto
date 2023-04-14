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
package org.eclipse.ditto.internal.utils.headers.conditional;

import static org.eclipse.ditto.base.model.common.ConditionChecker.checkNotNull;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.base.model.entity.Entity;
import org.eclipse.ditto.base.model.headers.DittoHeaderDefinition;
import org.eclipse.ditto.base.model.headers.IfEqualOption;
import org.eclipse.ditto.base.model.signals.WithOptionalEntity;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.json.JsonCollectors;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;

/**
 * TODO TJ doc
 * @param <C>
 */
@Immutable
public final class IfEqualPreconditionHeader<C extends Command<?>> implements PreconditionHeader<Entity<?>> {

    /**
     *
     */
    public static final String IF_EQUAL_OUTCOME_HEADER = "if-equal-outcome";

    /**
     *
     */
    public static final String COMPLETELY_EQUAL = "completely";

    /**
     *
     */
    public static final String PARTIALLY_EQUAL = "partially";

    private static final String IF_EQUAL_KEY = DittoHeaderDefinition.IF_EQUAL.getKey();

    private final C command;
    private final IfEqualOption ifEqualOption;
    private final ConditionalHeadersValidator.ValidationSettings validationSettings;

    private IfEqualPreconditionHeader(final C command, final IfEqualOption ifEqualOption,
            final ConditionalHeadersValidator.ValidationSettings validationSettings) {
        this.command = checkNotNull(command, "command");
        this.ifEqualOption = checkNotNull(ifEqualOption, "ifEqualOption");
        this.validationSettings = checkNotNull(validationSettings, "validationSettings");
    }

    /**
     * Extracts an {@link IfEqualPreconditionHeader} from the given {@code command} if present.
     *
     * @param command The command containing potentially the {@code if-equal} and the value to modify
     * @param validationSettings TODO TJ
     * @param <C> TODO TJ
     * @return Optional of {@link IfEqualPreconditionHeader}. Empty if the given {@code command} don't contain an
     * {@code if-equal} or if it is not a modifying command.
     */
    public static <C extends Command<?>> Optional<IfEqualPreconditionHeader<C>> fromDittoHeaders(
            final C command,
            final ConditionalHeadersValidator.ValidationSettings validationSettings) {

        final Command.Category category = command.getCategory();
        if (category == Command.Category.MODIFY || category == Command.Category.MERGE) {
            return command.getDittoHeaders().getIfEqual()
                    .map(ifEqual -> new IfEqualPreconditionHeader<>(command, ifEqual, validationSettings));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String getKey() {
        return IF_EQUAL_KEY;
    }

    @Override
    public String getValue() {
        return ifEqualOption.toString();
    }

    /**
     * Indicates whether this {@link IfEqualPreconditionHeader} passes for the given {@code entity}.
     * This means that the {@code command} field which is currently processed would not change the passed {@code entity},
     * so the targeted desired state change indicated by the command is already present.
     *
     * @param entity The entity for which the equality condition should be met.
     * @return True if the equality condition is met. False if not.
     */
    @Override
    public boolean meetsConditionFor(@Nullable final Entity<?> entity) {

        if (entity == null) {
            return false;
        }

        if (ifEqualOption == IfEqualOption.SKIP || ifEqualOption == IfEqualOption.UPDATE_METADATA_ONLY) {
            if (command.getCategory() == Command.Category.MODIFY &&
                    command instanceof WithOptionalEntity withOptionalEntity) {
                return withOptionalEntity.getEntity()
                        .map(newValue -> {
                            final Optional<JsonValue> previousValue = entity.toJson()
                                    .getValue(command.getResourcePath());
                            return previousValue.filter(jsonValue -> jsonValue.equals(newValue))
                                    .isPresent();
                        })
                        .orElse(false);
            } else if (command.getCategory() == Command.Category.MERGE &&
                    command instanceof WithOptionalEntity withOptionalEntity) {
                return withOptionalEntity.getEntity()
                        .map(newValue -> {
                            final Optional<JsonValue> previousValue = entity.toJson()
                                    .getValue(command.getResourcePath());
                            if (newValue.isObject()) {
                                return previousValue.filter(JsonValue::isObject)
                                        .map(JsonValue::asObject)
                                        .filter(previousObject -> {
                                            final JsonObject patchedAndSortedNewObject =
                                                    JsonFactory.mergeJsonValues(newValue.asObject(), previousObject)
                                                            .asObject().stream()
                                                            .sorted(Comparator.comparing(j -> j.getKey().toString()))
                                                            .collect(JsonCollectors.fieldsToObject());
                                            final JsonObject sortedOldObject = previousObject.stream()
                                                    .sorted(Comparator.comparing(j -> j.getKey().toString()))
                                                    .collect(JsonCollectors.fieldsToObject());
                                            return patchedAndSortedNewObject.equals(sortedOldObject);
                                        }).isPresent();
                            } else {
                                return previousValue.filter(jsonValue -> jsonValue.equals(newValue))
                                        .isPresent();
                            }
                        })
                        .orElse(false);
            } else {
                // other commands to "MODIFY" and "MERGE" do never match the "if-equal" precondition header
                return false;
            }
        } else {
            // for previous default behavior, "if-equal: update", don't match:
            return false;
        }
    }

    /**
     * TODO TJ Doc
     * @return
     */
    public C handleCommand(final BooleanSupplier isCompletelyEqualSupplier) {

        final C potentiallyAdjustedCommand;
        if (ifEqualOption == IfEqualOption.UPDATE) {
            // default behavior - no change, just use the complete modify command, not matter what:
            potentiallyAdjustedCommand = command;
        } else {
            // lazily check for equality as this might be expensive to do:
            final boolean completelyEqual = isCompletelyEqualSupplier.getAsBoolean();
            final Command.Category category = command.getCategory();
            if (completelyEqual &&
                    (category == Command.Category.MODIFY || category == Command.Category.MERGE)) {
                if (ifEqualOption == IfEqualOption.SKIP) {
                    potentiallyAdjustedCommand = respondWithNotModified();
                } else if (ifEqualOption == IfEqualOption.UPDATE_METADATA_ONLY) {
                    potentiallyAdjustedCommand = handleUpdateMetadataOnly(true);
                } else {
                    potentiallyAdjustedCommand = command;
                }
            } else if (category == Command.Category.MERGE) {
                // partially equal in the provided "merge" (patch)
                if (ifEqualOption == IfEqualOption.SKIP) {
                    // we have to forward the command in order to apply the merge only partially
                    potentiallyAdjustedCommand = insertIfEqualOutcomeHeader(false);
                } else if (ifEqualOption == IfEqualOption.UPDATE_METADATA_ONLY) {
                    potentiallyAdjustedCommand = handleUpdateMetadataOnly(false);
                } else {
                    potentiallyAdjustedCommand = command;
                }
            } else {
                potentiallyAdjustedCommand = command;
            }
        }
        return potentiallyAdjustedCommand;
    }

    private C handleUpdateMetadataOnly(final boolean completelyEqual) {
        final C potentiallyAdjustedCommand;
        if (!command.getDittoHeaders().getMetadataFieldsToGet().isEmpty() ||
                !command.getDittoHeaders().getMetadataFieldsToDelete().isEmpty() ||
                !command.getDittoHeaders().getMetadataHeadersToPut().isEmpty()
        ) {
            // we have to forward the command in order to keep only metadata
            potentiallyAdjustedCommand = insertIfEqualOutcomeHeader(completelyEqual);
        } else {
            // if no metadata was present, we can immediately return 304
            potentiallyAdjustedCommand = respondWithNotModified();
        }
        return potentiallyAdjustedCommand;
    }

    @SuppressWarnings("unchecked")
    private C insertIfEqualOutcomeHeader(final boolean completelyEqual) {
        return (C) command.setDittoHeaders(
                command.getDittoHeaders().toBuilder()
                        .putHeader(IF_EQUAL_OUTCOME_HEADER, completelyEqual ? COMPLETELY_EQUAL : PARTIALLY_EQUAL)
                        .build()
        );
    }

    private C respondWithNotModified() {
        throw validationSettings
                .createPreconditionNotModifiedForEqualityExceptionBuilder()
                .dittoHeaders(command.getDittoHeaders())
                .build();
    }

}
