//
// Created by ruofei qian on 2019/11/25.
//

#if defined(_MSC_VER)
#define NOMINMAX
#pragma once
#endif

#ifndef PBRT_SAMPLERS_POISSON_H
#define PBRT_SAMPLERS_POISSON_H

#include "sampler.h"
#include "rng.h"

namespace pbrt {

    class PoissonSampler : public PixelSampler {

    public:

        PoissonSampler(int numPoints, float minDist, int nSampledDimensions)
            : PixelSampler(numPoints, nSampledDimensions),
              numPoints(numPoints),
              minDist(minDist){}

        void StartPixel(const Point2i &);
        std::unique_ptr<Sampler> Clone(int seed);

    private:
        const int numPoints;
        const float minDist;
    };

    PoissonSampler *CreatePoissonSampler(const ParamSet &params);

}


#endif //PBRT_SAMPLERS_POISSON_H
