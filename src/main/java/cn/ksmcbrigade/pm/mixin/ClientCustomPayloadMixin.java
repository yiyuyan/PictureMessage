package cn.ksmcbrigade.pm.mixin;

import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientboundCustomPayloadPacket.class)
public class ClientCustomPayloadMixin {

    @ModifyConstant(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",constant = @Constant(intValue = 1048576))
    public int initByte(int constant){
        return Integer.MAX_VALUE;
    }

    @ModifyConstant(method = "<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)V",constant = @Constant(intValue = 1048576))
    public int initRes(int constant){
        return Integer.MAX_VALUE;
    }
}
