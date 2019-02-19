package js.csv;

import js.format.Format;
import js.lang.Config;
import js.log.Log;
import js.log.LogFactory;
import js.util.Classes;
import js.util.Types;

/**
 * CSV value descriptor for column name, object property and optional value format. Value descriptor is part of
 * {@link CsvDescriptor}; in fact CSV descriptor is a list of value descriptors.
 * <p>
 * A value descriptor is just a map of CSV column to object instance field. It has a name, used on CSV header and a property
 * that identify object field mapped to CSV column. Optional value descriptor has a formatter.
 * 
 * <pre>
 * &lt;value name="Communication Date" property="communicationDate" format="js.format.ShortDate" /&gt;
 * </pre>
 * 
 * @author Iulian Rotaru
 * @version draft
 */
final class ValueDescriptor {
	private static final Log log = LogFactory.getLog(ValueDescriptor.class);

	private final String name;
	/** Named value mandates header. */
	private final boolean namedValue;
	private final String property;
	private final Format format;

	/**
	 * Load values descriptor from configuration element.
	 * 
	 * @param element configuration element.
	 * @throws CsvException
	 */
	public ValueDescriptor(Config element) throws CsvException {
		String name = element.getAttribute("name");
		if (name == null) {
			name = element.getAttribute("index");
			if (name == null) {
				throw new CsvException("Value descriptor should have either <index> or <name> attribute.");
			}
			this.namedValue = false;
		} else {
			this.namedValue = true;
		}
		this.name = name;

		this.property = element.getAttribute("property");
		if (this.property == null) {
			throw new CsvException("Value descriptor should have <property> attribute.");
		}

		String className = element.getAttribute("format");
		this.format = className != null ? createFormatter(className) : null;
	}

	public String getName() {
		return name;
	}

	public boolean isNamedValue() {
		return namedValue;
	}

	public String getProperty() {
		return property;
	}

	public boolean hasFormat() {
		return format != null;
	}

	public Format getFormat() {
		return format;
	}

	/**
	 * Create instance of named formatter. Returns null if formatter class not found.
	 * 
	 * @param className formatter qualified class name.
	 * @return formatter instance or null.
	 * @throws ClassCastException if found class cannot cast to formatter.
	 */
	private static Format createFormatter(String className) {
		Class<? extends Format> formatterClass = Classes.forOptionalName(className);
		if (formatterClass == null) {
			log.error("Formatter class |%s| not found.", className);
			return null;
		}
		if (Types.isKindOf(formatterClass, Format.class)) {
			return Classes.newInstance(formatterClass);
		}
		return null;
	}
}