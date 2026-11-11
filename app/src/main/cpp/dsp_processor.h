#pragma once
#include <atomic>

struct DspParameters {
    float inputGain = 1.0f;
    float outputGain = 1.0f;
    float highPassHz = 0.0f; // 0 = bypass
};

class DspProcessor {
public:
    void setParameters(const DspParameters& parameters) noexcept;
    float process(float input, float sampleRate) noexcept;
    void reset() noexcept;
private:
    std::atomic<float> inputGain_{1.0f};
    std::atomic<float> outputGain_{1.0f};
    std::atomic<float> highPassHz_{0.0f};
    float previousInput_ = 0.0f;
    float previousOutput_ = 0.0f;
};
