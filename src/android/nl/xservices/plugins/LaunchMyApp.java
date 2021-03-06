package nl.xservices.plugins;

import android.content.Intent;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import android.util.Log;

public class LaunchMyApp extends CordovaPlugin {

	private static final String TAG = "LaunchMyApp";
	
  private static final String ACTION_CHECKINTENT = "checkIntent";

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		Log.e(TAG, "action=" + action);
    if (ACTION_CHECKINTENT.equalsIgnoreCase(action)) {
			Log.e(TAG, "if1=true");
      final Intent intent = ((CordovaActivity) this.webView.getContext()).getIntent();
      final String intentString = intent.getDataString();
			Log.e(TAG, "intentString=" + intentString);
      if (intentString != null && intentString.contains("://") &&
          intent.getScheme() != null && intent.hasCategory(Intent.CATEGORY_BROWSABLE)) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, intent.getDataString()));
        intent.setData(null);
				Log.e(TAG, "if2=true");
        return true;
      } else {
        callbackContext.error("App was not started via the launchmyapp URL scheme. Ignoring this errorcallback is the best approach.");
				Log.e(TAG, "if2=false");
        return false;
      }
    } else {
			Log.e(TAG, "if1=false");
      callbackContext.error("This plugin only responds to the " + ACTION_CHECKINTENT + " action.");
      return false;
    }
  }

  @Override
  public void onNewIntent(Intent intent) {
    final String intentString = intent.getDataString();
    if (intentString != null && intentString.contains("://") &&
        intent.getScheme() != null && intent.hasCategory(Intent.CATEGORY_BROWSABLE)) {
			Log.e(TAG, "onNewIntent if1=true");
      intent.setData(null);
      try {
        StringWriter writer = new StringWriter(intentString.length() * 2);
        escapeJavaStyleString(writer, intentString, true, false);
				String s = writer.toString();
				Log.e(TAG, "onNewIntent writer.toString = "+s);
        webView.loadUrl("javascript:handleOpenURL('" + s + "');");
      } catch (IOException ignore) {
      }
    }
  }

  // Taken from commons StringEscapeUtils
  private static void escapeJavaStyleString(Writer out, String str, boolean escapeSingleQuote,
                                            boolean escapeForwardSlash) throws IOException {
    if (out == null) {
      throw new IllegalArgumentException("The Writer must not be null");
    }
    if (str == null) {
      return;
    }
    int sz;
    sz = str.length();
    for (int i = 0; i < sz; i++) {
      char ch = str.charAt(i);

      // handle unicode
      if (ch > 0xfff) {
        out.write("\\u" + hex(ch));
      } else if (ch > 0xff) {
        out.write("\\u0" + hex(ch));
      } else if (ch > 0x7f) {
        out.write("\\u00" + hex(ch));
      } else if (ch < 32) {
        switch (ch) {
          case '\b':
            out.write('\\');
            out.write('b');
            break;
          case '\n':
            out.write('\\');
            out.write('n');
            break;
          case '\t':
            out.write('\\');
            out.write('t');
            break;
          case '\f':
            out.write('\\');
            out.write('f');
            break;
          case '\r':
            out.write('\\');
            out.write('r');
            break;
          default:
            if (ch > 0xf) {
              out.write("\\u00" + hex(ch));
            } else {
              out.write("\\u000" + hex(ch));
            }
            break;
        }
      } else {
        switch (ch) {
          case '\'':
            if (escapeSingleQuote) {
              out.write('\\');
            }
            out.write('\'');
            break;
          case '"':
            out.write('\\');
            out.write('"');
            break;
          case '\\':
            out.write('\\');
            out.write('\\');
            break;
          case '/':
            if (escapeForwardSlash) {
              out.write('\\');
            }
            out.write('/');
            break;
          default:
            out.write(ch);
            break;
        }
      }
    }
  }

  private static String hex(char ch) {
    return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
  }
}