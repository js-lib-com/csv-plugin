package js.tiny.plugin.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import js.csv.CsvDescriptor;
import js.csv.CsvFactory;
import js.csv.CsvWriter;
import js.lang.BugError;
import js.lang.ConfigBuilder;
import js.lang.ConfigException;
import js.log.Log;
import js.log.LogFactory;
import js.tiny.container.http.ContentType;
import js.tiny.container.mvc.AbstractView;
import js.util.Classes;
import js.util.Types;

/**
 * View used to export list of objects in CSV format.
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;csv class="com.company.SalesLead" delimiter="tab" null-value="null"&gt;
 * 	&lt;column field="employee" /&gt;
 * 	&lt;column field="beginDate" format="js.format.ShortDate" /&gt;
 * 	&lt;column field="endDate" format="js.format.ShortDate" /&gt;
 * 	...
 * 	&lt;column field="cessionPercent" format="ro.gnotis.comedien.format.Percent" /&gt;
 * 	&lt;column field="quantity" /&gt;
 * 	&lt;column field="totalRate" /&gt;
 * &lt;/csv&gt;
 * </pre>
 * 
 * @author Iulian Rotaru
 * @version draft
 */
public class CsvView extends AbstractView {
	/** Class logger. */
	static final Log log = LogFactory.getLog(CsvView.class);

	/** Content type for CSV documents, see https://www.iana.org/assignments/media-types/text/csv */
	private static ContentType CONTENT_TYPE = new ContentType("text/csv;charset=UTF-8;header=present");

	// implementation note:
	// view instances can be subject to pooling so take care to not reuse previous state
	// be sure to initialize all this instance state on _serialize implementation

	private final CsvFactory csvFactory;

	/** Create CSV view instance. */
	public CsvView() {
		log.trace("CsvView()");
		csvFactory = Classes.loadService(CsvFactory.class);
	}

	@Override
	public ContentType getContentType() {
		return CONTENT_TYPE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void serialize(OutputStream outputStream) throws IOException {
		if (model == null) {
			throw new BugError("Missing model for CSV view |%s|.", meta.getName());
		}
		if (!Types.isArrayLike(model)) {
			throw new BugError("Model for CSV view |%s| is not array like.", meta.getName());
		}
		long timestamp = new Date().getTime();
		
		CsvDescriptor<?> descriptor;
		try {
			ConfigBuilder builder = new ConfigBuilder(meta.getTemplateFile());
			descriptor = csvFactory.getDescriptor(builder.build());
		} catch (ConfigException e) {
			throw new IOException(e);
		}

		@SuppressWarnings("rawtypes")
		CsvWriter writer = csvFactory.getWriter(descriptor, outputStream);
		for (Object object : Types.asIterable(model)) {
			writer.write(object);
		}
		writer.close();

		log.debug("CSV processing last %d msec.", new Date().getTime() - timestamp);
	}
}
