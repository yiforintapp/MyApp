#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include <sys/ptrace.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>

#include <sys/inotify.h>
#include <sys/ioctl.h>
#include <errno.h>

static jboolean isCopy = JNI_TRUE;
static const char *s_key = "825D4A3EC7084A2141C3D5C5A86AEEA9";
static const char *s_iv = "2A9B3662D6E99ED072FBB875DD382213";

JNIEXPORT void JNICALL Java_com_leo_appmaster_AppMasterApplication_restartApplocker(
		JNIEnv * env, jclass cls, jint sdk_version, jstring userSerial) {
	pid_t pid = fork();
	if (pid < 0) {

	} else if (pid == 0) {
		pid_t ppid = getppid();
		while (1) {
			sleep(5);

			if(getppid() != ppid) {
				if (sdk_version >= 17) {
				    if(userSerial == NULL) {
						execlp("am", "am", "startservice", "--user", "0", "-n",
								"com.leo.appmaster/com.leo.appmaster.applocker.service.TaskDetectService", (char *) NULL);
					} else {
						execlp("am", "am", "startservice", "--user", (*env)->GetStringUTFChars(env, userSerial, &isCopy), "-n",
														"com.leo.appmaster/com.leo.appmaster.applocker.service.TaskDetectService", (char *) NULL);
					}
				} else {
					execlp("am", "am", "startservice", "-n",
							"com.leo.appmaster/com.leo.appmaster.applocker.service.TaskDetectService", (char *) NULL);
				}
				return ;
			}
		}
	} else {
	}
}

JNIEXPORT jobjectArray JNICALL Java_com_leo_appmaster_AppMasterApplication_getKeyArray(
		JNIEnv *env, jobject obj) {
    jclass clazz = (*env)->FindClass(env, "java/lang/String");
    jstring init_str = (*env)->NewStringUTF(env, "");
    jobjectArray array = (*env)->NewObjectArray(env, 2, clazz, init_str);

    jstring key_str = (*env)->NewStringUTF(env, s_key);
    jstring iv_str = (*env)->NewStringUTF(env, s_iv);
    (*env)->SetObjectArrayElement(env, array, 0, key_str);
    (*env)->SetObjectArrayElement(env, array, 1, iv_str);

    return array;
}
