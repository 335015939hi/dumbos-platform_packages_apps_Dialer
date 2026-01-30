/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.common.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * Utility class for closing Closeable objects.
 * This is a stub implementation to replace the AOSP internal com.android.common.io package.
 */
public class MoreCloseables {

  private MoreCloseables() {
    // Private constructor to prevent instantiation
  }

  /**
   * Closes the given closeable, ignoring any exceptions.
   *
   * @param closeable The closeable to close, may be null
   */
  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        // Silently ignore
      } catch (RuntimeException e) {
        // Silently ignore
      }
    }
  }
}
