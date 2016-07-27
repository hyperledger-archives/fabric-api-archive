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
import org.hyperledger.block.Block;
import org.hyperledger.block.Header;
import org.hyperledger.block.HyperledgerHeader;
import org.hyperledger.merkletree.MerkleRoot;
import org.hyperledger.merkletree.MerkleTree;
import org.hyperledger.merkletree.MerkleTreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HLAPIBlock extends Block {

    public HLAPIBlock(HLAPIHeader header, List<? extends MerkleTreeNode> transactions) {
        super(header, transactions);
    }

    public static class Builder {
        int height;

        protected BID previousHash = BID.INVALID;
        protected Header header;
        protected MerkleRoot merkleRoot;
        protected int createTime;
        protected List<MerkleTreeNode> transactions = new ArrayList<>();


        public Builder previousHash(BID previousHash) {
            this.previousHash = previousHash;
            return this;
        }

        public Builder header(Header header) {
            this.header = header;
            return this;
        }

        public Builder merkleRoot(MerkleRoot merkleRoot) {
            this.merkleRoot = merkleRoot;
            return this;
        }

        public Builder createTime(int createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder transactions(Iterable<HLAPITransaction> transactions) {
            transactions.forEach(this.transactions::add);
            return this;
        }

        public Builder transactions(HLAPITransaction... transactions) {
            Collections.addAll(this.transactions, transactions);
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public HLAPIBlock build() {
            if (header == null) {
                if (merkleRoot == null) {
                    merkleRoot = MerkleTree.computeMerkleRoot(transactions);
                }
                header = new HyperledgerHeader(previousHash, merkleRoot, createTime);
            }
            return new HLAPIBlock(new HLAPIHeader(header, height), transactions);
        }
    }

    public int getHeight() {
        return getHeader().getHeight();
    }

    @Override
    @SuppressWarnings("unchecked")
    public HLAPIHeader getHeader() {
        return (HLAPIHeader) super.getHeader();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<HLAPITransaction> getTransactions() {
        return (List<HLAPITransaction>) super.getTransactions();
    }
}
