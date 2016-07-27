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
package org.hyperledger.merkletree;

import org.hyperledger.common.Hash;

public class MerkleRoot extends Hash {
    public static final MerkleRoot INVALID = new MerkleRoot(new byte[32]);

    public MerkleRoot(Hash hash) {
        super(hash.unsafeGetArray());
    }

    public MerkleRoot(byte[] hash) {
        super(hash);
    }

    public MerkleRoot(String hex) {
        super(hex);
    }

    private MerkleRoot(byte[] hash, boolean safe) {
        super(hash, safe);
    }

    public static MerkleRoot createFromSafeArray(byte[] hash) {
        if (hash.length != 32) {
            throw new IllegalArgumentException("Digest length must be 32 bytes for Hash");
        }
        return new MerkleRoot(hash, true);
    }
}
