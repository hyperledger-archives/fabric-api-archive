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

import org.hyperledger.merkletree.MerkleRoot;

public abstract class HeaderBuilder<T extends HeaderBuilder<T>> {
    protected int version;
    protected BID previousID = BID.INVALID;
    protected MerkleRoot merkleRoot = MerkleRoot.INVALID;
    protected int createTime;
    protected int difficultyTarget;
    protected int nonce;

    protected HeaderBuilder() {
    }

    public T version(int version) {
        this.version = version;
        return getThis();
    }

    public T previousID(BID previousHash) {
        this.previousID = previousHash;
        return getThis();
    }

    public T merkleRoot(MerkleRoot merkleRoot) {
        this.merkleRoot = merkleRoot;
        return getThis();
    }

    public T createTime(int createTime) {
        this.createTime = createTime;
        return getThis();
    }

    public T difficultyTarget(int difficultyTarget) {
        this.difficultyTarget = difficultyTarget;
        return getThis();
    }

    public T nonce(int nonce) {
        this.nonce = nonce;
        return getThis();
    }

    protected abstract T getThis();
}
