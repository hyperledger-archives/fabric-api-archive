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
package org.hyperledger.merkletree;

import org.hyperledger.common.Hash;

/**
 * A node in the merkle tree.
 * It holds the id of a transaction at the leafs and digest of digests on higher level
 */
public interface MerkleTreeNode {
    /**
     * The ID of the transaction if a leaf node otherwise digest of digests
     *
     * @return
     */
    Hash getID();

    /**
     * The height in the tree. 0 is leaf.
     *
     * @return
     */
    int getMerkleHeight();
}
