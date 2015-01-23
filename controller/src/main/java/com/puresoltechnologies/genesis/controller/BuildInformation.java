package com.puresoltechnologies.genesis.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

import com.puresoltechnologies.versioning.Version;

public class BuildInformation {

	public static Version getVersion() {
		try (InputStream manifestStream = BuildInformation.class
				.getResourceAsStream("/META-INF/MANIFEST.MF")) {
			Manifest manifest = new Manifest(manifestStream);
			return Version.valueOf(manifest.getMainAttributes().getValue(
					"Implementation-Version"));
		} catch (IOException e) {
			return null;
		}
	}

}
