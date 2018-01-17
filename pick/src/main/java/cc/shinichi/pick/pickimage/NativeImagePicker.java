package cc.shinichi.pick.pickimage;

import android.app.Activity;
import android.content.Intent;
import com.unity3d.player.UnityPlayer;

/**
 * Created by SherlockHolmes on 2018/1/16.10:27
 */

public class NativeImagePicker {

    private static String TAG = "NativeImagePicker";

    public static void pickImage(String type) {
        //在这里判断是打开本地相册还是直接照相
        if (type.equals("takePhoto")) {
            FromCamera();
        } else {
            FromLibrary();
        }
    }

    private static void FromLibrary() {
        Activity a = UnityPlayer.currentActivity;
        Intent intent = new Intent(a, NativeImagePickerActivity.class);
        intent.putExtra("fromCamera", false);
        a.startActivity(intent);
    }

    private static void FromCamera() {
        Activity a = UnityPlayer.currentActivity;
        Intent intent = new Intent(a, NativeImagePickerActivity.class);
        intent.putExtra("fromCamera", true);
        a.startActivity(intent);
    }
}
