#include "dsp_processor.h"
#include <algorithm>
#include <cmath>

void DspProcessor::setParameters(const DspParameters& p) noexcept {
    inputGain_.store(std::clamp(p.inputGain, 0.0f, 4.0f), std::memory_order_relaxed);
    outputGain_.store(std::clamp(p.outputGain, 0.0f, 4.0f), std::memory_order_relaxed);
    highPassHz_.store(std::clamp(p.highPassHz, 0.0f, 1000.0f), std::memory_order_relaxed);
}

float DspProcessor::process(float input, float sampleRate) noexcept {
    float x = input * inputGain_.load(std::memory_order_relaxed);
    const float cutoff = highPassHz_.load(std::memory_order_relaxed);
    if (cutoff > 0.0f && sampleRate > 0.0f) {
        const float rc = 1.0f / (6.28318530718f * cutoff);
        const float dt = 1.0f / sampleRate;
        const float alpha = rc / (rc + dt);
        const float y = alpha * (previousOutput_ + x - previousInput_);
        previousInput_ = x;
        previousOutput_ = y;
        x = y;
    }
    return std::clamp(x * outputGain_.load(std::memory_order_relaxed), -1.0f, 1.0f);
}

void DspProcessor::reset() noexcept {
    previousInput_ = 0.0f;
    previousOutput_ = 0.0f;
}
