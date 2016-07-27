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

package org.hyperledger.common;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * An EC public Key suitable for verifying a signature created with the corresponding EC PrivateKey
 *
 * @see PrivateKey
 */
public class PublicKey implements Key {
    private final Cryptography crypto;
    private final byte[] pub;

    /**
     * Create from uncompressed binary representation
     */
    public PublicKey(byte[] pub, Cryptography crypto) {
        this.pub = pub;
        this.crypto = crypto;
    }

    public static PublicKey fromCompressed(byte[] pub, Cryptography crypto) {
        return new PublicKey(crypto.uncompressPoint(pub), crypto);
    }

    @Override
    public byte[] toByteArray() {
        return pub.clone();
    }

    /**
     * verify a signature created with the private counterpart of this key
     *
     * @param hash      arbitrary data
     * @param signature signature
     * @return true if valid
     */
    public boolean verify(byte[] hash, byte[] signature) {
        return crypto.verify(hash, signature, pub);
    }

    @Override
    public PublicKey offsetKey(BigInteger offset) {
        byte[] atOffset = crypto.getPublicKeyAtOffset(pub, offset.toByteArray());
        return fromCompressed(atOffset, crypto);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pub);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PublicKey) {
            PublicKey other = (PublicKey) obj;
            return Arrays.equals(this.pub, other.pub);
        }
        return false;
    }

    @Override
    public String toString() {
        return ByteUtils.toHex(pub);
    }

}
