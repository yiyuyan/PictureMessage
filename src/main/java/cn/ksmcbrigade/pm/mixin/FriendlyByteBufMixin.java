package cn.ksmcbrigade.pm.mixin;

import cn.ksmcbrigade.pm.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin implements Utils.bytesPacketFix {

    @Shadow public abstract ByteBuf readBytes(byte[] p_130310_);

    @Override
    public byte[] readLong(int p_130102_) {
        byte[] abyte = new byte[p_130102_];
        this.readBytes(abyte);
        return abyte;
    }
}
