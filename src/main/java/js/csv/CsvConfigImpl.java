package js.csv;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import js.lang.Config;
import js.lang.ConfigBuilder;
import js.lang.ConfigException;
import js.lang.Configurable;
import js.log.Log;
import js.log.LogFactory;
import js.util.I18nFile;
import js.util.I18nRepository;

public class CsvConfigImpl implements CsvConfig, Configurable {
	private static final Log log = LogFactory.getLog(CsvConfigImpl.class);

	private Map<Class<?>, CsvDescriptor> descriptors = new HashMap<Class<?>, CsvDescriptor>();

	@Override
	public void config(Config config) throws Exception {
		log.trace("config(Config)");
		for (Config repositorySection : config.findChildren("repository")) {
			// load repository directory and files pattern and create I18N repository instance

			String repositoryDir = repositorySection.getAttribute("path");
			if (repositoryDir == null) {
				throw new ConfigException("Invalid views repository configuration. Missing <path> attribute.");
			}

			String filesPattern = repositorySection.getAttribute("files-pattern");
			if (filesPattern == null) {
				throw new ConfigException("Invalid views repository configuration. Missing <files-pattern> attribute.");
			}

			ConfigBuilder builder = new I18nRepository.ConfigBuilder(repositoryDir, filesPattern);
			Iterable<I18nFile> repository = new I18nRepository(builder.build());

			for (I18nFile template : repository) {
				try {
					builder = new ConfigBuilder(template.getFile());
				} catch (FileNotFoundException e) {
					throw new ConfigException(e);
				}
				CsvDescriptor descriptor = new CsvDescriptor(builder.build());
				descriptors.put(descriptor.getType(), descriptor);
			}
		}
	}

	@Override
	public CsvDescriptor getDescriptor(Class<?> type) {
		return descriptors.get(type);
	}
}
