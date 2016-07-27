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


/**
 * Listener interface for transactions sent by the server.
 *
 * @returns the return value is not evaluated by the server
 */
public interface TransactionListener {
    /**
     * Process a transaction
     *
     * @param t a transaction wrapped into an HLAPITransaction that also carries the block ID if available
     * @throws HLAPIException
     */
    void process(HLAPITransaction t) throws HLAPIException;
}
