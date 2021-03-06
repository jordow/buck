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

import com.facebook.buck.rules.AbstractBuildRuleBuilder;
import com.facebook.buck.rules.ProjectConfigRule;
import com.google.common.base.Function;
import com.google.common.base.Optional;

import java.util.List;

public class ProjectConfigRuleFactory extends AbstractBuildRuleFactory {

  @Override
  ProjectConfigRule.Builder newBuilder() {
    return ProjectConfigRule.newProjectConfigRuleBuilder();
  }

  @Override
  protected void amendBuilder(AbstractBuildRuleBuilder abstractBuilder,
      BuildRuleFactoryParams params)
      throws NoSuchBuildTargetException {
    ProjectConfigRule.Builder builder = ((ProjectConfigRule.Builder)abstractBuilder);
    Function<String, String> contextualBuildParser = createBuildTargetParseFunction(params);

    // src_target
    Optional<String> srcTargetId = params.getOptionalStringAttribute("src_target");
    if (srcTargetId.isPresent()) {
      builder.setSrcTarget(contextualBuildParser.apply(srcTargetId.get()));
    }

    // src_roots
    // Note that the values in this array are passed as-is, rather than resolved to local project
    // paths.
    List<String> srcRoots = params.getNullableListAttribute("src_roots");
    builder.setSrcRoots(srcRoots);

    // test_target
    Optional<String> testTargetId = params.getOptionalStringAttribute("test_target");
    if (testTargetId.isPresent()) {
      builder.setTestTarget(contextualBuildParser.apply(testTargetId.get()));
    }

    // test_roots
    // Note that this value is passed as-is, rather than resolved to a project local path.
    List<String> testRoots = params.getNullableListAttribute("test_roots");
    builder.setTestRoots(testRoots);

    // is_intellij_plugin
    builder.setIsIntelliJPlugin(params.getBooleanAttribute("is_intellij_plugin"));
  }
}
