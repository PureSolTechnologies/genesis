package com.puresoltechnologies.genesis.tracker.hadoop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puresoltechnologies.commons.misc.hash.HashId;
import com.puresoltechnologies.commons.misc.hash.HashUtilities;
import com.puresoltechnologies.genesis.commons.ProvidedVersionRange;
import com.puresoltechnologies.genesis.commons.SequenceMetadata;
import com.puresoltechnologies.genesis.commons.TransformationException;
import com.puresoltechnologies.genesis.commons.TransformationMetadata;
import com.puresoltechnologies.genesis.commons.hadoop.HadoopClientHelper;
import com.puresoltechnologies.genesis.tracker.spi.Severity;
import com.puresoltechnologies.genesis.tracker.spi.TransformationTracker;
import com.puresoltechnologies.versioning.Version;

/**
 * This tracker uses files within a Hadoop's HDFS to track changes.
 * 
 * @author Rick-Rainer Ludwig
 */
public class HadoopTransformationTracker implements TransformationTracker {

    private static final Logger logger = LoggerFactory.getLogger(HadoopTransformationTracker.class);

    private static final Path genesisDirectoryPath = new Path("/apps/Genesis");
    private static final Path migrationLogFilePath = new Path(genesisDirectoryPath, "migration.log");
    private static final Path trackerDirectoryPath = new Path(genesisDirectoryPath, "tracker");

    private FileSystem fileSystem = null;

    private static Path createComponentDirectoryPath(String component) {
	return new Path(trackerDirectoryPath, component);
    }

    private static Path createMachineDirectoryPath(String component, InetAddress machine) {
	return new Path(createComponentDirectoryPath(component), machine.getHostAddress());
    }

    private static Path createVersionFilePath(Path machineDirectoryPath, Version version) {
	return new Path(machineDirectoryPath, version.toString());
    }

    private static Path createLastTransformationFilePath(Path machineDirectoryPath) {
	return new Path(machineDirectoryPath, "last_transformation");
    }

    @Override
    public void open() throws TransformationException {
	connect();
	prepare();
    }

    private void connect() throws TransformationException {
	try {
	    Configuration configuration = HadoopClientHelper.createConfiguration();
	    fileSystem = FileSystem.newInstance(configuration);
	} catch (IOException e) {
	    throw new TransformationException("Could not connect to Hadoop file system.", e);
	}
    }

    private void prepare() throws TransformationException {
	try {
	    if (!fileSystem.exists(genesisDirectoryPath)) {
		if (!fileSystem.mkdirs(genesisDirectoryPath)) {
		    throw new TransformationException("Could not create Genesis workspace directory '"
			    + genesisDirectoryPath + "' in Hadoop file system.");
		}
	    } else {
		FileStatus genesisDirectoryStatus = fileSystem.getFileStatus(genesisDirectoryPath);
		if (!genesisDirectoryStatus.isDirectory()) {
		    throw new TransformationException("Genesis workspace directory path '" + genesisDirectoryPath
			    + "' exists, but is not a directory.");
		}
	    }

	    if (!fileSystem.exists(trackerDirectoryPath)) {
		if (!fileSystem.mkdirs(trackerDirectoryPath)) {
		    throw new TransformationException("Could not create Genesis tracker directory '"
			    + trackerDirectoryPath + "' in Hadoop file system.");
		}
	    } else {
		FileStatus genesisDirectoryStatus = fileSystem.getFileStatus(trackerDirectoryPath);
		if (!genesisDirectoryStatus.isDirectory()) {
		    throw new TransformationException("Genesis tracker directory path '" + trackerDirectoryPath
			    + "' exists, but is not a directory.");
		}
	    }

	    if (!fileSystem.exists(migrationLogFilePath)) {
		if (!fileSystem.createNewFile(migrationLogFilePath)) {
		    throw new TransformationException("Could not create Genesis migration log '" + migrationLogFilePath
			    + "' in Hadoop file system.");
		}
	    } else {
		FileStatus genesisDirectoryStatus = fileSystem.getFileStatus(migrationLogFilePath);
		if (!genesisDirectoryStatus.isFile()) {
		    throw new TransformationException(
			    "Genesis migration log path '" + migrationLogFilePath + "' exists, but is not a file.");
		}
	    }
	} catch (IOException e) {
	    throw new TransformationException("Could not prepare Genesis workspace in Hadoop file system.", e);
	}
    }

    @Override
    public void close() {
	try {
	    fileSystem.close();
	} catch (IOException e) {
	    logger.warn("Could not close Hadoop file system.", e);
	} finally {
	    fileSystem = null;
	}
    }

    private void checkMachineDirectory(Path path) throws TransformationException {
	try {
	    if (!fileSystem.exists(path)) {
		if (!fileSystem.mkdirs(path)) {
		    throw new TransformationException(
			    "Could not create machine directory '" + path + "' in Hadoop file system.");
		}
	    } else {
		FileStatus directoryStatus = fileSystem.getFileStatus(path);
		if (!directoryStatus.isDirectory()) {
		    throw new TransformationException(
			    "Machine directory path '" + path + "' exists, but is not a directory.");
		}
	    }
	} catch (IOException e) {
	    throw new TransformationException(
		    "Could not prepare machine directory '" + path + "' in Hadoop file system.", e);
	}
    }

    private void checkVersionFile(Path path) throws TransformationException {
	try {
	    if (!fileSystem.exists(path)) {
		if (!fileSystem.createNewFile(path)) {
		    throw new TransformationException(
			    "Could not create version file '" + path + "' in Hadoop file system.");
		}
	    } else {
		FileStatus fileStatus = fileSystem.getFileStatus(path);
		if (!fileStatus.isFile()) {
		    throw new TransformationException("Version file path '" + path + "' exists, but is not a file.");
		}
	    }
	} catch (IOException e) {
	    throw new TransformationException("Could not prepare version file '" + path + "' in Hadoop file system.",
		    e);
	}
    }

    private void checkLastTransformationFile(Path path) throws TransformationException {
	try {
	    if (!fileSystem.exists(path)) {
		if (!fileSystem.createNewFile(path)) {
		    throw new TransformationException(
			    "Could not create last transformation file '" + path + "' in Hadoop file system.");
		}
	    } else {
		FileStatus fileStatus = fileSystem.getFileStatus(path);
		if (!fileStatus.isFile()) {
		    throw new TransformationException(
			    "Last transformation file path '" + path + "' exists, but is not a file.");
		}
	    }
	} catch (IOException e) {
	    throw new TransformationException(
		    "Could not prepare last transformation file '" + path + "' in Hadoop file system.", e);
	}
    }

    @Override
    public void trackMigration(InetAddress machine, TransformationMetadata metadata) throws TransformationException {
	try {
	    Path machineDirectoryPath = createMachineDirectoryPath(metadata.getComponentName(), machine);
	    checkMachineDirectory(machineDirectoryPath);
	    Path versionFilePath = createVersionFilePath(machineDirectoryPath, metadata.getTargetVersion());
	    checkVersionFile(versionFilePath);
	    Path lastTransformationFilePath = createLastTransformationFilePath(machineDirectoryPath);
	    fileSystem.delete(lastTransformationFilePath, true);
	    checkLastTransformationFile(lastTransformationFilePath);
	    String line = createLogLine(machine, metadata);
	    try (FSDataOutputStream versionFileOutputStream = fileSystem.append(versionFilePath);
		    BufferedWriter bufferedWriter = new BufferedWriter(
			    new OutputStreamWriter(versionFileOutputStream, Charset.defaultCharset()))) {
		bufferedWriter.write(line);
	    }
	    try (FSDataOutputStream lastTransformationFileOutputStream = fileSystem.append(lastTransformationFilePath);
		    BufferedWriter bufferedWriter = new BufferedWriter(
			    new OutputStreamWriter(lastTransformationFileOutputStream, Charset.defaultCharset()))) {
		bufferedWriter.write(line);
	    }
	} catch (IOException e) {
	    throw new TransformationException(
		    "Could not log migration '" + metadata + "' for machine '" + machine.getHostAddress() + "'.", e);
	}
    }

    private String createLogLine(InetAddress machine, TransformationMetadata metadata) throws IOException {
	HashId hashId = HashUtilities.createHashId(metadata.getCommand());
	StringBuilder builder = new StringBuilder();
	SimpleDateFormat simpleDateFormat = createDateFormat();
	builder.append(simpleDateFormat.format(new Date()));
	builder.append("\t");
	builder.append(metadata.getComponentName());
	builder.append("\t");
	builder.append(machine.getHostAddress());
	builder.append("\t");
	builder.append(metadata.getStartVersion().toString());
	builder.append("\t");
	builder.append(metadata.getTargetVersion().toString());
	builder.append("\t");
	builder.append(metadata.getNextVersion().toString());
	builder.append("\t");
	builder.append("\"" + metadata.getCommand() + "\"");
	builder.append("\t");
	builder.append("\"" + metadata.getDeveloper() + "\"");
	builder.append("\t");
	builder.append(hashId.toString());
	builder.append("\t");
	builder.append("\"" + metadata.getComment() + "\"");
	builder.append("\n");
	return builder.toString();
    }

    private SimpleDateFormat createDateFormat() {
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd HH:MM:ss");
	return simpleDateFormat;
    }

    @Override
    public boolean wasMigrated(String component, InetAddress machine, Version version, String command) {
	try {
	    Path machineDirectoryPath = createMachineDirectoryPath(component, machine);
	    checkMachineDirectory(machineDirectoryPath);
	    Path versionFilePath = createVersionFilePath(machineDirectoryPath, version);
	    checkVersionFile(versionFilePath);
	    try (FSDataInputStream inputStream = fileSystem.open(versionFilePath);
		    BufferedReader reader = new BufferedReader(
			    new InputStreamReader(inputStream, Charset.defaultCharset()))) {
		String line = reader.readLine();
		while (line != null) {
		    String[] tokens = line.split("\t");
		    SequenceMetadata sequenceMetadata = new SequenceMetadata(tokens[1], Version.valueOf(tokens[3]),
			    new ProvidedVersionRange(Version.valueOf(tokens[4]), Version.valueOf(tokens[5])));
		    TransformationMetadata transformationMetadata = new TransformationMetadata(sequenceMetadata,
			    tokens[7], tokens[6], tokens[9]);
		    if (transformationMetadata.getCommand().equals("\"" + command + "\"")) {
			return true;
		    }
		    line = reader.readLine();
		}
	    }
	    return false;
	} catch (TransformationException | IOException e) {
	    logger.error("Could not read the transformation log for component '" + component + "' and machine '"
		    + machine.getHostAddress() + "'.", e);
	    return false;
	}
    }

    @Override
    public void dropComponentHistory(String component, InetAddress machine) {
	try {
	    if (!fileSystem.delete(createComponentDirectoryPath(component), true)) {
		logger.error("Could not drop component history for component '" + component + "'.");
	    }
	} catch (IOException e) {
	    logger.error("Could not drop component history for component '" + component + "'.", e);
	}
    }

    @Override
    public void log(Date time, Severity severity, InetAddress machine, Thread thread, String message, Throwable cause) {
	SimpleDateFormat dateFormat = createDateFormat();
	StringBuilder builder = new StringBuilder();
	builder.append(dateFormat.format(time));
	builder.append("\t");
	builder.append(severity.name());
	builder.append("\t");
	builder.append(machine.getHostAddress());
	builder.append("\t");
	builder.append(thread.getName());
	builder.append("\t");
	builder.append(message);
	builder.append("\t");
	if (cause == null) {
	    builder.append("\t");
	} else {
	    builder.append(cause.getClass().getName());
	    builder.append("\t");
	    builder.append(cause.getMessage());
	}
	builder.append("\n");

	try {
	    try (FSDataOutputStream migrationLogFileOutputStream = fileSystem.append(migrationLogFilePath);
		    BufferedWriter bufferedWriter = new BufferedWriter(
			    new OutputStreamWriter(migrationLogFileOutputStream, Charset.defaultCharset()))) {
		bufferedWriter.write(builder.toString());
	    }
	} catch (IOException e) {
	    logger.warn("Could not log '" + builder.toString() + "' to '" + migrationLogFilePath + "'.", e);
	}

    }

    @Override
    public TransformationMetadata getLastTransformationMetadata(String component, InetAddress machine) {
	try {
	    Path machineDirectoryPath = createMachineDirectoryPath(component, machine);
	    checkMachineDirectory(machineDirectoryPath);
	    Path lastTransformationFilePath = createLastTransformationFilePath(machineDirectoryPath);
	    if (!fileSystem.exists(lastTransformationFilePath)) {
		return null;
	    }
	    try (FSDataInputStream inputStream = fileSystem.open(lastTransformationFilePath);
		    BufferedReader reader = new BufferedReader(
			    new InputStreamReader(inputStream, Charset.defaultCharset()))) {
		String line = reader.readLine();
		if (line == null) {
		    return null;
		}
		String[] tokens = line.split("\t");
		SequenceMetadata sequenceMetadata = new SequenceMetadata(tokens[1], Version.valueOf(tokens[3]),
			new ProvidedVersionRange(Version.valueOf(tokens[4]), Version.valueOf(tokens[5])));
		TransformationMetadata transformationMetadata = new TransformationMetadata(sequenceMetadata, tokens[7],
			tokens[6], tokens[9]);
		return transformationMetadata;
	    }
	} catch (TransformationException | IOException e) {
	    logger.error("Could not read last transformation for component '" + component + "' and machine '"
		    + machine.getHostAddress() + "'.", e);
	    return null;
	}
    }

}
