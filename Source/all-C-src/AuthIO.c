/*
** AuthIO.c -- JNI C code
**
** Copyright 2002-2003 by Gregory L. Guerin.
** All rights reserved.
**
** 27Nov2002 GLG  create
** 24Jun2003 GLG  improve error-return values
*/

#include "glguerin_authkit_imp_macosx_AuthProcess.h"

#include <errno.h>
#include <unistd.h>



/* # # # # # # # # # # # # # # # # # # # # # # # # # # # # */



/*
 * Class:     glguerin_authkit_imp_macosx_AuthProcess
 * Method:    read
 * Signature: (I[BII)I
 */

/**
** Read bytes, returning count actually read,
** or 0 on EOF, or -errno on error.
**<p>
** The specific reasons for failure may be distinguished by decoding negative numbers
** as errno negatives, or all failures can be lumped together into one IOException flavor.
*/
JNIEXPORT jint JNICALL
Java_glguerin_authkit_imp_macosx_AuthProcess_read (
	JNIEnv * env,  jclass ignored,
	jint inFD, jbyteArray jBytes, jint offset, jint count )
{
	jbyte * bufPtr;
	ssize_t result;

	// It's silly that the GetByteArrayElements() might make a copy, but I know of no
	// other way to get the bytes back into the jBytes array, short of making another buffer.
	// And if another buffer is going to be made, it may as well be the JVM that does it.
	bufPtr = (*env)->GetByteArrayElements( env, jBytes, NULL );
	if ( bufPtr == NULL )
		return ( -EINVAL );

	// Perform the read, at the indicated offset.
	errno = 0;
	result = read( inFD, bufPtr + offset, count );

	// Always have to release array elements, so do it with same mode = 0 no matter what.
	(*env)->ReleaseByteArrayElements( env, jBytes, bufPtr, 0 );

	if ( result < 0 )
		return ( -errno );

	return ( result );
}



/*
 * Class:     glguerin_authkit_imp_macosx_AuthProcess
 * Method:    write
 * Signature: (I[BII)I
 */

/**
** Write bytes, returning count actually written,
** or -errno on error.
**<p>
** The specific reasons for failure may be distinguished by decoding negative numbers
** as errno negatives, or all failures can be lumped together into one IOException flavor.
*/

JNIEXPORT jint JNICALL
Java_glguerin_authkit_imp_macosx_AuthProcess_write (
	JNIEnv * env,  jclass ignored,
	jint outFD, jbyteArray jBytes, jint offset, jint count )
{
	jbyte * bufPtr;
	ssize_t result;

	// Even if GetByteArrayElements() might make a copy, we'll release it using JNI_ABORT,
	// to avoid write-back.
	bufPtr = (*env)->GetByteArrayElements( env, jBytes, NULL );
	if ( bufPtr == NULL )
		return ( -EINVAL );

	// Perform the write, at the indicated offset.
	errno = 0;
	result = write( outFD, bufPtr + offset, count );

	// Always have to release array elements, so do it with same mode no matter what.
	(*env)->ReleaseByteArrayElements( env, jBytes, bufPtr, JNI_ABORT );

	// Consider a partial write to be an error.
	if ( result != count )
		return ( -errno );

	return ( result );
}



/*
 * Class:     glguerin_authkit_imp_macosx_AuthProcess
 * Method:    close
 * Signature: (I)I
 */

/**
** Close the file-descriptor, returning 0 on success or errno on failure.
** Unlike read() and write(), the actual errno value, not its negative, is returned.
*/

JNIEXPORT jint JNICALL
Java_glguerin_authkit_imp_macosx_AuthProcess_close (
	JNIEnv * env,  jclass ignored,
	jint anFD )
{
	if ( anFD < 0 )
		return ( EINVAL );

	errno = 0;
	if ( close( anFD ) != 0 )
		return ( errno );

	return ( 0 );
}
