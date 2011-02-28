/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.ormunit.xml.editor.contentassist;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.wst.xml.core.internal.contentmodel.CMContent;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;

/**
 * Our own content assist processor. The algorithm for actually creating
 * meaningful content assist isn't the point of this example (plus they're
 * already provided), so this example will always propose the same thing.
 */
public class RobContentAssistProcessor extends XMLContentAssistProcessor {

	/**
	 * 
	 */
	public RobContentAssistProcessor() {
		super();
	}

	// <ormunit></ormunit>
	public void addStartDocumentProposals(ContentAssistRequest request){
		super.addStartDocumentProposals(request);
	}
	
	//ref(), ormref()
	public void addAttributeValueProposals(ContentAssistRequest request){
		super.addAttributeValueProposals(request);
	}
		
	// properties, <entry>, 
	public void addTagInsertionProposals(ContentAssistRequest request, int childPosition){
		super.addTagInsertionProposals(request, childPosition);
	}
	
	/*
	 * 
	 */
	protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest) {
	
		// add a proposal for "sample"
		contentAssistRequest.addProposal(new CompletionProposal("sample", contentAssistRequest.getReplacementBeginPosition(), contentAssistRequest.getReplacementLength(), contentAssistRequest.getReplacementLength(), null, "sample", null, "Just sets the attribute name to be \"sample\".  Nothing clever."));
		
		super.addAttributeNameProposals(contentAssistRequest);
	}
}
