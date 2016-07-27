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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * An EC Private Key, technically a big positive integer capable of signing such that the signature can be verified
 * with the corresponding EC Public Key
 *
 * @see PublicKey
 */
public class PrivateKey implements Key {
    private static final Logger log = LoggerFactory.getLogger(PrivateKey.class);

    private final byte[] priv;
    private final Cryptography crypto;
    private PublicKey publicKey = null; // lazy initialization

    /**
     * Create from uncompressed binary representation
     */
    private PrivateKey(byte[] priv, Cryptography crypto) {
        this.priv = priv;
        this.crypto = crypto;
    }

    public static PrivateKey createNew(Cryptography crypto) {
        return new PrivateKey(crypto.createNewPrivateKey(), crypto);
    }

    @Override
    public byte[] toByteArray() {
        return priv.clone();
    }

    /**
     * @return the corresponding public key
     */
    public PublicKey getPublic() {
        if (publicKey == null) {
            publicKey = new PublicKey(crypto.getPublicFor(priv), crypto);
        }
        return publicKey;
    }

    /**
     * Sign a digest with this key.
     *
     * @param hash arbitrary data
     * @return signature
     */
    public byte[] sign(byte[] hash) {
        return crypto.sign(hash, toByteArray());
    }

    @Override
    public String toString() {
        return "private key of " + getPublic();
    }

    @Override
    public PrivateKey offsetKey(BigInteger offset) {
        return new PrivateKey(crypto.getPrivateKeyAtOffset(priv, offset.toByteArray()), crypto);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(priv);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PrivateKey) {
            PrivateKey other = (PrivateKey) obj;
            return Arrays.equals(priv, other.priv);
        }
        return false;
    }
}
