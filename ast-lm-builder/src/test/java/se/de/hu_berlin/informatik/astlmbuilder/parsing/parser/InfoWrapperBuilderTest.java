package se.de.hu_berlin.informatik.astlmbuilder.parsing.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;

import junit.framework.TestCase;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InfoWrapperBuilder;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.SymbolTable;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.VariableInfoWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Test class suite for the builder of the information wrapper objects
 */
public class InfoWrapperBuilderTest extends TestCase {

	@Test
	public void testBuildInfoWrapperForSimpleMethod() {
		Log.out(this, "Started first test");

		ClassLoader classLoader = getClass().getClassLoader();

		File testFile = new File(classLoader.getResource("test_files/TestClassForInfoWrapper.java").getFile());

		if (!testFile.exists()) {
			Log.err(this, "Could not find the test file at ", testFile.getAbsolutePath());
			return;
		}

		CompilationUnit cu = null;

		try {
			cu = JavaParser.parse(testFile);

			// search for a node deep in the AST
			ForStmt forNode = (ForStmt) getSomeInterestingNode(cu);

			InformationWrapper iw = InfoWrapperBuilder.buildInfoWrapperForNode(forNode);

			assertNotNull(iw);
			assertNotNull(iw.getNodeHistory());

			SymbolTable symbolTable = iw.getSymbolTable();

			assertNotNull(symbolTable);

			List<VariableInfoWrapper> gloVars = symbolTable.getAllGlobalVarInfoWrapper();

			assertNotNull(gloVars);
			assertEquals(4, gloVars.size()); // three at the start and one
												// at the end of the file

			List<VariableInfoWrapper> argVars = symbolTable.getAllParameterVarInfoWrapper();

			assertNotNull(argVars);
			assertEquals(2, argVars.size());

			List<VariableInfoWrapper> locVars = symbolTable.getAllLocalVarInfoWrapper();

			assertNotNull(locVars);
			assertEquals(14, locVars.size());

			List<VariableInfoWrapper> allInts = symbolTable.getAllVarInfoWrapperWithType("int");

			assertNotNull(allInts);
			assertEquals(6, allInts.size()); // the global index at the start
												// and end, both arguments, two
												// locals

			List<VariableInfoWrapper> allIndizes = symbolTable.getAllVarInfoWrapperWithName("Index");

			assertNotNull(allIndizes);
			assertEquals(2, allIndizes.size()); // one global and one local

			List<VariableInfoWrapper> noMembers = symbolTable.getAllVarInfoWrapperWithName("WhoNamesAVarThis?");

			assertNotNull(noMembers);
			assertEquals(0, noMembers.size()); // there should be none

			List<Optional<Node>> nodeHistory = iw.getNodeHistory();

			assertNotNull(nodeHistory);

		} catch (FileNotFoundException e) {
			Log.err(this, e);
		}

		Log.out(this, "Finished first test");
	}

	@Test
	public void testBuildInfoWrapperForNodeInsideFirstLoop() {
		Log.out(this, "Started inside loop test");

		ClassLoader classLoader = getClass().getClassLoader();

		File testFile = new File(classLoader.getResource("test_files/TestClassForInfoWrapper.java").getFile());

		if (!testFile.exists()) {
			Log.err(this, "Could not find the test file at ", testFile.getAbsolutePath());
			return;
		}

		CompilationUnit cu = null;

		try {
			cu = JavaParser.parse(testFile);

			// search for a node deep in the AST
			ForStmt forNode = (ForStmt) getSomeInterestingNode(cu);

			// test the first node inside the for statement body
			Node insideBody = forNode.getBody().getChildNodes().get(0);

			assertNotNull(insideBody);
			assertTrue(insideBody instanceof ExpressionStmt);

			InformationWrapper iw = InfoWrapperBuilder.buildInfoWrapperForNode(insideBody);

			assertNotNull(iw);
			assertNotNull(iw.getNodeHistory());

			SymbolTable symbolTable = iw.getSymbolTable();

			assertNotNull(symbolTable);

			List<VariableInfoWrapper> gloVars = symbolTable.getAllGlobalVarInfoWrapper();

			assertNotNull(gloVars);
			assertEquals(4, gloVars.size()); // three at the start and one at
												// the end of the file

			List<VariableInfoWrapper> argVars = symbolTable.getAllParameterVarInfoWrapper();

			assertNotNull(argVars);
			assertEquals(2, argVars.size());

			List<VariableInfoWrapper> locVars = symbolTable.getAllLocalVarInfoWrapper();

			assertNotNull(locVars);
			assertEquals(15, locVars.size());

			List<VariableInfoWrapper> allInts = symbolTable.getAllVarInfoWrapperWithType("int");

			assertNotNull(allInts);
			assertEquals(7, allInts.size()); // the global index at the start
												// and end, both arguments, two
												// locals and the for loop init

			List<VariableInfoWrapper> allIndizes = symbolTable.getAllVarInfoWrapperWithName("Index");

			assertNotNull(allIndizes);
			assertEquals(2, allIndizes.size()); // one global and one local

			List<VariableInfoWrapper> noMembers = symbolTable.getAllVarInfoWrapperWithName("WhoNamesAVarThis?");

			assertNotNull(noMembers);
			assertEquals(0, noMembers.size()); // there should be none

			List<Optional<Node>> nodeHistory = iw.getNodeHistory();

			assertNotNull(nodeHistory);

		} catch (FileNotFoundException e) {
			Log.err(this, e);
		}

		Log.out(this, "Finished inside loop test");
	}

	@Test
	public void testBuildInfoWrapperForNodeInsideNestedSecondLoop() {
		Log.out(this, "Started inside nested loop test 1");

		ClassLoader classLoader = getClass().getClassLoader();

		File testFile = new File(classLoader.getResource("test_files/TestClassForInfoWrapper.java").getFile());

		if (!testFile.exists()) {
			Log.err(this, "Could not find the test file at ", testFile.getAbsolutePath());
			return;
		}

		CompilationUnit cu = null;

		try {
			cu = JavaParser.parse(testFile);

			// search for a node number 17 which should be a for statement
			ForStmt forNode = (ForStmt) getNestedLoopNode(cu);

			// test the first node inside the for statement body
			WhileStmt whileLoop = (WhileStmt) forNode.getBody().getChildNodes().get(1);
			ForeachStmt foreachLoop = (ForeachStmt) whileLoop.getBody().getChildNodes().get(1);
			ExpressionStmt deepestNestedSysPrint = (ExpressionStmt) foreachLoop.getBody().getChildNodes().get(1);

			InformationWrapper iw = InfoWrapperBuilder.buildInfoWrapperForNode(deepestNestedSysPrint);

			assertNotNull(iw);
			assertNotNull(iw.getNodeHistory());

			SymbolTable symbolTable = iw.getSymbolTable();

			assertNotNull(symbolTable);

			List<VariableInfoWrapper> gloVars = symbolTable.getAllGlobalVarInfoWrapper();

			assertNotNull(gloVars);
			assertEquals(4, gloVars.size()); // three at the start and one at
												// the end of the file

			List<VariableInfoWrapper> argVars = symbolTable.getAllParameterVarInfoWrapper();

			assertNotNull(argVars);
			assertEquals(2, argVars.size());

			List<VariableInfoWrapper> locVars = symbolTable.getAllLocalVarInfoWrapper();

			assertNotNull(locVars);
			assertEquals(21, locVars.size()); // 15 regular ones + 2 from outer
												// loop + 1 from inner loop + 2
												// from deepest loop
			
			// check if the string array was handled properly
			assertEquals( locVars.get(7).getType(), "string[]" );

			List<VariableInfoWrapper> allInts = symbolTable.getAllVarInfoWrapperWithType("int");

			assertNotNull(allInts);
			assertEquals(9, allInts.size()); // the global index at the start
												// and end, both arguments, four
												// locals

			List<VariableInfoWrapper> allIndizes = symbolTable.getAllVarInfoWrapperWithName("Index");

			assertNotNull(allIndizes);
			assertEquals(2, allIndizes.size()); // one global and one local

			List<VariableInfoWrapper> noMembers = symbolTable.getAllVarInfoWrapperWithName("WhoNamesAVarThis?");

			assertNotNull(noMembers);
			assertEquals(0, noMembers.size()); // there should be none

			List<Optional<Node>> nodeHistory = iw.getNodeHistory();

			assertNotNull(nodeHistory);

		} catch (FileNotFoundException e) {
			Log.err(this, e);
		}

		Log.out(this, "Finished inside nested loop test 1.");
	}

	@Test
	public void testBuildInfoWrapperMultipleVarDecs() {
		Log.out(this, "Started multiple variable declarations at once test.");

		ClassLoader classLoader = getClass().getClassLoader();

		File testFile = new File(classLoader.getResource("test_files/TestClassForInfoWrapper.java").getFile());

		if (!testFile.exists()) {
			Log.err(this, "Could not find the test file at ", testFile.getAbsolutePath());
			return;
		}

		CompilationUnit cu = null;

		try {
			cu = JavaParser.parse(testFile);

			// search for the else block in the parameter method
			Node sysPrint = getElseNode( cu );
			
			InformationWrapper iw = InfoWrapperBuilder.buildInfoWrapperForNode(sysPrint);
			assertNotNull(iw);
			assertNotNull(iw.getNodeHistory());

			SymbolTable symbolTable = iw.getSymbolTable();

			assertNotNull(symbolTable);
			
			List<VariableInfoWrapper> allLocals = symbolTable.getAllLocalVarInfoWrapper();
			assertNotNull( allLocals );
			assertEquals( allLocals.size(), 3); // there should be three ints

		} catch (FileNotFoundException e) {
			Log.err(this, e);
		}

		Log.out(this, "Finished multiple variable declarations at once test." );
	}
	
	/**
	 * Works only for the test class in the resource directory Currently
	 * searches for the for statement in the calc sum method
	 * 
	 * @param aCU
	 * The compilation unit
	 * @return The loop node in the calcSumFromTo method
	 */
	private Node getSomeInterestingNode(CompilationUnit aCU) {

		// get the class declaration
		ClassOrInterfaceDeclaration cdec = getNodeFromChildren(aCU, ClassOrInterfaceDeclaration.class);
		if (cdec == null) {
			return null;
		}

		MethodDeclaration calcMD = getNodeFromChildren(cdec, MethodDeclaration.class, "calcSumFromTo");
		if (calcMD == null) {
			return null;
		}

		// get the block statement
		BlockStmt block = getNodeFromChildren(calcMD, BlockStmt.class);
		if (block == null) {
			return null;
		}

		// get the for stmt
		ForStmt forStmt = getNodeFromChildren(block, ForStmt.class);
		return forStmt;
	}

	/**
	 * Works only for the test class in the resource directory Currently
	 * searches for the for statement in the calc sum method
	 * 
	 * @param aCU
	 * The compilation unit
	 * @return The loop node in the calcSumFromTo method
	 */
	private Node getNestedLoopNode(CompilationUnit aCU) {

		// get the class declaration
		ClassOrInterfaceDeclaration cdec = getNodeFromChildren(aCU, ClassOrInterfaceDeclaration.class);
		if (cdec == null) {
			return null;
		}

		MethodDeclaration calcMD = getNodeFromChildren(cdec, MethodDeclaration.class, "calcSumFromTo");
		if (calcMD == null) {
			return null;
		}

		// get the block statement
		BlockStmt block = getNodeFromChildren(calcMD, BlockStmt.class);
		if (block == null) {
			return null;
		}

		// get the for stmt
		List<Node> children = block.getChildNodes();
		return children.get(18);
	}
	
	/**
	 * Works only for the test class in the resource directory Currently
	 * searches for the for statement in the calc sum method
	 * 
	 * @param aCU
	 * The compilation unit
	 * @return The loop node in the calcSumFromTo method
	 */
	private Node getElseNode(CompilationUnit aCU) {

		// get the class declaration
		ClassOrInterfaceDeclaration cdec = getNodeFromChildren(aCU, ClassOrInterfaceDeclaration.class);
		if (cdec == null) {
			return null;
		}

		MethodDeclaration getSP = getNodeFromChildren(cdec, MethodDeclaration.class, "getSomeParameters");
		if (getSP == null) {
			return null;
		}

		// get the block statement
		BlockStmt block = getNodeFromChildren(getSP, BlockStmt.class);
		if (block == null) {
			return null;
		}

		// get the if else stmt
		IfStmt ifStmt = getNodeFromChildren( block, IfStmt.class );
		if( ifStmt == null ) {
			return null;
		}
		
		// get the else part
		Statement elseStmt = ifStmt.getElseStmt().get();
		
		// the last statement is the system print after some variable decs and assignments
		List<Node> children = elseStmt.getChildNodes();
		return children.get(5);
	}

	/**
	 * The variant with a name currently only works for method declarations
	 * because of the getNameAsString method
	 * @param aParentNode
	 * The node that has children
	 * @param aTypeOfNode
	 * The type of node that should be returned
	 * @param aName
	 * The name of the node that should be returned
	 * @return The node with the given type and name or null
	 */
	@SuppressWarnings("unchecked")
	private <T extends Node> T getNodeFromChildren(Node aParentNode, Class<T> aTypeOfNode, String aName) {

		if (aParentNode == null || aParentNode.getChildNodes() == null) {
			return null;
		}

		for (Node n : aParentNode.getChildNodes()) {
			if (aTypeOfNode.isInstance(n)) {
				// only works for method decs currently
				// could be adjusted if there is a pattern to node with names
				if (n instanceof MethodDeclaration) {
					if (((MethodDeclaration) n).getNameAsString().equalsIgnoreCase(aName)) {
						return (T) n;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Searches for a node of the given type in the list of children and returns
	 * it
	 * @param aParentNode
	 * The node with children
	 * @param aTypeOfNode
	 * The type of node that should be returned
	 * @return The first node with the given type
	 */
	@SuppressWarnings("unchecked")
	private <T extends Node> T getNodeFromChildren(Node aParentNode, Class<T> aTypeOfNode) {

		if (aParentNode == null || aParentNode.getChildNodes() == null) {
			return null;
		}

		for (Node n : aParentNode.getChildNodes()) {
			if (aTypeOfNode.isInstance(n)) {
				return (T) n;
			}
		}

		return null;
	}
}
