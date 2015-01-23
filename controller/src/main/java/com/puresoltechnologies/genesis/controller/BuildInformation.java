package com.puresoltechnologies.genesis.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.puresoltechnologies.versioning.Version;

public class BuildInformation {

	public static Version getVersion() {
		try (InputStream manifestStream = BuildInformation.class
				.getResourceAsStream("/META-INF/MANIFEST.MF")) {
			Manifest manifest = new Manifest(manifestStream);
			Attributes mainAttributes = manifest.getMainAttributes();
			String versionString = mainAttributes
					.getValue("Implementation-Version");
			if (versionString == null) {
				return new Version(0, 0, 0);
			}
			return Version.valueOf(versionString);
		} catch (IOException e) {
			return null;
		}
	}

}
