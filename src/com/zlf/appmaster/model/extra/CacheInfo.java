package com.zlf.appmaster.model.extra;

public class CacheInfo {
	// app size
	public long codeSize;
	// data size
	public long dataSize;
	// cache size
	public long cacheSize;
	// taotal
	public long total;

	@Override
	public String toString() {
		return "codeSize = " + codeSize + "       dataSize = " + dataSize
				+ "        cacheSize = " + cacheSize + "      total = " + total;
	}

}
