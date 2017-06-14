package es.upm.ies.goniophotometer.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import java.util.Properties;
import java.util.Set;

/**
 * Class used to manage the properties of a program.<br>
 * <br>
 * Programs that are developed to maximize compatibility have to deal with the
 * problem of defining default properties. When distributing the program, the
 * users will surely change the default properties to those that suit best at
 * that moment. However, when they restore to the default properties they may
 * not want to restore all the properties to the manufactured default properties
 * (those set by the developer when writing the code). Instead, the may want to
 * restore the properties to a default value set by them (probably in the first
 * use). For this reason, this class has three set of properties:<br>
 * <br>
 * <ul>
 * <li><b>Developer defaults</b>: Properties set by the developer. Probably useless to
 * the user, but a start point when executing the program. Furthermore, if
 * something goes wrong with the properties files, the program using this class
 * can restore this set of properties instead of crashing. They must be
 * hard-coded by the programmer using <code>setDeveloperDefaults</code> method
 * and not read from a file.</li>
 * <li><b>Default properties</b>: The values of this properties are the same as the
 * developer defaults at first. However, they can be changed by the user in
 * order to establish a point-of-return. They are stored in a file called
 * default_config.properties.</li>
 * <li><b>Properties</b>: These are the current properties defined by the user. They
 * are recovered session after session from a file called config.properties.
 * They can be restored whenever the user wants to the default values (from the
 * default properties set).</li>
 * </ul>
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public final class PropsMngr {

	// Singleton class
	private static PropsMngr propsMngr = null;

	private Properties props = new Properties();
	// Default properties
	private Properties defaults = new Properties();
	// Developer's default properties
	private Properties developerDefaults = new Properties();

	/*
	 * Creates the only instance of <code>PropsMngr</code> during execution
	 * time. It loads the default properties.
	 */
	private PropsMngr() {
		loadDefaultProperties();
	}

	/**
	 * Returns the only instance of <code>PropsMngr</code>.
	 * 
	 * @return The only instance of <code>PropsMngr</code>.
	 */
	public static PropsMngr getInstance() {
		if (propsMngr == null) {
			propsMngr = new PropsMngr();
		}
		return propsMngr;
	}

	/**
	 * Saves the properties to a file named config.properties.
	 * 
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public void saveProps() throws IOException {
		OutputStream out = null;
		try {
			out = new FileOutputStream("config.properties");
			props.store(out, null);
		} catch (IOException e1) {
			throw e1;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e2) {
					throw e2;
				}
			}
		}
	}

	/**
	 * Saves the default properties to a file named default_config.properties.
	 */
	public void saveDefaults() {
		OutputStream out = null;
		try {
			out = new FileOutputStream("default_config.properties");
			defaults.store(out, null);
		} catch (IOException e1) {
			/*
			 * Do nothing. It's OK if the default properties are not written
			 * into a file.
			 */
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e2) {
					// Do nothing
				}
			}
		}
	}

	/**
	 * Loads the properties from the file config.properties. If the properties
	 * has different keys than the developer defaults after loading, then the
	 * defaults are written to the properties.
	 */
	public void loadProperties() {
		boolean clean = true;
		InputStream in = null;
		try {
			in = new FileInputStream("config.properties");
			props.load(in);
		} catch (IOException e1) {
			/* Do nothing - by this point, props is not clean anyway */
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e2) {
					/* Do nothing - by this point, props is not clean anyway */
				}
			}
		}
		// Check that config.properties has no missing keys (nor excess keys)
		if (!props.keySet().containsAll(developerDefaults.keySet())) {
			clean = false;
		}

		if (!clean) {
			try {
				restoreDefaults();
			} catch (IOException e3) {
				/*
				 * Do nothing. It doesn't matter if the properties can't get
				 * stored into a file.
				 */
			}
		}
	}

	/**
	 * Loads the default properties from the file default_config.properties. If
	 * the default properties has different keys than the developer defaults
	 * after loading, then the developer defaults are written to the default
	 * properties.
	 */
	public void loadDefaultProperties() {
		boolean clean = true;
		InputStream in = null;
		try {
			in = new FileInputStream("default_config.properties");
			defaults.load(in);
		} catch (IOException e1) {
			/* Do nothing */
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e2) {
					/* Do nothing */
				}
			}
		}
		/*
		 * Check that the default properties has no missing keys (nor excess
		 * keys) after loading
		 */
		if (!defaults.keySet().containsAll(developerDefaults.keySet())) {
			clean = false;
		}
		if (!clean) {
			writeNewDefaultProperties();
		}
	}

	/*
	 * Assign the developer defaults to the defaults and store them.
	 */
	private void writeNewDefaultProperties() {
		defaults.putAll(developerDefaults);
		// Write defaults to default_config.properties
		saveDefaults();
	}

	/**
	 * Returns the property value with the specified key value. If the key is
	 * not found, it assigns its value to be the default value and returns the
	 * default value. If the key isn't among the defaults either, it assigns its
	 * value according to the developer defaults and then returns it.
	 * 
	 * @param key
	 *            The property key.
	 * @return The value with the specified key value.
	 */
	public String getProperty(String key) {
		String value = props.getProperty(key);
		if (value == null) {
			value = getDefaultProperty(key);
			try {
				restoreDefaults();
			} catch (IOException e) {
				/*
				 * Do nothing. It doesn't matter if the properties can't get
				 * stored into a file.
				 */
			}
		}
		return value;
	}

	/**
	 * Sets the value of the property with the specified key.
	 * 
	 * @param key
	 *            The value to be set.
	 * @param value
	 *            The property key.
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public void setProperty(String key, String value) throws IOException {
		try {
			props.setProperty(key, value);
			// Write properties into config.properties
			saveProps();
		} catch (IOException e1) {
			throw e1;
		}
	}

	/**
	 * Sets the properties specified in a KEY-VALUE
	 * <code>HashMap&#60;String, String&#62;</code>.
	 * 
	 * @param mapProps
	 *            HashMap with the specified properties to be set. It must have
	 *            a (KEY,VALUE) format.
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public void setProperties(HashMap<String, String> mapProps) throws IOException {
		// aux entry set
		Set<Entry<String, String>> auxes = mapProps.entrySet();
		Iterator<Entry<String, String>> itr = auxes.iterator();

		try {
			while (itr.hasNext()) {
				Entry<String, String> mapEntry = itr.next();
				props.setProperty(mapEntry.getKey(), mapEntry.getValue());
			}
			// Write properties into config.properties
			saveProps();
		} catch (IOException e1) {
			throw e1;
		}
	}

	/**
	 * Returns the default value with the specified key value.
	 * 
	 * @param key
	 *            The property key.
	 * @return The default value with the specified key value.
	 */
	public String getDefaultProperty(String key) {
		String value = defaults.getProperty(key);
		if (value == null) {
			writeNewDefaultProperties();
			value = defaults.getProperty(key);
		}
		return value;
	}

	/**
	 * Sets the default value of the property with the specified key value.
	 * 
	 * @param key
	 *            The property key.
	 * @param value
	 *            The value to be set
	 */
	public void setDefaultProperty(String key, String value) {
		defaults.setProperty(key, value);
	}

	/**
	 * Sets the developer defaults. It is important to make a call to this
	 * method right after calling to <code>getInstance()</code> for the first
	 * time in your code. The parameter <code>developerDefaults</code> must
	 * contain all the properties that are going to be used through your
	 * program. After setting the developer defaults, it calls
	 * <code>loadDefaults</code>.
	 * 
	 * @param developerDefaults
	 *            Properties instance with all the properties to be used through
	 *            the program.
	 */
	public void setDeveloperDefaults(Properties developerDefaults) {
		this.developerDefaults.putAll(developerDefaults);
		loadDefaultProperties();
	}

	/**
	 * Assign the default value to all the properties.
	 * 
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public void restoreDefaults() throws IOException {
		loadDefaultProperties();
		props.putAll(defaults);
		// Write properties into config.properties
		saveProps();
	}

	/**
	 * Convert the current properties into default properties.
	 */
	public void mkPropsDefault() {
		defaults.putAll(props);
		saveDefaults();
	}
}
