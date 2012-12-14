package com.pelzer.util;

import java.util.Set;

import junit.framework.TestCase;

public class ClassFinderTest extends TestCase {
	public void testGeneralCase(){
		ClassFinder finder = new ClassFinder();
		assertNull(finder.getLocationOf(ClassFinder.class));
		Set<Class<?>> classes = finder.findSubclasses("com.pelzer.util.ClassFinder");
		assertNotNull(classes);
		assertTrue("Matching class list was empty", classes.size()>0);
		// There should only be the one instance, extended below
		assertEquals("Should only be one child of ClassFinder", 1, classes.size());
	}
	
	public static class ClassFinderChild extends ClassFinder{}
}
