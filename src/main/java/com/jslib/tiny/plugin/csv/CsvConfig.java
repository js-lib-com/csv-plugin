package com.jslib.tiny.plugin.csv;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import com.jslib.api.csv.CsvDescriptor;
import com.jslib.api.csv.CsvFactory;
import com.jslib.api.log.Log;
import com.jslib.api.log.LogFactory;
import com.jslib.lang.Config;
import com.jslib.lang.ConfigBuilder;
import com.jslib.lang.ConfigException;
import com.jslib.util.Classes;
import com.jslib.util.I18nFile;
import com.jslib.util.I18nRepository;

public class CsvConfig {
	private static final Log log = LogFactory.getLog(CsvConfig.class);

	private final Map<Class<?>, CsvDescriptor<?>> descriptors = new HashMap<>();

	private final CsvFactory csvFactory;

	public CsvConfig() {
		log.trace("CsvConfigImpl()");
		csvFactory = Classes.loadService(CsvFactory.class);
	}

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
				CsvDescriptor<?> descriptor = csvFactory.getDescriptor(builder.build());
				descriptors.put(descriptor.type(), descriptor);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> CsvDescriptor<T> getDescriptor(Class<T> type) {
		return (CsvDescriptor<T>) descriptors.get(type);
	}
}
