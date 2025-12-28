/**
 * @file StorageType.java
 * @package com.ucoruh.password
 * @enum StorageType
 * @brief Enum for supported storage types.
 * @author Password Manager Team
 * @version 1.0
 *
 * This enum defines the types of storage available for password storage,
 * including file-based and SQLite-based implementations.
 */
package com.ucoruh.password;

/**
 * @brief Enum for supported storage types.
 */
public enum StorageType {
  /**
   * @brief Represents file-based storage.
   */
  FILE,

  /**
   * @brief Represents SQLite-based storage.
   */
  SQLITE
}
