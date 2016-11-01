package se.de.hu_berlin.informatik.benchmark.api.defects4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.de.hu_berlin.informatik.benchmark.api.AbstractBuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class Defects4JEntity extends AbstractBuggyFixedEntity {
		
	private final boolean buggyVersion;
	private final int bugID;
	private final String project;
	
	private static Map<String, Defects4JEntity> ENTITY_CACHE = new ConcurrentHashMap<String, Defects4JEntity>();
	
	public static Defects4JEntity getBuggyDefects4JEntity(String project, String bugId) {
		return ENTITY_CACHE.computeIfAbsent(project + bugId +"b", k -> new Defects4JEntity(project, bugId, true));
	}
	
	public static Defects4JEntity getFixedDefects4JEntity(String project, String bugId) {
		return ENTITY_CACHE.computeIfAbsent(project + bugId +"f", k -> new Defects4JEntity(project, bugId, false));
	}
	
	public static Defects4JEntity getProjectEntity(String project) {
		return new Defects4JEntity(project);
	}
	
	@Override
	public BuggyFixedEntity getBuggyVersion() {
		if (buggyVersion) {
			return this;
		} else {
			return getBuggyDefects4JEntity(project, String.valueOf(bugID));
		}
	}

	@Override
	public BuggyFixedEntity getFixedVersion() {
		if (buggyVersion) {
			return getFixedDefects4JEntity(project, String.valueOf(bugID));
		} else {
			return this;
		}
	}

	/**
	 * @param project
	 * a project identifier, serving as a directory name
	 * @param bugID
	 * id of the bug
	 * @param buggy
	 * whether to use the buggy or the fixed version of the bug with the given id
	 */
	private Defects4JEntity(String project, String bugID, boolean buggy) {
		super(new Defects4JDirectoryProvider(project, bugID, buggy));
		try {
			this.bugID = Integer.parseInt(bugID);
		} catch(NumberFormatException e) {
			throw e;
		}
		this.project = project;
		
		Defects4J.validateProjectAndBugID(project, this.bugID, true);
		
		this.buggyVersion = buggy;
	}
	
	/**
	 * @param project
	 * a project identifier, serving as a directory name
	 */
	private Defects4JEntity(String project) {
		super(new Defects4JDirectoryProvider(project, 0, true));
		this.project = project;
		this.bugID = 0;
		
		Defects4J.validateProject(project, false);
		
		this.buggyVersion = true;
	}
	
	private Defects4JEntity() {
		super(new Defects4JDirectoryProvider("dummy", 0, true));
		this.project = "dummy";
		this.bugID = 0;
		this.buggyVersion = true;
	}
	
	public Path getProjectDir(boolean executionMode) {
		return ((Defects4JDirectoryProvider)getDirectoryProvider()).getProjectDir(executionMode);
	}
	
	
	public int getBugId() {
		return bugID;
	}
	
	public String getProject() {
		return project;
	}
	
	
	private boolean checkoutBug(boolean executionMode) {
		if (getWorkDir(executionMode).toFile().exists()) {
			return false;
		}
		getEntityDir(executionMode).toFile().mkdirs();

		String version = null;
		if (buggyVersion) {
			version = getBugId() + "b";
		} else {
			version = getBugId() + "f";
		}
		
		Defects4J.executeCommand(getEntityDir(executionMode).toFile(), 
				Defects4J.getDefects4JExecutable(), "checkout", 
				"-p", getProject(), "-v", version, "-w", getWorkDir(executionMode).toString());
		return true;
	}

	
	public String getInfo(boolean executionMode) {
		return Defects4J.executeCommandWithOutput(getEntityDir(executionMode).toFile(), false, 
				Defects4J.getDefects4JExecutable(), "info", "-p", getProject(), "-b", String.valueOf(getBugId()));
	}


	@Override
	public void removeUnnecessaryFiles(boolean executionMode) {
		/* #====================================================================================
		 * # clean up unnecessary directories (binary classes)
		 * #==================================================================================== */
		FileUtils.delete(getWorkDir(executionMode).resolve(getMainBinDir(executionMode)));
		FileUtils.delete(getWorkDir(executionMode).resolve(getTestBinDir(executionMode)));
		/* #====================================================================================
		 * # clean up unnecessary directories (doc files, svn/git files)
		 * #==================================================================================== */
		FileUtils.delete(getWorkDir(executionMode).resolve("doc"));
		FileUtils.delete(getWorkDir(executionMode).resolve(".git"));
		FileUtils.delete(getWorkDir(executionMode).resolve(".svn"));
	}

	@Override
	public boolean compile(boolean executionMode) {
		if (!getWorkDir(executionMode).resolve(".defects4j.config").toFile().exists()) {
			Log.abort(Defects4JEntity.class, "Defects4J config file doesn't exist: '%s'.", 
					getWorkDir(executionMode).resolve(".defects4j.config"));
		}
		Defects4J.executeCommand(getWorkDir(executionMode).toFile(), 
				Defects4J.getDefects4JExecutable(), "compile");
		return true;
	}

	@Override
	public String toString() {
		return "Project: " + project + ", bugID: " + bugID;
	}

	@Override
	public String getUniqueIdentifier() {
		if (buggyVersion) {
			return project + "-" + bugID + "b";
		} else {
			return project + "-" + bugID + "f";
		}
	}

	public List<String> getModifiedClassesFromInfo(boolean executionMode) {
		/* #====================================================================================
		 * # collect bug info
		 * #==================================================================================== */
		String infoOutput = getInfo(executionMode);
		
		return parseInfoString(infoOutput);
	}
	
	/**
	 * Parses a Defects4J info string and returns a String which contains all modified
	 * source files with one file per line.
	 * @param info
	 * the info string
	 * @return
	 * modified source files, separated by new lines
	 */
	private List<String> parseInfoString(String info) {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new StringReader(info))) {
			String line = null;
			boolean modifiedSourceLine = false;
			while ((line = reader.readLine()) != null) {
				if (line.equals("List of modified sources:")) {
					modifiedSourceLine = true;
					continue;
				}
				if (modifiedSourceLine && line.startsWith(" - ")) {
					lines.add(line.substring(3));
				} else {
					modifiedSourceLine = false;
				}
			}
		} catch (IOException e) {
			Log.abort(this, "IOException while reading info string.");
		}
		
		return lines;
	}

	@Override
	public boolean initialize(boolean executionMode) {
		return checkoutBug(executionMode);
	}

	@Override
	public String computeClassPath(boolean executionMode) {
		return Defects4J.getD4JExport(getWorkDir(executionMode).toString(), buggyVersion, "cp.classes");
	}

	@Override
	public String computeTestClassPath(boolean executionMode) {
		return Defects4J.getD4JExport(getWorkDir(executionMode).toString(), buggyVersion, "cp.test");
	}

	@Override
	public List<String> computeTestCases(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Path> computeTestClasses(boolean executionMode) {
		String list;
		if (Boolean.parseBoolean(Defects4J.getValueOf(Defects4JProperties.ONLY_RELEVANT_TESTS))) {
			list = Defects4J.getD4JExport(getWorkDir(executionMode).toString(), buggyVersion, "tests.relevant");
		} else {
			list = Defects4J.getD4JExport(getWorkDir(executionMode).toString(), buggyVersion, "tests.all");
		}
		String[] array = list.split(System.lineSeparator());
		List<Path> testClasses = new ArrayList<>(array.length);
		for (String item : array) {
			testClasses.add(Paths.get(item));
		}
		return testClasses;
	}

}
