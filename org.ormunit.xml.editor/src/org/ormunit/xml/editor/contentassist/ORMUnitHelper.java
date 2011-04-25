package org.ormunit.xml.editor.contentassist;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ORMUnitHelper {

	public static String[] getORMUnitXmlFiles(IJavaProject project) {
		Set<String> result = new HashSet<String>();

		try {
			IPackageFragmentRoot[] packageFragmentRoots = project.getPackageFragmentRoots();
			for (IPackageFragmentRoot root : packageFragmentRoots) {
				Object[] nonJavaResources = root.getNonJavaResources();
				for (Object o : nonJavaResources) {
					if (o instanceof IResource) {
						IResource res = (IResource) o;
						if ("xml".equals(res.getFullPath().getFileExtension())) {
							result.add(res.getFullPath().toString().substring(root.getPath().toString().length()));
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return result.toArray(new String[result.size()]);
	}

	public static Collection<String> getClassNames(IJavaProject project, String start) {
		if (start == null)
			start = "";

		start = start.trim().toLowerCase();

		TreeMap<String, String> result = new TreeMap<String, String>();

		if (!"".equals(start)) {
			try {
				IPackageFragment[] packageFragments = project.getPackageFragments();
				for (IPackageFragment root : packageFragments) {
					IClassFile[] classFiles = root.getClassFiles();
					for (IClassFile cf : classFiles) {
						IType type = cf.getType();
						
						String className = cf.getElementName();
						className = className.substring(0, className.indexOf("."));
						
						String fullName = root.getElementName() + "." + className;
						result.put(fullName.toLowerCase(), fullName);
						result.put(className.toLowerCase(), fullName);
					}
					ICompilationUnit[] compilationUnits = root.getCompilationUnits();
					for (ICompilationUnit cp : compilationUnits) {
						String className = cp.getElementName();
						className = className.substring(0, className.indexOf("."));
						
						String fullName = root.getElementName() + "."+className;
						
						result.put(fullName.toLowerCase(), fullName);
						result.put(className.toLowerCase(), fullName);
					}
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		
		String end = start.subSequence(0, start.length()-1)+""+((char)(start.charAt(start.length()-1)+1));
		
		return result.subMap(start, end).values();
	}

}
