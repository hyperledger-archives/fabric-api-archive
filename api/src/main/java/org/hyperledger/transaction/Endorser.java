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

import org.hyperledger.common.PrivateKey;
import org.hyperledger.common.PublicKey;

public class Endorser {

    private final byte[] signature;

    public Endorser(byte[] signature) {
        this.signature = signature;
    }

    public static Endorser create(byte[] hash, PrivateKey key) {
        return new Endorser(key.sign(hash));
    }

    public boolean verify(byte[] hash, PublicKey key) {
        return key.verify(hash, signature);
    }

    public byte[] getSignature() {
        return signature;
    }
}
