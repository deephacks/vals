package org.deephacks.vals;

import java.util.HashMap;
import java.util.Map;

public interface VirtualState {
  /**
   * Get the value of a parameter.
   *
   * @param paramName parameter name
   * @return value of any type.
   */
  public Object get(String paramName);

  /**
   * Set value of a parameter.
   *
   * @param paramName parameter name
   * @param value of any type
   */
  public void set(String paramName, Object value);

  /**
   * @param paramName if data associated with this param is unavailable.
   * @return true if unavailable.
   */
  public boolean isNull(String paramName);

  public static class DefaultVirtualStorage implements VirtualState {

    private final Map<String, Object> values = new HashMap<>();

    public Object get(String paramName) {
      return values.get(paramName);
    }

    public void set(String paramName, Object value) {
      values.put(paramName, value);
    }

    public boolean isNull(String paramName) {
      return !values.containsKey(paramName);
    }

  }
}

