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
import org.hyperledger.merkletree.MerkleTreeNode;

/**
 * PrunedNode is a MerkleTreeNode that may be compressed into other PrunedNodes
 * without losing information we want to preserve.
 */
public class PrunedNode implements MerkleTreeNode {
    private final Hash hash;
    private final int merkleHeight;

    @Override
    public Hash getID() {
        return hash;
    }

    public int getMerkleHeight() {
        return merkleHeight;
    }

    public PrunedNode(Hash hash, int merkleHeight) {
        this.hash = hash;
        this.merkleHeight = merkleHeight;
    }
}
