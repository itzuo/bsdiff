#include <jni.h>
#include <string>

extern "C"{
    //引入bspatch.c里的main方法
    extern int main(int argc,char * argv[]);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_zuo_bsdiff_MainActivity_bspatch(JNIEnv *env, jobject instance, jstring oldapk_,
                                                 jstring patch_, jstring output_) {
    const char *oldapk = env->GetStringUTFChars(oldapk_, 0);
    const char *patch = env->GetStringUTFChars(patch_, 0);
    const char *output = env->GetStringUTFChars(output_, 0);


    int argc = 4;
    char *argv[4] ={"", const_cast<char *>(oldapk),const_cast<char *>(output),const_cast<char *>(patch)};
    main(argc,argv);

    env->ReleaseStringUTFChars(oldapk_, oldapk);
    env->ReleaseStringUTFChars(patch_, patch);
    env->ReleaseStringUTFChars(output_, output);
}