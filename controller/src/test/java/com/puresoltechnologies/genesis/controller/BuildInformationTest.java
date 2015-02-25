package com.puresoltechnologies.genesis.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.puresoltechnologies.versioning.Version;

public class BuildInformationTest {

    @Test
    public void testInceptionYear() {
	String year = BuildInformation.getInceptionYear();
	assertNotNull(year);
	assertEquals("2009", year);
    }

    @Test
    public void testBuildYear() {
	String year = BuildInformation.getBuildYear();
	assertNotNull(year);
	String currentYear = new SimpleDateFormat("yyyy").format(new Date());
	assertEquals(currentYear, year);
    }

    @Test
    public void testVersion() {
	String version = BuildInformation.getVersion();
	assertNotNull(version);
	Version.valueOf(version);
    }

}
