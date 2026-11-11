#include "audio_engine.h"
#include <jni.h>
#include <memory>
#include <mutex>
#include <string>

namespace { std::mutex gMutex; std::unique_ptr<AudioEngine> gEngine; }

extern "C" JNIEXPORT jboolean JNICALL
Java_com_voicemuse_NativeAudioEngine_nativeStart(JNIEnv*, jobject, jint outputDeviceId) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gEngine && gEngine->isRunning()) return JNI_TRUE;
    auto engine = std::make_unique<AudioEngine>();
    if (!engine->start(outputDeviceId)) return JNI_FALSE;
    gEngine = std::move(engine);
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_voicemuse_NativeAudioEngine_nativeStop(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gEngine) { gEngine->stop(); gEngine.reset(); }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_voicemuse_NativeAudioEngine_nativeGetStats(JNIEnv* env, jobject) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (!gEngine) return env->NewStringUTF("stopped");
    const std::string value = std::string(gEngine->isRunning() ? "running" : "error") +
        ";rate=" + std::to_string(gEngine->sampleRate()) +
        ";device=" + std::to_string(gEngine->outputDeviceId()) +
        ";underruns=" + std::to_string(gEngine->underruns()) +
        ";overruns=" + std::to_string(gEngine->overruns());
    return env->NewStringUTF(value.c_str());
}