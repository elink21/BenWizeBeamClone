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
package org.apache.beam.runners.dataflow.worker;

import com.google.api.services.dataflow.model.CounterUpdate;
import com.google.api.services.dataflow.model.SourceOperationRequest;
import com.google.api.services.dataflow.model.SourceOperationResponse;
import com.google.api.services.dataflow.model.SourceSplitRequest;
import java.io.Closeable;
import java.util.Collections;
import org.apache.beam.runners.core.metrics.ExecutionStateTracker;
import org.apache.beam.runners.core.metrics.ExecutionStateTracker.ExecutionState;
import org.apache.beam.runners.dataflow.worker.counters.CounterSet;
import org.apache.beam.sdk.options.PipelineOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An executor for a source operation, defined by a {@code SourceOperationRequest}. */
@SuppressWarnings({
  "rawtypes", // TODO(https://github.com/apache/beam/issues/20447)
  "nullness" // TODO(https://github.com/apache/beam/issues/20497)
})
public class WorkerCustomSourceOperationExecutor implements SourceOperationExecutor {
  private static final Logger LOG =
      LoggerFactory.getLogger(WorkerCustomSourceOperationExecutor.class);
  private final PipelineOptions options;
  private final SourceOperationRequest request;
  private final CounterSet counters;
  private SourceOperationResponse response;

  private ExecutionStateTracker executionStateTracker;
  private ExecutionState executionState;

  public WorkerCustomSourceOperationExecutor(
      PipelineOptions options,
      SourceOperationRequest request,
      CounterSet counters,
      DataflowExecutionContext executionContext,
      DataflowOperationContext operationContext) {
    this.counters = counters;
    this.options = options;
    this.request = request;
    this.executionStateTracker = executionContext.getExecutionStateTracker();
    this.executionState = operationContext.newExecutionState(getOperationDescription(request));
  }

  @Override
  public CounterSet getOutputCounters() {
    return counters;
  }

  private static String getOperationDescription(SourceOperationRequest request) {
    if (request.getSplit() != null) {
      return "split";
    } else {
      throw new UnsupportedOperationException("Unsupported source operation request: " + request);
    }
  }

  @Override
  public void execute() throws Exception {
    LOG.debug("Executing source operation");
    try (Closeable stateTrackerCloser = executionStateTracker.activate()) {
      try (Closeable stateCloser = executionStateTracker.enterState(executionState)) {
        SourceSplitRequest split = request.getSplit();
        if (split != null) {
          this.response = WorkerCustomSources.performSplit(split, options);
        } else {
          throw new UnsupportedOperationException(
              "Unsupported source operation request: " + request);
        }
      }
    }
    LOG.debug("Source operation execution complete");
  }

  @Override
  public SourceOperationResponse getResponse() {
    return response;
  }

  @Override
  public Iterable<CounterUpdate> extractMetricUpdates() {
    return Collections.emptyList();
  }
}
