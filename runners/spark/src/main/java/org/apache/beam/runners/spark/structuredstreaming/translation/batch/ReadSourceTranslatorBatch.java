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
package org.apache.beam.runners.spark.structuredstreaming.translation.batch;

import static org.apache.beam.runners.spark.structuredstreaming.Constants.BEAM_SOURCE_OPTION;
import static org.apache.beam.runners.spark.structuredstreaming.Constants.DEFAULT_PARALLELISM;
import static org.apache.beam.runners.spark.structuredstreaming.Constants.PIPELINE_OPTIONS;

import java.io.IOException;
import org.apache.beam.runners.core.construction.ReadTranslation;
import org.apache.beam.runners.core.serialization.Base64Serializer;
import org.apache.beam.runners.spark.structuredstreaming.translation.AbstractTranslationContext;
import org.apache.beam.runners.spark.structuredstreaming.translation.TransformTranslator;
import org.apache.beam.runners.spark.structuredstreaming.translation.helpers.EncoderHelpers;
import org.apache.beam.runners.spark.structuredstreaming.translation.helpers.RowHelpers;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.runners.AppliedPTransform;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.windowing.GlobalWindow;
import org.apache.beam.sdk.util.WindowedValue;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

class ReadSourceTranslatorBatch<T>
    implements TransformTranslator<PTransform<PBegin, PCollection<T>>> {

  private static final String sourceProviderClass = DatasetSourceBatch.class.getCanonicalName();

  @SuppressWarnings("unchecked")
  @Override
  public void translateTransform(
      PTransform<PBegin, PCollection<T>> transform, AbstractTranslationContext context) {
    AppliedPTransform<PBegin, PCollection<T>, PTransform<PBegin, PCollection<T>>> rootTransform =
        (AppliedPTransform<PBegin, PCollection<T>, PTransform<PBegin, PCollection<T>>>)
            context.getCurrentTransform();

    BoundedSource<T> source;
    try {
      source = ReadTranslation.boundedSourceFromTransform(rootTransform);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    SparkSession sparkSession = context.getSparkSession();

    String serializedSource = Base64Serializer.serializeUnchecked(source);
    Dataset<Row> rowDataset =
        sparkSession
            .read()
            .format(sourceProviderClass)
            .option(BEAM_SOURCE_OPTION, serializedSource)
            .option(
                DEFAULT_PARALLELISM,
                String.valueOf(context.getSparkSession().sparkContext().defaultParallelism()))
            .option(PIPELINE_OPTIONS, context.getSerializableOptions().toString())
            .load();

    // extract windowedValue from Row
    WindowedValue.FullWindowedValueCoder<T> windowedValueCoder =
        WindowedValue.FullWindowedValueCoder.of(
            source.getOutputCoder(), GlobalWindow.Coder.INSTANCE);

    Dataset<WindowedValue<T>> dataset =
        rowDataset.map(
            RowHelpers.extractWindowedValueFromRowMapFunction(windowedValueCoder),
            EncoderHelpers.fromBeamCoder(windowedValueCoder));

    PCollection<T> output = (PCollection<T>) context.getOutput();
    context.putDataset(output, dataset);
  }
}
