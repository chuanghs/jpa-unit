package org.ormunit.xml.editor.scm;

import java.util.HashMap;
import java.util.Map;

public class SCMClass {

	private SCMPackage scmPackage;

	private String name;

	private Map<String, SCMMethod> methods = new HashMap<String, SCMMethod>();

	private Map<String, SCMField> fields = new HashMap<String, SCMField>();

}
