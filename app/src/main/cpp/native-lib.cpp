#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_mso_android_objectdetectexample_MainActivity_objectDetectPoints(
        JNIEnv *env,
        jclass javaClass,
        jbyteArray image,
        jint width,
        jint height) {


    std::string hello = "Hello from C++";

    jintArray result = (env)->NewIntArray(8);

    jint fill[8];



    fill[0] = 130;
    fill[1] = 131;
    fill[2] = 132;
    fill[3] = 133;
    fill[4] = 135;
    fill[5] = 136;
    fill[6] = 137;
    fill[7] = 138;

    (env)->SetIntArrayRegion(result, 0, 8, fill);

    return result;

}
