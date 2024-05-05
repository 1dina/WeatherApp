import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyBootReceiver : BroadcastReceiver() {

    @SuppressLint("ServiceCast")
    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent != null) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                // Perform tasks to run after the device has booted
                Log.i("MyBootReceiver", "Device booted. Performing startup tasks...");
            }


        }
    }
}
