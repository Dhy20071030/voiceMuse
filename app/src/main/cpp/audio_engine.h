#pragma once
#include <atomic>
#include <cstdint>
#include <memory>
#include <oboe/Oboe.h>
#include "dsp_processor.h"
#include <vector>

class AudioEngine final : public oboe::AudioStreamDataCallback,
                          public oboe::AudioStreamErrorCallback {
public:
    AudioEngine() = default;
    ~AudioEngine() override;
    bool start(int32_t outputDeviceId);
    void stop();
    bool isRunning() const noexcept { return running_.load(); }
    int32_t sampleRate() const noexcept { return sampleRate_.load(); }
    int32_t outputDeviceId() const noexcept { return outputDeviceId_.load(); }
    uint32_t underruns() const noexcept { return underruns_.load(); }
    uint32_t overruns() const noexcept { return overruns_.load(); }
    void onErrorAfterClose(oboe::AudioStream*, oboe::Result) override;
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream*, void*, int32_t) override;
private:
    std::shared_ptr<oboe::AudioStream> input_;
    std::shared_ptr<oboe::AudioStream> output_;
    std::atomic<bool> running_{false};
    std::atomic<bool> failed_{false};
    std::vector<float> ring_;
    DspProcessor dsp_;
    std::atomic<uint32_t> writeFrame_{0}, readFrame_{0};
    std::atomic<uint32_t> underruns_{0}, overruns_{0};
    std::atomic<int32_t> sampleRate_{0}, outputDeviceId_{-1};
};