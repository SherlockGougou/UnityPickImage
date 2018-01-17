package cc.shinichi.pickimage;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Intent intent = new Intent(this, NativeImagePickerActivity.class);
        //intent.putExtra("fromCamera", false);
        //startActivity(intent);
    }
}
