package de.unibayreuth.bayceer.bayeos.gateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalFilePathConfig {
	
	@Value("${LOCAL_FILE_PATH:/var/lib/bayeos-gateway-influx/uploads}")
	private String localFilePath;
	
	@Bean
	public Path localFilePath() throws IOException {
		try {
			Files.createDirectories(Paths.get(localFilePath));
		} catch (IOException e) {
			throw new IOException("Could not initialize file path: " + localFilePath);
		}
		return Paths.get(localFilePath);
	}

}
