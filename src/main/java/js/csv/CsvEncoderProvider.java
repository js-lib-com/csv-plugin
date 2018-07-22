package js.csv;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import js.http.ContentType;
import js.http.encoder.ArgumentsReader;
import js.http.encoder.EncoderKey;
import js.http.encoder.HttpEncoderProvider;
import js.http.encoder.ValueWriter;

/**
 * Server HTTP encoder provider for CSV types. Current implementation provides only arguments reader for {@link CsvReader}
 * transported as <code>multipart/form-data</code>.
 * 
 * @author Iulian Rotaru
 * @version draft
 */
public class CsvEncoderProvider implements HttpEncoderProvider {
	/** Registered CSV arguments readers. */
	private static final Map<EncoderKey, ArgumentsReader> READERS = new HashMap<>();
	static {
		READERS.put(new EncoderKey(ContentType.MULTIPART_FORM, CsvReader.class), new CsvMultipartFormArgumentsReader());
	}

	/**
	 * Get HTTP request {@link ArgumentsReader} for CSV types. Current implementation uses
	 * {@link CsvMultipartFormArgumentsReader} to read {@link CsvReader} stream encoded as <code>multipart/form-data</code>.
	 * 
	 * @return CSV arguments readers.
	 */
	@Override
	public Map<EncoderKey, ArgumentsReader> getArgumentsReaders() {
		return READERS;
	}

	/**
	 * Get HTTP response {@link ValueWriter} for CSV types. Current implementation does not support CSV value writers and always
	 * returns empty map.
	 * 
	 * @return CSV value writers, always empty.
	 */
	@Override
	public Map<ContentType, ValueWriter> getValueWriters() {
		return Collections.emptyMap();
	}
}
