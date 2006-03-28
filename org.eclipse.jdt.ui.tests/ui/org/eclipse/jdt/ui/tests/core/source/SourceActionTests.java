/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.tests.core.source;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.ui.tests.core.ProjectTestSetup;
import org.eclipse.jdt.ui.tests.core.source.AddUnimplementedConstructorsTest;
import org.eclipse.jdt.ui.tests.core.source.AddUnimplementedMethodsTest;
import org.eclipse.jdt.ui.tests.core.source.GenerateGettersSettersTest;

/**
 * 
 * Tests for the actions in the source menu
 * 
 */
public class SourceActionTests extends TestCase {

	public static Test suite() {

		TestSuite suite= new TestSuite();
		suite.addTest(AddUnimplementedMethodsTest.allTests());
		suite.addTest(GenerateGettersSettersTest.allTests());
		suite.addTest(GenerateDelegateMethodsTest.allTests());
		suite.addTest(AddUnimplementedConstructorsTest.allTests());
		suite.addTest(GenerateConstructorUsingFieldsTest.allTests());
		suite.addTest(GenerateHashCodeEqualsTest.allTests());

		return new ProjectTestSetup(suite);
	}

	public SourceActionTests(String name) {
		super(name);
	}

}
