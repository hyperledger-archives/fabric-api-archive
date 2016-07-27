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
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.hyperledger.api.HLAPI;
import org.hyperledger.api.HLAPIException;
import org.hyperledger.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MeasurableTransaction {
    private static final Logger log = LoggerFactory.getLogger(MeasurableTransaction.class);

    public static int TIMOUT_SEC = 120;

    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("transaction-timeout-%d")
            .build();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, threadFactory);

    public final Transaction tx;
    public long completionTime;
    public AtomicBoolean multipleNotification = new AtomicBoolean(false);
    private final Stopwatch stopwatch = Stopwatch.createUnstarted();
    private CompletableFuture<Long> future;
    private final OpenTransactionLimiter limiter;

    public MeasurableTransaction(Transaction t, OpenTransactionLimiter limiter) {
        tx = t;
        this.limiter = limiter;
    }

    public void send(HLAPI api) throws HLAPIException {
        log.info("Sending transaction {}", tx.getID().toUuidString());
        future = failAfter(Duration.ofSeconds(TIMOUT_SEC));
        limiter.newTx();
        stopwatch.start();
        api.sendTransaction(tx);
    }

    public void complete() {
        log.info("Complete called for txid={} uuid={}", tx.getID(), tx.getID().toUuidString());
        if (stopwatch.isRunning()) {
            stopwatch.stop();
            completionTime = System.currentTimeMillis();
            future.complete(stopwatch.elapsed(MILLISECONDS));
            limiter.completeTx();
        } else {
            log.error("Listener called multiple times for txid={} uuid={}", tx.getID(), tx.getID().toUuidString());
            multipleNotification.set(true);
        }
    }

    public long get() throws ExecutionException, InterruptedException {
        return future.get();
    }

    private CompletableFuture<Long> failAfter(Duration duration) {
        final CompletableFuture<Long> promise = new CompletableFuture<>();
        final Runnable timeoutFn = () -> {
            String message = "Timeout for txid=" + tx.getID() + " uuid=" + tx.getID().toUuidString() + " after " + duration;
            if (promise.completeExceptionally(new TimeoutException(message))) {
                log.error(message);
            }
        };
        scheduler.schedule(timeoutFn, duration.toMillis(), MILLISECONDS);
        return promise;
    }

}
