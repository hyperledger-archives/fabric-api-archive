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
package org.hyperledger.block;

import org.hyperledger.common.Hash;

/**
 * A Block or header ID
 * This is technically a cryptographic hash of the header content.
 * <p>
 * Introducing this class to allow other implementations of the ID and to
 * ensure transaction IDs are not mixed up with block/header IDs
 */
public class BID extends Hash {
    public static final BID INVALID = new BID(new byte[32]);

    public BID(Hash hash) {
        super(hash.unsafeGetArray());
    }

    public BID(byte[] hash) {
        super(hash);
    }

    private BID(byte[] hash, boolean safe) {
        super(hash, safe);
    }

    public static BID createFromSafeArray(byte[] hash) {
        if (hash.length != 32) {
            throw new IllegalArgumentException("Digest length must be 32 bytes for Hash");
        }
        return new BID(hash, true);
    }

    public BID(String hex) {
        super(hex);
    }
}
