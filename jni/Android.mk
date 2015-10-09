LOCAL_PATH 		:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE			:= leo_service

LOCAL_SRC_FILES = restart.c

LOCAL_CFLAGS 		+= -O2 -MD

LOCAL_LDLIBS 		+= -lz -llog

include $(BUILD_SHARED_LIBRARY)
