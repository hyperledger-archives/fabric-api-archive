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

import org.hyperledger.merkletree.MerkleRoot;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

public class HyperledgerHeader implements Header {
    private BID ID;
    private final BID previousID;
    private final MerkleRoot merkleRoot;
    private final int createTime;

    public HyperledgerHeader(BID previousID, MerkleRoot merkleRoot, int createTime) {
        this.previousID = previousID;
        this.merkleRoot = merkleRoot;
        this.createTime = createTime;
    }

    public static HyperledgerHeader.Builder create() {
        return new HyperledgerHeader.Builder();
    }

    public static class Builder extends HeaderBuilder<Builder> {
        protected Builder() {
            super();
        }

        public HyperledgerHeader build() {
            return new HyperledgerHeader(previousID, merkleRoot, createTime);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }

    /**
     * The ID of the header. It is technically a cryptographic hash of the header's content.
     * For Bitcoin it is also the proof of work, see getEncodedDifficulty
     *
     * @return unique ID
     */
    @Override
    public BID getID() {
        return ID;
    }

    /**
     * The ID of the previos header, these liks create the block chain
     *
     * @return
     */
    @Override
    public BID getPreviousID() {
        return previousID;
    }

    /**
     * The merkle root of transaction within the associated block (@Link https://en.wikipedia.org/wiki/Merkle_tree)
     *
     * @return the merkle root of transaction hashes
     */
    @Override
    public MerkleRoot getMerkleRoot() {
        return merkleRoot;
    }

    /**
     * Unfortunately Satoshi used a 32bit unsigned integer for time as seconds in the Unix era.
     * This will turn negative in java's integer in 2038 (@Link https://en.wikipedia.org/wiki/Year_2038_problem)
     * and will ultimately overflow in 2106.
     * <p>
     * For above reasons, do not use this method for application purposes, but the getLocalCreateTime that
     * will ensure seemless transition as block header format eventually changes, hopefully before 2106.
     *
     * @return the time point the block was created. This is seconds in the Unix era.
     */
    @Override
    @Deprecated
    public int getCreateTime() {
        return createTime;
    }

    /**
     * @return the time point of the block was created as observed in local time
     */
    @Override
    public LocalTime getLocalCreateTime() {
        return LocalTime.from(Instant.ofEpochSecond(Integer.toUnsignedLong(createTime)).atZone(ZoneId.of("Z")));
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HyperledgerHeader header = (HyperledgerHeader) o;
        return Objects.equals(getID(), header.getID());
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    @Override
    public String toString() {
        return getID().toString();
    }
}
