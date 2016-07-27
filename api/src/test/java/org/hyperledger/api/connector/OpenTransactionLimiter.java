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

import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class OpenTransactionLimiter {
    private final Optional<Semaphore> slots;

    public OpenTransactionLimiter(int limit) {
        if (limit == 0) {
            slots = Optional.empty();
        } else {
            slots = Optional.of(new Semaphore(limit));
        }
    }

    public void newTx() {
            slots.map(OpenTransactionLimiter::acquire);
    }

    private static boolean acquire(Semaphore s) {
        try {
            if (s.tryAcquire(1, TimeUnit.MINUTES)) {
                return true;
            } else {
                throw new RuntimeException("Too much wait for send");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void completeTx() {
        slots.map(OpenTransactionLimiter::release);
    }

    private static boolean release(Semaphore s) {
        s.release();
        return true;
    }
}
