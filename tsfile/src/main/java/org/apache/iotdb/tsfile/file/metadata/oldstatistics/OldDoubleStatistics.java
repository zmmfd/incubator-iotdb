/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.tsfile.file.metadata.oldstatistics;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.iotdb.tsfile.utils.ReadWriteIOUtils;

/**
 * Statistics for double type.
 */
public class OldDoubleStatistics extends OldStatistics<Double> {

  private double min;
  private double max;
  private double first;
  private double last;
  private double sum;

  @Override
  public Double getMin() {
    return min;
  }

  @Override
  public Double getMax() {
    return max;
  }

  @Override
  public Double getFirst() {
    return first;
  }

  @Override
  public Double getLast() {
    return last;
  }

  @Override
  public double getSum() {
    return sum;
  }
  
  @Override
  public String toString() {
    return "[min:" + min + ",max:" + max + ",first:" + first + ",last:" + last + ",sum:" + sum
        + "]";
  }

  @Override
  void deserialize(ByteBuffer byteBuffer) throws IOException {
    this.min = ReadWriteIOUtils.readDouble(byteBuffer);
    this.max = ReadWriteIOUtils.readDouble(byteBuffer);
    this.first = ReadWriteIOUtils.readDouble(byteBuffer);
    this.last = ReadWriteIOUtils.readDouble(byteBuffer);
    this.sum = ReadWriteIOUtils.readDouble(byteBuffer);
  }
}
