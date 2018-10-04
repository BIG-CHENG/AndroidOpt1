package tw.game.ai.prof1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "Prof1.MainActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

    }



    public void onClickRun11(View v) {
        new ProfTest(this).utest4jni1();
    }

    public void onClickRun12(View v) {
        new ProfTest(this).utest4jni2();
    }

    public void onClickRun13(View v) {
        new ProfTest(this).utest4jni3();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

//    public native String passingDataToJni(double[] doubleLeftArray, double[] doubleRightArray, int intValue, String stringValue);


}
