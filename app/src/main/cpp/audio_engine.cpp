#include "audio_engine.h"
#include <algorithm>

AudioEngine::~AudioEngine() { stop(); }

bool AudioEngine::start(int32_t requestedOutputDeviceId) {
    if (running_.load()) return true;
    failed_.store(false);
    underruns_.store(0);
    overruns_.store(0);
    ring_.assign(96000U, 0.0f);
    writeFrame_.store(0);
    readFrame_.store(0);

    auto openOutput = [&](int32_t deviceId, int32_t channels) {
        oboe::AudioStreamBuilder builder;
        builder.setDirection(oboe::Direction::Output)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setSharingMode(oboe::SharingMode::Shared)
            ->setFormat(oboe::AudioFormat::Float)
            ->setChannelCount(channels)
            ->setDataCallback(this)
            ->setErrorCallback(this);
        if (deviceId >= 0) builder.setDeviceId(deviceId);
        return builder.openStream(output_) == oboe::Result::OK && output_;
    };

    bool outputOpened = requestedOutputDeviceId >= 0 && openOutput(requestedOutputDeviceId, 2);
    if (!outputOpened && requestedOutputDeviceId >= 0) output_.reset();
    if (!outputOpened) outputOpened = openOutput(oboe::kUnspecified, 2);
    if (!outputOpened) {
        output_.reset();
        outputOpened = openOutput(oboe::kUnspecified, 1);
    }
    if (!outputOpened || !output_) return false;

    outputDeviceId_.store(output_->getDeviceId());
    sampleRate_.store(output_->getSampleRate());

    oboe::AudioStreamBuilder in;
    in.setDirection(oboe::Direction::Input)
      ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
      ->setSharingMode(oboe::SharingMode::Shared)
      ->setFormat(oboe::AudioFormat::Float)
      ->setChannelCount(1)
      ->setSampleRate(output_->getSampleRate())
      ->setDataCallback(this)
      ->setErrorCallback(this);
    if (in.openStream(input_) != oboe::Result::OK || !input_) {
        input_.reset();
        in.setSampleRate(oboe::kUnspecified);
        if (in.openStream(input_) != oboe::Result::OK || !input_) {
            output_->close(); output_.reset(); return false;
        }
    }

    if (output_->requestStart() != oboe::Result::OK) { stop(); return false; }
    if (input_->requestStart() != oboe::Result::OK) { stop(); return false; }
    running_.store(true);
    return true;
}

void AudioEngine::stop() {
    running_.store(false);
    if (input_) { input_->requestStop(); input_->close(); input_.reset(); }
    if (output_) { output_->requestStop(); output_->close(); output_.reset(); }
}

void AudioEngine::onErrorAfterClose(oboe::AudioStream*, oboe::Result) {
    failed_.store(true);
    running_.store(false);
}

oboe::DataCallbackResult AudioEngine::onAudioReady(oboe::AudioStream* stream, void* audioData, int32_t frames) {
    if (!running_.load(std::memory_order_relaxed)) {
        if (stream->getDirection() == oboe::Direction::Output)
            std::fill_n(static_cast<float*>(audioData), frames * stream->getChannelCount(), 0.0f);
        return oboe::DataCallbackResult::Continue;
    }
    const uint32_t capacity = static_cast<uint32_t>(ring_.size());
    if (stream->getDirection() == oboe::Direction::Input) {
        const auto* input = static_cast<const float*>(audioData);
        const int32_t channels = stream->getChannelCount();
        uint32_t write = writeFrame_.load(std::memory_order_relaxed);
        const uint32_t read = readFrame_.load(std::memory_order_acquire);
        for (int32_t i = 0; i < frames; ++i) {
            const uint32_t next = (write + 1U) % capacity;
            if (next == read) { overruns_.fetch_add(1, std::memory_order_relaxed); break; }
            float mono = 0.0f;
            for (int32_t ch = 0; ch < channels; ++ch) mono += input[i * channels + ch];
            ring_[write] = mono / static_cast<float>(channels);
            write = next;
        }
        writeFrame_.store(write, std::memory_order_release);
        return oboe::DataCallbackResult::Continue;
    }

    auto* output = static_cast<float*>(audioData);
    uint32_t read = readFrame_.load(std::memory_order_relaxed);
    const uint32_t write = writeFrame_.load(std::memory_order_acquire);
    const int32_t channels = stream->getChannelCount();
    uint32_t missing = 0;
    for (int32_t i = 0; i < frames; ++i) {
        float sample = 0.0f;
        if (read != write) { sample = ring_[read]; read = (read + 1U) % capacity; }
        else ++missing;
        for (int32_t ch = 0; ch < channels; ++ch) output[i * channels + ch] = sample;
    }
    if (missing) underruns_.fetch_add(missing, std::memory_order_relaxed);
    readFrame_.store(read, std::memory_order_release);
    return oboe::DataCallbackResult::Continue;
}