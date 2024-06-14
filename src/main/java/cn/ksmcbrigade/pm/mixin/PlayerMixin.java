package cn.ksmcbrigade.pm.mixin;

import cn.ksmcbrigade.pm.Utils;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public class PlayerMixin implements Utils.networkPlayer{
    @Unique
    public boolean send = false;

    @Unique
    public boolean ret = false;

    @Unique
    public boolean need = false;

    @Unique
    public boolean sent = false;

    @Override
    public void set(boolean sendSet) {
        this.send = sendSet;
    }

    @Override
    public boolean get() {
        return this.send;
    }

    @Override
    public void set2(boolean retSet) {
        this.ret = retSet;
    }

    @Override
    public void set3(boolean needSet) {
        this.need = needSet;
    }

    @Override
    public void set4(boolean sentSet) {
        this.sent = sentSet;
    }

    @Override
    public boolean get2() {
        return this.ret;
    }

    @Override
    public boolean get3() {
        return this.need;
    }

    @Override
    public boolean get4() {
        return this.sent;
    }
}
