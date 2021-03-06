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
package org.apache.beam.runners.spark.structuredstreaming.translation.helpers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.beam.sdk.coders.Coder;
import org.apache.spark.broadcast.Broadcast;

/** Broadcast helper for side inputs. */
@SuppressWarnings({
  "nullness" // TODO(https://github.com/apache/beam/issues/20497)
})
public class SideInputBroadcast implements Serializable {

  private final Map<String, Broadcast<?>> bcast = new HashMap<>();
  private final Map<String, Coder<?>> coder = new HashMap<>();

  public SideInputBroadcast() {}

  public void add(String key, Broadcast<?> bcast, Coder<?> coder) {
    this.bcast.put(key, bcast);
    this.coder.put(key, coder);
  }

  public Broadcast<?> getBroadcastValue(String key) {
    return bcast.get(key);
  }

  public Coder<?> getCoder(String key) {
    return coder.get(key);
  }
}
