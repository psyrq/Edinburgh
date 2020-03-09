//
// Created by ruofei qian on 2019/11/27.
//


#if defined(_MSC_VER)
#define NOMINMAX
#pragma once
#endif

#ifndef PBRT_MATERIALS_ANISOTROPIC_H
#define PBRT_MATERIALS_ANISOTROPIC_H

#include "pbrt.h"
#include "material.h"
#include "reflection.h"
#include "bssrdf.h"

namespace pbrt {

    class AnisotropicMaterial : public Material {

    public:
        AnisotropicMaterial(const std::shared_ptr<Texture<Spectrum>> &Rd,
                            const std::shared_ptr<Texture<Spectrum>> &Rs,
                            const std::shared_ptr<Texture<Float>> &nu,
                            const std::shared_ptr<Texture<Float>> &nv,
                            const std::shared_ptr<Texture<Float>> &bumpMap) :
                            Rd(Rd),
                            Rs(Rs),
                            nu(nu),
                            nv(nv),
                            bumpMap(bumpMap) {
        }
        void ComputeScatteringFunctions(SurfaceInteraction *si, MemoryArena &arena, TransportMode mode, bool allowMultipleLobes) const;

    private:
        std::shared_ptr<Texture<Spectrum>> Rd, Rs;
        std::shared_ptr<Texture<Float>> nu, nv;
        std::shared_ptr<Texture<Float>> bumpMap;
    };

    AnisotropicMaterial *CreateAnisotropicMaterial(const TextureParams &mp);
}

#endif //PBRT_MATERIALS_ANISOTROPIC_H
