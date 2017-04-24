/*
** Copyright 2002-2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the Authorization Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/index.html#authkit> 
*/

package glguerin.authkit.imp.macosx;

import java.io.*;


// --- Revision History ---
// 27Nov2002 GLG  create
// 15Jan2003 GLG  cut junk
// 19Jun2003 GLG  expand doc-comments
// 24Jun2003 GLG  improve error-checking of FIn and FOut methods


/**
** AuthProcess represents the privileged child-process created by MacOSXAuthorization.
** This class owes much of its decrepitude to the decrepitude of the
** Authorization Services API function that runs the privileged process.
** That is, this class sucks because AuthorizationExecuteWithPrivileges() sucks.
**<p>
** The nested classes FIn and FOut are needed because I have no details on how Java's normal
** file-descriptor class, FileDescriptor, is actually implemented in a given JVM.
** IMHO, exposing FileDescriptor without a clearly defined API or rationale for it was a mistake.
** It should be internal, or it should have a reason for being public. 
** As it now stands, it's just a pointless impediment to clarity and portability.
**
** @author Gregory Guerin
*/

public final class AuthProcess
  extends Process
{
	// This class relies on MacOSXAuthorization to load its JNI library.
	// Since an AuthProcess should only be instantiated by MacOSXAuthorization,
	// MacOSXAuthorization will always be able to do this.

	private String myName;

	private InputStream in;
	private OutputStream out;

	/**
	** The FD's may be identical, in which case the pipe is bidirectional,
	** and closing either stream will close the other, too.
	** This is bad, but all the other options are worse.
	*/
	protected
	AuthProcess( String name, int inFD, int outFD )
	{
		super();
		myName = name;
		in = new FIn( inFD );
		out = new FOut( outFD );
	}

	/** Return name at creation. */
	public String
	toString()
	{  return ( myName );  }


	/**
	** Return the stream feeding stdin of the subprocess.
	*/
	public OutputStream
	getOutputStream()
	{  return ( out );  }

	/**
	** Return the stream fed by stdout of the subprocess.
	*/
	public InputStream
	getInputStream()
	{  return ( in );  }

	/**
	** Return the stream fed by stderr of the subprocess.
	**<p>
	** Not implemented, so always throws an IllegalArgumentException.
	*/
	public InputStream
	getErrorStream()
	{  throw new IllegalArgumentException( "No stderr stream" );  }


	/**
	** Wait for the subprocess to terminate (exit).
	**<p>
	** Not implemented, so always throws an IllegalArgumentException.
	*/
	public int
	waitFor()
	  throws InterruptedException
	{  throw new IllegalArgumentException( "No waiting" );  }

	/**
	** Return the exit value, or throw an IllegalThreadStateException
	** if this Process has not terminated (exited) yet.
	**<p>
	** Not implemented, so always throws an IllegalArgumentException.
	*/
	public int
	exitValue()
	{  throw new IllegalArgumentException( "No exit value" );  }

	/**
	** Forcibly terminate this Process.
	**<p>
	** Not implemented, so always throws an IllegalArgumentException.
	*/
	public void
	destroy()
	{  throw new IllegalArgumentException( "Can't destroy" );  }



	/**
	** An instance of FIn is an InputStream connected to a privileged process.
	*/
	public static class FIn
	  extends InputStream
	{
		private byte[] one;

		private int myFD;

		/**
		** Create with given FD (an int), which must be
		** ready for reading at its current position.
		** When this InputStream is closed, its underlying FD is also closed.
		*/
		public
		FIn( int aFD )
		{
			super();
			one = new byte[ 1 ];
			myFD = aFD;
		}

		/** Call close() on finalize. */
		protected void
		finalize()
		  throws Throwable
		{  close();  }

		/**
		** Read one byte, returning it unsigned in low 8-bits of int,
		** or return -1 on EOF.
		*/
		public int
		read()
		  throws IOException
		{
			int got = read( one, 0, 1 );
			if ( got == 1 )
				return ( 0xFF & one[ 0 ] );
			else
				return ( -1 );
		}

		/**
		** Read bytes into a range of an array.
		** If any bytes are read, return a non-zero count.
		** If no bytes are read, check for EOF.
		*/
		public int 
		read( byte[] buffer, int offset, int count )
		  throws IOException
		{
			if ( myFD < 0 )
				throw new IOException( "Closed" );

			// Returns negative error-code, 0 at EOF, or positive byte-count.
			int got = AuthProcess.read( myFD, buffer, offset, count );
			if ( got > 0 )
				return ( got );

			if ( got == 0 )
				return ( -1 );

			throw new IOException( "Errno: " + (-got) );
		}

		/**
		** Close.
		*/
		public void
		close()
		  throws IOException
		{
			if ( myFD >= 0 )
			{
				int wasFD = myFD;
				myFD = -1;

				int result = AuthProcess.close( wasFD );
				if ( result != 0 )
					throw new IOException( "Errno: " + result );
			}
		}

	}


	/**
	** An instance of FOut is an OutputStream connected to a privileged process.
	*/
	public static class FOut
	  extends OutputStream
	{
		private byte[] one;

		private int myFD;

		/**
		** Create with given FD (an int), which must be
		** ready for writing at its current position.
		** When this OutputStream is closed, its underlying FD is also closed.
		*/
		public
		FOut( int aFD )
		{
			super();
			one = new byte[ 1 ];
			myFD = aFD;
		}

		/** Call close() on finalize. */
		protected void
		finalize()
		  throws Throwable
		{  close();  }


		/**
		** Write one byte.
		*/
		public void
		write( int abyte )
		  throws IOException
		{
			one[ 0 ] = (byte) abyte;
			write( one, 0, 1 );
		}

		/**
		** Write range of byte-array.
		*/
		public void
		write( byte[] bytes, int offset, int count )
		  throws IOException
		{
			if ( myFD < 0 )
				throw new IOException( "Closed" );

			// The returned value will be negative on errors.
			int result = AuthProcess.write( myFD, bytes, offset, count );
			if ( result != count )
				throw new IOException( "Errno: " + (-result) );
		}


		/**
		** Close.
		*/
		public void
		close()
		  throws IOException
		{
			if ( myFD >= 0 )
			{
				int wasFD = myFD;
				myFD = -1;

				int result = AuthProcess.close( wasFD );
				if ( result != 0 )
					throw new IOException( "Errno: " + result );
			}
		}

	}



	// ###  J N I   F U N C T I O N   B I N D I N G S  ###

	/**
	** Read bytes, returning count actually read,
	** or 0 on EOF, or -errno on error.
	**<p>
	** The specific reasons for failure may be distinguished by decoding negative numbers
	** as errno negatives, or all failures can be lumped together into one IOException flavor.
	 */
	protected static native int
		read( int inFD, byte[] buf, int offset, int count );

	/**
	** Write bytes, returning count actually written,
	** or -errno on error.
	**<p>
	** The specific reasons for failure may be distinguished by decoding negative numbers
	** as errno negatives, or all failures can be lumped together into one IOException flavor.
	 */
	protected static native int
		write( int outFD, byte[] buf, int offset, int count );

	/**
	** Close the file-descriptor, returning 0 on success or errno on failure.
	** Unlike read() and write(), the actual errno value, not its negative, is returned.
	 */
	protected static native int
		close( int aFD );

}