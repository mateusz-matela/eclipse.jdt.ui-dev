/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.IErrorWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.PreviewWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryWizard;

/**
 * Preview page for refactoring history wizards.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryPreviewPage extends PreviewWizardPage {

	/** Is the current refactoring the last one? */
	private boolean fLastRefactoring= false;

	/** The current refactoring, or <code>null</code> */
	private Refactoring fRefactoring;

	/** The refactoring status */
	private RefactoringStatus fStatus= new RefactoringStatus();

	/**
	 * Creates a new refactoring history preview page.
	 */
	public RefactoringHistoryPreviewPage() {
		super(true);
		setTitle(RefactoringUIMessages.RefactoringHistoryOverviewPage_title);
		setDescription(RefactoringUIMessages.RefactoringHistoryPreviewPage_description);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canFlipToNextPage() {
		return !fLastRefactoring;
	}

	/**
	 * {@inheritDoc}
	 */
	public IWizardPage getNextPage() {
		if (fChange != null && fRefactoring != null && !fStatus.hasFatalError()) {
			final RefactoringHistoryWizard result= getRefactoringHistoryWizard();
			if (result != null) {
				final RefactoringStatus status= result.performPreviewChange(fChange, fRefactoring);
				if (!status.isOK()) {
					final RefactoringStatusEntry entry= status.getEntryWithHighestSeverity();
					if (entry != null) {
						if (entry.getSeverity() == RefactoringStatus.INFO && entry.getCode() == RefactoringHistoryWizard.STATUS_CODE_INTERRUPTED)
							return this;
						final IErrorWizardPage page= result.getErrorPage();
						if (page instanceof RefactoringHistoryErrorPage) {
							final RefactoringHistoryErrorPage extended= (RefactoringHistoryErrorPage) page;
							extended.setStatus(status);
							extended.setLastRefactoring(fLastRefactoring);
							extended.setTitle(RefactoringUIMessages.RefactoringHistoryPreviewPage_apply_error_title);
							extended.setDescription(RefactoringUIMessages.RefactoringHistoryPreviewPage_apply_error);
							return extended;
						}
					}
				}
			}
		}
		return getWizard().getNextPage(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public IWizardPage getPreviousPage() {
		return getWizard().getPreviousPage(this);
	}

	/**
	 * Returns the current refactoring.
	 * 
	 * @return the current refactoring
	 */
	public Refactoring getRefactoring() {
		return fRefactoring;
	}

	/**
	 * Returns the refactoring history wizard.
	 * 
	 * @return the refactoring history wizard
	 */
	protected RefactoringHistoryWizard getRefactoringHistoryWizard() {
		final IWizard result= getWizard();
		if (result instanceof RefactoringHistoryWizard)
			return (RefactoringHistoryWizard) result;
		return null;
	}

	/**
	 * Is the current refactoring the last one?
	 * 
	 * @return <code>true</code> if it is the last one, <code>false</code>
	 *         otherwise
	 */
	public boolean isLastRefactoring() {
		return fLastRefactoring;
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean performFinish() {
		return true;
	}

	/**
	 * Determines whether the current refactoring is the last one.
	 * 
	 * @param last
	 *            <code>true</code> if it is the last one, <code>false</code>
	 *            otherwise
	 */
	public void setLastRefactoring(final boolean last) {
		fLastRefactoring= last;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPageComplete(final boolean complete) {
		super.setPageComplete(!fLastRefactoring);
	}

	/**
	 * Sets the current refactoring.
	 * 
	 * @param refactoring
	 *            the current refactoring, or <code>null</code>
	 */
	public void setRefactoring(final Refactoring refactoring) {
		fRefactoring= refactoring;
	}

	/**
	 * Sets the status of the change generation.
	 * 
	 * @param status
	 *            the status
	 */
	public void setStatus(final RefactoringStatus status) {
		Assert.isNotNull(status);
		fStatus= status;
	}

	/**
	 * Sets the title of the page according to the refactoring.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor, or <code>null</code>
	 */
	public void setTitle(final RefactoringDescriptorProxy descriptor) {
		if (descriptor != null)
			setTitle(descriptor.getDescription());
		else
			setTitle(RefactoringUIMessages.RefactoringHistoryOverviewPage_title);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setWizard(final IWizard newWizard) {
		Assert.isTrue(newWizard instanceof RefactoringHistoryWizard);
		super.setWizard(newWizard);
	}
}