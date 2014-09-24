package com.leo.appmaster.model;

import android.content.pm.PermissionInfo;

public class AppPermissionInfo {
	private PermissionInfo[] mPermissions;

	public PermissionInfo[] getPermissions() {
		return mPermissions;
	}

	public void setPermissions(PermissionInfo[] mPermissions) {
		this.mPermissions = mPermissions;
	}

}
