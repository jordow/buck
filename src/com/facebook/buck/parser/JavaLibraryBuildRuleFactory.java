/*
 * Copyright 2012-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.parser;

import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.AbstractBuildRuleBuilder;
import com.facebook.buck.rules.AnnotationProcessingParams;
import com.facebook.buck.rules.DefaultJavaLibraryRule;
import com.facebook.buck.shell.JavacOptionsUtil;
import com.google.common.base.Optional;

import java.util.List;

class JavaLibraryBuildRuleFactory extends AbstractBuildRuleFactory {

  @Override
  DefaultJavaLibraryRule.Builder newBuilder() {
    return DefaultJavaLibraryRule.newJavaLibraryRuleBuilder();
  }

  @Override
  protected void amendBuilder(AbstractBuildRuleBuilder abstractBuilder,
                              BuildRuleFactoryParams params) throws NoSuchBuildTargetException {
    DefaultJavaLibraryRule.Builder builder = ((DefaultJavaLibraryRule.Builder) abstractBuilder);

    Optional<String> proguardConfig = params.getOptionalStringAttribute("proguard_config");
    if (proguardConfig.isPresent()) {
      String proguardConfigFile =
          params.resolveFilePathRelativeToBuildFileDirectory(proguardConfig.get());
      builder.setProguardConfig(proguardConfigFile);
    }

    extractAnnotationProcessorParameters(
        builder.getAnnotationProcessingBuilder(), builder, params);

    Optional<String> sourceLevel = params.getOptionalStringAttribute("source");
    builder.setSourceLevel(sourceLevel.or(JavacOptionsUtil.DEFAULT_SOURCE_LEVEL));

    Optional<String> targetLevel = params.getOptionalStringAttribute("target");
    builder.setTargetLevel(targetLevel.or(JavacOptionsUtil.DEFAULT_TARGET_LEVEL));
  }

  static void extractAnnotationProcessorParameters(
      AnnotationProcessingParams.Builder annotationProcessingBuilder,
      AbstractBuildRuleBuilder buildRuleBuilder,
      BuildRuleFactoryParams params)
      throws NoSuchBuildTargetException {

    // annotation_processors
    //
    // Names of the classes used for annotation processing.  These must be implemented
    // in a BuildTarget listed in annotation_processor_deps.
    List<String> annotationProcessors = params.getOptionalListAttribute("annotation_processors");
    if (!annotationProcessors.isEmpty()) {
      annotationProcessingBuilder.addAllProcessors(annotationProcessors);

      // annotation_processor_deps
      //
      // These are the targets that implement one or more of the annotation_processors.
      BuildTarget target = params.target;
      ParseContext buildFileParseContext = ParseContext.forBaseName(target.getBaseName());

      for (String processor : params.getOptionalListAttribute("annotation_processor_deps")) {
        BuildTarget buildTarget = params.buildTargetParser.parse(processor, buildFileParseContext);
        buildRuleBuilder.addDep(buildTarget.getFullyQualifiedName());
        annotationProcessingBuilder.addProcessorBuildTarget(buildTarget);
      }

      // annotation_processor_params
      //
      // These will be prefixed with "-A" when passing to javac.  They may be of the
      // form "parameter_name" or "parameter_name=string_value".
      for (String parameter : params.getOptionalListAttribute("annotation_processor_params")) {
        annotationProcessingBuilder.addParameter(parameter);
      }
    }
  }
}
