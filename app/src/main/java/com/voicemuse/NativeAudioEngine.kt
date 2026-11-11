package com.voicemuse

object NativeAudioEngine {
    init { System.loadLibrary("voicemuse") }
    external fun nativeStart(): Boolean
    external fun nativeStop()
}
