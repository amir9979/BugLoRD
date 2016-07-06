/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking;
import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.utils.fileoperations.StringListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

/**
 * Checks out a Defects4J buggy project version. Will delete existing
 * versions if executed again.
 * 
 * @author SimHigh
 */
public class CheckoutAndGenerateSBFLRankings {
	
	private final static String SEP = File.separator;
	
	public final static String PATH_MARK = "-";
	
	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "CheckoutAndGenerateSBFLRankings -p project -b bugID [-l loc1 loc2 ...]"; 
		final String tool_usage = "CheckoutAndGenerateSBFLRankings";
		final OptionParser options = new OptionParser(tool_usage, args);

        options.add(Prop.OPT_PROJECT, "project", true, "A project of the Defects4J benchmark. "
        		+ "Should be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.", true);
        options.add(Prop.OPT_BUG_ID, "bugID", true, "A number indicating the id of a buggy project version. "
        		+ "Value ranges differ based on the project.", true);
        
//        options.add("r", "onlyRelevant", false, "Set if only relevant tests shall be executed.");
		
        options.add(Option.builder(Prop.OPT_LOCALIZERS).longOpt("localizers").optionalArg(true).hasArgs()
        		.desc("A list of identifiers of Cobertura localizers (e.g. 'Tarantula', 'Jaccard', ...).")
				.build());
        
        options.parseCommandLine();
        
        return options;
	}
	
	
	/**
	 * @param args
	 * -p project -b bugID [-l loc1 loc2 ...]
	 */
	public static void main(String[] args) {
		
		OptionParser options = getOptions(args);	
		
		String project = options.getOptionValue(Prop.OPT_PROJECT);
		String id = options.getOptionValue(Prop.OPT_BUG_ID);
		int parsedID = Integer.parseInt(id);
		
		Prop.validateProjectAndBugID(project, parsedID, true);
		
		String buggyID = id + "b";
		String fixedID = id + "f";
		
		//this is important!!
		Prop.loadProperties(project, buggyID, fixedID);
		
		File executionProjectDir = Paths.get(Prop.projectDir).toFile();
		executionProjectDir.mkdirs();
		File executionBuggyVersionDir = Paths.get(Prop.executionBuggyWorkDir).toFile();
		File executionFixedVersionDir = Paths.get(Prop.executionFixedWorkDir).toFile();
		
		//delete existing directories, if any
		Misc.delete(Paths.get(Prop.executionBuggyWorkDir));
		Misc.delete(Paths.get(Prop.executionFixedWorkDir));
		
		/* #====================================================================================
		 * # checkout buggy version
		 * #==================================================================================== */
		Prop.executeCommand(executionProjectDir, 
				Prop.defects4jExecutable, "checkout", "-p", project, "-v", buggyID, "-w", Prop.executionBuggyWorkDir);
		
		/* #====================================================================================
		 * # collect bug info
		 * #==================================================================================== */
		String infoFile = Prop.executionBuggyWorkDir + SEP + ".info";
		
		String processOutput = Prop.executeCommandWithOutput(executionBuggyVersionDir, false, 
				Prop.defects4jExecutable, "info", "-p", project, "-b", id);
		try {
			Misc.writeString2File(processOutput, Paths.get(infoFile).toFile());
		} catch (IOException e) {
			Misc.abort("IOException while trying to write to file '" + infoFile + "'.");
		}
		
		String buggyMainSrcDir = Prop.executeCommandWithOutput(executionBuggyVersionDir, false, 
				Prop.defects4jExecutable, "export", "-p", "dir.src.classes");
		Misc.out("main source directory: <" + buggyMainSrcDir + ">");
		String buggyMainBinDir = Prop.executeCommandWithOutput(executionBuggyVersionDir, false, 
				Prop.defects4jExecutable, "export", "-p", "dir.bin.classes");
		Misc.out("main binary directory: <" + buggyMainBinDir + ">");
		String buggyTestBinDir = Prop.executeCommandWithOutput(executionBuggyVersionDir, false,
				Prop.defects4jExecutable, "export", "-p", "dir.bin.tests");
		Misc.out("test binary directory: <" + buggyTestBinDir + ">");
		
		String buggyTestCP = Prop.executeCommandWithOutput(executionBuggyVersionDir, false, 
				Prop.defects4jExecutable, "export", "-p", "cp.test");
		Misc.out("test class path: <" + buggyTestCP + ">");
		
		/* #====================================================================================
		 * # compile buggy version
		 * #==================================================================================== */
		if (!Paths.get(Prop.executionBuggyWorkDir + SEP + ".defects4j.config").toFile().exists()) {
			Misc.abort("Defects4J config file doesn't exist.");
		}
		Prop.executeCommand(executionBuggyVersionDir, Prop.defects4jExecutable, "compile");
		
		/* #====================================================================================
		 * # generate coverage traces via cobertura and calculate rankings
		 * #==================================================================================== */
		String testClassesFile = Prop.executionBuggyWorkDir + SEP + "test_classes.txt";
		if (Prop.relevant) {
			Prop.executeCommand(executionBuggyVersionDir, 
					Prop.defects4jExecutable, "export", "-p", "tests.relevant", "-o", testClassesFile);
		} else {
			Prop.executeCommand(executionBuggyVersionDir, 
					Prop.defects4jExecutable, "export", "-p", "tests.all", "-o", testClassesFile);
		}
		
		String rankingDir = Prop.executionBuggyWorkDir + SEP + "ranking";
		String[] localizers = options.getOptionValues(Prop.OPT_LOCALIZERS);
		Cob2Instr2Coverage2Ranking.generateRankingForDefects4JElement(
				Prop.executionBuggyWorkDir, buggyMainSrcDir, buggyTestBinDir, buggyTestCP, 
				Prop.executionBuggyWorkDir + SEP + buggyMainBinDir, testClassesFile, 
				rankingDir, localizers);
		
		/* #====================================================================================
		 * # clean up unnecessary directories (binary classes, doc files, svn/git files)
		 * #==================================================================================== */
		Misc.delete(Paths.get(Prop.executionBuggyWorkDir + SEP + buggyMainBinDir));
		Misc.delete(Paths.get(Prop.executionBuggyWorkDir + SEP + buggyTestBinDir));
		Misc.delete(Paths.get(Prop.executionBuggyWorkDir + SEP + "doc"));
		Misc.delete(Paths.get(Prop.executionBuggyWorkDir + SEP + ".git"));
		Misc.delete(Paths.get(Prop.executionBuggyWorkDir + SEP + ".svn"));
		
		/* #====================================================================================
		 * # checkout fixed version for comparison purposes
		 * #==================================================================================== */
		Prop.executeCommand(executionProjectDir, 
				Prop.defects4jExecutable, "checkout", "-p", project, "-v", fixedID, "-w", Prop.executionFixedWorkDir);
		
		/* #====================================================================================
		 * # check modifications
		 * #==================================================================================== */
		String modifiedSourcesFile = Prop.executionBuggyWorkDir + SEP + ".info.mod";
		
		//TODO is storing this as a file really valuable?
		List<String> modifiedSources = parseInfoFile(infoFile);
		new StringListToFileWriterModule<List<String>>(Paths.get(modifiedSourcesFile), true)
		.submit(modifiedSources);
		
		String fixedMainSrcDir = Prop.executeCommandWithOutput(executionFixedVersionDir, false, 
				Prop.defects4jExecutable, "export", "-p", "dir.src.classes");
		Misc.out("main source directory: <" + fixedMainSrcDir + ">");
		
		//iterate over all modified source files
		List<String> result = new ArrayList<>();
		for (String modifiedSourceIdentifier : modifiedSources) {
			String path = modifiedSourceIdentifier.replace('.','/') + ".java";
			result.add(PATH_MARK + path);
			
			//extract the changes
			result.addAll(ChangeChecker.checkForChanges(
					Paths.get(Prop.executionBuggyWorkDir, buggyMainSrcDir, path).toFile(), 
					Paths.get(Prop.executionFixedWorkDir, fixedMainSrcDir, path).toFile()));
		}
		
		//save the gathered information about modified lines in a file
		new StringListToFileWriterModule<List<String>>(Paths.get(Prop.executionBuggyWorkDir, ".modifiedLines"), true)
		.submit(result);
		
		//delete the fixed version directory, since it's not needed anymore
		Misc.delete(Paths.get(Prop.executionFixedWorkDir));
		
		/* #====================================================================================
		 * # move to archive directory, in case it differs from the execution directory
		 * #==================================================================================== */
		File archiveProjectDir = Paths.get(Prop.archiveProjectDir).toFile();
		if (!archiveProjectDir.equals(executionProjectDir)) {
			File archiveBuggyVersionDir = Paths.get(Prop.archiveProjectDir + SEP + buggyID).toFile();
			Misc.delete(archiveBuggyVersionDir);
			try {
				Misc.copyFileOrDir(executionBuggyVersionDir, archiveBuggyVersionDir);
			} catch (IOException e) {
				Misc.abort((Object)null, "IOException while trying to copy directory '%s' to '%s'.",
						executionBuggyVersionDir, archiveBuggyVersionDir);
			}
			Misc.delete(executionBuggyVersionDir);
		}
	}
	
	/**
	 * Parses the info file and returns a String which contains all modified
	 * source files with one file per line.
	 * @param infoFile
	 * the path to the info file
	 * @return
	 * modified source files, separated by new lines
	 */
	private static List<String> parseInfoFile(String infoFile) {
		List<String> lines = new ArrayList<>();
		try (BufferedReader bufRead = new BufferedReader(new FileReader(infoFile))) {
			String line = null;
			boolean modifiedSourceLine = false;
			while ((line = bufRead.readLine()) != null) {
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
		} catch (FileNotFoundException e) {
			Misc.abort("Info file does not exist: '" + infoFile + "'.");
		} catch (IOException e) {
			Misc.abort("IOException while reading info file: '" + infoFile + "'.");
		}
		
		return lines;
	}
}