//
// Created by ruofei qian on 2019/11/25.
//

#include <stats.h>
#include "samplers/poissondisk.h"
#include "paramset.h"
#include "sampling.h"

namespace pbrt {

    void PoissonSampler::StartPixel(const Point2i &p) {

        ProfilePhase _(Prof::StartPixel);
        // Generate single stratified samples for the pixel
        for (size_t i = 0; i < samples1D.size(); ++i) {
            PoissonDiskSample1D(&samples1D[i][0], numPoints, minDist, rng);
        }

        for (size_t i = 0; i < samples2D.size(); ++i) {
            PoissonDiskSample2D(&samples2D[i][0], numPoints, minDist, rng);
        }

        // Generate arrays of stratified samples for the pixel
        for (size_t i = 0; i < samples1DArraySizes.size(); ++i)
            for (int64_t j = 0; j < samplesPerPixel; ++j) {
                int count = samples1DArraySizes[i];
                PoissonDiskSample1D(&samples1D[i][j * count], numPoints, minDist, rng);
            }
        for (size_t i = 0; i < samples2DArraySizes.size(); ++i)
            for (int64_t j = 0; j < samplesPerPixel; ++j) {
                int count = samples2DArraySizes[i];
                PoissonDiskSample2D(&samples2D[i][j * count], numPoints, minDist, rng);
            }
        PixelSampler::StartPixel(p);
    }

    std::unique_ptr<Sampler> pbrt::PoissonSampler::Clone(int seed) {
        PoissonSampler *ps = new PoissonSampler(*this);
        ps->rng.SetSequence(seed);
        return std::unique_ptr<Sampler>(ps);
    }

    PoissonSampler *CreatePoissonSampler(const ParamSet &params) {
        int numPoints = params.FindOneInt("pixelsamples", 16);
        float minDist = params.FindOneFloat("minDist", -1.0f);
        int sd = params.FindOneInt("dimensions", 4);
        if (PbrtOptions.quickRender) numPoints = 1;
        return new PoissonSampler(numPoints, minDist, sd);
    }
}


