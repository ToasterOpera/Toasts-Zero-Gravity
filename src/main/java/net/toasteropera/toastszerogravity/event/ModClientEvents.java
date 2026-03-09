package net.toasteropera.toastszerogravity.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.toasteropera.toastszerogravity.ToastsZeroGravity;

@EventBusSubscriber(modid = ToastsZeroGravity.MOD_ID)
public class ModClientEvents {
    private static final double MAX_DY = .4;
    private static final double MIN_DY = -.4;
    private static final double UP_THRUST = .02;
    private static final double DOWN_THRUST = -.02;
    private static final double DY_DRAG = .05;

    private static double dy = 0;

    @SubscribeEvent // on the game event bus only on the physical client
    public static void onClientTick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        var settings = mc.options;
        Player player = mc.player;
        if (mc.getConnection() == null)
            return;
        assert player != null;
        boolean up = settings.keyJump.isDown();
        boolean down = settings.keyShift.isDown();

        if (!player.getAbilities().flying && !player.onGround() && !player.isSwimming() && player.getAttributeValue(Attributes.GRAVITY) < 0.07) {
            dy *= 1 - DY_DRAG;
            if (up) {
                dy = Math.min(MAX_DY, dy + UP_THRUST);
            }
            if (down) {
                dy = Math.max(MIN_DY, dy + DOWN_THRUST);
            }
            player.move(MoverType.SELF, new Vec3(0, dy, 0));
        }
        else {
            dy = 0;
        }
    }
}
