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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.runners.dataflow.worker.counters;

import static org.apache.beam.runners.dataflow.worker.counters.DataflowCounterUpdateExtractor.longToSplitInt;
import static org.apache.beam.runners.dataflow.worker.counters.DataflowCounterUpdateExtractor.splitIntToLong;

import com.google.api.services.dataflow.model.CounterUpdate;
import com.google.api.services.dataflow.model.IntegerMean;
import java.util.List;

@SuppressWarnings({
  "nullness" // TODO(https://github.com/apache/beam/issues/20497)
})
public class MeanCounterUpdateAggregator implements CounterUpdateAggregator {

  @Override
  public CounterUpdate aggregate(List<CounterUpdate> counterUpdates) {
    if (counterUpdates == null || counterUpdates.isEmpty()) {
      return null;
    }
    if (counterUpdates.stream().anyMatch(c -> c.getIntegerMean() == null)) {
      throw new UnsupportedOperationException(
          "Aggregating MEAN counter updates over non-integerMean type is not implemented.");
    }

    CounterUpdate initial = counterUpdates.remove(0);
    return counterUpdates.stream()
        .reduce(
            initial,
            (first, second) ->
                first.setIntegerMean(
                    new IntegerMean()
                        .setCount(
                            longToSplitInt(
                                splitIntToLong(first.getIntegerMean().getCount())
                                    + splitIntToLong(second.getIntegerMean().getCount())))
                        .setSum(
                            longToSplitInt(
                                splitIntToLong(first.getIntegerMean().getSum())
                                    + splitIntToLong(second.getIntegerMean().getSum())))));
  }
}
