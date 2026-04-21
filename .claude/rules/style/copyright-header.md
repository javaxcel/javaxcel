---
name: copyright-header
description: Every .java and .groovy source file must begin with the Apache 2.0 copyright header.
globs: ["**/*.java", "**/*.groovy"]
alwaysApply: false
---

RULE: Every .java and .groovy source file must start with the Apache 2.0 copyright block before the package declaration.

WHY: The project uses the Apache 2.0 license; the header template is defined in `intellij-copyright.txt`. All existing files under `core/src/main/java/` carry this header (e.g., `AbstractExcelReader.java` line 1).

- Header format (exact):
  ```
  /*
   * Copyright <YEAR> Javaxcel
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *     http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */
  ```
- `<YEAR>` is the file's **creation year**, not the current year. Check neighboring files in the same package to determine the year (e.g., files in `in/strategy/impl/` use `2021`).
- When **editing** an existing file, preserve the header unchanged — do not update the year.
- When **creating** a new file, infer the year from sibling files in the same package.
- Never strip or omit the header.
- The header goes on line 1, followed by a blank line, then `package ...`.
