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

import java.util.ArrayList;
import java.util.List;

/**
 * Merkle Tree is a digest of information organized such that inclusion of a piece can be proven
 * without revealing the rest of the content. @Link https://en.wikipedia.org/wiki/Merkle_tree
 */
public class MerkleTree {

    /**
     * Digest a list of nodes into a single root digest
     *
     * @param nodes list of nodes to compress
     * @return root digest
     */
    public static MerkleRoot computeMerkleRoot(List<? extends MerkleTreeNode> nodes) {
        List<MerkleTreeNode> merkleTreeNodeList = new ArrayList<>(nodes.size());
        for (MerkleTreeNode n : nodes) {
            merkleTreeNodeList.add(new PrunedNode(n.getID(), 0));
        }

        MerkleTreeNode rootNode = compress(merkleTreeNodeList).get(0);
        return new MerkleRoot(rootNode.getID());
    }

    /**
     * Compress a merkle tree into the smallest possible representation that
     * does not lose information on nodes that are not instances of PrunedNode
     *
     * @param nodes list of nodes to compress
     * @return root digest
     * @see PrunedNode
     */
    public static List<MerkleTreeNode> compress(List<MerkleTreeNode> nodes) {
        List<MerkleTreeNode> result = new ArrayList<>(nodes);
        boolean hasChanged;
        do {
            if (result.size() <= 1) return result;
            hasChanged = false;
            int leftPos, rightPos;
            List<MerkleTreeNode> source = result;
            result = new ArrayList<>();
            int prevPos = -twoPower(source.get(0).getMerkleHeight());
            for (int i = 0; i < source.size(); i++) {
                MerkleTreeNode left = source.get(i);
                leftPos = prevPos + twoPower(left.getMerkleHeight());
                // if 'left' node is the last so it is without 'right' node
                if (i == source.size() - 1) {
                    result.add(left);
                    prevPos = leftPos;
                    continue;
                }
                MerkleTreeNode right = source.get(i + 1);
                rightPos = leftPos + twoPower(left.getMerkleHeight());

                if (isMergeableDistantPrunedNeighbours(left, leftPos, right, rightPos)) {
                    result.add(new PrunedNode(
                            Hash.merge(left.getID(), mergeWithItself(right, left.getMerkleHeight() - right.getMerkleHeight())),
                            left.getMerkleHeight() + 1));
                    hasChanged = true;
                    i++;
                } else if (isNeighbours(left, leftPos, right, rightPos)) {
                    if (left instanceof PrunedNode && right instanceof PrunedNode) {
                        result.add(new PrunedNode(Hash.merge(left.getID(), right.getID()), left.getMerkleHeight() + 1));
                        prevPos = rightPos;
                        hasChanged = true;
                        i++;
                    } else {
                        result.add(left);
                        result.add(right);
                        prevPos = rightPos;
                        i++;
                    }
                } else {
                    result.add(left);
                    prevPos = leftPos;
                }
            }
        } while (hasChanged);
        return result;
    }

    private static Hash mergeWithItself(MerkleTreeNode left, int count) {
        Hash hash = left.getID();
        for (int i = 0; i < count; i++) {
            hash = Hash.merge(hash, hash);
        }
        return hash;
    }


    // Tests if a higher level pruned node and a right hand side
    // leftover pruned node can be further pruned.
    // Two pruned nodes are lonely neighbours if and only if
    // 1) they are prune nodes and
    // 2) the left one is on a higher level and
    // 3) the left one is in an even position on that level of the full tree and
    // 4) their position differece is according to the left's level
    private static boolean isMergeableDistantPrunedNeighbours(MerkleTreeNode prev, int prevPos, MerkleTreeNode curr, int currPos) {
        return prev instanceof PrunedNode && curr instanceof PrunedNode &&
                prev.getMerkleHeight() > curr.getMerkleHeight() &&
                (prevPos >> prev.getMerkleHeight() & 1) == 0 &&
                currPos - prevPos == twoPower(prev.getMerkleHeight());
    }

    // Two nodes are neighbours if and only if they are
    // 1) on the same height and
    // 2) next to each other where the left one is in an even and the
    //    right one is in the next odd position on that height of a full binary tree
    private static boolean isNeighbours(MerkleTreeNode left, int posLeft, MerkleTreeNode right, int posRight) {
        return left.getMerkleHeight() == right.getMerkleHeight() &&
                (posLeft ^ posRight) == twoPower(left.getMerkleHeight());
    }

    private static int twoPower(int power) {
        return 1 << power;
    }
}
