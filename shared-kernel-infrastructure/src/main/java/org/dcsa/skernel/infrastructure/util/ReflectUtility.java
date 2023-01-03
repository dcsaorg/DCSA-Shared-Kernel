package org.dcsa.skernel.infrastructure.util;


import jakarta.el.MethodNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * A helper class with a lot of Reflection utilities
 */
public class ReflectUtility {

  private ReflectUtility() { }

  /**
   * Finds the getter method on clazz corresponding to the name fieldName with the arguments valueFieldTypes
   * @param clazz the class to investigate
   * @param fieldName the name of the field to find a getter method for
   * @return the method corresponding to fieldName with valueFieldTypes as arguments
   */
  public static Method getGetterMethodFromName(Class<?> clazz, String fieldName) {
    return getAccessorFromName(clazz, "get", fieldName);
  }

  private static Method getAccessorFromName(Class<?> clazz, String prefix, String fieldName, Class<?> ... valueFieldTypes) {
    try {
      // Try the raw field name as a method call
      return getMethod(clazz, fieldName, valueFieldTypes);
    } catch (Exception exception) {
      String capitalizedFieldName = capitalize(fieldName);
      return getMethod(clazz, prefix + capitalizedFieldName, valueFieldTypes);
    }
  }

  /**
   * Investigate if a class contains a method be the name methodName with arguments corresponding to valueFieldTypes.
   * The method must be public.
   * Throws a MethodNotFoundException if the method requested is not public or does not exist
   * @param clazz the class to investigate
   * @param methodName name of the method to return
   * @param valueFieldTypes arguments for the method to return
   * @return the public method corresponding to methodName and with the arguments containing valueFieldTypes
   */
  public static Method getMethod(Class<?> clazz, String methodName, Class<?>... valueFieldTypes) {
    try {
      Method tempMethod = clazz.getMethod(methodName, valueFieldTypes);
      if ((tempMethod.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC) {
        return tempMethod;
      } else {
        throw new MethodNotFoundException("Method: " + methodName + " is not public and thus cannot be accessed on Object:" + clazz.getName());
      }
    } catch (NoSuchMethodException noSuchMethodException) {
      throw new MethodNotFoundException("Method: " + methodName + " does not exist on on Object:" + clazz.getName(), noSuchMethodException);
    }
  }

  /**
   * Changes the first letter of name to uppercase and returns the result.
   *
   * @param name the name to change to Title-case
   * @return name with first letter capitalized
   */
  public static String capitalize(String name) {
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }


  /**
   * A helper method to get a field by the name of fieldName on the class provided by clazz
   *
   * @param clazz the class to investigate
   * @param fieldName the name of the field to find
   * @return the field with the name fieldName on the class clazz
   * @throws NoSuchFieldException if no field of the name fieldName exists on the class clazz
   */
  public static Field getDeclaredField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
    Objects.requireNonNull(clazz);
    Objects.requireNonNull(fieldName);
    try {
      return clazz.getDeclaredField(fieldName);
    } catch (NoSuchFieldException noSuchFieldException) {
      // Try the super class
      clazz = clazz.getSuperclass();
      if (clazz != Object.class) {
        return getDeclaredField(clazz, fieldName);
      }
      throw noSuchFieldException;
    }
  }

}
