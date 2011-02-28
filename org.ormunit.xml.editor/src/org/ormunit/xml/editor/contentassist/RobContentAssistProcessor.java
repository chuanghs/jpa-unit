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
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.contentmodel.CMContent;
import org.eclipse.wst.xml.core.internal.parser.regions.AttributeValueRegion;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.w3c.dom.Node;

/**
 * Our own content assist processor. The algorithm for actually creating
 * meaningful content assist isn't the point of this example (plus they're
 * already provided), so this example will always propose the same thing.
 */
@SuppressWarnings({ "restriction", "deprecation" })
public class RobContentAssistProcessor extends XMLContentAssistProcessor {

	/**
	 * 
	 */
	public RobContentAssistProcessor() {
		super();
	}

	public void addStartDocumentProposals(ContentAssistRequest request) {
		request.addProposal(new CompletionProposal("<ormunit>\n\n</ormunit>",
				request.getReplacementBeginPosition(), request
						.getReplacementLength(), 10, null, "<ormunit>", null,
				"ORM-Unit document starting tag"));

		super.addAttributeNameProposals(request);
	}

	// ref(), ormref()
	public void addAttributeValueProposals(ContentAssistRequest request) {
		
		AttributeValueRegion region = (AttributeValueRegion) request.getRegion();
		System.out.println(region.getType());
		
		Node node = request.getNode();
		while (node != null) {
			System.out.println(node.getNodeName());
			node = node.getParentNode();
		}

		super.addAttributeValueProposals(request);
	}

	// properties, <entry>,
	public void addTagInsertionProposals(ContentAssistRequest request,
			int childPosition) {

		Node node = request.getNode();
		while (node != null) {
			System.out.println(node.getNodeName());
			node = node.getParentNode();
		}

		super.addTagInsertionProposals(request, childPosition);
	}

	/*
	 * 
	 */
	protected void addAttributeNameProposals(ContentAssistRequest request) {

		Node node = request.getNode();
		while (node != null) {
			System.out.println(node.getNodeName());
			node = node.getParentNode();
		}

		request.addProposal(new CompletionProposal("sample", request
				.getReplacementBeginPosition(), request.getReplacementLength(),
				request.getReplacementLength(), null, "sample", null,
				"Just sets the attribute name to be \"sample\".  Nothing clever."));

		super.addAttributeNameProposals(request);
	}
}
