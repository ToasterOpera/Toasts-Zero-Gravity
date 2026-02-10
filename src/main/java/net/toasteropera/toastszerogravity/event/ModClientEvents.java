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
    private static final double MAX_HORIZONTAL = .4;
    private static final double UP_THRUST = .02;
    private static final double DOWN_THRUST = -.02;
    private static final double HORIZONTAL_THRUST = .02;
    private static final double DY_DRAG = .05;
    private static final double HORIZONTAL_DRAG = .05;

    private static double dy = 0;
    private static double dx = 0;
    private static double dz = 0;


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
        boolean left = settings.keyLeft.isDown();
        boolean right = settings.keyRight.isDown();
        boolean forward = settings.keyUp.isDown();
        boolean back = settings.keyDown.isDown();

        double facing = (player.getYRot() % 360) * Math.PI / 180;


        if (!player.getAbilities().flying && !player.onGround() && !player.isSwimming() && player.getAttributeValue(Attributes.GRAVITY) < 0.0031) {
            //Relative dx and dz, to save money on trigonometry
            double rdx = 0;
            double rdz = 0;

            dy *= 1 - DY_DRAG;
            //TODO: horizontal drag using the holy unit circle

            if (up) {
                dy = Math.min(MAX_DY, dy + UP_THRUST);
            }
            if (down) {
                dy = Math.max(MIN_DY, dy + DOWN_THRUST);
            }
            if (forward) {
                rdz += HORIZONTAL_THRUST;
            }
            if (back) {
                rdz -= HORIZONTAL_THRUST;
            }
            if (left) {
                rdx -= HORIZONTAL_THRUST;
            }
            if (right) {
                rdx += HORIZONTAL_THRUST;
            }

            //Convert relative to actual x and z axis, add to dx and dz
            double tdx = dx + rdx * Math.sin(facing);
            double tdz = dz + rdz * Math.cos(facing);
            //dx and dz if you were going at max speed
            double sqrt = Math.sqrt(tdx * tdx + tdz * tdz);
            double cdx = dx + MAX_HORIZONTAL * tdx / sqrt;
            double cdz = dz + MAX_HORIZONTAL * tdz / sqrt;
            //Clamp dx and dz according to max total speed
            dx = Math.abs(tdx) < Math.abs(cdx) ? tdx : cdx;
            dz = Math.abs(tdz) < Math.abs(cdz) ? tdz : cdz;

            player.move(MoverType.SELF, new Vec3(dx, dy, dz));
        }
        else {
            dy = 0;
        }
    }
}
