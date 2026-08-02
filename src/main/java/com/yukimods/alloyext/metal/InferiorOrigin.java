package com.yukimods.alloyext;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 标记物品的"劣等"出身。
 * <p>
 * 当劣等合金锭/流体被锻造成浇铸为 TFC 标准部件时，
 * 产物携带此组件以追踪其真实材料来源。
 * 熔融时 HeatingRecipeMixin 检测此组件并将输出流体替换为对应劣等合金。
 *
 * @param baseMetal 基体金属名，如 "copper"、"tin"，对应 {@link InferiorMetal#getName()}
 */
public record InferiorOrigin(String baseMetal) {

    public static final Codec<InferiorOrigin> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.STRING.fieldOf("base_metal").forGetter(InferiorOrigin::baseMetal))
                    .apply(instance, InferiorOrigin::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InferiorOrigin> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, InferiorOrigin::baseMetal, InferiorOrigin::new);
}
