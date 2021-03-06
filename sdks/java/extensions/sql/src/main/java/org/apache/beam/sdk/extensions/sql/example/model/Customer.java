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
package org.apache.beam.sdk.extensions.sql.example.model;

import java.io.Serializable;
import java.util.Objects;
import org.apache.beam.sdk.schemas.JavaBeanSchema;
import org.apache.beam.sdk.schemas.annotations.DefaultSchema;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Describes a customer. */
@DefaultSchema(JavaBeanSchema.class)
@SuppressWarnings({
  "nullness" // TODO(https://github.com/apache/beam/issues/20497)
})
public class Customer implements Serializable {
  private int id;
  private String name;
  private String countryOfResidence;

  public Customer(int id, String name, String countryOfResidence) {
    this.id = id;
    this.name = name;
    this.countryOfResidence = countryOfResidence;
  }

  public Customer() {}

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getCountryOfResidence() {
    return countryOfResidence;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCountryOfResidence(String countryOfResidence) {
    this.countryOfResidence = countryOfResidence;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Customer customer = (Customer) o;
    return id == customer.id
        && Objects.equals(name, customer.name)
        && Objects.equals(countryOfResidence, customer.countryOfResidence);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, countryOfResidence);
  }

  @Override
  public String toString() {
    return "Customer{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", countryOfResidence='"
        + countryOfResidence
        + '\''
        + '}';
  }
}
