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
package org.apache.beam.sdk.schemas.logicaltypes;

import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

/** Base class for types representing timestamps or durations as nanoseconds. */
abstract class NanosType<T> implements Schema.LogicalType<T, Row> {
  private final String identifier;
  // TODO(https://github.com/apache/beam/issues/20540): This should be a constant
  protected final Schema schema;

  NanosType(String identifier) {
    this.identifier = identifier;
    this.schema = Schema.builder().addInt64Field("seconds").addInt32Field("nanos").build();
  }

  @Override
  public String getIdentifier() {
    return identifier;
  }

  @Override
  public Schema.FieldType getArgumentType() {
    return Schema.FieldType.STRING;
  }

  @Override
  public String getArgument() {
    return "";
  }

  @Override
  public Schema.FieldType getBaseType() {
    return Schema.FieldType.row(schema);
  }
}
