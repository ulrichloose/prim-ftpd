package org.primftpd.saf.reflective;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil
{
	public static Object getConstant(Class<?> clazz, String name)
		throws NoSuchFieldException, IllegalAccessException,
		IllegalArgumentException
	{
		Field field = clazz.getDeclaredField(name);
		return field.get(null);
	}

	public static Object invokeMethod(Object obj, String name, Object... args)
		throws IllegalAccessException, IllegalArgumentException,
		InvocationTargetException
	{
		Class<? extends Object> clazz = obj.getClass();
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.getName().equals(name)) {
				return method.invoke(obj, args);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T cast(Object obj) {
		return (T)obj;
	}
}
