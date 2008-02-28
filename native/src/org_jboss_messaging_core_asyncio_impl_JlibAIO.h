/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_jboss_messaging_core_asyncio_impl_JlibAIO */

#ifndef _Included_org_jboss_messaging_core_asyncio_impl_JlibAIO
#define _Included_org_jboss_messaging_core_asyncio_impl_JlibAIO
#ifdef __cplusplus
extern "C" {
#endif
/* Inaccessible static: log */
/* Inaccessible static: loaded */
/*
 * Class:     org_jboss_messaging_core_asyncio_impl_JlibAIO
 * Method:    init
 * Signature: (Ljava/lang/String;Ljava/lang/Class;ILorg/jboss/messaging/core/logging/Logger;)J
 */
JNIEXPORT jlong JNICALL Java_org_jboss_messaging_core_asyncio_impl_JlibAIO_init
  (JNIEnv *, jclass, jstring, jclass, jint, jobject);

/*
 * Class:     org_jboss_messaging_core_asyncio_impl_JlibAIO
 * Method:    write
 * Signature: (JJJLjava/nio/ByteBuffer;Lorg/jboss/messaging/core/asyncio/AIOCallback;)V
 */
JNIEXPORT void JNICALL Java_org_jboss_messaging_core_asyncio_impl_JlibAIO_write
  (JNIEnv *, jclass, jlong, jlong, jlong, jobject, jobject);

/*
 * Class:     org_jboss_messaging_core_asyncio_impl_JlibAIO
 * Method:    read
 * Signature: (JJJLjava/nio/ByteBuffer;Lorg/jboss/messaging/core/asyncio/AIOCallback;)V
 */
JNIEXPORT void JNICALL Java_org_jboss_messaging_core_asyncio_impl_JlibAIO_read
  (JNIEnv *, jclass, jlong, jlong, jlong, jobject, jobject);

/*
 * Class:     org_jboss_messaging_core_asyncio_impl_JlibAIO
 * Method:    preAllocate
 * Signature: (JIJ)V
 */
JNIEXPORT void JNICALL Java_org_jboss_messaging_core_asyncio_impl_JlibAIO_preAllocate
  (JNIEnv *, jclass, jlong, jint, jlong);

/*
 * Class:     org_jboss_messaging_core_asyncio_impl_JlibAIO
 * Method:    closeInternal
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_jboss_messaging_core_asyncio_impl_JlibAIO_closeInternal
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_jboss_messaging_core_asyncio_impl_JlibAIO
 * Method:    internalPollEvents
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_jboss_messaging_core_asyncio_impl_JlibAIO_internalPollEvents
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_jboss_messaging_core_asyncio_impl_JlibAIO
 * Method:    destroyBuffer
 * Signature: (Ljava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_org_jboss_messaging_core_asyncio_impl_JlibAIO_destroyBuffer
  (JNIEnv *, jobject, jobject);

/*
 * Class:     org_jboss_messaging_core_asyncio_impl_JlibAIO
 * Method:    newBuffer
 * Signature: (J)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_org_jboss_messaging_core_asyncio_impl_JlibAIO_newBuffer
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif