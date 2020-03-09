
/*
    pbrt source code is Copyright(c) 1998-2016
                        Matt Pharr, Greg Humphreys, and Wenzel Jakob.

    This file is part of pbrt.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are
    met:

    - Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

    - Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
    IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
    TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
    HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
    SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
    LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
    OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

#if defined(_MSC_VER)
#define NOMINMAX
#pragma once
#endif

#ifndef PBRT_CORE_SAMPLING_H
#define PBRT_CORE_SAMPLING_H

// core/sampling.h*
#include "pbrt.h"
#include "geometry.h"
#include "rng.h"
#include <algorithm>

namespace pbrt {

// Sampling Declarations
void StratifiedSample1D(Float *samples, int nsamples, RNG &rng,
                        bool jitter = true);
void StratifiedSample2D(Point2f *samples, int nx, int ny, RNG &rng,
                        bool jitter = true);

void PoissonDiskSample1D(Float *samples, int numPoints, float minDist, RNG &rng);
void PoissonDiskSample2D(Point2f *samples, int numPoints, float minDist, RNG & rng);

void RelaxedPoissonDiskSample1D(Float *samples, int numPoints, float minDist, RNG &rng);
void RelaxedPoissonDiskSample2D(Point2f *samples, int numPoints, float minDist, RNG & rng);

void LatinHypercube(Float *samples, int nSamples, int nDim, RNG &rng);
struct Distribution1D {
    // Distribution1D Public Methods
    Distribution1D(const Float *f, int n) : func(f, f + n), cdf(n + 1) {
        // Compute integral of step function at $x_i$
        cdf[0] = 0;
        for (int i = 1; i < n + 1; ++i) cdf[i] = cdf[i - 1] + func[i - 1] / n;

        // Transform step function integral into CDF
        funcInt = cdf[n];
        if (funcInt == 0) {
            for (int i = 1; i < n + 1; ++i) cdf[i] = Float(i) / Float(n);
        } else {
            for (int i = 1; i < n + 1; ++i) cdf[i] /= funcInt;
        }
    }
    int Count() const { return (int)func.size(); }
    Float SampleContinuous(Float u, Float *pdf, int *off = nullptr) const {
        // Find surrounding CDF segments and _offset_
        int offset = FindInterval((int)cdf.size(),
                                  [&](int index) { return cdf[index] <= u; });
        if (off) *off = offset;
        // Compute offset along CDF segment
        Float du = u - cdf[offset];
        if ((cdf[offset + 1] - cdf[offset]) > 0) {
            CHECK_GT(cdf[offset + 1], cdf[offset]);
            du /= (cdf[offset + 1] - cdf[offset]);
        }
        DCHECK(!std::isnan(du));

        // Compute PDF for sampled offset
        if (pdf) *pdf = (funcInt > 0) ? func[offset] / funcInt : 0;

        // Return $x\in{}[0,1)$ corresponding to sample
        return (offset + du) / Count();
    }
    int SampleDiscrete(Float u, Float *pdf = nullptr,
                       Float *uRemapped = nullptr) const {
        // Find surrounding CDF segments and _offset_
        int offset = FindInterval((int)cdf.size(),
                                  [&](int index) { return cdf[index] <= u; });
        if (pdf) *pdf = (funcInt > 0) ? func[offset] / (funcInt * Count()) : 0;
        if (uRemapped)
            *uRemapped = (u - cdf[offset]) / (cdf[offset + 1] - cdf[offset]);
        if (uRemapped) CHECK(*uRemapped >= 0.f && *uRemapped <= 1.f);
        return offset;
    }
    Float DiscretePDF(int index) const {
        CHECK(index >= 0 && index < Count());
        return func[index] / (funcInt * Count());
    }

    // Distribution1D Public Data
    std::vector<Float> func, cdf;
    Float funcInt;
};

Point2f RejectionSampleDisk(RNG &rng);
Vector3f UniformSampleHemisphere(const Point2f &u);
Float UniformHemispherePdf();
Vector3f UniformSampleSphere(const Point2f &u);
Float UniformSpherePdf();
Vector3f UniformSampleCone(const Point2f &u, Float thetamax);
Vector3f UniformSampleCone(const Point2f &u, Float thetamax, const Vector3f &x,
                           const Vector3f &y, const Vector3f &z);
Float UniformConePdf(Float thetamax);
Point2f UniformSampleDisk(const Point2f &u);
Point2f ConcentricSampleDisk(const Point2f &u);
Point2f UniformSampleTriangle(const Point2f &u);
class Distribution2D {
  public:
    // Distribution2D Public Methods
    Distribution2D(const Float *data, int nu, int nv);
    Point2f SampleContinuous(const Point2f &u, Float *pdf) const {
        Float pdfs[2];
        int v;
        Float d1 = pMarginal->SampleContinuous(u[1], &pdfs[1], &v);
        Float d0 = pConditionalV[v]->SampleContinuous(u[0], &pdfs[0]);
        *pdf = pdfs[0] * pdfs[1];
        return Point2f(d0, d1);
    }
    Float Pdf(const Point2f &p) const {
        int iu = Clamp(int(p[0] * pConditionalV[0]->Count()), 0,
                       pConditionalV[0]->Count() - 1);
        int iv =
            Clamp(int(p[1] * pMarginal->Count()), 0, pMarginal->Count() - 1);
        return pConditionalV[iv]->func[iu] / pMarginal->funcInt;
    }

  private:
    // Distribution2D Private Data
    std::vector<std::unique_ptr<Distribution1D>> pConditionalV;
    std::unique_ptr<Distribution1D> pMarginal;
};

// Sampling Inline Functions
template <typename T>
void Shuffle(T *samp, int count, int nDimensions, RNG &rng) {
    for (int i = 0; i < count; ++i) {
        int other = i + rng.UniformUInt32(count - i);
        for (int j = 0; j < nDimensions; ++j)
            std::swap(samp[nDimensions * i + j], samp[nDimensions * other + j]);
    }
}

inline Vector3f CosineSampleHemisphere(const Point2f &u) {
    Point2f d = ConcentricSampleDisk(u);
    Float z = std::sqrt(std::max((Float)0, 1 - d.x * d.x - d.y * d.y));
    return Vector3f(d.x, d.y, z);
}

inline Float CosineHemispherePdf(Float cosTheta) { return cosTheta * InvPi; }

inline Float BalanceHeuristic(int nf, Float fPdf, int ng, Float gPdf) {
    return (nf * fPdf) / (nf * fPdf + ng * gPdf);
}

inline Float PowerHeuristic(int nf, Float fPdf, int ng, Float gPdf) {
    Float f = nf * fPdf, g = ng * gPdf;
    return (f * f) / (f * f + g * g);
}

// start: inline functions for PoissonDiskSampling
inline Float getRadius(float minDist, float f1) {
    return minDist * (f1 + 1.0f);
}

inline Float getDistance(Point2f &p1, Point2f &p2) {
    return sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
}

inline Point2f popRandom2D(std::vector<Point2f> &points, RNG &rng) {
    int index = rng.UniformUInt32(points.size());
    Point2f p = points[index];
    points.erase(points.begin() + index);
    return p;
}

inline bool isAcceptable2D(std::vector<Point2f> samplePoints, Point2f &point, float minDist) {

    for (auto & samplePoint : samplePoints) {
        if (getDistance(samplePoint, point) < minDist) { return false; }
    }
    return true;
}

inline Point2f generateRandomPointAround2D(Point2f &p, float minDist, RNG &rng) {

    while (true) {

        float f1 = rng.UniformFloat();
        float f2  = rng.UniformFloat();

        float radius = getRadius(minDist, f1);
        float angle = 2 * Pi * f2;

        float x = p.x + radius * cos(angle);
        float y = p.y + radius * sin(angle);

        if (x > 0 && x < OneMinusEpsilon && y > 0 && y < OneMinusEpsilon) return Point2f(x, y);
    }
}

inline std::vector<Point2f> generatePoissonPoints2D(int numPoints, int candidatePoints, float minDist, RNG &rng) {

    std::vector<Point2f> samplePoints;
    std::vector<Point2f> processList;

    if (minDist < 0 || minDist > 1) minDist = sqrt((float)numPoints) / (float)numPoints;

    for (int i = 0; i < numPoints; ++i) {

        if (samplePoints.size() == numPoints) break;

        if (i == 0) {
            Point2f firstPoint = Point2f(rng.UniformFloat(), rng.UniformFloat());
            samplePoints.push_back(firstPoint);
            processList.push_back(firstPoint);
            continue;
        }

        Point2f point;
        if (!processList.empty())
            point = popRandom2D(processList, rng);
        else
            point = Point2f(rng.UniformFloat(), rng.UniformFloat());

        for (int j = 0; j < candidatePoints; ++j) {

//            Point2f candidatePoint = Point2f(rng.UniformFloat(), rng.UniformFloat());
            Point2f candidatePoint = generateRandomPointAround2D(point, minDist, rng);
            if (isAcceptable2D(samplePoints, candidatePoint, minDist)) {
                if (samplePoints.size() < numPoints) {
                    samplePoints.push_back(candidatePoint);
                    processList.push_back(candidatePoint);
                } else break;
            }
        }
    }

    return samplePoints;
}

inline std::vector<Point2f> generateRelaxedPoissonPoints2D(int numPoints, int candidatePoints, float minDist, RNG &rng) {

    std::vector<Point2f> samplePoints;
    std::vector<Point2f> processList;

    if (minDist < 0 || minDist > 1) minDist = sqrt((float)numPoints) / (float)numPoints;

    for (int i = 0; i < numPoints; ++i) {

        if (samplePoints.size() == numPoints) break;

        if (i == 0) {
            Point2f firstPoint = Point2f(rng.UniformFloat(), rng.UniformFloat());
            samplePoints.push_back(firstPoint);
            processList.push_back(firstPoint);
            continue;
        }

        Point2f point;
        if (!processList.empty())
            point = popRandom2D(processList, rng);
        else
            point = Point2f(rng.UniformFloat(), rng.UniformFloat());

        bool relaxed = false;

        while (!relaxed) {

            if (samplePoints.size() == numPoints) break;

            for (int j = 0; j < candidatePoints; ++j) {

                Point2f candidatePoint = Point2f(rng.UniformFloat(), rng.UniformFloat());
//                Point2f candidatePoint = generateRandomPointAround2D(point, minDist, rng);

                if (isAcceptable2D(samplePoints, candidatePoint, minDist)) {
                    if (samplePoints.size() < numPoints) {
                        relaxed = true;
                        samplePoints.push_back(candidatePoint);
                        processList.push_back(candidatePoint);
                    } else break;
                }
            }
            if (!relaxed) minDist *= 0.99;
        }
    }
    return samplePoints;
}

// end: inline functions for PoissonDiskSampling

}  // namespace pbrt

#endif  // PBRT_CORE_SAMPLING_H
