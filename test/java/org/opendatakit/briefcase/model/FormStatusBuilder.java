/*
 * Copyright (C) 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.briefcase.model;

import static java.util.stream.Collectors.toList;
import static org.opendatakit.briefcase.model.FormStatus.TransferType.EXPORT;

import java.util.List;
import java.util.stream.IntStream;

public class FormStatusBuilder {

  public static FormStatus buildFormStatus(int id) {
    try {
      return new FormStatus(EXPORT, new TestFormDefinition(id));
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public static List<FormStatus> buildFormStatusList(int amount) {
    return IntStream.range(0, amount).boxed().map(FormStatusBuilder::buildFormStatus).collect(toList());
  }
}
