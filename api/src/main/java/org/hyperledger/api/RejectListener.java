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

import org.hyperledger.common.Hash;

/**
 * Listener of reject messages from the 'satoshi' client This listener is called if the Server runs in slave mode behind a 'satoshi' border router and the
 * 'satoshi' node rejects a message the server sends to it.
 */
public interface RejectListener {
    /**
     * Reject message callback from 'satoshi' node
     *
     * @param command       - the command that was sent to the server, usually transaction
     * @param hash          - identity of the payload rejected
     * @param reason        - rejection reason in plain text
     * @param rejectionCode - rejection reason as code
     */
    void rejected(String command, Hash hash, String reason, int rejectionCode);
}
