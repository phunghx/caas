/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tf.jcaas.common;

import com.google.common.collect.ImmutableMap;

import java.util.LinkedHashMap;
import java.util.Map;


public class ImmutableSettings implements Settings {

    public static final ImmutableSettings EMPTY = new ImmutableSettings();
    private final ImmutableMap<String, String> _options;

    private ImmutableSettings() {
        _options = new ImmutableMap.Builder<String, String>().build();
    }

    private ImmutableSettings(Builder builder) {
        _options = ImmutableMap.copyOf(builder._options);
    }

    @Override
    public String get(String key) {
        return _options.get(key);
    }

    @Override
    public long getAsLong(String key, long defaultVal) {
        String get = _options.get(key);
        if (get == null) {
            return defaultVal;
        }
        try {
            return Long.parseLong(get);
        } catch (NumberFormatException nfe) {
            return defaultVal;
        }
    }

    @Override
    public int getAsInteger(String key, int defaultVal) {
        String get = _options.get(key);
        if (get == null) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(get);
        } catch (NumberFormatException nfe) {

            return defaultVal;
        }
    }

    @Override
    public double getAsDouble(String key, double defaultVal) {
        String get = _options.get(key);
        if (get == null) {
            return defaultVal;
        }
        try {
            return Double.parseDouble(get);
        } catch (NumberFormatException nfe) {

            return defaultVal;
        }
    }

    @Override
    public String[] getAsList(String key, String delimiter, String[] defaultVal) {
        if (!_options.containsKey(key)) {
            return defaultVal;
        }
        String value = _options.get(key);
        return value.split(delimiter);
    }

    @Override
    public int hashCode() {
        int temp = _options != null ? _options.hashCode() : 0;
        return 31 * temp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final ImmutableSettings other = (ImmutableSettings) obj;

        return (_options != null ? _options.equals(other._options) : other._options == null);
    }

    public static class Builder {

        private final Map<String, String> _options = new LinkedHashMap<>();

        public Builder put(String key, String value) {
            _options.put(key, value);
            return this;
        }

        public Builder put(Map<String, String> options) {
            _options.putAll(options);
            return this;
        }

        public ImmutableSettings build() {
            return new ImmutableSettings(this);
        }
    }

}
