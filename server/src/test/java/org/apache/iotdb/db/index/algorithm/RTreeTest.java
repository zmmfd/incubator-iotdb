/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.iotdb.db.index.algorithm;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BiConsumer;
import org.apache.iotdb.db.index.algorithm.RTree.RNode;
import org.apache.iotdb.db.index.algorithm.RTree.SeedsPicker;
import org.apache.iotdb.tsfile.utils.ReadWriteIOUtils;
import org.junit.Assert;
import org.junit.Test;

public class RTreeTest {

  @Test
  public void testRTreeToString() {
    String gt = "nMax:4,nMin:2,dim:2,seedsPicker:LINEAR\n"
        + "RNode{LB=[0.0, 0.0], UB=[19.0, 17.0], leaf=false}\n"
        + "--RNode{LB=[0.0, 8.0], UB=[19.0, 17.0], leaf=false}\n"
        + "----RNode{LB=[15.0, 13.0], UB=[19.0, 17.0], leaf=true}\n"
        + "------Item: 4,RNode{LB=[19.0, 14.0], UB=[19.0, 14.0], leaf=true}\n"
        + "------Item: 2,RNode{LB=[15.0, 13.0], UB=[15.0, 13.0], leaf=true}\n"
        + "------Item: 5,RNode{LB=[17.0, 17.0], UB=[17.0, 17.0], leaf=true}\n"
        + "------Item: 19,RNode{LB=[18.0, 15.0], UB=[18.0, 15.0], leaf=true}\n"
        + "----RNode{LB=[0.0, 8.0], UB=[3.0, 8.0], leaf=true}\n"
        + "------Item: 0,RNode{LB=[0.0, 8.0], UB=[0.0, 8.0], leaf=true}\n"
        + "------Item: 10,RNode{LB=[3.0, 8.0], UB=[3.0, 8.0], leaf=true}\n"
        + "----RNode{LB=[4.0, 13.0], UB=[5.0, 15.0], leaf=true}\n"
        + "------Item: 8,RNode{LB=[4.0, 15.0], UB=[4.0, 15.0], leaf=true}\n"
        + "------Item: 18,RNode{LB=[5.0, 13.0], UB=[5.0, 13.0], leaf=true}\n"
        + "--RNode{LB=[1.0, 0.0], UB=[17.0, 7.0], leaf=false}\n"
        + "----RNode{LB=[11.0, 0.0], UB=[12.0, 1.0], leaf=true}\n"
        + "------Item: 12,RNode{LB=[12.0, 0.0], UB=[12.0, 0.0], leaf=true}\n"
        + "------Item: 3,RNode{LB=[11.0, 1.0], UB=[11.0, 1.0], leaf=true}\n"
        + "----RNode{LB=[12.0, 2.0], UB=[15.0, 4.0], leaf=true}\n"
        + "------Item: 7,RNode{LB=[15.0, 4.0], UB=[15.0, 4.0], leaf=true}\n"
        + "------Item: 6,RNode{LB=[13.0, 2.0], UB=[13.0, 2.0], leaf=true}\n"
        + "------Item: 14,RNode{LB=[12.0, 3.0], UB=[12.0, 3.0], leaf=true}\n"
        + "----RNode{LB=[4.0, 5.0], UB=[17.0, 7.0], leaf=true}\n"
        + "------Item: 1,RNode{LB=[9.0, 7.0], UB=[9.0, 7.0], leaf=true}\n"
        + "------Item: 11,RNode{LB=[4.0, 7.0], UB=[4.0, 7.0], leaf=true}\n"
        + "------Item: 15,RNode{LB=[5.0, 5.0], UB=[5.0, 5.0], leaf=true}\n"
        + "------Item: 16,RNode{LB=[17.0, 7.0], UB=[17.0, 7.0], leaf=true}\n"
        + "----RNode{LB=[1.0, 0.0], UB=[3.0, 2.0], leaf=true}\n"
        + "------Item: 9,RNode{LB=[1.0, 0.0], UB=[1.0, 0.0], leaf=true}\n"
        + "------Item: 13,RNode{LB=[3.0, 2.0], UB=[3.0, 2.0], leaf=true}\n"
        + "------Item: 17,RNode{LB=[2.0, 2.0], UB=[2.0, 2.0], leaf=true}\n";
    int dim = 2;
    Random random = new Random(0);
    RTree<Integer> rTree = new RTree<>(4, 2, 2, SeedsPicker.LINEAR);
    for (int i = 0; i < 20; i++) {
      float[] in = new float[2];
      for (int j = 0; j < dim; j++) {
        in[j] = ((float) random.nextInt(20));
      }
      System.out.println(String.format("add: %s, value: %d", Arrays.toString(in), i));
      rTree.insert(in, i);
      if (!checkRTree(rTree)) {
        fail();
      }
    }
    System.out.println(rTree);
    Assert.assertEquals(gt, rTree.toString());
  }

  @Test
  public void testRTreeSerialization() throws IOException {
    int dim = 2;
    Random random = new Random(0);
    RTree<Integer> rTree = new RTree<>(4, 2, 2, SeedsPicker.LINEAR);
    int dataSize = 20;
    int[] beforeData = new int[dataSize];
    for (int i = 0; i < 20; i++) {
      float[] in = new float[2];
      for (int j = 0; j < dim; j++) {
        in[j] = ((float) random.nextInt(20));
      }
      System.out.println(String.format("add: %s, value: %d", Arrays.toString(in), i));
      rTree.insert(in, i);
      beforeData[i] = i;
    }

    ByteArrayOutputStream boas = new ByteArrayOutputStream();
    ReadWriteIOUtils.write(dataSize, boas);
    BiConsumer<Integer, OutputStream> serial = (i, o) -> {
      try {
        ReadWriteIOUtils.write(beforeData[i], o);
      } catch (IOException e) {
        e.printStackTrace();
      }
    };
    rTree.serialize(boas, serial);
    //deserialize
    ByteBuffer byteBuffer = ByteBuffer.wrap(boas.toByteArray());
    int afterSize = ReadWriteIOUtils.readInt(byteBuffer);
    int[] afterData = new int[afterSize];
    BiConsumer<Integer, ByteBuffer> deserial = (i, b) -> {
      int value = ReadWriteIOUtils.readInt(b);
      afterData[i] = value;
    };
    RTree<Integer> afterRTree = RTree.deserialize(byteBuffer, deserial);
    System.out.println(rTree);
    System.out.println(afterRTree);
    Assert.assertEquals(rTree.toString(), afterRTree.toString());
    Assert.assertArrayEquals(beforeData, afterData);

  }

  /**
   * check whether the tree satisfies constraints:
   * <p><ul>
   * <li>Root node has no parent, the number of children is less than nMaxPerNode.</li>
   * <li>The number of children in other inner nodes is between nMinPerNode and nMaxPerNode.</li>
   * <li>The number of items in a leaf node is between 1 and nMaxPerNode.</li>
   * <li>The upper/lower bound of a node should be tight.</li>
   * <li>If Node_A is Node_B's parent, Node_B.parent should point to Node_A</li>
   * </ul>
   * </p>
   */
  public boolean checkRTree(RTree rTree) {
    return checkNode(rTree, rTree.root);
  }

  private boolean checkNode(RTree rTree, RNode node) {
    //check number of children
    int nChildren = node.children.size();
    if (node == rTree.root) {
      if (nChildren > rTree.nMaxPerNode) {
        System.out.println("Invalid: root has too many children: " + nChildren);
        return false;
      }
    } else if (node.isLeaf) {
      if (nChildren > rTree.nMaxPerNode) {
        System.out.println("Invalid: leaf has too many children: " + nChildren);
        return false;
      }
    } else {
      if (nChildren > rTree.nMaxPerNode || nChildren < rTree.nMinPerNode) {
        System.out.println("Invalid: root has invalid children: " + nChildren);
        return false;
      }
    }
    //check bound tightness
    if (nChildren > 0) {
      for (int i = 0; i < rTree.dim; i++) {
        float minLb = Float.MAX_VALUE;
        float maxUb = -Float.MAX_VALUE;
        for (RNode child : node.children) {
          if (child.lbs[i] < minLb) {
            minLb = child.lbs[i];
          }
          if (child.ubs[i] > maxUb) {
            maxUb = child.ubs[i];
          }
        }
        if (node.lbs[i] != minLb || node.ubs[i] != maxUb) {
          System.out.println("Invalid: node bound is not tight. ");
          return false;
        }
      }
    }
    //check parent relationship
    for (RNode child : node.children) {
      if (child.parent != node) {
        System.out.println("Invalid: parent-child relationship");
        return false;
      }
    }
    //check its children recursively
    for (RNode child : node.children) {
      if (!checkNode(rTree, child)) {
        return false;
      }
    }
    return true;
  }
}