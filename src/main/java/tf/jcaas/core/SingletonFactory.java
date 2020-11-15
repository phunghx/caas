/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tf.jcaas.core;

import tf.jcaas.common.ImmutableSettings;
import tf.jcaas.common.Settings;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SingletonFactory will be able to factory an object An object typically is
 * controller or external service
 */
public class SingletonFactory {

	protected static final Map<Class, Map<Settings, SingletonObject>> _mapControllers = new ConcurrentHashMap<>();

	/**
	 *
	 * @param <T> Singleton Type
	 * @param cls Singleton Class To Get
	 * @return Return controller with empty settings or null
	 */
	public static <T> T getInstance(Class<T> cls) {
		return getInstance(cls, ImmutableSettings.EMPTY);
	}

	/**
	 *
	 * @param <T> Singleton Type
	 * @param cls Singleton Class To Get
	 * @param settings Setting to build controller
	 * @return Return controller with settings has built or null
	 */
	public static <T> T getInstance(Class<T> cls, Settings settings) {
		assert settings != null;
		if (!_mapControllers.containsKey(cls)) {
			synchronized (_mapControllers) {
				if (!_mapControllers.containsKey(cls)) {
					_mapControllers.put(cls, new ConcurrentHashMap<Settings, SingletonObject>());
				}
			}
		}
		Map<Settings, SingletonObject> _classInst = _mapControllers.get(cls);
		if (!_classInst.containsKey(settings)) {
			synchronized (cls.getClass()) {
				if (!_classInst.containsKey(settings)) {
					try {
						if (SingletonObject.class.isAssignableFrom(cls)) {
							Constructor<T> constructor = cls.getConstructor(Settings.class);
							_classInst.put(settings, (SingletonObject) constructor.newInstance(settings));
						} else {
							throw new IllegalArgumentException(cls.getName() + " is not assignable from BaseController");
						}
					} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
						Logger.getLogger(SingletonFactory.class).error(ex);
					}
				}
			}
		}
		return (T) _classInst.get(settings);

	}
}
