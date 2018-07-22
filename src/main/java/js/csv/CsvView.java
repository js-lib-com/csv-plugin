package js.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

import js.http.ContentType;
import js.lang.BugError;
import js.lang.ConfigBuilder;
import js.lang.ConfigException;
import js.log.Log;
import js.log.LogFactory;
import js.mvc.AbstractView;
import js.util.Types;

/**
 * View used for exporting list of objects in CSV format.
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;csv separator="tab" null-value="null"&gt;
 * 	&lt;value name="Employee" property="employee" /&gt;
 * 	&lt;value name="Begin Date" property="beginDate" format="js.format.ShortDate" /&gt;
 * 	&lt;value name="End Date" property="endDate" format="js.format.ShortDate" /&gt;
 * 	...
 * 	&lt;value name="Cession Percent" property="cessionPercent" format="ro.gnotis.comedien.format.Percent" /&gt;
 * 	&lt;value name="quantity" property="quantity" /&gt;
 * 	&lt;value name="Total Rate" property="totalRate" /&gt;
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

	private CsvDescriptor descriptor;

	/** Create CSV view instance. */
	public CsvView() {
		log.trace("CsvView()");
	}

	@Override
	protected ContentType getContentType() {
		return CONTENT_TYPE;
	}

	@Override
	protected void serialize(OutputStream outputStream) throws IOException {
		if (model == null) {
			throw new BugError("Missing model for CSV view |%s|.", meta.getName());
		}
		if (!Types.isArrayLike(model)) {
			throw new BugError("Model for CSV view |%s| is not array like.", meta.getName());
		}
		long timestamp = new Date().getTime();

		ConfigBuilder builder = new ConfigBuilder(meta.getTemplateFile());
		try {
			descriptor = new CsvDescriptor(builder.build());
		} catch (ConfigException e) {
			throw new IOException(e);
		}

		CsvWriter writer = new CsvWriter(descriptor, new OutputStreamWriter(outputStream));
		for (Object object : Types.asIterable(model)) {
			writer.write(object);
		}
		writer.close();

		log.debug("CSV processing last %d msec.", new Date().getTime() - timestamp);
	}
}
