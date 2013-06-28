package src.uk.ac.glasgow.jsinger.nanopatterns;



// TestPatternSpotter.java
// Jeremy Singer
// 10 Nov 08



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class PatternSpotterForGUI {

	/** Analyser modes (helps reusing existing methods) **/
	public final int ANALYSE_CLASS_FILE = 0;
	public  final int ANALYSE_JAR_FILE = 1;
	public  final int ANALYSE_CLASS_FROM_CLASSPATH = 2;
	public  final int ANALYSE_METHOD_FROM_CLASSPATH = 3;
	public  final int TEST_MODE = 4;

	public  int mode;


	//list to hold the results of the analysis for a single class
	public  ArrayList<String[]> output;

	public ArrayList<ArrayList<String[]>> allClasses;

	/** Argument - class or jar file, class or method name **/
	private static String arg;



	/**
	 * detects nanopatterns 
	 * @param args each argument is the classpath for a file to be analysed
	 * @param gui the GUI requesting the analysis
	 */
	public  ArrayList<ArrayList<String[]>> detect(String[] args) {

		//list to hold the analysis of all methods for one class (one element per method)
		output = new ArrayList<String[]>();

		//list to hold all the output from all classes analysed
		allClasses = new ArrayList<ArrayList<String[]>>();


		//for all the arguments (ie can have more than one class file analysed at a time), analyse the class
		for (int i = 0; i < args.length; i++) {

			arg = args[i];

			//check whether arg is a class file and set mode, then analyse
			if (arg.endsWith(".class")) {
				mode = ANALYSE_CLASS_FILE;
				analyseClassFile(arg);

				//make a copy of the results for the class and add it to the list of results for all classes
				ArrayList<String[]>fileToAdd = (ArrayList<String[]>) output.clone();
				allClasses.add(fileToAdd);

				//reset the list of methods to be output otherwise output will be duplicated
				output.clear();
				continue;
			}

			//check whether arg is a jar file and set mode, then analyse
			if (arg.endsWith(".jar")) {
				mode = ANALYSE_JAR_FILE;
				scanJar(arg);

				allClasses.add(output);
				continue;
			}

			//check whether arg is a classpath and set mode, then anlayse
			if (arg.contains(":")) {
				mode = ANALYSE_METHOD_FROM_CLASSPATH;
				scanClassPath();

				allClasses.add(output);
				continue;
			}

			mode = ANALYSE_CLASS_FROM_CLASSPATH;

			if (arg.charAt(0) == 'L') {
				arg = arg.substring(1, arg.length());
			}
			arg = arg.replace(".", "/");
			arg = arg.replace("\\", "/");
			scanClassPath();
		}


		return allClasses;

	}

	/** SCANNERS **/


	public  void scanClassPath() {

		String list = System.getProperty("java.class.path");
		for (String path : list.split(";")) {
			File thing = new File(path);
			if (thing.isDirectory()) {
				scanDirectory(thing);
			} else if (path.endsWith(".class")) {
				analyseClassFile(path);
			} else if (path.endsWith(".jar")) {
				scanJar(path);
			}
		}
	}

	public  void scanDirectory(File directory) {
		for (String entry : directory.list()) {

			String path = directory.getPath() + "\\" + entry;			
			File thing = new File(path);
			if (thing.isDirectory()) {
				scanDirectory(thing);
			} else if (thing.isFile() && path.endsWith(".class")) {
				analyseClassFile(path);
			} else if (thing.isFile() && path.endsWith(".jar")) {
				scanJar(path);
			}
		}
	}

	public  void scanJar(String path) {
		try {
			JarFile jar = new JarFile(path);
			Enumeration<JarEntry> enums = jar.entries();
			while (enums.hasMoreElements()) {
				JarEntry file = enums.nextElement();
				if (!file.isDirectory() && file.getName().endsWith(".class")) {
					analyseInputStream(jar.getInputStream(file));


					ArrayList<String[]>fileToAdd = (ArrayList<String[]>) output.clone();

					//add the analysed file to the list of classes
					allClasses.add(fileToAdd);
					output.clear();
				}
			}
			jar.close();
		} catch (IOException e) {
			System.out.println("Failed to open following JAR file: " + path);
		}
	}

	/** ANALYSERS **/

	public  void analyseClassFile(String path) {
		try {
			FileInputStream f = new FileInputStream(path);

			analyseInputStream(f);
		} catch (IOException e) {

			//System.out.println("File was not found: " + path);
		}
	}


	public  void analyseInputStream(InputStream is) {
		try {
			ClassReader cr = new ClassReader(is);
			ClassNode cn = new ClassNode();
			cr.accept(cn, ClassReader.SKIP_DEBUG);

			if (mode == ANALYSE_CLASS_FROM_CLASSPATH && !cn.name.equals(arg)) {
				return;
			}

			//a list of all methods in the class 
			List methods = cn.methods;

			//analyse each method in the class
			for (int i = 0; i < methods.size(); ++i) {
				analyseMethod(cn, (MethodNode) methods.get(i));

			}



		}catch (IOException e) {
		}

	}






	public  PatternList analyseMethod(ClassNode cn, MethodNode method) {


		try {

			if (mode == ANALYSE_METHOD_FROM_CLASSPATH) {
				String s = method.name + ":" + method.desc;
				if (!arg.equals(s))
					return null;
			}


			//use the visitor pattern to check for all nanopatterns
			RecursivePatternSpotter rps = new RecursivePatternSpotter(
					new EmptyVisitor(), cn.name, method.name, method.desc);
			OOAccessPatternSpotter ops = new OOAccessPatternSpotter(
					new EmptyVisitor());
			TypeManipulatorPatternSpotter tps = new TypeManipulatorPatternSpotter(
					new EmptyVisitor());
			ControlFlowPatternSpotter cps = new ControlFlowPatternSpotter(
					new EmptyVisitor());
			ArrayAccessPatternSpotter aps = new ArrayAccessPatternSpotter(
					new EmptyVisitor());
			PolymorphicPatternSpotter pps = new PolymorphicPatternSpotter(
					new EmptyVisitor());
			ReturnPatternSpotter retps = new ReturnPatternSpotter(
					new EmptyVisitor());
			MethodPatternSpotter mps = new MethodPatternSpotter(new EmptyVisitor());
			// check following
			// properties directly from
			// method descriptor
			boolean noParams = false;
			boolean noReturn = false;
			boolean throwsExceptions = false;
			if (method.desc.startsWith("()")) {
				noParams = true;
			}
			if (method.desc.endsWith(")V")) {
				noReturn = true;
			}
			if (method.exceptions.size() > 0) {
				throwsExceptions = true;
			}
			if (method.instructions.size() > 0) {
				for (int j = 0; j < method.instructions.size(); ++j) {
					Object insn = method.instructions.get(j);
					((AbstractInsnNode) insn).accept(rps);
					((AbstractInsnNode) insn).accept(ops);
					((AbstractInsnNode) insn).accept(tps);
					((AbstractInsnNode) insn).accept(cps);
					((AbstractInsnNode) insn).accept(aps);
					((AbstractInsnNode) insn).accept(mps);

				}


				int numInstrs = method.instructions.size();



				//creates an array of the meta information for the method
				String[] methodOutput = new String[6];


				//cn.name contains the package name followed by the class name
				//use helper methods to separate cn.name into substrings				
				methodOutput[0] = findPackages(cn.name);
				methodOutput[1] = findClass(cn.name);
				
				
				methodOutput[2] = method.name;
				methodOutput[3] = method.desc;
				methodOutput[4] = "" +numInstrs;



				//output to console for testing
				//System.out.print("" + cn.name + " " + method.name + " "
				//		+ method.desc + " " + numInstrs);



				PatternList resultList = new PatternList();


				resultList.noParams = noParams;
				resultList.noReturn = noReturn;

				//recursive pattern spotter
				resultList.isRecursive = rps.isRecursive();
				resultList.isSameNameCaller = rps.isSameNameCaller();
				resultList.isLeaf = rps.isLeaf();

				//OO Access pattern spotter
				resultList.isObjectCreator = ops.isObjectCreator();
				resultList.isThisInstanceFieldReader = ops.isThisInstanceFieldReader();
				resultList.isThisInstanceFieldWriter = ops.isThisInstanceFieldWriter();
				resultList.isOtherInstanceFieldReader = ops.isOtherInstanceFieldReader();
				resultList.isOtherInstanceFieldWriter = ops.isOtherInstanceFieldWriter();
				resultList.isStaticFieldReader = ops.isStaticFieldReader();
				resultList.isStaticFieldWriter = ops.isStaticFieldWriter();

				//type manipulator pattern spotter
				resultList.isTypeManipulator = tps.isTypeManipulator();

				//control flow pattern spotter
				resultList.isStraightLineCode = cps.isStraightLineCode();
				resultList.isLoopingCode = cps.isLoopingCode();
				resultList.isSwitcher = cps.isSwitcher();
				resultList.throwsExceptions = throwsExceptions;
				resultList.isLocalVarReader = aps.isLocalVarReader();
				resultList.isLocalVarWriter = aps.isLocalVarWriter();
				resultList.isArrayCreator = aps.isArrayCreator();
				resultList.isArrayReader= aps.isArrayReader();
				resultList.isArrayWriter = aps.isArrayWriter();

				//polymorphic pattern spotters
				resultList.isPolymorphic = pps.isPolymorphic();

				//return pattern spotter
				resultList.isSingleReturner= retps.isSingleReturner();
				resultList.isMultipleReturner= retps.isMultipleReturner();

				//method patterns spotter
				resultList.isClient = mps.isClient();
				resultList.isJdkClient = mps.isJdkClient();
				resultList.isTailCaller = mps.isTailCaller();


				//String in the form of binary for all the patterns in this method
				String binaryOutput = resultList.printResultsAsBooleans();		

				//add the binary string to an array containing its associated meta info
				methodOutput[5] = binaryOutput;

				//add the array containing the binary String
				output.add(methodOutput);		


				return resultList;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}


	private String findClass(String name)
	{
		if (name.contains ("/"))
		{
			String theClass = name.substring(name.lastIndexOf("/")+1);
			return theClass;
		}
		else
		{
			return name;
		}
	}
	
	/**
	 * Finds the package the method is in according to its filename
	 * 
	 * @param name the filename 
	 * @return the package if applicable, or "default" if not
	 */
	private String findPackages(String name) {

		if (name.contains("/"))
		{

			String thePackage = name.substring(0, name.lastIndexOf('/'));
			
			//possibly not needed - replaces / in the package name with the more traditional .
			thePackage = thePackage.replaceAll("/", ".");

			return thePackage;
		}
		else return "default";		

	}

}



