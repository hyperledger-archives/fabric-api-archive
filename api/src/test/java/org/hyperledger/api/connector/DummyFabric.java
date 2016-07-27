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

package org.hyperledger.api.connector;

import org.hyperledger.api.*;
import org.hyperledger.block.BID;
import org.hyperledger.block.Block;
import org.hyperledger.block.Header;
import org.hyperledger.block.HyperledgerHeader;
import org.hyperledger.merkletree.MerkleTree;
import org.hyperledger.transaction.TID;
import org.hyperledger.transaction.Transaction;

import java.util.*;

public class DummyFabric implements HLAPI {

    private Map<TID, Transaction> txs = new HashMap<>();
    private Map<BID, Block> blocks = new HashMap<>();
    private Map<TID, BID> index = new HashMap<>();
    private BID top = null;
    private List<TransactionListener> txListeners = new ArrayList<>();
    private List<TrunkListener> trunkListeners = new ArrayList<>();
    private List<RejectListener> rejectListeners = new ArrayList<>();

    @Override
    public String getClientVersion() throws HLAPIException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServerVersion() throws HLAPIException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long ping(long nonce) throws HLAPIException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAlertListener(AlertListener listener) throws HLAPIException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAlertListener(AlertListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getChainHeight() throws HLAPIException {
        return 1 + blocks.size();
    }

    @Override
    public HLAPIHeader getBlockHeader(BID hash) throws HLAPIException {
        return getBlock(hash).getHeader();
    }

    @Override
    public HLAPIBlock getBlock(BID hash) throws HLAPIException {
        return Optional.ofNullable(blocks.get(hash))
                .map(this::toHLAPIBlock)
                .orElse(null);
    }

    @Override
    public HLAPITransaction getTransaction(TID hash) throws HLAPIException {
        return Optional.ofNullable(txs.get(hash))
                .map(this::toHLAPITx)
                .orElse(null);
    }

    @Override
    public void sendTransaction(Transaction transaction) throws HLAPIException {
        if (transaction.toByteArray().length == 0) {
            rejectListeners.forEach(listener -> listener.rejected("rejected", transaction.getID(), "rejected", 0));
        } else {
            Block block = createBlock(transaction);

            blocks.put(block.getID(), block);
            txs.put(transaction.getID(), transaction);
            index.put(transaction.getID(), block.getID());

            HLAPIBlock b = toHLAPIBlock(block);

            trunkListeners.forEach(listener -> listener.trunkUpdate(Collections.singletonList(b)));

            for (TransactionListener listener : txListeners) {
                listener.process(toHLAPITx(transaction));
            }
        }
    }

    private Block createBlock(Transaction t) {
        Header h = new HyperledgerHeader(top, MerkleTree.computeMerkleRoot(Collections.singletonList(t)), 0);
        return new Block(h, Collections.singletonList(t));
    }

    private HLAPIBlock toHLAPIBlock(Block b) {
        return new HLAPIBlock(toHLAPIHeader(b.getHeader()), b.getTransactions());
    }

    private HLAPIHeader toHLAPIHeader(Header h) {
        return new HLAPIHeader(h, blocks.size());
    }

    private HLAPITransaction toHLAPITx(Transaction tx) {
        return new HLAPITransaction(tx, index.get(tx.getID()));
    }

    @Override
    public void registerRejectListener(RejectListener rejectListener) throws HLAPIException {
        rejectListeners.add(rejectListener);
    }

    @Override
    public void removeRejectListener(RejectListener rejectListener) {
        rejectListeners.remove(rejectListener);
    }

    @Override
    public void sendBlock(Block block) throws HLAPIException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerTransactionListener(TransactionListener listener) throws HLAPIException {
        txListeners.add(listener);
    }

    @Override
    public void removeTransactionListener(TransactionListener listener) {
        txListeners.remove(listener);
    }

    @Override
    public void registerTrunkListener(TrunkListener listener) throws HLAPIException {
        trunkListeners.add(listener);
    }

    @Override
    public void removeTrunkListener(TrunkListener listener) {
        trunkListeners.remove(listener);
    }

    @Override
    public void catchUp(List<BID> inventory, int limit, boolean headers, TrunkListener listener) throws HLAPIException {
        throw new UnsupportedOperationException();
    }
}
