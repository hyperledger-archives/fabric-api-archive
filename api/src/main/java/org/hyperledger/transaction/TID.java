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

import org.hyperledger.common.Hash;

/**
 * A Transaction ID
 * This is technically a cryptographic hash of its content.
 * Bitcoin hashes the entire transaction content that makes reference to
 * unsigned or partially signed transaction impractical.
 * <p>
 * Introducing this class to allow other implementations of the ID and to
 * ensure transaction IDs are not mixed up with block/header IDs
 */
public class TID extends Hash {
    public static final TID INVALID = new TID(new byte[32]);
    // TODO in Sidechain Elements this is the genesis block hash
    public static final TID BITCOIN_NATIVE = new TID(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1});

    public TID(Hash hash) {
        super(hash.unsafeGetArray());
    }

    public TID(byte[] hash) {
        super(hash);
    }

    public TID(String hex) {
        super(hex);
    }

    private TID(byte[] hash, boolean safe) {
        super(hash, safe);
    }

    public static TID createFromSafeArray(byte[] hash) {
        if (hash.length != 32) {
            throw new IllegalArgumentException("Digest length must be 32 bytes for Hash");
        }
        return new TID(hash, true);
    }
}
