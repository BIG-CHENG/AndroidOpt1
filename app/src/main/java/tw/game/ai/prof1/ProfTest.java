package tw.game.ai.prof1;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ProfTest {
    static final String TAG = ProfTest.class.getSimpleName();

    MainActivity mainActivity;

    AssetManager assetManager;


    // the life time for ProfTest is typically only "one click"
    public ProfTest(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        this.assetManager = mainActivity.getAssets();
    }

    private Bitmap asset2bmp(String strName)
    {
        InputStream istr = null;
        try {
            istr = assetManager.open(strName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        //bitmap = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, false);
        Log.d(TAG, String.format("asset2bmp %s", strName) );
        return bitmap;
    }

    public void utest4jni1() {

        long t1 = System.currentTimeMillis();

        String msg = mainActivity.stringFromJNI();

        long t2 = System.currentTimeMillis();
        Log.d(TAG, String.format("time = %d, msg = %s ", t2-t1, msg));

    }

/*    public void utest4jni2() {

        long t1 = System.currentTimeMillis();

        double [] tmpArrayLeft = {1, 2, 3};
        double [] tmpArrayRight = {4, 5, 6};
        int tmpIntValue = 1;
//        float tmpFloatValue = 2.3f;
        String sReturn = "test ok";
        String msg = mainActivity.passingDataToJni(tmpArrayLeft, tmpArrayRight, tmpIntValue, sReturn);

        long t2 = System.currentTimeMillis();
        Log.d(TAG, String.format("time = %d, msg = %s ", t2-t1, msg));
    } */

    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;    // float 32 for each color

    static final int DIM_IMG_SIZE_X = 160;
    static final int DIM_IMG_SIZE_Y = 160;

    private static final int FLOAT32_SIZE = 4;    // float 32 for each color
    public static final String IMG_FILE1 = "Nicole_Kidman_0001.png";

//    public native int chkBmpJNI(Bitmap bitmap, ByteBuffer data);
    public native int chkBmpJNI(Bitmap bitmap, int[] result);
    public native int bmp2faJNI(Bitmap bitmap, float[] result);

    public void utest4jni2() {

        long t1 = System.currentTimeMillis();

        Bitmap bitmap = asset2bmp(IMG_FILE1);
        int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

//        ByteBuffer imgData = ByteBuffer.allocateDirect(DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE * 4);   // float

//        int ret = chkBmpJNI(bitmap, imgData);
        int[] results = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE *4];
        int ret = chkBmpJNI(bitmap, results);

        long t2 = System.currentTimeMillis();
        Log.d(TAG, String.format("time = %d, ret = %d ", t2-t1, ret));
    }

    public void utest4jni3() {

        long t1 = System.currentTimeMillis();

        Bitmap bitmap = asset2bmp(IMG_FILE1);
        int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        float[] results = new float[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE];
        int ret = bmp2faJNI(bitmap, results);

        long t2 = System.currentTimeMillis();
        Log.d(TAG, String.format("time = %d, ret = %d ", t2-t1, ret));
    }

}