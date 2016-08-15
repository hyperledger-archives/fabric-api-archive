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

import org.hyperledger.api.HLAPI;
import org.hyperledger.api.HLAPIBlock;
import org.hyperledger.api.HLAPIException;
import org.hyperledger.api.HLAPITransaction;
import org.hyperledger.transaction.TID;
import org.hyperledger.transaction.Transaction;
import org.hyperledger.transaction.TransactionTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class GRPCClientTest {
    private static final Logger log = LoggerFactory.getLogger(GRPCClientTest.class);

    private HLAPI client;
    private OpenTransactionLimiter unlimited = new OpenTransactionLimiter(0);

    @Before
    public void setUp() {
        client = new DummyFabric();
//        client = new GRPCClient("localhost", 7051, 7053);
    }

    @Test
    public void testGetBlockHeight() throws HLAPIException {
        int height = client.getChainHeight();

        log.debug("testGetBlockHeight height=" + height);

        assertTrue(height > 0);
    }

    @Test
    public void getNonExistingTransaction() throws HLAPIException {
        assertNull(client.getTransaction(TID.INVALID));
    }

    @Test
    public void sendTransaction() throws HLAPIException, InterruptedException {
        Transaction tx = TransactionTest.randomTx();
        int originalHeight = client.getChainHeight();
        client.sendTransaction(tx);

        Thread.sleep(1500);

        HLAPITransaction res = client.getTransaction(tx.getID());
        assertEquals(tx, res);

        int newHeight = client.getChainHeight();
        assertEquals(originalHeight + 1, newHeight);
    }

    @Test
    public void transactionListener() throws HLAPIException, InterruptedException, ExecutionException {
        MeasurableTransaction tx1 = new MeasurableTransaction(TransactionTest.randomTx(), unlimited);
        MeasurableTransaction tx2 = new MeasurableTransaction(TransactionTest.randomTx(), unlimited);

        client.registerTransactionListener(t -> {
            if (t.equals(tx1.tx)) {
                tx1.complete();
            } else if (t.equals(tx2.tx)) {
                tx2.complete();
            } else {
                fail("Unknown transaction");
            }
        });

        tx1.send(client);
        tx2.send(client);

        tx1.get();
        tx2.get();
    }

    @Test
    public void trunkListener() throws HLAPIException, InterruptedException, ExecutionException {
        MeasurableTransaction tx1 = new MeasurableTransaction(TransactionTest.randomTx(), unlimited);
        MeasurableTransaction tx2 = new MeasurableTransaction(TransactionTest.randomTx(), unlimited);

        client.registerTrunkListener(added -> {
            for (HLAPIBlock block : added) {
                boolean listenerCalled = false;
                if (block.getTransactions().contains(tx1.tx)) {
                    listenerCalled = true;
                    tx1.complete();
                }
                if (block.getTransactions().contains(tx2.tx)) {
                    listenerCalled = true;
                    tx2.complete();
                }
                if (!listenerCalled) {
                    fail("Block containing no known transaction");
                }
            }
        });

        tx1.send(client);
        tx2.send(client);

        tx1.get();
        tx2.get();
    }

    @Test
    public void rejectListener() throws HLAPIException, InterruptedException, ExecutionException {
        class InvalidTransaction extends Transaction {

            public InvalidTransaction() {
                super(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            }

            @Override
            public byte[] toByteArray() {
                return new byte[0];
            }
        }

        MeasurableTransaction tx = new MeasurableTransaction(new InvalidTransaction(), unlimited);

        client.registerRejectListener((command, hash, reason, rejectionCode) -> {
            tx.complete();
        });

        tx.send(client);

        tx.get();
    }

}
