/*** Copyright 2002-2003 by Gregory L. Guerin.** Terms of use:**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely.**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>** This file is from the Authorization Toolkit for Java:**   <http://www.amug.org/~glguerin/sw/index.html#authkit> */package app.authkit.test;import java.io.*;import glguerin.authkit.*;// --- Revision History ---// 29Oct2002 GLG  create// 20Jun2003 GLG  cleanup/**** The AllTests class invokes a series of desired AuthTest subclasses.** Its static main() method is the entry point.**<p>** The property "authkit.imp" is ** the fully qualified class name of an Authorization to instantiate and use** for all tests.**** @author Gregory Guerin*/public class AllTests{	/**	** Static entry point of application.	*/	public static void 	main( String[] args ) 	{		tell( "AllTests.main(): starting..." );		Authorization auth = AuthTest.makeAuth( null );		tell( "Authorization: " + auth.getClass() );		new TestPrivs().testAuth( auth, "interact", args );//		new TestExternalize().testAuth( auth, "interact", args );		new TestReattach().testAuth( auth, "interact", args );		tell( "AllTests.main(): I'm outta here...\n" );	}	private static void	tell( String toTell )	{		System.out.println( toTell );	}}