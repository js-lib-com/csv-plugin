package js.csv;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import js.lang.Config;
import js.lang.ConfigBuilder;
import js.lang.ConfigException;
import js.util.Classes;

public final class CsvDescriptor {
	/** Default CSV columns separator. */
	public static final char DEFAULT_SEPARATOR = ',';
	/** Default string used for null values. */
	public static final String DEFAULT_NULL_VALUE = "";

	/** Separators for CSV columns. See {@link CsvDescriptor} for current supported values. */
	private static final Map<String, Character> SEPARATORS = new HashMap<>();
	static {
		SEPARATORS.put("tab", '\t');
		SEPARATORS.put("space", ' ');
		SEPARATORS.put("comma", ',');
		SEPARATORS.put("dot", '.');
		SEPARATORS.put("colon", ':');
		SEPARATORS.put("semicolon", ';');
	}

	/** Class with fields mapped to CSV columns. */
	private final Class<?> type;
	/**
	 * Optional CSV columns separator, default to comma. Loaded from <code>separator</code> attribute of CSV root element. See
	 * {@link CsvDescriptor} for current supported values.
	 */
	private final Character separator;

	/** Optional value used when object property is null, default to empty string. */
	private final String nullValue;

	/** Optional characters set used to encode CSV content, default to UTF-8. */
	private Charset charset;
	
	private final boolean debug;

	/** CSV value descriptors related to CSV columns. */
	private final List<ValueDescriptor> valueDescriptors;

	/**
	 * Create CSV descriptor instance and initialize it from configuration object.
	 * 
	 * @param config CSV configuration object.
	 * @throws CsvException if configuration object is invalid.
	 */
	public CsvDescriptor(Config config) throws CsvException {
		String className = config.getAttribute("class");
		if (className == null) {
			throw new CsvException("Invalid CSV configuration. Missing class attribute.");
		}
		this.type = Classes.forOptionalName(className);
		if (this.type == null) {
			throw new CsvException("Invalid CSV configuration. Class |%s| not found.", className);
		}
		if (!Classes.isInstantiable(this.type)) {
			throw new CsvException("Invalid CSV configuration. Class |%s| not instantiable.", className);
		}

		String separatorValue = config.getAttribute("separator");
		if (separatorValue == null) {
			this.separator = CsvDescriptor.DEFAULT_SEPARATOR;
		} else {
			this.separator = SEPARATORS.get(separatorValue);
			if (this.separator == null) {
				throw new CsvException("Invalid CSV configuration. Separator |%s| not supported.", separatorValue);
			}
		}

		this.nullValue = config.getAttribute("null-value", CsvDescriptor.DEFAULT_NULL_VALUE);
		this.charset = Charset.forName(config.getAttribute("charset", "UTF-8"));
		this.debug = config.getAttribute("debug", boolean.class, false);

		this.valueDescriptors = new ArrayList<ValueDescriptor>();
		for (Config childConfig : config.getChildren()) {
			this.valueDescriptors.add(new ValueDescriptor(childConfig));
		}
	}

	public CsvDescriptor(InputStream configStream) throws CsvException {
		this(config(configStream));
	}

	private static Config config(InputStream configStream) throws CsvException {
		ConfigBuilder builder = new ConfigBuilder(configStream);
		try {
			return builder.build();
		} catch (ConfigException e) {
			throw new CsvException("Invalid configuration source.");
		}
	}

	public Class<?> getType() {
		return type;
	}

	public char getSeparator() {
		return separator;
	}

	public String getNullValue() {
		return nullValue;
	}

	public Charset getCharset() {
		return charset;
	}

	public boolean isDebug() {
		return debug;
	}

	public List<ValueDescriptor> getValueDescriptors() {
		return valueDescriptors;
	}
}
