package js.csv;

import java.io.IOException;

/**
 * Exception thrown if CSV stream is empty or required header is missing.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public class CsvException extends IOException {
	/** Java serialization version. */
	private static final long serialVersionUID = -3178250588487274353L;

	/**
	 * Create CSV exception with formatted message. Message may be formatted as supported by
	 * {@link String#format(String, Object...)}.
	 * 
	 * @param message message with optional formatting tags,
	 * @param args optional arguments if message is formatted.
	 */
	public CsvException(String message, Object... args) {
		super(String.format(message, args));
	}
}
