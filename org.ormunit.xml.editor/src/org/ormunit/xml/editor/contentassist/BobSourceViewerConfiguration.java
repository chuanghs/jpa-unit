/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.ormunit.xml.editor.contentassist;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.wst.xml.ui.StructuredTextViewerConfigurationXML;

/**
 * A source viewer configuration for Rob files, based on the XML configuration.
 */
public class BobSourceViewerConfiguration extends StructuredTextViewerConfigurationXML {

	/**
	 * 
	 */
	public BobSourceViewerConfiguration() {
		super();
	}

	/*
	 * Return our customized processor for the main partition types. Because the
	 * ContentAssistant which handles the UI only allows one processor per
	 * partition type, we'll have to replace the default
	 * XMLContentAssistProcessor with one of our own choosing. Since we don't
	 * want to lose its proposals, we'll subclass it.
	 * 
	 * Eventually WTP's editor should support individually contributable content
	 * assist "computers" like JDT, so the source viewer configuration class
	 * would become unnecessary.
	 */
	protected IContentAssistProcessor[] getContentAssistProcessors(ISourceViewer sourceViewer, String partitionType) {
		IContentAssistProcessor[] contentAssistProcessors = super.getContentAssistProcessors(sourceViewer, partitionType);
		
		IContentAssistProcessor[] result = new IContentAssistProcessor[contentAssistProcessors.length+1];
		result[0] = new RobContentAssistProcessor();
		
		System.arraycopy(contentAssistProcessors, 0, result, 1, contentAssistProcessors.length);
		return result;
	}

}
