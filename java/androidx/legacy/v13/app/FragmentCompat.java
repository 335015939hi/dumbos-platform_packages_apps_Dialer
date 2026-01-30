package androidx.legacy.v13.app;

import android.app.Activity;
import android.app.Fragment;
import androidx.core.app.ActivityCompat;

// TODO: Stub compatibility class - androidx.legacy:legacy-support-v13 library not working properly
// This provides basic functionality to allow compilation. Consider migrating to androidx.fragment.app.Fragment.
public class FragmentCompat {

    public static void requestPermissions(Fragment fragment, String[] permissions, int requestCode) {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    public static boolean shouldShowRequestPermissionRationale(Fragment fragment, String permission) {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
        }
        return false;
    }

    public interface OnRequestPermissionsResultCallback {
        void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
    }
}
