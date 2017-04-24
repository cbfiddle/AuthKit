/*
** AuthKit.c -- JNI C code
**
** Copyright 2002 by Gregory L. Guerin.
** All rights reserved.
**
** 06Nov2002 GLG  create with stubs
** 26Nov2002 GLG  add fileRef arg to rootExec()
** 26Nov2002 GLG  add stubs for AuthProcess's "stdio" functions
** 24Jun2003 GLG  FIX: externalize() now accepts buffers longer than needed
** 24Jun2003 GLG  add options arg to rootExec() and internalizePriv()
*/

#include "glguerin_authkit_imp_macosx_MacOSXAuthorization.h"

#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
	// memset() might be in <memory.h>

#include <Security/Authorization.h>


// ###########

#define SUCCESS			errAuthorizationSuccess
#define BAD_AUTHREF	errAuthorizationInvalidRef

#define NO_ENV				kAuthorizationEmptyEnvironment
#define NO_OPTIONS		kAuthorizationFlagDefaults


/*
 * Class:     glguerin_authkit_imp_macosx_MacOSXAuthorization
 * Method:    makeSession
 * Signature: ([J)I
 */

/**
	private static native synchronized int makeSession( long[] sessionRef );

	 @return				in C: <CODE>xx</CODE>
*/

JNIEXPORT jint JNICALL
Java_glguerin_authkit_imp_macosx_MacOSXAuthorization_makeSession(
	JNIEnv * env,  jclass ignored,
	jlongArray sessionRef )
{
	AuthorizationRef authRef;
	OSStatus result;

	// Create AuthRef with no Rights, no env, default flags.
	result = AuthorizationCreate( NULL, NO_ENV, NO_OPTIONS, &authRef );

	// On failure, force the contents of sessionRef to NULL.
	if ( result != SUCCESS )
		authRef = NULL;

	// Always put something in sessionRef.
	(*env)->SetLongArrayRegion( env, sessionRef, 0, 1, (jlong *) &authRef );

	return ( result );
}


/*
 * Class:     glguerin_authkit_imp_macosx_MacOSXAuthorization
 * Method:    killSession
 * Signature: (JI)I
 */

/**
	private static native synchronized int killSession( long session, int options );

** Accept a session value of 0 without error, but do nothing with it.

	 @return				in C: <CODE>xx</CODE>
*/

JNIEXPORT jint JNICALL
Java_glguerin_authkit_imp_macosx_MacOSXAuthorization_killSession(
	JNIEnv * env,  jclass ignored,
	jlong session, jint options )
{
	OSStatus result;

	if ( session == 0 )
		return ( BAD_AUTHREF );

	result = AuthorizationFree( (AuthorizationRef) session, options );

	return ( result );
}


/*
 * Class:     glguerin_authkit_imp_macosx_MacOSXAuthorization
 * Method:    request
 * Signature: (JILjava/lang/String;[BI)I
 */

/**
		request( long session, int options, String name, byte[] value, int flags );

	 The session is from makeSession() or something similar.
	 The options are the AuthorizationFlags, i.e. the "options" representing actions/cmds.
	 The name, value, and flags designate a single Right.
	 @return				in C: <CODE>xx</CODE>
*/

JNIEXPORT jint JNICALL
Java_glguerin_authkit_imp_macosx_MacOSXAuthorization_request(
	JNIEnv * env,  jclass ignored,
	jlong session, jint options,
	jstring name, jbyteArray value, jint flags )
{
	const char * utfName;
	jboolean isCopy;
	jsize valueLen;
	jbyte * valueBytes;

	AuthorizationItem item;
	AuthorizationRights rights;
	OSStatus result;

	if ( session == 0 )
		return ( BAD_AUTHREF );

	// Always get name as UTF8 nul-terminated bytes.
	utfName = (*env)->GetStringUTFChars( env, name, &isCopy );
	if ( utfName == NULL )
		return ( -1 );  // OutOfMemoryError will be thrown

	// Only get value's bytes if something is there.
	valueBytes = NULL;
	valueLen = (*env)->GetArrayLength( env, value );
	if ( valueLen > 0 )
	{
		valueBytes = (*env)->GetByteArrayElements( env, value, NULL );
		if ( valueBytes == NULL )
		{
			(*env)->ReleaseStringUTFChars( env, name, utfName );
			return ( -1 );  // OutOfMemoryError will be thrown
		}
	}

	// Fill in AuthItem with appropriate data...
	item.name = (AuthorizationString) utfName;
	item.valueLength = valueLen;
	item.value = valueBytes;
	item.flags = flags;

	// Point AuthRights to single item...
	rights.count = 1;
	rights.items = &item;

	result = AuthorizationCopyRights(
			(AuthorizationRef) session, &rights, NO_ENV, (AuthorizationFlags) options, NULL );

	// On success or failure, release UTF8 bytes and value-bytes.
	// The valueBytes are released under JNI_ABORT option (no copy-back).
	(*env)->ReleaseStringUTFChars( env, name, utfName );
	if ( valueBytes != NULL )
		(*env)->ReleaseByteArrayElements( env, value, valueBytes, JNI_ABORT );

	return ( result );
}



/*
** Free the NULL-terminated array
** of nul-terminated UTF-8 C-strings,
** stored in malloc'ed memory.
*/
static void
freeArgs( char **args )
{
	char **scan;
	char * each;

	for ( scan = args;  true; )
	{
		each = *scan++;
		if ( each == NULL )
			break;
		free( each );
	}

	free( args );
}


/*
** Translate String[] into NULL-terminated array
** of nul-terminated UTF-8 C-strings,
** stored in malloc'ed memory.
** If any failures, free everything allocated so far and return NULL.
*/
static char **
getArgs( JNIEnv * env, jobjectArray jArgs )
{
	char **args;
//	char ** scan;
	char * each;
	jsize nargs, i, lenStr, lenUTF;
	jobject jArg;

	// Allocate a zeroed (NULL-filled) block to serve as args array.
	// It's one larger to hold the terminating NULL pointer.
	nargs = (*env)->GetArrayLength( env, jArgs );
	args = (char **) calloc( nargs + 1, sizeof( char * ) );
	if ( args == NULL )
		return ( NULL );

	for ( i = 0;  i < nargs;  ++i )
	{
		// The GetObjectArrayElement() must have a matching DeleteLocalRef(),
		// otherwise we'll consume too many local-refs in the loop.
		// On failure, we DO NOT have to clean up the local-ref,
		// since returning from the JNI function will accomplish that.
		jArg = (*env)->GetObjectArrayElement( env, jArgs, i );

		// I don't know if GetStringUTFLength() counts the terminating NUL-byte or not.
		// To be safe, allocate space for lenUTF+1 bytes.  If malloc() fails, abandon it all.
		lenUTF = (*env)->GetStringUTFLength( env, jArg );
		args[ i ] = each = (char *) malloc( lenUTF + 1 );
		if ( each == NULL )
		{
			freeArgs( args );
			return ( NULL );
		}

		// Copy entire String (i.e. lenStr Unicode chars) from jArg into buffer as UTF-8.
		lenStr = (*env)->GetStringLength( env, jArg );
		(*env)->GetStringUTFRegion( env, jArg, 0, lenStr, each );

		// The malloc()'ed memory was not cleared, so store a NUL byte after the UTF-8 bytes.
		// If the JVM put its own NUL at end, we'll just be storing a NUL after that one,
		// which is redundant but safe.
		each[ lenUTF ] = 0;

		// Release the local-ref of jArg, in preparation for getting another one.
		(*env)->DeleteLocalRef( env, jArg );
	}

	return ( args );
}


/*
 * Class:     glguerin_authkit_imp_macosx_MacOSXAuthorization
 * Method:    rootExec
 * Signature: (JLjava/lang/String;[Ljava/lang/String;)I
 */

/**
		rootExec( long session, int options, String pathToTool, String[] toolArgs, int[] fdRef );
*/

JNIEXPORT jint JNICALL
Java_glguerin_authkit_imp_macosx_MacOSXAuthorization_rootExec(
	JNIEnv * env,  jclass ignored,
	jlong session, jint options,  jstring pathToTool, jobjectArray toolArgs,
	jintArray fdRef )
{
	const char * utfToolName;
	jboolean isCopy;
	char **args;
	FILE * commPipe = NULL;
	jint filedes[ 2 ];
	OSStatus result;

	if ( session == 0 )
		return ( BAD_AUTHREF );

	// Always get utfToolName as UTF8 nul-terminated bytes.
	utfToolName = (*env)->GetStringUTFChars( env, pathToTool, &isCopy );
	if ( utfToolName == NULL )
		return ( -1 );  // OutOfMemoryError will be thrown

	// Convert args to C form.
	args = getArgs( env, toolArgs );
	if ( args == NULL )
	{
		(*env)->ReleaseStringUTFChars( env, pathToTool, utfToolName );
		return ( -1 );  // OutOfMemoryError will be thrown
	}

	// Fork and exec the process...
	result = AuthorizationExecuteWithPrivileges(
			(AuthorizationRef) session, utfToolName, options, args, &commPipe );

	// On success or failure, release and deallocate everything.
	(*env)->ReleaseStringUTFChars( env, pathToTool, utfToolName );
	freeArgs( args );

	// Get the filedes from commPipe.
	// Could use dup() or fcntl(), but there's no point, since both filedes's will still refer to
	// the same pipe.  Returning the identical filedes twice is the least problematic way.
	if ( result == SUCCESS )
	{
		filedes[ 0 ] = filedes[ 1 ] = fileno( commPipe );
		(*env)->SetIntArrayRegion( env, fdRef, 0, 2, filedes );
	}

	return ( result );
}






/*
 * Class:     glguerin_authkit_imp_macosx_MacOSXAuthorization
 * Method:    externalize
 * Signature: (J[B)I
 */

/**
	 The session is from makeSession() or something similar.
		externalize( long session, byte[] buffer );

	 @return				in C: <CODE>xx</CODE>
*/

JNIEXPORT jint JNICALL
Java_glguerin_authkit_imp_macosx_MacOSXAuthorization_externalize(
	JNIEnv * env,  jclass ignored,
	jlong session, jbyteArray buffer )
{
	jsize bufLen;
	AuthorizationExternalForm externalized;
	OSStatus result;

	if ( session == 0 )
		return ( BAD_AUTHREF );

	// Supplied buffer may be be larger than needed, but never smaller.
	bufLen = (*env)->GetArrayLength( env, buffer );
	if ( bufLen < sizeof( externalized ) )
		return ( errAuthorizationInvalidPointer );

	result = AuthorizationMakeExternalForm( (AuthorizationRef) session, &externalized );

	// Copy externalized into target buffer.
	if ( result == SUCCESS )
		(*env)->SetByteArrayRegion( env, buffer, 0, sizeof( externalized ), (jbyte *) &externalized );

	// Obliterate contents of local buffer.  I hope this doesn't get optimized out.
	memset( &externalized, 0, sizeof( externalized ) );

	return ( result );
}



/*
 * Class:     glguerin_authkit_imp_macosx_MacOSXAuthorization
 * Method:    internalize
 * Signature: ([B[J)I
 */

/**
		internalize( byte[] buffer, long[] sessionRef );

	 @return				in C: <CODE>xx</CODE>
*/

JNIEXPORT jint JNICALL
Java_glguerin_authkit_imp_macosx_MacOSXAuthorization_internalize(
	JNIEnv * env,  jclass ignored,
	jbyteArray buffer, jlongArray sessionRef )
{
	jsize bufLen;
	AuthorizationExternalForm externalized;
	AuthorizationRef session;
	OSStatus result;

	// Supplied buffer must be exactly the size necessary
	bufLen = (*env)->GetArrayLength( env, buffer );
	if ( bufLen != sizeof( externalized ) )
		return ( errAuthorizationInvalidPointer );

	// Copy from the Java byte[] into my local storage.
	// This ensures I can obliterate it when I want.
	(*env)->GetByteArrayRegion( env, buffer, 0, sizeof(externalized), (jbyte *) &externalized );

	// Call Auth Services.
	result = AuthorizationCreateFromExternalForm( &externalized, &session );

	// Copy recovered AuthRef into sessionRef.
	if ( result == SUCCESS )
		(*env)->SetLongArrayRegion( env, sessionRef, 0, 1, (jlong *) &session );

	// Obliterate contents of local buffer.  I hope this doesn't get optimized out.
	memset( &externalized, 0, sizeof( externalized ) );

	return ( result );
}


/*
 * Class:     glguerin_authkit_imp_macosx_MacOSXAuthorization
 * Method:    internalizePriv
 * Signature: ([J)I
 */

/**
		internalizePriv( int options, long[] sessionRef );
*/

JNIEXPORT jint JNICALL
Java_glguerin_authkit_imp_macosx_MacOSXAuthorization_internalizePriv(
	JNIEnv * env,  jclass ignored,
	jint options, jlongArray sessionRef )
{
	AuthorizationRef session = NULL;
	OSStatus result;

	// Call Auth Services.
	result = AuthorizationCopyPrivilegedReference( &session, options );

	// Copy recovered AuthRef into sessionRef.
	if ( result == SUCCESS )
		(*env)->SetLongArrayRegion( env, sessionRef, 0, 1, (jlong *) &session );

	return ( result );
}
