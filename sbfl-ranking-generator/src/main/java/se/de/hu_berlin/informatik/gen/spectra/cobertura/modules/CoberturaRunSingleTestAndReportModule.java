/**
 * 
 */
package se.de.hu_berlin.informatik.gen.spectra.cobertura.modules;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.NativeReport;
import se.de.hu_berlin.informatik.gen.spectra.cobertura.modules.sub.CoberturaRunTestInNewJVMModule;
import se.de.hu_berlin.informatik.gen.spectra.cobertura.modules.sub.CoberturaRunTestInNewJVMModuleWithJava7Runner;
import se.de.hu_berlin.informatik.gen.spectra.cobertura.modules.sub.CoberturaRunTestLocallyModule;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunSingleTestAndReportModule;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunTestInNewJVMModule;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunTestInNewJVMModuleWithJava7Runner;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunTestLocallyModule;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.coveragedata.LockableProjectData;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.coveragedata.MyTouchCollector;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.report.CoberturaReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class CoberturaRunSingleTestAndReportModule extends AbstractRunSingleTestAndReportModule<ProjectData,CoberturaReportWrapper> {

	final public static CoberturaReportWrapper ERROR_WRAPPER = new CoberturaReportWrapper(null, null, false);
	
	final private Path dataFile;
	private Map<Class<?>, Integer> registeredClasses;
	private Arguments reportArguments;
	private ProjectData initialProjectData;
	private String testOutput;
	private ClassLoader cl;
	private boolean debugOutput;
	private String javaHome;
	private Long timeout;
	private int repeatCount;
	private String instrumentedClassPath;
	private boolean fullSpectra;
	private File projectDir;
	private String java7RunnerJar;
	boolean isFirst = true;

	@SuppressWarnings("unchecked")
	public CoberturaRunSingleTestAndReportModule(final Path dataFile, final String testOutput, final File projectDir, final String srcDir, 
			final boolean fullSpectra, final boolean debugOutput, Long timeout, final int repeatCount,
			String instrumentedClassPath, final String javaHome, final String java7RunnerJar, boolean useSeparateJVMalways, 
			boolean alwaysUseJava7, int maxErrors, String[] failingtests,
			final StatisticsCollector<StatisticsData> statisticsContainer, ClassLoader cl) {
		super(testOutput, debugOutput, timeout, repeatCount, useSeparateJVMalways, alwaysUseJava7, 
				maxErrors, failingtests, statisticsContainer, cl);
		this.testOutput = testOutput;
		this.projectDir = projectDir;
		this.fullSpectra = fullSpectra;
		this.debugOutput = debugOutput;
		this.timeout = timeout;
		this.repeatCount = repeatCount;
		this.instrumentedClassPath = instrumentedClassPath;
		this.javaHome = javaHome;
		this.java7RunnerJar = java7RunnerJar;
		this.cl = cl;

		this.dataFile = dataFile;
		String baseDir = null;
		validateDataFile(this.dataFile.toString());
		validateAndCreateDestinationDirectory(testOutput);

		ArgumentsBuilder builder = new ArgumentsBuilder();
		builder.setDataFile(this.dataFile.toString());
		builder.setDestinationDirectory(testOutput);
		builder.addSources(srcDir, baseDir == null);

		reportArguments = builder.build();
		
		initialProjectData = CoverageDataFileHandler.loadCoverageData(dataFile.toFile());

		//try to get access to necessary fields from Cobertura with reflection...
		try {
			Field registeredClassesField = TouchCollector.class.getDeclaredField("registeredClasses");
			registeredClassesField.setAccessible(true);
			registeredClasses = (Map<Class<?>, Integer>) registeredClassesField.get(null);
		} catch (Exception e) {
			//if reflection doesn't work, get the classes from the data file
			Collection<ClassData> classes = initialProjectData.getClasses();
			registeredClasses = new HashMap<>();
			for (ClassData classData : classes) {
				try {
					if (cl == null) {
						registeredClasses.put(Class.forName(classData.getName()), 0);
					} else {
						registeredClasses.put(Class.forName(classData.getName(), true, cl), 0);
					}
				} catch (ClassNotFoundException e1) {
					Log.err(this, "Class '%s' not found for registration.", classData.getName());
				}
			}
		}

		//in the original data file, all (executable) lines are contained, even though they are not executed at all;
		//so if we want to not have the full spectra, we have to reset this data here
		if (!this.fullSpectra) {
			initialProjectData = new LockableProjectData();
			MyTouchCollector.resetTouchesOnProjectData2(registeredClasses, initialProjectData);
		}

//		//initialize/reset the project data
//		ProjectData.saveGlobalProjectData();
		//turn off auto saving (removes the shutdown hook inside of Cobertura)
		ProjectData.turnOffAutoSave();
	}

	private void validateDataFile(String value) {
		File dataFile = new File(value);
		if (!dataFile.exists()) {
			Log.abort(this, "Error: data file " + dataFile.getAbsolutePath()
			+ " does not exist");
		}
		if (!dataFile.isFile()) {
			Log.abort(this, "Error: data file " + dataFile.getAbsolutePath()
			+ " must be a regular file");
		}
	}

	private void validateAndCreateDestinationDirectory(String value) {
		File destinationDir = new File(value);
		if (destinationDir.exists() && !destinationDir.isDirectory()) {
			Log.abort(this, "Error: destination directory " + destinationDir
					+ " already exists but is not a directory");
		}
		destinationDir.mkdirs();
	}

	@Override
	public CoberturaReportWrapper generateReport(TestWrapper testWrapper, TestStatistics testStatistics,
			ProjectData data) {
		if (fullSpectra && isFirst) {
			data.merge(initialProjectData);
			isFirst = false;
		}
		//generate the report
		ComplexityCalculator complexityCalculator = null;
//			= new ComplexityCalculator(reportArguments.getSources());
//			complexityCalculator.setEncoding(reportArguments.getEncoding());
//			complexityCalculator.setCalculateMethodComplexity(
//					reportArguments.isCalculateMethodComplexity());

		NativeReport report = new NativeReport(data, reportArguments
				.getDestinationDirectory(), reportArguments.getSources(),
				complexityCalculator, reportArguments.getEncoding());

		return new CoberturaReportWrapper(report, 
				testWrapper.toString(), testStatistics.wasSuccessful());
	}

	@Override
	public CoberturaReportWrapper getErrorReport() {
		return ERROR_WRAPPER;
	}

	@Override
	public AbstractRunTestLocallyModule<ProjectData> newTestRunLocallyModule() {
		return new CoberturaRunTestLocallyModule(dataFile, testOutput, fullSpectra, 
				debugOutput, timeout, repeatCount, cl, registeredClasses);
	}
	
	@Override
	public AbstractRunTestInNewJVMModule<ProjectData> newTestRunInNewJVMModule() {
		return new CoberturaRunTestInNewJVMModule(testOutput, debugOutput, timeout, 
				repeatCount, instrumentedClassPath + File.pathSeparator + new ClassPathParser().parseSystemClasspath().getClasspath(), 
				dataFile, javaHome, projectDir);
	}

	@Override
	public AbstractRunTestInNewJVMModuleWithJava7Runner<ProjectData> newTestRunInNewJVMModuleWithJava7Runner() {
		//remove as much irrelevant classes as possible from class path (does not work this way...) TODO
//		ClassPathParser systemClasspath = new ClassPathParser(true).parseSystemClasspath();
//		systemClasspath.removeElementsOtherThan("java7-test-runner", "ant-", "junit-4.12");
		String testClassPath = instrumentedClassPath + File.pathSeparator;
		if (java7RunnerJar == null) {
			testClassPath += new ClassPathParser().parseSystemClasspath().getClasspath();
		} else {
			testClassPath += java7RunnerJar;
		}
		return new CoberturaRunTestInNewJVMModuleWithJava7Runner(testOutput, 
				debugOutput, timeout, repeatCount, testClassPath,
				// + File.pathSeparator + systemClasspath.getClasspath(), 
				dataFile, javaHome, projectDir);
	}

}
