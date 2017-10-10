package cs3205.subsystem3.health.common.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import cs3205.subsystem3.health.MainActivity;
import cs3205.subsystem3.health.common.miscellaneous.Value;

/**
 * Created by danwen on 10/10/17.
 */

public class LogoutHelper {

    public static void logout(Context context) {
        //clean up
        SharedPreferences sharedpreferences = context.getSharedPreferences
                (Value.KEY_VALUE_SHARED_PREFERENCE_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();

        //logout
        Intent logoutFromMainIntent = new Intent(context, MainActivity.class);
        logoutFromMainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(logoutFromMainIntent);
    }
}
