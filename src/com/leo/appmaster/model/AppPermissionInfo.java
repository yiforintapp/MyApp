package com.leo.appmaster.model;

import android.content.pm.PermissionInfo;

public class AppPermissionInfo {
	private PermissionInfo[] mPermissions;
	private String sharedPkgList[];
	
	public PermissionInfo[] getPermissions() {
		return mPermissions;
	}

	public void setPermissions(PermissionInfo[] mPermissions) {
		this.mPermissions = mPermissions;
	}

    public String[] getPermissionList() {
        return sharedPkgList;
    }
	
	public void setPermissionList(String [] list) {
	    sharedPkgList = list;
	}
	
}
