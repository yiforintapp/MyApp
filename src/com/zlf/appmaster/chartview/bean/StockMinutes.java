package com.zlf.appmaster.chartview.bean;

import com.zlf.appmaster.utils.QLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

public class StockMinutes extends StockVolume
  implements Serializable
{
  public static final String TAG = "StockMinutes";
  private float a;
  private float b;
  private long c;
  private int d;
  private long e;
  private long f;

  public float getNowPrice()
  {
    return this.a;
  }

  public void setNowPrice(float paramFloat)
  {
    this.a = paramFloat;
  }

  public float getYestodayPrice()
  {
    return this.b;
  }

  public void setYestodayPrice(float paramFloat)
  {
    this.b = paramFloat;
  }

  public long getDataTime()
  {
    return this.c;
  }

  public void setDataTime(long paramLong)
  {
    this.c = paramLong;
  }

  public int getStockStatus()
  {
    return this.d;
  }

  public void setStockStatus(int paramInt)
  {
    this.d = paramInt;
  }

  public float getMaValue()
  {
    if (this.f != 0L)
      return (float)this.e / (float)this.f;
    return 0.0F;
  }

  public StockMinutes(float paramFloat1, float paramFloat2, long paramLong1, long paramLong2, int paramInt, long paramLong3)
  {
    this.a = paramFloat2;
    setTradeCount(paramLong1);
    this.b = paramFloat1;
    this.c = paramLong3;
    this.d = paramInt;
    this.e = paramLong2;
    this.f = paramLong1;
  }

  public static StockMinutes resolveMinuteJsonObject(JSONObject paramJSONObject, boolean paramBoolean)
  {
    if (paramBoolean)
    {
      float f1 = (float)paramJSONObject.optDouble("HQZJCJ", 0.0D);
      long l1 = paramJSONObject.optLong("HQCJSL");
      int i = paramJSONObject.optInt("stockStatus", 0);
      float f3 = (float)paramJSONObject.optDouble("HQZRSP", 0.0D);
      long l4 = (long)paramJSONObject.optDouble("time", 0.0D);
      long l6 = paramJSONObject.optLong("HQCJJE");
      return new StockMinutes(f3, f1, l1, l6, i, l4);
    }
    float f1 = (float)paramJSONObject.optDouble("ZSZJZS", 0.0D);
    float f2 = (float)paramJSONObject.optDouble("ZSSSZS", 0.0D);
    long l2 = paramJSONObject.optLong("ZSCJSL") * 100L;
    long l3 = (long)paramJSONObject.optDouble("time", 0.0D);
    long l5 = paramJSONObject.optLong("ZSCJJE");
    return new StockMinutes(f2, f1, l2, l5, 0, l3);
  }

  public static ArrayList<StockMinutes> resloveMinutesData(Object paramObject, boolean paramBoolean, long paramLong)
  {
    JSONObject localJSONObject = (JSONObject)paramObject;
    ArrayList localArrayList = new ArrayList();
    float f1 = 0.0F;
    try
    {
      JSONArray localJSONArray = localJSONObject.optJSONArray("data");
      if ((localJSONArray == null) || (localJSONArray.length() == 0))
        return null;
      StockMinutes localStockMinutes1 = resolveMinuteJsonObject(localJSONArray.getJSONObject(0), paramBoolean);
      f1 = localStockMinutes1.getYestodayPrice();
      float f3 = f1;
      int i;
      if (localJSONArray.length() > 242)
        i = 242;
      else
        i = localJSONArray.length();
      long l = 0L;
      for (int j = 0; j < i; j++)
      {
        StockMinutes localStockMinutes2 = resolveMinuteJsonObject(localJSONArray.getJSONObject(j), paramBoolean);
        if (localStockMinutes2.getDataTime() > paramLong)
        {
          QLog.e("StockMinutes", "大于了最大的时间：" + localStockMinutes2.getDataTime());
        }
        else
        {
          float f2 = localStockMinutes2.getNowPrice();
          localArrayList.add(localStockMinutes2);
          localStockMinutes2.setTradeCount(localStockMinutes2.getTradeCount() - l);
          l += localStockMinutes2.getTradeCount();
          if (f2 >= f3)
            localStockMinutes2.setUp(true);
          else
            localStockMinutes2.setUp(false);
          f3 = f2;
        }
      }
    }
    catch (JSONException localJSONException)
    {
      localJSONException.printStackTrace();
    }
    return localArrayList;
  }

  public static byte[] toByteArray(ArrayList<StockMinutes> paramArrayList)
  {
    if ((paramArrayList == null) || (paramArrayList.size() == 0))
      return null;
    try
    {
      ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream localObjectOutputStream = new ObjectOutputStream(localByteArrayOutputStream);
      localObjectOutputStream.writeObject(paramArrayList);
      localObjectOutputStream.flush();
      byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
      localByteArrayOutputStream.close();
      localObjectOutputStream.close();
      return arrayOfByte;
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    return null;
  }

  public static ArrayList<StockMinutes> resloveByteArray(byte[] paramArrayOfByte)
  {
    if ((paramArrayOfByte == null) || (paramArrayOfByte.length == 0))
      return null;
    try
    {
      ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(paramArrayOfByte);
      ObjectInputStream localObjectInputStream = new ObjectInputStream(localByteArrayInputStream);
      ArrayList localArrayList = (ArrayList)localObjectInputStream.readObject();
      localByteArrayInputStream.close();
      localObjectInputStream.close();
      return localArrayList;
    }
    catch (StreamCorruptedException localStreamCorruptedException)
    {
      localStreamCorruptedException.printStackTrace();
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      localClassNotFoundException.printStackTrace();
    }
    return null;
  }

  public String toString()
  {
    return "StockMinutes{mNowPrice=" + this.a + ", mYestodayPrice=" + this.b + ", mDataTime=" + this.c + ", mStockStatus=" + this.d + ", mTotalTradeMoney=" + this.e + ", mTotalTradeCount=" + this.f + '}';
  }
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.bean.StockMinutes
 * JD-Core Version:    0.6.2
 */