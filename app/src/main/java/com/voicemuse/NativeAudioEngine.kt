package com.voicemuse

object NativeAudioEngine {
    init { System.loadLibrary("voicemuse") }
    external fun nativeStart(outputDeviceId: Int): Boolean
    external fun nativeStop()
    external fun nativeGetStats(): String
}