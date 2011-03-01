/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.javaeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;

import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;

import org.eclipse.jdt.internal.ui.text.JavaWordFinder;


/**
 * Java element hyperlink detector.
 *
 * @since 3.1
 */
public class JavaElementHyperlinkDetector extends AbstractHyperlinkDetector {

	/*
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion, boolean)
	 */
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		ITextEditor textEditor= (ITextEditor)getAdapter(ITextEditor.class);
		if (region == null || !(textEditor instanceof JavaEditor))
			return null;

		IAction openAction= textEditor.getAction("OpenEditor"); //$NON-NLS-1$
		if (!(openAction instanceof SelectionDispatchAction))
			return null;

		int offset= region.getOffset();

		IJavaElement input= EditorUtility.getEditorInputJavaElement(textEditor, false);
		if (input == null)
			return null;

		try {
			IDocument document= textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
			IRegion wordRegion= JavaWordFinder.findWord(document, offset);
			if (wordRegion == null || wordRegion.getLength() == 0)
				return null;

			if (isInheritDoc(document, wordRegion) && getClass() != JavaElementHyperlinkDetector.class)
				return null;
			
			ASTNode labelNode= getBreakOrContinueLabelNode(input, region);
			if (labelNode != null) {
				IHyperlink link= createHyperlink(wordRegion, (SelectionDispatchAction)openAction, null, false, null);
				return link != null ? new IHyperlink[] { link } : null;
			}

			IJavaElement[] elements= ((ICodeAssist) input).codeSelect(wordRegion.getOffset(), wordRegion.getLength());
			elements= selectOpenableElements(elements);
			if (elements.length == 0)
				return null;
			
			IHyperlink[] links= new IHyperlink[elements.length];
			int j= 0;
			for (int i= 0; i < elements.length; i++) {
				IHyperlink link= createHyperlink(wordRegion, (SelectionDispatchAction)openAction, elements[i], elements.length > 1, (JavaEditor)textEditor);
				if (link != null) {
					links[j++]= link;
				}
			}
			if (j == 0) {
				return null;
			} else if (j < elements.length) {
				IHyperlink[] result= new IHyperlink[j];
				System.arraycopy(links, 0, result, 0, j);
				return result;
			}
			return links;

		} catch (JavaModelException e) {
			return null;
		}
	}

	/**
	 * Returns whether the word is "inheritDoc".
	 * 
	 * @param document the document
	 * @param wordRegion the word region
	 * @return <code>true</code> iff the word is "inheritDoc"
	 * @since 3.7
	 */
	private static boolean isInheritDoc(IDocument document, IRegion wordRegion) {
		try {
			String word= document.get(wordRegion.getOffset(), wordRegion.getLength());
			return "inheritDoc".equals(word); //$NON-NLS-1$
		} catch (BadLocationException e) {
			return false;
		}
	}

	/**
	 * Creates a java element hyperlink.
	 * 
	 * @param wordRegion the region of the link
	 * @param openAction the action to use to open the java elements
	 * @param element the java element to open or <code>null</code> if its a label
	 * @param qualify <code>true</code> if the hyperlink text should show a qualified name for
	 *            element
	 * @param editor the active java editor
	 * @return a Java element hyperlink or <code>null</code> if no hyperlink can be created for the
	 *         given arguments
	 * @since 3.5
	 */
	protected IHyperlink createHyperlink(IRegion wordRegion, SelectionDispatchAction openAction, IJavaElement element, boolean qualify, JavaEditor editor) {
		return new JavaElementHyperlink(wordRegion, openAction, element, qualify);
	}


	/**
	 * Selects the openable elements out of the given ones.
	 *
	 * @param elements the elements to filter
	 * @return the openable elements
	 * @since 3.4
	 */
	private IJavaElement[] selectOpenableElements(IJavaElement[] elements) {
		List result= new ArrayList(elements.length);
		for (int i= 0; i < elements.length; i++) {
			IJavaElement element= elements[i];
			switch (element.getElementType()) {
				case IJavaElement.PACKAGE_DECLARATION:
				case IJavaElement.PACKAGE_FRAGMENT:
				case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				case IJavaElement.JAVA_PROJECT:
				case IJavaElement.JAVA_MODEL:
					break;
				default:
					result.add(element);
					break;
			}
		}
		return (IJavaElement[]) result.toArray(new IJavaElement[result.size()]);
	}

	/**
	 * Gets the break/continue label node.
	 * 
	 * @param input the editor input
	 * @param region the region
	 * @return the break/continue label or <code>null</code> if none
	 * @since 3.7
	 */
	public static ASTNode getBreakOrContinueLabelNode(IJavaElement input, IRegion region) {
		CompilationUnit ast= SharedASTProvider.getAST((ITypeRoot)input, SharedASTProvider.WAIT_NO, null);
		if (ast == null)
			return null;

		ASTNode node= NodeFinder.perform(ast, region.getOffset(), region.getLength());
		if (node instanceof SimpleName) {
			SimpleName simpleName= (SimpleName)node;
			ASTNode parent= simpleName.getParent();
			if (parent instanceof ContinueStatement || parent instanceof BreakStatement)
				return simpleName;
		}
		return null;
	}
}
