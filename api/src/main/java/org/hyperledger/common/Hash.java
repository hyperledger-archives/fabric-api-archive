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

import org.hyperledger.block.BID;
import org.hyperledger.block.HyperledgerHeader;
import org.hyperledger.transaction.TID;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;

/**
 * A Hash identifies objects, that is blocks and transactions, in the ledger.
 * Technically it is a double SHA256 digest of the object's content.
 *
 * @see TID
 * @see BID
 */
public class Hash {
    public static final Hash INVALID = Hash.createFromSafeArray(new byte[32]);

    private final byte[] bytes;

    /**
     * create a Hash from a digest
     *
     * @param hash - digest must be 32 bytes long
     */
    public Hash(byte[] hash) {
        if (hash.length != 32) {
            throw new IllegalArgumentException("Digest length must be 32 bytes for Hash");
        }
        this.bytes = new byte[32];
        System.arraycopy(hash, 0, this.bytes, 0, 32);
    }

    /**
     * create a Hash from a hexadecimal representation of the digest
     * Note that this is in the reverse byte order of the internal binary representation.
     *
     * @param hex - a digest as a 64 character hexadecimal sequence in reverse byte order.
     */
    public Hash(String hex) {
        if (hex.length() != 64) {
            throw new IllegalArgumentException("Digest length must be 64 hex characters for Hash");
        }

        this.bytes = ByteUtils.reverse(ByteUtils.fromHex(hex));
    }

    /**
     * An unsafe constructor of a Hash from a byte array. Unsafe as it does not copy the
     * array but keeps reference to it. It is only used in TID and BID
     *
     * @param hash - a 32 byte digest
     * @param safe - a dummy paramater to distinguish this unsafe constructor from the safe one.
     * @see TID
     * @see BID
     */
    protected Hash(byte[] hash, boolean safe) {
        bytes = hash;
    }

    /**
     * Unsafe get of the internal byte array. Use this with extreme care and only if you are sure
     * that the content will not be modified as that would lead to hard to locate bugs by shared mutable data.
     * The only reason this method exists is that the code uses lots of Hashes avoiding unnecesary copy
     * of their content significantly reduces memory footprint and increases performance. There is a risk
     * tradoff however.
     *
     * @return internal representation of Hash. DO NOT ALTER
     */
    public byte[] unsafeGetArray() {
        return bytes;
    }

    /**
     * Unsafe constructor of a Hash. Use this with extreme care and only if you are sure
     * that the byte array in parameter will not be modified elswhere after calling this as that would lead to hard
     * to locate bugs by shared mutable data.
     * The only reason this method exists is that the code uses lots of Hashes avoiding unnecessary copy
     * of their content significantly reduces memory footprint and increases performance. There is a risk
     * tradeoff however.
     *
     * @param hash a digest. DO NOT ALTER the array after creating a Hash with it.
     * @return a new Hash that shares internal representation with the parameter byte array.
     */
    public static Hash createFromSafeArray(byte[] hash) {
        if (hash.length != 32) {
            throw new IllegalArgumentException("Digest length must be 32 bytes for Hash");
        }
        return new Hash(hash, true);
    }

    /**
     * Merge two Hashes into one for Merkle Tree calculation
     *
     * @param a - a Hash
     * @param b - another Hash
     * @return SHA256(SHA256(a||b))
     */
    public static Hash merge(Hash a, Hash b) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(a.bytes);
            return Hash.createFromSafeArray(digest.digest(digest.digest(b.bytes)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * SHA256 hash of arbitrary data
     *
     * @param data   arbitary data
     * @param offset start hashing at this offset (0 starts)
     * @param len    hash len number of bytes
     * @return SHA256(data)
     */
    public static byte[] hash(byte[] data, int offset, int len) {
        try {
            MessageDigest a = MessageDigest.getInstance("SHA-256");
            a.update(data, offset, len);
            return a.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * SHA256 hash of arbitrary data
     *
     * @param data arbitrary data
     * @return SHA256(data)
     */
    public static byte[] hash(byte[] data) {
        return hash(data, 0, data.length);
    }

    /**
     * Create a Hash with double SHA256 hash of arbitrary data
     *
     * @param data arbitrary data
     * @return a Hash initialized with SHA256(SHA256(data))
     */
    public static Hash of(byte[] data) {
        return new Hash(hash(data, 0, data.length));
    }

    /**
     * UUID created from the first 128 bits of SHA256
     *
     * @return String
     */
    public String toUuidString() {
            String result = String.join("-", contentAsHex(0, 4), contentAsHex(4, 6),
                                             contentAsHex(6, 8), contentAsHex(8, 10),
                                             contentAsHex(10, 16));
            return result.toLowerCase();
    }

    /**
     * Safe access to the digest stored in a Hash
     *
     * @return a copy of the internal digest in Hash
     */
    public byte[] toByteArray() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Convert a Hash into a big positive integer. See HyperledgerHeader for its use for proof-of-work.
     *
     * @return Hash as big positive integer
     * @see HyperledgerHeader
     */
    public BigInteger toBigInteger() {
        return new BigInteger(1, ByteUtils.reverse(toByteArray()));
    }

    @Override
    public String toString() {
        return ByteUtils.toHex(toByteArray());
    }

    @Override
    public int hashCode() {
        return (((bytes[0] & 0xff) << 8 | (bytes[1] & 0xff)) << 8 | (bytes[2] & 0xff)) << 8 | (bytes[3] & 0xff);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hash hash = (Hash) o;

        if (!Arrays.equals(bytes, hash.bytes)) return false;

        return true;
    }

    private String contentAsHex(int i, int j) {
        return ByteUtils.toHex((Arrays.copyOfRange(bytes, i, j)));
    }
}
