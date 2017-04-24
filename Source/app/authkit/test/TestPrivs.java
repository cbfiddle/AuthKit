/*** Copyright 2002-2003 by Gregory L. Guerin.** Terms of use:**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely.**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>** This file is from the Authorization Toolkit for Java:**   <http://www.amug.org/~glguerin/sw/index.html#authkit> */package app.authkit.test;import glguerin.authkit.*;// --- Revision History ---// 09Nov2002 GLG  create// 20Jun2003 GLG  doc-comments/**** TestPrivs is one of the simplest possible tests of the Privilege class.** It just makes 'em and displays 'em.**<p>** The property "authkit.imp" is ** the fully qualified class name of an Authorization to instantiate and use.** It must be instantiable, but this test doesn't actually use it for anything.**** @author Gregory Guerin*/public class TestPrivs  extends AuthTest{	/**	** Static entry point, when used as an application.	*/	public static void 	main( String[] args ) 	{		tell( "TestPrivs.main(): starting..." );		Authorization auth = makeAuth( null );		tell( "Authorization: " + auth.getClass() );		new TestPrivs().testAuth( auth, "interact", args );		tell( "TestPrivs.main(): done...\n" );	}	/** Do the test on one arg.  Doesn't use 'auth' or 'interact'. */	public void	testAuthOne( Authorization auth, boolean interact, String arg )	{		Privilege priv = new Privilege( arg );		tell( "Privilege: " + priv );		priv = new Privilege( arg, arg.toUpperCase(), 0 );		tell( "    upper: " + priv );	}}