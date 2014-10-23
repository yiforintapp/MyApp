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

JNIEXPORT void JNICALL Java_com_leo_appmaster_AppMasterApplication_restartApplocker(
		JNIEnv * env, jclass cls, jint sdk_version) {
	pid_t pid = fork();
	if (pid < 0) {

	} else if (pid == 0) {
		pid_t ppid = getppid();
		while (1) {
			sleep(3);

			if(getppid() != ppid) {
				if (sdk_version >= 17) {
					execlp("am", "am", "startservice", "--user", "0", "-n",
							"com.leo.appmaster/com.leo.appmaster.applocker.service.LockService", (char *) NULL);
				} else {
					execlp("am", "am", "startservice", "-n",
							"com.leo.appmaster/com.leo.appmaster.applocker.service.LockService", (char *) NULL);
				}
				return ;
			}
		}
	} else {
	}
}
