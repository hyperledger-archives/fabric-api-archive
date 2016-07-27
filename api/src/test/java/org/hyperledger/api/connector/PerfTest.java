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

import com.google.common.base.Stopwatch;
import org.hyperledger.api.HLAPI;
import org.hyperledger.api.HLAPIException;
import org.hyperledger.api.RejectListener;
import org.hyperledger.api.TransactionListener;
import org.hyperledger.transaction.TID;
import org.hyperledger.transaction.Transaction;
import org.hyperledger.transaction.TransactionTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class PerfTest {

    private static final int SIZE = 1000;
    private static final int NR_OF_GROUPS = 10;
    private static final int CHUNK_SIZE = SIZE / NR_OF_GROUPS;
    private static final int NR_OF_CONCURRENT_TRANSACTIONS = 500;

//    private final HLAPI api = new GRPCClient("localhost", 30303, 31315);
    private final HLAPI api = new DummyFabric();
    private final TransactionListener listener;
    private final RejectListener rejectionListener;
    private final List<MeasurableTransaction> txs;
    private final Map<TID, MeasurableTransaction> txMap;
    private final AtomicInteger rejectionCounter = new AtomicInteger(0);
    private int timoutCounter;

    public PerfTest() {
        OpenTransactionLimiter limiter = new OpenTransactionLimiter(NR_OF_CONCURRENT_TRANSACTIONS);
        List<MeasurableTransaction> transactions = new ArrayList<>();
        Map<TID, MeasurableTransaction> transactionMap = new HashMap<>();
        for (int i = 0; i < SIZE; i++) {
            MeasurableTransaction t = new MeasurableTransaction(TransactionTest.randomTx(), limiter);
            transactions.add(t);
            transactionMap.put(t.tx.getID(), t);
        }
        txs = Collections.unmodifiableList(transactions);
        txMap = Collections.unmodifiableMap(transactionMap);

        listener = t -> txMap.get(t.getID()).complete();
        rejectionListener = (command, hash, reason, rejectionCode) -> rejectionCounter.incrementAndGet();
    }

    @Before
    public void setUp() throws HLAPIException {
        api.registerTransactionListener(listener);
        api.registerRejectListener(rejectionListener);
    }

    @After
    public void tearDown() {
        api.removeTransactionListener(listener);
    }

    @Test
    public void performance() throws HLAPIException, ExecutionException, InterruptedException {
        Stopwatch totalTime = Stopwatch.createStarted();

        for (MeasurableTransaction t : txs) {
            t.send(api);
        }
        List<Long> results = getResults();

        totalTime.stop();

        int notFound = checkTransactionsAdded();
        long multipleNotifications = getNrOfMultipleNotification();

        printResults(totalTime, results, multipleNotifications, notFound);
    }

    private List<Long> getResults() throws ExecutionException, InterruptedException {
        List<Long> results = new ArrayList<>();
        timoutCounter = 0;
        for (MeasurableTransaction t : txs) {
            try {
                results.add(t.get());
            } catch (InterruptedException | ExecutionException e) {
                if (e.getCause() instanceof TimeoutException) {
                    timoutCounter++;
                    results.add(MeasurableTransaction.TIMOUT_SEC * 1000L);
                } else {
                    throw e;
                }
            }
        }
        return results;
    }

    private long getNrOfMultipleNotification() {
        return txs.stream().filter(tx -> tx.multipleNotification.get()).count();
    }

    private int checkTransactionsAdded() throws HLAPIException {
        int notFound = 0;
        for (MeasurableTransaction t : txs) {
            TID id = t.tx.getID();
            Transaction storedTx = api.getTransaction(id);
            if (storedTx != null) {
                assertEquals(t.tx, storedTx);
            } else {
                notFound++;
            }
        }
        return notFound;
    }

    private void printResults(Stopwatch totalTime, List<Long> results, long multipleNotifications, int notFound) {
        println("====== Test results ======\n");

        double totalSeconds = totalTime.elapsed(TimeUnit.MILLISECONDS) / 1000.0;
        println("Total: %d transactions in %.2f sec", SIZE, totalSeconds);
        println("Average transaction/s: %.2f", SIZE / totalSeconds);
        println("Average transaction process time: %.2f ms", avg(results));
        println("Listener not called for %d transactions", timoutCounter);
        println("Listener called multiple times for %d transactions", multipleNotifications);
        println("%d transactions not found in the ledger", notFound);
        println("%d transactions rejected", rejectionCounter.get());

        println("Distribution:");
        for (int i = 0; i < NR_OF_GROUPS; i++) {
            int lowerBound = i * CHUNK_SIZE;
            int upperBound = (i + 1) * CHUNK_SIZE;

            double timeDiffSec = (txs.get(upperBound - 1).completionTime - txs.get(lowerBound).completionTime) / 1000.0;
            double txsPerSec = (double) CHUNK_SIZE / timeDiffSec;
            double processTime = avg(results.subList(lowerBound, upperBound));

            println("%6d - %6d:\ttx/sec=%5.2f\tavg_tx_time=%5.2f ms", lowerBound, upperBound, txsPerSec, processTime);
        }
    }

    private void println(String format, Object... args) {
        System.out.format(format + "\n", args);
    }

    private double avg(List<Long> l) {
        return l.stream().mapToLong(Long::longValue).average().getAsDouble();
    }
}
