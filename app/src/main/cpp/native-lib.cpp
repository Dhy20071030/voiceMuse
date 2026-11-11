#include "audio_engine.h"
#include <jni.h>
#include <memory>
#include <mutex>
namespace { std::mutex gMutex; std::unique_ptr<AudioEngine> gEngine; }
extern "C" JNIEXPORT jboolean JNICALL Java_com_voicemuse_NativeAudioEngine_nativeStart(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gEngine && gEngine->isRunning()) return JNI_TRUE;
    auto engine = std::make_unique<AudioEngine>();
    if (!engine->start()) return JNI_FALSE;
    gEngine = std::move(engine);
    return JNI_TRUE;
}
extern "C" JNIEXPORT void JNICALL Java_com_voicemuse_NativeAudioEngine_nativeStop(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gEngine) { gEngine->stop(); gEngine.reset(); }
}
