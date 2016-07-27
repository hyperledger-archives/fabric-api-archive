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

import org.hyperledger.common.*;
import org.hyperledger.merkletree.MerkleRoot;
import org.hyperledger.merkletree.MerkleTree;
import org.hyperledger.merkletree.MerkleTreeNode;
import org.hyperledger.transaction.Transaction;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A block of the ledger.
 * It consists a HyperledgerHeader and a list of Transaction
 * <p>
 * Some or all transactions might have been pruned, that is removed and replaced with MerkleTree nodes
 * that prove that the merkle tree (@Link https://en.wikipedia.org/wiki/Merkle_tree) root of the block header is valid even if some transactions are no
 * longer available.
 *
 * @see HyperledgerHeader
 * @see Transaction
 * @see MerkleTreeNode
 */
public class Block {
    private final Header header;
    private List<? extends MerkleTreeNode> nodes;
    private List<Transaction> transactions;

    /**
     * Create a block from a header and transaction list.
     * The transaction list might be pruned, hence it is a list of MerkleTreeNodes
     *
     * @param header       - the header
     * @param transactions - the potentially transaction list
     */
    @SuppressWarnings("unchecked")
    public Block(Header header, List<? extends MerkleTreeNode> transactions) {
        this.header = header;
        this.nodes = Collections.unmodifiableList(transactions);
        int pruned = 0;
        for (MerkleTreeNode n : transactions) {
            if (!(n instanceof Transaction))
                ++pruned;
        }
        if (pruned > 0) {
            this.transactions = new ArrayList<>(nodes.size() - pruned);
            for (MerkleTreeNode n : transactions) {
                if (n instanceof Transaction)
                    this.transactions.add((Transaction) n);
            }
            this.transactions = Collections.unmodifiableList(this.transactions);
        } else {
            this.transactions = (List<Transaction>) nodes;
        }
    }

    /**
     * @return - true if some transactions were pruned.
     */
    public boolean isPruned() {
        return transactions != nodes;
    }

    /**
     * create a Block builder.
     *
     * @return builder
     */
    public static Builder create() {
        return new Builder();
    }

    /**
     * block builder helper class
     */
    public static class Builder {
        private static final Header INVALID_HEADER = HyperledgerHeader.create().previousID(BID.INVALID).build();
        protected List<Transaction> transactions = new ArrayList<>();
        private Header header = INVALID_HEADER;

        public Builder transactions(Iterable<Transaction> transactions) {
            transactions.forEach(this.transactions::add);
            return this;
        }

        public Builder transactions(Transaction... transactions) {
            Collections.addAll(this.transactions, transactions);
            return this;
        }

        public Builder header(Header header) {
            this.header = header;
            return this;
        }

        public Block build() {
            if (MerkleRoot.INVALID.equals(header.getMerkleRoot())) {
                    header = new HyperledgerHeader(header.getPreviousID(), MerkleTree.computeMerkleRoot(transactions), header.getCreateTime());
            }
            return new Block(header, transactions);
        }
    }

    /**
     * @return the block header of this block
     */
    public Header getHeader() {
        return header;
    }

    /**
     * @return the block's unique id, that is identical to its header's id
     */
    public BID getID() {
        return header.getID();
    }

     /**
     * @return pointer to the previous block, this creates the block chain
     */
    public BID getPreviousID() {
        return header.getPreviousID();
    }

    /**
     * The merkle root of transaction within this block (@Link https://en.wikipedia.org/wiki/Merkle_tree)
     *
     * @return the merkle root of transaction hashes
     */
    public Hash getMerkleRoot() {
        return header.getMerkleRoot();
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
    @Deprecated
    public int getCreateTime() {
        return header.getCreateTime();
    }

    /**
     * @return the time point of the block was created
     */
    @SuppressWarnings("deprecation")
    public LocalTime getLocalCreateTime() {
        return header.getLocalCreateTime();
    }


    /**
     * Transactions of the block. This might not be a complete list if the block is pruned.
     *
     * @return immutable transaction list
     */
    public List<? extends Transaction> getTransactions() {
        return transactions;
    }

    /**
     * direct access to a transaction by its index in the block
     *
     * @param i - index of the transaction within the block. 0 is coin base
     * @return
     */
    public Transaction getTransaction(int i) {
        return transactions.get(i);
    }

    /**
     * Merkle tree nodes in the block. Nodes might be transactions or merkle tree nodes that were computed
     * out of transactions already pruned.
     *
     * @return immutable list of merkle tree nodes
     * @see MerkleTreeNode
     * @see MerkleTree
     */
    public List<? extends MerkleTreeNode> getMerkleTreeNodes() {
        return nodes;
    }


    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Block) {
            return getID().equals(((Block) o).getID());
        }
        return false;
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
