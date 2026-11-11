#pragma once
#include <atomic>
#include <cstdint>
#include <memory>
#include <oboe/Oboe.h>
#include <vector>

class AudioEngine final : public oboe::AudioStreamDataCallback {
public:
    AudioEngine() = default;
    ~AudioEngine();
    bool start();
    void stop();
    bool isRunning() const noexcept { return running_.load(); }
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream* stream, void* audioData, int32_t numFrames) override;
private:
    std::shared_ptr<oboe::AudioStream> input_;
    std::shared_ptr<oboe::AudioStream> output_;
    std::atomic<bool> running_{false};
    std::vector<float> ring_;
    std::atomic<uint32_t> writeFrame_{0};
    std::atomic<uint32_t> readFrame_{0};
    int32_t outputChannels_{2};
    int32_t inputChannels_{1};
};
