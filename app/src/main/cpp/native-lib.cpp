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

#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <math.h>

extern "C" JNIEXPORT jstring

JNICALL
Java_tw_game_ai_prof1_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

inline int rgba2int1(uint8_t R, uint8_t G, uint8_t B, uint8_t A)
{
    return (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
}

//#define MASK_BYTE0 0x000000ff
//#define MASK_BYTE1 0x0000ff00
//#define MASK_BYTE2 0x00ff0000
//#define MASK_BYTE3 0xff000000
inline int rgba2int2(uint32_t* p0)
{
    uint8_t* p1 = (uint8_t*)p0;
//    return (*p1 & MASK_BYTE3) << 24 | (rgba & MASK_BYTE0) << 16 | (rgba & 0xff) << 8 | (rgba & 0xff);
    return (*(p1+3) & 0xff) << 24 | (*(p1) & 0xff) << 16 | (*(p1+1) & 0xff) << 8 | (*(p1+2) & 0xff);
}

const int n_x = 160;
const int n_y = 160;

extern "C" JNIEXPORT jint

JNICALL
Java_tw_game_ai_prof1_ProfTest_chkBmpJNI(
        JNIEnv *env,
        jobject obj,
        jobject bitmap,
        jintArray ia
    ) {
    AndroidBitmapInfo info;
    int ret;
    void* pixels;
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) <0) {
        return -1;
    }
    if (info.format!= ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return -2;
    }
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) <0) {
        // ...
    }
//    convolute(&info, pixels);
    //ret = (int)(((uint32_t *)pixels)[0]);   // wrong !!!

    uint8_t* u8a = (uint8_t*)pixels;
    ret = rgba2int1(u8a[0], u8a[1], u8a[2], u8a[3]);
// ok !

//    jclass cls = env->GetObjectClass(obj);
//    jfieldID fid = env->GetFieldID(cls, "data","Ljava/nio/ByteBuffer;");
//    jobject bar = env->GetObjectField(obj, fid);
//    uint32_t* results = (uint32_t*)env->GetDirectBufferAddress(bar);

    jint* results = env->GetIntArrayElements(ia, NULL);


    uint32_t* rgba = (uint32_t*)pixels;
//    float tmp;
    for (int i=0; i<n_x; i++) {
        for (int j=0; j<n_y; j++) {
            //tmp = (float)rgba2int(rgba[i*n_y+j], rgba[i*n_y+j+1], rgba[i*n_y+j+2], rgba[i*n_y+j+3]);
            results[i*n_y+j] = rgba2int2(rgba+i*n_y+j);
        }
    }

    env->ReleaseIntArrayElements(ia, results, 0);

    AndroidBitmap_unlockPixels(env, bitmap );

    return ret;
}


void prewhiten(float* fa, int n) {

    //int n = fa.length;

    float sum = 0;
    for (int i = 0; i < n; i++) {
        sum += fa[i];
    }
    float mean = sum / n;

    sum = 0;
    for (int i = 0; i < n; i++) {
        float d = (fa[i] - mean);
        sum += d * d;
    }
    double std = sqrt(sum / n);

    //std adj
    double std_adj = fmax(std, 1.0 / sqrt(n));

    int imageMean = (int) mean;
    float imageStd = (float) std_adj;

    // modify
    for (int i = 0; i < n; ++i) {
        fa[i] = (fa[i] - imageMean) / imageStd;
    }
}

extern "C" JNIEXPORT jint

JNICALL
Java_tw_game_ai_prof1_ProfTest_bmp2faJNI(
        JNIEnv *env,
        jobject self,
        jobject bitmap,
        jfloatArray fa
        ) {
    AndroidBitmapInfo info;
    int ret;
    void* pixels;
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) <0) {
        return -1;
    }
    if (info.format!= ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return -2;
    }
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) <0) {
        // ...
    }
    //ret = (int)(((uint32_t *)pixels)[0]);   // wrong !!!

    uint8_t* u8a = (uint8_t*)pixels;
    ret = rgba2int1(u8a[0], u8a[1], u8a[2], u8a[3]);
    // ok !

    jfloat* results = env->GetFloatArrayElements(fa, NULL);

    uint32_t* rgba = (uint32_t*)pixels;
//    float tmp;
    int pos = 0;
    for (int i=0; i<n_x; i++) {
        for (int j=0; j<n_y; j++) {
            uint32_t val = rgba[pos];
            // c is little end-ian
//            results[pos * 3 + 0] = (float) ((val >> 16) & 0xFF);
//            results[pos * 3 + 1] = (float) ((val >> 8) & 0xFF);
//            results[pos * 3 + 2] = (float) (val & 0xFF);
            results[pos * 3 + 0] = (float) (val & 0xFF);
            results[pos * 3 + 1] = (float) ((val >> 8) & 0xFF);
            results[pos * 3 + 2] = (float) ((val >> 16) & 0xFF);
            //SetFloatArrayRegion
            pos++;
        }
    }

    prewhiten(results, n_x*n_y*3);

    env->ReleaseFloatArrayElements(fa, results, 0);

    AndroidBitmap_unlockPixels(env, bitmap );

    return ret;
}

