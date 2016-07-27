/**
 * Copyright 2016 Digital Asset Holdings, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hyperledger.transaction;

import org.hyperledger.common.BouncyCastleCrypto;
import org.hyperledger.common.Cryptography;
import org.hyperledger.common.PrivateKey;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.*;

public class TransactionTest {

    private static final Random random = new Random();
    private static final Cryptography crypto = new BouncyCastleCrypto();

    @Test
    public void serialization() throws IOException {
        Transaction original = randomTx();
        byte[] serialized = original.toByteArray();
        Transaction result = Transaction.fromByteArray(serialized);

        assertEquals(original, result);
    }

    public static Transaction randomTx() {
        return new TransactionBuilder()
                .input(new TID(randomBytes(32)))
                .output(randomBytes(100))
                .endorse(PrivateKey.createNew(crypto))
                .build();
    }

    private static byte[] randomBytes(int length) {
        byte[] payload = new byte[length];
        random.nextBytes(payload);
        return payload;
    }

    @Test
    public void signatureValidationSucceeds() {
        PrivateKey key = PrivateKey.createNew(crypto);

        Transaction t = new TransactionBuilder()
                .output(randomBytes(100))
                .endorse(key)
                .build();

        assertTrue(t.verify(t.getEndorsers().get(0), key.getPublic()));
    }

    @Test
    public void signatureValidationFails() {
        PrivateKey key1 = PrivateKey.createNew(crypto);
        PrivateKey key2 = PrivateKey.createNew(crypto);

        Transaction t = new TransactionBuilder()
                .output(randomBytes(100))
                .endorse(key1)
                .build();

        assertFalse(t.verify(t.getEndorsers().get(0), key2.getPublic()));
    }

}
