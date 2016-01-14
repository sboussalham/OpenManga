package org.nv95.openmanga.utils;

import android.content.Context;
import android.content.res.Resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by nv95 on 18.12.15.
 */
public class AppHelper {

  public static String getRawString(Context context, int res) {
    try {
      Resources resources = context.getResources();
      InputStream is = resources.openRawResource(res);
      String myText;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int i = is.read();
      while (i != -1) {
        baos.write(i);
        i = is.read();
      }
      myText = baos.toString();
      is.close();
      return myText;
    } catch (IOException e) {
      return e.getMessage();
    }
  }

  public static String getReadableDateTime(long milliseconds) {
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(milliseconds);
    return formatter.format(calendar.getTime());
  }
}
