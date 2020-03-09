//
// Created by ruofei qian on 2019/11/25.
//

#include "relaxedpoissondisk.h"
#include "paramset.h"
#include "sampling.h"
#include <stats.h>

namespace pbrt {

    void RelaxedPoissonSampler::StartPixel(const Point2i &p) {

        ProfilePhase _(Prof::StartPixel);
        // Generate single stratified samples for the pixel
        for (size_t i = 0; i < samples1D.size(); ++i) {
            RelaxedPoissonDiskSample1D(&samples1D[i][0], numPoints, minDist, rng);
        }

        for (size_t i = 0; i < samples2D.size(); ++i) {
            RelaxedPoissonDiskSample2D(&samples2D[i][0], numPoints, minDist, rng);
        }

        // Generate arrays of stratified samples for the pixel
        for (size_t i = 0; i < samples1DArraySizes.size(); ++i)
            for (int64_t j = 0; j < samplesPerPixel; ++j) {
                int count = samples1DArraySizes[i];
                RelaxedPoissonDiskSample1D(&samples1D[i][j * count], numPoints, minDist, rng);
            }
        for (size_t i = 0; i < samples2DArraySizes.size(); ++i)
            for (int64_t j = 0; j < samplesPerPixel; ++j) {
                int count = samples2DArraySizes[i];
                RelaxedPoissonDiskSample2D(&samples2D[i][j * count], numPoints, minDist, rng);
            }
        PixelSampler::StartPixel(p);
    }

    std::unique_ptr<Sampler> RelaxedPoissonSampler::Clone(int seed) {
        RelaxedPoissonSampler *rps = new RelaxedPoissonSampler(*this);
        rps->rng.SetSequence(seed);
        return std::unique_ptr<Sampler>(rps);
    }

    RelaxedPoissonSampler *CreateRelaxedPoissonSampler(const ParamSet &params) {
        int numPoints = params.FindOneInt("pixelsamples", 16);
        float minDist = params.FindOneFloat("minDist", -1.0f);
        int sd = params.FindOneInt("dimensions", 4);
        if (PbrtOptions.quickRender) numPoints = 1;
        return new RelaxedPoissonSampler(numPoints, minDist, sd);
    }
}

