/*
** Copyright 2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
*/

package glguerin.util;

import java.io.*;


// --- Revision History ---
// 30Mar2003 GLG  create
// 01Apr2003 GLG  add makeDaemon()
// 01Apr2003 GLG  add code to accept null InputStream
// 07Apr2003 GLG  cover null threadName arg to makeDaemon()
// 18Apr2003 GLG  add flushOutput constructor arg, rearrange args
// 10Jun2003 GLG  rescope fields to 'protected'


/**
** A Streamer "pumps" an InputStream to an OutputStream until EOF.
** If the OutputStream is null, then the InputStream is simply consumed until EOF.
** If the InputStream is null, then nothing is read and nothing is written to the OutputStream.
** A null InputStream is not generally useful, but it won't fail.
**<p>
** A Streamer implements Runnable with a run() method that simply
** calls pump(), ignores IOExceptions, and returns on completion.
** This is so a Thread can pump() a Streamer with no other intervening code.
** The makeDaemon() method makes a daemon Thread that does this.
**
** @author Gregory Guerin
*/

public class Streamer
  implements Runnable
{
	/** The buffer assigned in constructor. */
	protected byte[] buffer;

	/** The InputStream assigned in constructor, which may be null. */
	protected InputStream in;

	/** Whether to close InputStream or not. */
	protected boolean closeInput;

	/** The OutputStream assigned in constructor, which may be null. */
	protected OutputStream out;

	/** Whether to close and/or flush OutputStream or not. */
	protected boolean closeOutput, flush;

	/**
	** Create with given buffer, streams, and stream-closing policies.
	** The InputStream and/or OutputStream may be null.
	** The buffer must be non-null.
	**<p>
	** If flushOutput is T, the OutputStream will be flush()'ed each time data is written to it.
	** If F, then the OutputStream is not overtly flush()'ed while pumping data.
	**<p>
	** The OutputStream is always flush()'ed when the InputStream returns EOF,
	** regardless of the state of flushOutput.  This is done because the OutputStream
	** may not be closed, and we want to push all the pumped data through.
	*/
	public
	Streamer( byte[] buffer, InputStream in, boolean closeInput,
			OutputStream out, boolean closeOutput, boolean flushOutput )
	{
		this.buffer = buffer;

		this.in = in;
		this.closeInput = closeInput;

		this.out = out;
		this.closeOutput = closeOutput;
		this.flush = flushOutput;
	}


	/**
	** "Pump" the InputStream to the OutputStream until EOF,
	** returning the count of bytes transferred.
	** The streams will be closed on successful return if so designated in the constructor.
	** Neither stream will be closed if an IOException is thrown.
	*/
	public long
	pump()
	  throws IOException
	{
		long total = 0;

		// We don't really need to check for null each iteration, but it's
		// a convenient way to avoid putting a loop inside an if{} block.
		// Speed-wise, it's insignificant compared to the I/O.
		while ( in != null )
		{
			int got = in.read( buffer );
			if ( got < 0 )
				break;

			if ( out != null )
			{
				out.write( buffer, 0, got );
				if ( flush )
					out.flush();
			}

			total += got;
		}

		// Always flush output, because we might not close() it.
		if ( out != null )
			out.flush();

		if ( closeInput  &&  in != null )
			in.close();

		if ( closeOutput  &&  out != null )
			out.close();

		return ( total );
	}


	/**
	** Call pump(), ignoring IOExceptions.
	*/
	public void
	run()
	{
		try
		{  pump();  }
		catch ( IOException ignored )
		{  ;  }
	}


	/**
	** Make a daemon Thread with given name and ThreadGroup,
	** that run()'s this Streamer when start()'ed.
	** The returned Thread is not started.
	**<p>
	** If the ThreadGroup is null, the current Thread's ThreadGroup is used.
	** If threadName is null, the new Thread's name is generated automatically.
	** If both are null, then both defaults happen.
	*/
	public Thread
	makeDaemon( ThreadGroup group, String threadName )
	{
		Thread daemon;
		if ( threadName != null )
			daemon = new Thread( group, this, threadName );
		else
			daemon = new Thread( group, this );

		daemon.setDaemon( true );
		return ( daemon );
	}

}
