package js.tiny.plugin.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import js.csv.CsvFactory;
import js.lang.IllegalArgumentException;
import js.log.Log;
import js.log.LogFactory;
import js.tiny.container.core.Factory;
import js.tiny.container.http.encoder.ArgumentsReader;
import js.util.Classes;
import js.util.Files;

/**
 * CSV reader argument transported as <code>multipart/form-data</code>.
 * 
 * @author Iulian Rotaru
 * @version draft
 */
public class CsvMultipartFormArgumentsReader implements ArgumentsReader {
	/** Class logger. */
	private static final Log log = LogFactory.getLog(CsvMultipartFormArgumentsReader.class);

	/**
	 * Store stream argument, if any, so that to be able to close it after method execution. Do not store stream argument as
	 * field of this arguments reader since instance is reused and cannot have state.
	 */
	private final ThreadLocal<Closeable> threadLocal = new ThreadLocal<>();

	private final CsvFactory csvFactory;

	public CsvMultipartFormArgumentsReader() {
		this.csvFactory = Classes.loadService(CsvFactory.class);
	}

	/**
	 * Read CSV stream from HTTP request.
	 * 
	 * @param httpRequest HTTP request,
	 * @param formalParameters requested formal parameters.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object[] read(HttpServletRequest httpRequest, Type[] formalParameters) throws IOException, IllegalArgumentException {
		if (formalParameters.length != 1) {
			log.error("Bad parameters count for multipart form. Method must have exactly one formal parameter but has |%d|.", formalParameters.length);
			throw new IllegalArgumentException(formalParameters);
		}
		if (!(formalParameters[0] instanceof ParameterizedType)) {
			throw new IllegalArgumentException("Expect parameterized formal parameter but got |%s|.", formalParameters[0]);
		}

		ParameterizedType parameterizedType = (ParameterizedType) formalParameters[0];
		Class typeArgument = (Class) parameterizedType.getActualTypeArguments()[0];

		CsvConfig config = Factory.getInstance(CsvConfig.class);

		Object[] arguments = new Object[1];
		arguments[0] = csvFactory.getReader(config.getDescriptor(typeArgument), getUploadStream(httpRequest, formalParameters));
		threadLocal.set((Closeable) (arguments[0]));
		return arguments;
	}

	@Override
	public void clean() {
		Files.close(threadLocal.get());
		threadLocal.remove();
	}

	// --------------------------------------------------------------------------------------------
	// UTILITY METHODS

	private static InputStream getUploadStream(HttpServletRequest httpRequest, Type[] formalParameters) throws IOException {
		ServletFileUpload upload = new ServletFileUpload();
		FileItemStream fileItemStream = null;
		try {
			FileItemIterator fileItemIterator = upload.getItemIterator(httpRequest);
			fileItemStream = fileItemIterator.next();
		} catch (FileUploadException e) {
			throw new IllegalArgumentException(formalParameters);
		}
		if (fileItemStream.isFormField()) {
			throw new IllegalArgumentException(formalParameters);
		}
		return fileItemStream.openStream();
	}
}
