/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.ormunit.xml.editor.contentassist;

import java.util.Iterator;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.parser.regions.AttributeNameRegion;
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

	public void addEmptyDocumentProposals(ContentAssistRequest request) {
		request.addProposal(new CompletionProposal("<ormunit>\n\n</ormunit>",
				request.getReplacementBeginPosition(), request
						.getReplacementLength(), 10, null, "<ormunit>", null,
				"ORM-Unit document starting tag"));
	}

	public void addStartDocumentProposals(ContentAssistRequest request) {
		request.addProposal(new CompletionProposal("<ormunit>\n\n</ormunit>",
				request.getReplacementBeginPosition(), request
						.getReplacementLength(), 10, null, "<ormunit>", null,
				"ORM-Unit document starting tag"));
	}

	// ref(), ormref()
	public void addAttributeValueProposals(ContentAssistRequest request) {
		
		String path = "";

		IStructuredDocumentRegion documentRegion = request.getDocumentRegion();

		int attributeOffest = request.getStartOffset()
				- documentRegion.getStartOffset();
		Iterator<?> iterator = documentRegion.getRegions().iterator();
		AttributeNameRegion anr = null;
		while (iterator.hasNext()) {
			ITextRegion next = (ITextRegion) iterator.next();

			if (next instanceof AttributeNameRegion) {
				anr = (AttributeNameRegion) next;
			}

			if (next.getStart() <= attributeOffest
					&& next.getEnd() > attributeOffest) {
				path = documentRegion.getFullText(anr);
				break;
			}
		}

		Node node = request.getNode();
		while (node != null && node.getParentNode() != null) {
			path = node.getNodeName() + "." + path;
			node = node.getParentNode();
		}

		if ("ormunit.include.src".equals(path)) {
			
		} else if ("ormunit.import.class".equals(path)) {

		} else if ("ormunit.import.alias".equals(path)) {

		}

		super.addAttributeValueProposals(request);
	}

	// properties, <entry>,
	public void addTagNameProposals(ContentAssistRequest request,
			int childPosition) {

		Node node = request.getNode();

		if (node.getParentNode() != null
				&& "ormunit".equals(node.getParentNode().getNodeName())) {
			if ("#document".equals(node.getParentNode().getParentNode()
					.getNodeName())) {
				request.addProposal(new CompletionProposal(
						"include src=\"\" />", request
								.getReplacementBeginPosition(), request
								.getReplacementLength(), 13, null, "include",
						null,
						"Just sets the attribute name to be \"sample\".  Nothing clever."));

				request.addProposal(new CompletionProposal(
						"import class=\"\"  />", request
								.getReplacementBeginPosition(), request
								.getReplacementLength(), 14, null, "import",
						null,
						"Just sets the attribute name to be \"sample\".  Nothing clever."));
				return;
			}
		}

		super.addTagInsertionProposals(request, childPosition);
	}

	public void addTagInsertionProposals(ContentAssistRequest request,
			int childPosition) {

		Node node = request.getNode();

		if (node.getParentNode() != null
				&& "ormunit".equals(node.getParentNode().getNodeName())) {
			if ("#document".equals(node.getParentNode().getParentNode()
					.getNodeName())) {
				request.addProposal(new CompletionProposal(
						"<include src=\"\" />", request
								.getReplacementBeginPosition(), request
								.getReplacementLength(), 14, null, "include",
						null,
						"Just sets the attribute name to be \"sample\".  Nothing clever."));

				request.addProposal(new CompletionProposal(
						"<import class=\"\"  />", request
								.getReplacementBeginPosition(), request
								.getReplacementLength(), 15, null, "import",
						null,
						"Just sets the attribute name to be \"sample\".  Nothing clever."));
				return;
			}
		}

		super.addTagInsertionProposals(request, childPosition);
	}

	/*
	 * 
	 */
	protected void addAttributeNameProposals(ContentAssistRequest request) {

		super.addAttributeNameProposals(request);
	}
}
