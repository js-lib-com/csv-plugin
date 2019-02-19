package js.csv;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import js.converter.Converter;
import js.converter.ConverterRegistry;
import js.log.Log;
import js.log.LogFactory;
import js.util.Classes;

/**
 * Reads objects of specified type from CSV stream via iterator or iterable interface.
 * 
 * @author Iulian Rotaru
 * @param <T> CSV objects type.
 * @version draft
 */
public final class CsvReader<T> implements Iterator<T>, Iterable<T>, Closeable {
	private static Log log = LogFactory.getLog(CsvReader.class);

	private final Class<T> type;
	private final BufferedReader reader;

	private final Converter converter;
	private final CsvDescriptor descriptor;
	private String line;

	/** Line index for error reporting. */
	private int lineIndex;

	@SuppressWarnings("unchecked")
	public CsvReader(CsvDescriptor descriptor, InputStream stream) throws IOException {
		log.trace("CsvReaderImpl(CsvDescriptor, InputStream)");
		type = (Class<T>) descriptor.getType();
		reader = new BufferedReader(new InputStreamReader(stream, descriptor.getCharset()));

		converter = ConverterRegistry.getConverter();
		this.descriptor = descriptor;

		line = reader.readLine();
		if (line == null) {
			throw new CsvException("Empty CSV stream.");
		}
		if (line.isEmpty()) {
			throw new CsvException("Invalid CSV file. First line should be header but is empty.");
		}

		if (descriptor.hasHeader()) {
			nextLine();
		}
	}

	@Override
	public boolean hasNext() {
		return line != null;
	}

	@Override
	public T next() {
		if (descriptor.isDebug()) {
			log.debug(line);
		}
		T instance = Classes.newInstance(type);

		List<String> values = parseLine(line, descriptor.getSeparator());
		List<ValueDescriptor> valueDescriptors = descriptor.getValueDescriptors();
		if (values.size() != valueDescriptors.size()) {
			log.debug("Expect |%d| columns on CSV but found |%d|.", valueDescriptors.size(), values.size());
			log.warn("Invalid CSV line on index |%d|: |%s|", lineIndex, line);
		} else {
			for (int valueIndex = 0; valueIndex < valueDescriptors.size(); ++valueIndex) {
				ValueDescriptor valueDescriptor = valueDescriptors.get(valueIndex);
				Field field = Classes.getField(type, valueDescriptor.getProperty());
				Object value = null;
				if (valueDescriptor.hasFormat()) {
					try {
						value = valueDescriptor.getFormat().parse(values.get(valueIndex));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						log.error(e);
					}
				} else {
					value = converter.asObject(values.get(valueIndex), field.getType());
				}
				Classes.setFieldValue(instance, field, value);
			}
		}

		nextLine();
		return instance;
	}

	public static List<String> parseLine(String line, char separator) {
		List<String> values = new ArrayList<>();
		StringBuilder valueBuilder = new StringBuilder();

		State state = State.VALUE_START;
		int quotesIndex = 0;

		for (int i = 0; i < line.length(); ++i) {
			final char c = line.charAt(i);

			switch (state) {
			case VALUE_START:
				if (c == '"') {
					state = State.UNESCAPE_VALUE;
					quotesIndex = 0;
					break;
				} else {
					state = State.READ_VALUE;
					// fall through READ_VALUE case
				}

			case READ_VALUE:
				if (c != separator) {
					valueBuilder.append(c);
				} else {
					values.add(valueBuilder.toString());
					valueBuilder.setLength(0);
					state = State.VALUE_START;
				}
				break;

			case UNESCAPE_VALUE:
				if (c == separator && quotesIndex % 2 == 1) {
					values.add(valueBuilder.toString());
					valueBuilder.setLength(0);
					state = State.VALUE_START;
					continue;
				}

				if (c == '"' && quotesIndex++ % 2 == 0) {
					// first double quote is ignored since it is used to escape double quote itself

					// this logic also solve double quote value ending:
					// - last char from an escaped value is double quote
					// - since it is first to appear in a double quotes pair it is ignored

					continue;
				}
				valueBuilder.append(c);
				break;
			}
		}

		values.add(valueBuilder.toString());
		return values;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	private void nextLine() {
		line = null;
		try {
			for (;;) {
				line = reader.readLine();
				++lineIndex;
				if (line == null) {
					return;
				}
				if (!line.isEmpty() && line.charAt(0) != '#') {
					break;
				}
			}
		} catch (IOException e) {
			log.error(e);
		}
	}

	/**
	 * CSV parser automata states.
	 * 
	 * @author Iulian Rotaru
	 */
	private enum State {
		VALUE_START, READ_VALUE, UNESCAPE_VALUE
	}
}
