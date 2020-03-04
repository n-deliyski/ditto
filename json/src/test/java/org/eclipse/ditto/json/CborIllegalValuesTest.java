/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.json;

import static org.eclipse.ditto.json.assertions.DittoJsonAssertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;

public final class CborIllegalValuesTest {

    @Test
    public void binaryValue() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final CBORGenerator generator = new CBORFactory().createGenerator(byteArrayOutputStream);
        generator.writeBinary(new byte[]{(byte) 0x42, (byte) 0x8, (byte) 0x15});
        generator.close();

        final byte[] array = byteArrayOutputStream.toByteArray();
        boolean exceptionThrown = false;
        try {
            CborFactory.readFrom(array);
        } catch (final JsonParseException e) {
            exceptionThrown = true;
            assertThat(e)
                    .withFailMessage("offending Object not included in error")
                    .hasMessageContaining("420815");
        }
        assertThat(exceptionThrown).isTrue();
    }
}
