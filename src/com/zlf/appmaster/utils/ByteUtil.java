package com.zlf.appmaster.utils;
public class ByteUtil {

	public static byte[] float2byte(float f) {
		byte[] res = new byte[4];
		int l = Float.floatToIntBits(f);
		for (int i = 3; i >= 0; i--) {
			res[i] = new Integer(l).byteValue();
			l >>= 8;
		}
		return res;
	}

	public static byte[] int2byte(int i) {
		byte[] b = new byte[4];

		b[3] = (byte) (0xff & i);
		b[2] = (byte) ((0xff00 & i) >> 8);
		b[1] = (byte) ((0xff0000 & i) >> 16);
		b[0] = (byte) ((0xff000000 & i) >> 24);
		return b;
	}

	public static byte[] long2byte(long l) {
		byte[] res = new byte[8];
		res[7] = (byte) l;
		res[6] = (byte) (l >>> 8);
		res[5] = (byte) (l >>> 16);
		res[4] = (byte) (l >>> 24);
		res[3] = (byte) (l >>> 32);
		res[2] = (byte) (l >>> 40);
		res[1] = (byte) (l >>> 48);
		res[0] = (byte) (l >>> 56);
		return res;
	}

	public static int byte2int(byte[] bytes) {
		return (((bytes[0] << 24) >>> 24) << 24)
				| (((bytes[1] << 24) >>> 24) << 16)
				| (((bytes[2] << 24) >>> 24) << 8) | ((bytes[3] << 24) >>> 24);
	}

	public static long byte2long(byte[] bytes) {
		long l0 = bytes[0];
		long l1 = bytes[1];
		long l2 = bytes[2];
		long l3 = bytes[3];
		long l4 = bytes[4];
		long l5 = bytes[5];
		long l6 = bytes[6];
		long l7 = bytes[7];
		long res = (l0 << 56) | (((l1 << 56) >>> 56) << 48)
				| (((l2 << 56) >>> 56) << 40) | (((l3 << 56) >>> 56) << 32)
				| (((l4 << 56) >>> 56) << 24) | (((l5 << 56) >>> 56) << 16)
				| (((l6 << 56) >>> 56) << 8) | ((l7 << 56) >>> 56);
		return res;
	}

	public static float byte2float(byte[] bytes) {
		int l = byte2int(bytes);
		float res = Float.intBitsToFloat(l);
		return res;
	}

	public static byte[] double2byte(double d) {
		long l = Double.doubleToLongBits(d);
		byte[] res = long2byte(l);
		return res;
	}

	/**
	 * 8字节的二进制数转换为双浮点数
	 * 
	 * @param bytes
	 *            8字节的二进制数
	 * @return 双浮点数double
	 */
	public static double byte2double(byte[] bytes) {
		long l = byte2long(bytes);
		double res = Double.longBitsToDouble(l);
		return res;
	}
}
