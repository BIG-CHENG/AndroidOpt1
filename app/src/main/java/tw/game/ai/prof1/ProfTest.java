// Copyright 2018 BIG CHENG (bigcheng.asus@gmail.com). All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ==============================================================================

// BIG CHENG, 2018/08/01, init, jni for optimization

package tw.game.ai.prof1;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import junit.framework.Assert;

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

    // basic connection test
    public void utest4jni1() {

        long t1 = System.currentTimeMillis();

        String msg = mainActivity.stringFromJNI();

        long t2 = System.currentTimeMillis();
        Log.d(TAG, String.format("time = %d, msg = %s ", t2-t1, msg));

    }

    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;    // float 32 for each color

    static final int DIM_IMG_SIZE_X = 160;
    static final int DIM_IMG_SIZE_Y = 160;

    private static final int FLOAT32_SIZE = 4;    // float 32 for each color
    public static final String IMG_FILE1 = "Nicole_Kidman_0001.png";

//    public native int chkBmpJNI(Bitmap bitmap, ByteBuffer data);
    public native int chkBmpJNI(Bitmap bitmap, int[] result);
    public native int bmp2faJNI(Bitmap bitmap, float[] result);

    // mirror test (no image modification)
    public void utest4jni2() {

        long t1 = System.currentTimeMillis();

        Bitmap bitmap = asset2bmp(IMG_FILE1);
        int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

//        ByteBuffer imgData = ByteBuffer.allocateDirect(DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE * 4);   // float

//        int ret = chkBmpJNI(bitmap, imgData);
        //int[] results = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE *4];
        int[] results = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y ];
        int ret = chkBmpJNI(bitmap, results);

        long t2 = System.currentTimeMillis();
        Log.d(TAG, String.format("time = %d, ret = %d ", t2-t1, ret));

        // assert
        for (int i=0; i<DIM_IMG_SIZE_X; i++) {
            for (int j=0; j<DIM_IMG_SIZE_Y; j++) {
                Assert.assertEquals(results[i*DIM_IMG_SIZE_Y+j], intValues[i*DIM_IMG_SIZE_Y+j]);
            }
        }
    }

    // image modification test
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


    void prewhiten(float[] iv) {
        assert (iv != null);
        assert (iv.length > 0);

        int n = iv.length;

        float sum = 0;
        for (int i = 0; i < n; i++) {
            sum += iv[i];
        }
        float mean = sum / n;

        sum = 0;
        for (int i = 0; i < n; i++) {
            float d = (iv[i] - mean);
            sum += d * d;
        }
        double std = Math.sqrt(sum / n);

        //std adj
        double std_adj = Math.max(std, 1.0 / Math.sqrt(n));

        int imageMean = (int) mean;
        float imageStd = (float) std_adj;

        for (int i = 0; i < n; ++i) {
            iv[i] = (iv[i] - imageMean) / imageStd;
        }

        //return iv;
    }

    private void bmp2faJava(int [] intValues, float [] results) {

        // Convert the image to floating point.
        int pixel = 0;
        long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel];
                results[pixel * 3 + 0] = (float) ((val >> 16) & 0xFF);
                results[pixel * 3 + 1] = (float) ((val >> 8) & 0xFF);
                results[pixel * 3 + 2] = (float) (val & 0xFF);
                pixel++;
            }
        }

        prewhiten(results);
    }

    // image modification test via java
    public void utest4jni4() {

        long t1 = System.currentTimeMillis();

        Bitmap bitmap = asset2bmp(IMG_FILE1);
        int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        float[] results = new float[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE];

        bmp2faJava(intValues, results);

        long t2 = System.currentTimeMillis();
        Log.d(TAG, String.format("time = %d", t2-t1));
    }


}