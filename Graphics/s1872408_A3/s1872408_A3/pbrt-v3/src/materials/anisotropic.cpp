//
// Created by ruofei qian on 2019/11/27.
//

#include "anisotropic.h"
#include "bssrdf.h"
#include "interaction.h"
#include "paramset.h"
#include "reflection.h"
#include "stats.h"
#include "stringprint.h"
#include "texture.h"
#include "rng.h"

namespace pbrt {

    void AnisotropicMaterial::ComputeScatteringFunctions(
            SurfaceInteraction *si, MemoryArena &arena, TransportMode mode,
            bool allowMultipleLobes) const {
        // Perform bump mapping with _bumpMap_, if present
        if (bumpMap) Bump(bumpMap, si);
        si->bsdf = ARENA_ALLOC(arena, BSDF)(*si);
        Spectrum d = Rd->Evaluate(*si).Clamp();
        Spectrum s = Rs->Evaluate(*si).Clamp();
        Float u = nu->Evaluate(*si);
        Float v = nv->Evaluate(*si);

        si->bsdf->Add(ARENA_ALLOC(arena, Anisotropic)(d, s, u, v, si->dpdu, si->dpdv));
    }

    AnisotropicMaterial *CreateAnisotropicMaterial(const TextureParams &mp) {
        std::shared_ptr<Texture<Spectrum>> Rd =
                mp.GetSpectrumTexture("Rd", Spectrum(.5f));
        std::shared_ptr<Texture<Spectrum>> Rs =
                mp.GetSpectrumTexture("Rs", Spectrum(.5f));
        std::shared_ptr<Texture<Float>> nu =
                mp.GetFloatTexture("nu", .1f);
        std::shared_ptr<Texture<Float>> nv =
                mp.GetFloatTexture("nv", .1f);
        std::shared_ptr<Texture<Float>> bumpMap =
                mp.GetFloatTextureOrNull("bumpmap");
        return new AnisotropicMaterial(Rd, Rs, nu, nv, bumpMap);
    }
}
