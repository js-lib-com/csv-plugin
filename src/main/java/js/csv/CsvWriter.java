package js.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import js.converter.Converter;
import js.converter.ConverterRegistry;
import js.util.Classes;
import js.util.Params;
import js.util.Strings;

/**
 * CSV writer.
 * 
 * @author Iulian Rotaru
 */
public class CsvWriter implements Closeable {
	/** Converter used to transform object field values into strings. */
	private final Converter converter = ConverterRegistry.getConverter();

	/** HTTP response output stream used to serialize model into CSV format. */
	private final Writer writer;

	/** CSV value descriptors related to CSV columns. */
	private final List<ValueDescriptor> valueDescriptors;

	/** CSV columns separator. */
	private final char separator;

	/** Null value used when object property is null. */
	private final String nullValue;

	public CsvWriter(CsvDescriptor descriptor, Writer writer) throws IOException {
		this.writer = writer;
		valueDescriptors = descriptor.getValueDescriptors();
		separator = descriptor.getSeparator();
		nullValue = descriptor.getNullValue();

		writeHeader();
	}

	public CsvWriter(Writer writer) {
		this.writer = writer;
		valueDescriptors = null;
		separator = CsvDescriptor.DEFAULT_SEPARATOR;
		nullValue = CsvDescriptor.DEFAULT_NULL_VALUE;
	}

	/**
	 * Write object fields to output CSV stream followed by new line.
	 * 
	 * @param object object to serialize as CSV line.
	 * @throws UnsupportedEncodingException if JVM does not support UTF-8 encoding. ;-)
	 * @throws IOException if writing to output CSV stream fails.
	 */
	public void write(Object object) throws IOException {
		Params.notNull(object, "Object argument");
		write(getFieldValue(object, 0));
		for (int i = 1; i < valueDescriptors.size(); ++i) {
			write(separator);
			write(getFieldValue(object, i));
		}
		write("\r\n");
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	/**
	 * Write CSV header.
	 * 
	 * @throws UnsupportedEncodingException if JVM does not support UTF-8 encoding. ;-)
	 * @throws IOException if writing to output CSV stream fails.
	 */
	private void writeHeader() throws UnsupportedEncodingException, IOException {
		write(valueDescriptors.get(0).getName());
		for (int i = 1; i < valueDescriptors.size(); ++i) {
			write(separator);
			write(valueDescriptors.get(i).getName());
		}
		write("\r\n");
	}

	/**
	 * Write string to output stream.
	 * 
	 * @param string string to write.
	 * @throws UnsupportedEncodingException if JVM does not support UTF-8 encoding. ;-)
	 * @throws IOException if writing to output CSV stream fails.
	 */
	private void write(String string) throws UnsupportedEncodingException, IOException {
		writer.write(string);
	}

	/**
	 * Writer character to output stream.
	 * 
	 * @param c character to write.
	 * @throws IOException if writing to output CSV stream fails.
	 */
	private void write(char c) throws IOException {
		write(Character.toString(c));
	}

	/**
	 * Get object field string value described by value descriptor with given column index.
	 * 
	 * @param object source object to get field value from,
	 * @param columnIndex column index.
	 * @return object field string value.
	 */
	private String getFieldValue(Object object, int columnIndex) {
		ValueDescriptor valueDescriptor = valueDescriptors.get(columnIndex);
		Object value = Classes.getFieldValue(object, valueDescriptor.getProperty());
		if (value == null) {
			return nullValue;
		}
		if (valueDescriptor.getFormat() != null) {
			return valueDescriptor.getFormat().format(value);
		}
		return escape(converter.asString(value));
	}

	/**
	 * Escape reserved characters. A CSV value is not allowed to contain newline or car return since is used as lines separator,
	 * or current selected column separator. Also double quote is used for escaping and is illegal too inside value.
	 * <p>
	 * If value contains illegal characters it is enclosed between double quotes. Also if value already contains double quotes
	 * it is doubled. See below syntax.
	 * 
	 * <pre>
	 * escaped = DQUOTE *(TEXTDATA / COMMA / CR / LF / 2DQUOTE) DQUOTE
	 * </pre>
	 * 
	 * @param value string value.
	 * @return normalized string value.
	 */
	private String escape(String value) {
		switch (needEscaping(value)) {
		case NONE:
			return value;

		case INTERNAL:
			StringBuilder builder = new StringBuilder();
			builder.append('"');
			for (int i = 0; i < value.length(); ++i) {
				final char c = value.charAt(i);
				if (c == '"') {
					builder.append('"');
				}
				builder.append(c);
			}
			builder.append('"');
			return builder.toString();

		case ENCLOSED:
			return Strings.concat('"', value, '"');
		}

		// just makes compiler happy; we never step here
		throw new IllegalStateException();
	}

	/**
	 * Detect if string value contains illegal characters: separator, CR, LF or double quotes.
	 * 
	 * @param value string value.
	 * @return true if string value contains illegal characters and need escaping.
	 */
	private EscapeProcessing needEscaping(String value) {
		EscapeProcessing escape = EscapeProcessing.NONE;
		for (int i = 0; i < value.length(); ++i) {
			final char c = value.charAt(i);
			if (c == '"') {
				return EscapeProcessing.INTERNAL;
			}
			if (c == separator || c == '\r' || c == '\n') {
				escape = EscapeProcessing.ENCLOSED;
			}
		}
		return escape;
	}

	/**
	 * Automata states for double quote escape processing.
	 * 
	 * @author Iulian Rotaru
	 */
	private enum EscapeProcessing {
		/** No escape processing required since string value does not contain illegal characters. */
		NONE,
		/** String value contains illegal characters but NOT double quotes and need only to be enclosed into double quotes. */
		ENCLOSED,
		/**
		 * String value contains double quote and should process internal value characters. Internal processing require enclosed
		 * processing too.
		 */
		INTERNAL
	}
}
