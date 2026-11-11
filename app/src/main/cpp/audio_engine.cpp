#include "audio_engine.h"
#include <algorithm>

AudioEngine::~AudioEngine() { stop(); }

bool AudioEngine::start() {
    if (running_.load()) return true;
    ring_.assign(48000U * 2U, 0.0f);
    writeFrame_.store(0);
    readFrame_.store(0);

    oboe::AudioStreamBuilder out;
    out.setDirection(oboe::Direction::Output)
       ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
       ->setSharingMode(oboe::SharingMode::Shared)
       ->setFormat(oboe::AudioFormat::Float)
       ->setChannelCount(outputChannels_)
       ->setSampleRate(48000)
       ->setDataCallback(this);
    if (out.openStream(output_) != oboe::Result::OK || !output_) return false;
    outputChannels_ = output_->getChannelCount();

    oboe::AudioStreamBuilder in;
    in.setDirection(oboe::Direction::Input)
      ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
      ->setSharingMode(oboe::SharingMode::Shared)
      ->setFormat(oboe::AudioFormat::Float)
      ->setChannelCount(inputChannels_)
      ->setSampleRate(output_->getSampleRate())
      ->setDataCallback(this);
    if (in.openStream(input_) != oboe::Result::OK || !input_) {
        output_->close(); output_.reset(); return false;
    }

    if (output_->requestStart() != oboe::Result::OK) { stop(); return false; }
    if (input_->requestStart() != oboe::Result::OK) { stop(); return false; }
    running_.store(true);
    return true;
}

void AudioEngine::stop() {
    running_.store(false);
    if (input_) { input_->stop(); input_->close(); input_.reset(); }
    if (output_) { output_->stop(); output_->close(); output_.reset(); }
}

oboe::DataCallbackResult AudioEngine::onAudioReady(oboe::AudioStream* stream, void* audioData, int32_t frames) {
    if (stream->getDirection() == oboe::Direction::Input) {
        const auto* input = static_cast<const float*>(audioData);
        const int32_t channels = stream->getChannelCount();
        const uint32_t capacity = static_cast<uint32_t>(ring_.size());
        uint32_t write = writeFrame_.load(std::memory_order_relaxed);
        const uint32_t read = readFrame_.load(std::memory_order_acquire);
        for (int32_t i = 0; i < frames; ++i) {
            const uint32_t next = (write + 1U) % capacity;
            if (next == read) break;
            float mono = 0.0f;
            for (int32_t ch = 0; ch < channels; ++ch) mono += input[i * channels + ch];
            ring_[write] = mono / static_cast<float>(channels);
            write = next;
        }
        writeFrame_.store(write, std::memory_order_release);
        return oboe::DataCallbackResult::Continue;
    }

    auto* output = static_cast<float*>(audioData);
    const uint32_t capacity = static_cast<uint32_t>(ring_.size());
    uint32_t read = readFrame_.load(std::memory_order_relaxed);
    const uint32_t write = writeFrame_.load(std::memory_order_acquire);
    const int32_t channels = stream->getChannelCount();
    for (int32_t i = 0; i < frames; ++i) {
        float sample = 0.0f;
        if (read != write) { sample = ring_[read]; read = (read + 1U) % capacity; }
        for (int32_t ch = 0; ch < channels; ++ch) output[i * channels + ch] = sample;
    }
    readFrame_.store(read, std::memory_order_release);
    return oboe::DataCallbackResult::Continue;
}
