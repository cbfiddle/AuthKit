/*** Copyright 2002-2003 by Gregory L. Guerin.** Terms of use:**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>** This file is from the Authorization Toolkit for Java:**   <http://www.amug.org/~glguerin/sw/index.html#authkit> */package glguerin.authkit.imp.plain;import java.util.Date;import java.util.Enumeration;import java.util.Hashtable;import glguerin.authkit.*;// --- Revision History ---// 05Nov2002 GLG  create// 07Nov2002 GLG  add internalize() and externalize()// 18Nov2002 GLG  revise for new execPrivileged() args// 21Nov2002 GLG  cover externalize() changes// 15Jun2003 GLG  add getCapabilities()// 15Jun2003 GLG  change to detach()// 19Jun2003 GLG  expand doc-comments/**** As its name implies, DenyAllAuthorization is an Authorization that denies all requests.** Every call to authorize() or preauthorize() is always denied, throwing an UnauthorizedException.**  Every call to isAvailable() always returns false.** This class is primarily for testing, since it doesn't allow any Privileges at all.**<p>** The implementation of DenyAllAuthorization uses only plain Java facilities,** so it is entirely platform-neutral. ** Since no action is ever authorized, there isn't much else to say about it.**** @author Gregory Guerin*/public class DenyAllAuthorization  extends Authorization{	/** A convenient way to obtain an empty Enumeration. */	private static final Hashtable empty = new Hashtable( 7 );	/** Create, unattached and unattachable. */	public	DenyAllAuthorization()	{  super();  }	/** Return zero: no special capabilities. */	public int	getCapabilities()	{  return ( 0 );  }	/**	** Always deny the Privilege by throwing an UnauthorizedException.	**	** @exception glguerin.authkit.UnauthorizedException	**   thrown when the requested Privilege is not granted, i.e. always.	*/	public void	authorize( Privilege toGrant, boolean interactionAllowed )	{  throw new UnauthorizedException( "Authorization denied: " + toGrant );  }	/**	** Always deny the Privilege by throwing an UnauthorizedException.	**	** @exception glguerin.authkit.UnauthorizedException	**   thrown when the requested Privilege is not granted, i.e. always.	*/	public void	preauthorize( Privilege toGrant )	{  throw new UnauthorizedException( "Preauthorization denied: " + toGrant );  }	/**	** Is the given Privilege currently authorized or preauthorized?	**<p>	** Always returns false.	*/	public boolean	isAvailable( Privilege toCheck )	{  return ( false );  }	/**	** Detach the underlying session and all its granted authorizations, credentials, and rights.	**<p>	** This imp always returns without throwing an exception.	*/	public void	detach( boolean revokeShared )	{  return;  }	/**	** Return all past granted Privileges in an Enumeration.	**<p>	** This imp always returns an empty but non-null Enumeration, since no Privilege is ever granted.	*/	public Enumeration	getPastGrantedPrivileges()	{  return ( empty.keys() );  }	/**	** Return a timestamp Date for when Privilege was granted.	**<p>	** This imp always returns null, since no Privilege is ever granted.	*/	public Date	getPastGrantedDate( Privilege privilege, int when )	{  return ( null );  }	/**	** Make and return a Privilege representing the right to execute a program as root.	**<p>	** This imp always returns a Privilege with the name "exec.root.denied"	** and a value of the cmdName.  It won't work, but it's non-null and distinctive.	*/	public Privilege	makeExecPrivilege( String cmdName )	{  return ( new Privilege( "exec.root.denied", cmdName, 0 ) );  }	/**	** Execute a program as root (run under root privileges).	** If the privilege is denied, an UnauthorizedException is thrown and no program is executed.	**<p>	** This imp always throws an UnauthorizedException.	**	** @exception glguerin.authkit.UnauthorizedException	**   thrown when the necessary Privilege is not granted or preauthorized.	** @exception java.lang.IllegalArgumentException	**   thrown when some malformation or structural error occurs.	*/	public Process	execPrivileged( String[] progArray )	{  throw new UnauthorizedException( "Execution denied: " + progArray[ 0 ] );  }	/**	** Return the length of a buffer that can hold the secret session identifier.	**<p>	** This imp returns a buffer that's a different size than GrantAll or MacOSXAuthorization.	** This is partly to distinguish it, and partly so tests have to deal with different sizes.	*/	public int	getSecretLength()	{  return ( 13 );  }	/**	** Return a secret identifier for this Authorization's current session.	**<p>	** This imp always throws an UnauthorizedException.	** I chose to do this rather than return a new byte[0] because it's more in line	** with the rest of this class's behavior.	**	** @exception glguerin.authkit.UnauthorizedException	**   thrown when the session identifier cannot be externalized, i.e. always.	*/	public byte[]	getSecretIdentifier()	{  throw new UnauthorizedException( "Authorization denied" );  }	/**	** Reconstitute the given secret externalized session ID,	** reconnecting (if possible) this Authorization to the underlying session.	** This Authorization must be newly constructed, or have just been detach()'d.	**<p>	** This imp always throws an UnauthorizedException, even if unattached.	**	** @exception glguerin.authkit.UnauthorizedException	**   thrown when the session identifier cannot be internalized, i.e. always.	*/	public void	attach( byte[] secretIdentifier )	{  throw new UnauthorizedException( "Authorization denied" );  }	/**	** This imp always throws an UnauthorizedException, even if unattached.	**	** @exception glguerin.authkit.UnauthorizedException	**   thrown when there is no privileged session to reconnect to, i.e. always.	*/	public void	attachPrivileged()	{  throw new UnauthorizedException( "Authorization denied" );  }}