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
package org.hyperledger.api;

import org.hyperledger.block.BID;
import org.hyperledger.block.Header;
import org.hyperledger.block.HeaderBuilder;
import org.hyperledger.block.HyperledgerHeader;
import org.hyperledger.merkletree.MerkleRoot;

import java.time.LocalTime;

public class HLAPIHeader implements Header {
    private final int height;
    private final Header header;

    public HLAPIHeader(Header header, int height) {
        this.header = header;
        this.height = height;
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder extends HeaderBuilder<Builder> {
        private int height = 0;

        private Builder() {
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public HLAPIHeader build() {
            return new HLAPIHeader(new HyperledgerHeader(previousID, merkleRoot, createTime), height);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }

    public int getHeight() {
        return height;
    }


    @Override
    public BID getID() {
        return header.getID();
    }

    @Override
    public BID getPreviousID() {
        return header.getPreviousID();
    }

    @Override
    public MerkleRoot getMerkleRoot() {
        return header.getMerkleRoot();
    }

    @Deprecated
    @Override
    public int getCreateTime() {
        return header.getCreateTime();
    }

    @Override
    public LocalTime getLocalCreateTime() {
        return header.getLocalCreateTime();
    }
}
