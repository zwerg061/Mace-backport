package de.wolfmod.macebackport;

import de.wolfmod.macebackport.events.CombatEvents;
import de.wolfmod.macebackport.events.SpearEvents;
import de.wolfmod.macebackport.network.ModNetworking;
import de.wolfmod.macebackport.registry.ModEnchantments;
import de.wolfmod.macebackport.registry.ModEntities;
import de.wolfmod.macebackport.registry.ModItems;
import de.wolfmod.macebackport.registry.ModParticles;
import de.wolfmod.macebackport.registry.ModSounds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.client.event.EntityRenderersEvent;

@Mod(MaceBackportMod.MODID)
public final class MaceBackportMod {
    public static final String MODID = "macebackport";

    public MaceBackportMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modBus);
        ModEnchantments.ENCHANTMENTS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModParticles.PARTICLE_TYPES.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);

        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(CombatEvents.class);
        MinecraftForge.EVENT_BUS.register(SpearEvents.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetworking::register);
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ClientEvents {
        private ClientEvents() {
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                net.minecraft.resources.ResourceLocation heldSpear = new net.minecraft.resources.ResourceLocation(MODID, "held_spear");
                registerSpearHeldProperty(ModItems.WOODEN_SPEAR.get(), heldSpear);
                registerSpearHeldProperty(ModItems.STONE_SPEAR.get(), heldSpear);
                registerSpearHeldProperty(ModItems.COPPER_SPEAR.get(), heldSpear);
                registerSpearHeldProperty(ModItems.IRON_SPEAR.get(), heldSpear);
                registerSpearHeldProperty(ModItems.GOLDEN_SPEAR.get(), heldSpear);
                registerSpearHeldProperty(ModItems.DIAMOND_SPEAR.get(), heldSpear);
                registerSpearHeldProperty(ModItems.NETHERITE_SPEAR.get(), heldSpear);
            });
        }

        private static void registerSpearHeldProperty(net.minecraft.world.item.Item item, net.minecraft.resources.ResourceLocation id) {
            net.minecraft.client.renderer.item.ItemProperties.register(item, id, (stack, level, entity, seed) ->
                    ClientForgeEvents.shouldUseHeldSpearModel(item, entity) ? 1.0F : 0.0F);
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.WIND_CHARGE.get(), de.wolfmod.macebackport.client.renderer.WindChargeRenderer::new);
        }

        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event) {
            event.register(ModParticles.GUST.get(), de.wolfmod.macebackport.particle.GustParticle.Provider::new);
            event.register(ModParticles.SMALL_GUST.get(), de.wolfmod.macebackport.particle.SmallGustParticle.Provider::new);
            event.register(ModParticles.GUST_EMITTER_SMALL.get(), new de.wolfmod.macebackport.particle.GustSeedParticle.Provider(0.25D, 7, 1));
            event.register(ModParticles.GUST_EMITTER_LARGE.get(), new de.wolfmod.macebackport.particle.GustSeedParticle.Provider(1.0D, 15, 2));
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static final class ClientForgeEvents {
        private static boolean renderingSpearHand;
        private static int spearEntityRenderDepth;
        private static net.minecraft.world.entity.LivingEntity currentSpearRenderEntity;

        private ClientForgeEvents() {
        }

        private static boolean shouldUseHeldSpearModel(net.minecraft.world.item.Item spearItem, net.minecraft.world.entity.LivingEntity entity) {
            if (renderingSpearHand) {
                return true;
            }

            if (currentSpearRenderEntity == null) {
                return false;
            }

            return currentSpearRenderEntity.getMainHandItem().is(spearItem)
                    || currentSpearRenderEntity.getOffhandItem().is(spearItem);
        }

        private static void pushSpearRenderEntity(net.minecraft.world.entity.LivingEntity livingEntity) {
            if (!isHoldingAnySpear(livingEntity)) {
                return;
            }

            spearEntityRenderDepth++;
            currentSpearRenderEntity = livingEntity;
        }

        private static void popSpearRenderEntity(net.minecraft.world.entity.LivingEntity livingEntity) {
            if (currentSpearRenderEntity != livingEntity) {
                return;
            }

            spearEntityRenderDepth--;
            if (spearEntityRenderDepth <= 0) {
                spearEntityRenderDepth = 0;
                currentSpearRenderEntity = null;
            }
        }

        @SubscribeEvent
        public static void beginSpearEntityRender(net.minecraftforge.client.event.RenderLivingEvent.Pre<?, ?> event) {
            pushSpearRenderEntity(event.getEntity());
        }

        @SubscribeEvent
        public static void endSpearEntityRender(net.minecraftforge.client.event.RenderLivingEvent.Post<?, ?> event) {
            popSpearRenderEntity(event.getEntity());
        }

        @SubscribeEvent
        public static void beginSpearPlayerRender(net.minecraftforge.client.event.RenderPlayerEvent.Pre event) {
            pushSpearRenderEntity(event.getEntity());
        }

        @SubscribeEvent
        public static void endSpearPlayerRender(net.minecraftforge.client.event.RenderPlayerEvent.Post event) {
            popSpearRenderEntity(event.getEntity());
        }

        private static boolean isHoldingAnySpear(net.minecraft.world.entity.LivingEntity livingEntity) {
            return livingEntity.getMainHandItem().is(de.wolfmod.macebackport.registry.ModItemTags.SPEARS)
                    || livingEntity.getOffhandItem().is(de.wolfmod.macebackport.registry.ModItemTags.SPEARS);
        }

        @SubscribeEvent
        public static void keepSpearChargeMovement(net.minecraftforge.client.event.MovementInputUpdateEvent event) {
            net.minecraft.world.entity.player.Player player = event.getEntity();
            if (!player.isUsingItem() || !player.getUseItem().is(de.wolfmod.macebackport.registry.ModItemTags.SPEARS)) {
                return;
            }

            net.minecraft.client.player.Input input = event.getInput();
            input.leftImpulse *= 5.0F;
            input.forwardImpulse *= 5.0F;
        }

        @SubscribeEvent
        public static void renderAnimatedSpearHand(net.minecraftforge.client.event.RenderHandEvent event) {
            net.minecraft.world.item.ItemStack stack = event.getItemStack();
            if (!stack.is(de.wolfmod.macebackport.registry.ModItemTags.SPEARS)) {
                return;
            }

            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            net.minecraft.client.player.LocalPlayer player = minecraft.player;
            if (player == null || minecraft.level == null) {
                return;
            }

            int side = getHandSide(player, event.getHand());
            float useTicks = player.isUsingItem() && player.getUsedItemHand() == event.getHand()
                    ? player.getTicksUsingItem() + event.getPartialTick()
                    : 0.0F;
            float swing = event.getSwingProgress();

            event.setCanceled(true);
            com.mojang.blaze3d.vertex.PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();

            SpearAnimationFrame frame;
            if (swing > 0.001F) {
                frame = sampleSpearFrame(JAB_FRAMES, swing);
            } else if (useTicks > 0.0F) {
                float chargeTime = net.minecraft.util.Mth.clamp(useTicks / 28.0F, 0.0F, 1.0F);
                frame = sampleSpearFrame(CHARGE_FRAMES, chargeTime);
            } else {
                frame = sampleSpearFrame(IDLE_FRAMES, 0.0F);
            }

            poseStack.translate(side * frame.side, frame.vertical, frame.depth);
            poseStack.mulPose(com.mojang.math.Vector3f.YP.rotationDegrees(side * frame.yRot));
            poseStack.mulPose(com.mojang.math.Vector3f.XP.rotationDegrees(frame.xRot));
            poseStack.mulPose(com.mojang.math.Vector3f.ZP.rotationDegrees(side * frame.zRot));
            poseStack.scale(frame.scale, frame.scale, 1.0F);

            net.minecraft.client.renderer.block.model.ItemTransforms.TransformType transformType = side > 0
                    ? net.minecraft.client.renderer.block.model.ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                    : net.minecraft.client.renderer.block.model.ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
            renderingSpearHand = true;
            try {
                minecraft.getItemRenderer().renderStatic(player, stack, transformType, side < 0, poseStack,
                        event.getMultiBufferSource(), minecraft.level, event.getPackedLight(),
                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0);
            } finally {
                renderingSpearHand = false;
            }

            poseStack.popPose();
        }

        @SubscribeEvent
        public static void triggerEmptySpearLunge(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
            if (!event.isAttack() || event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) {
                return;
            }

            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            net.minecraft.client.player.LocalPlayer player = minecraft.player;
            if (player == null || minecraft.screen != null || !player.getMainHandItem().is(de.wolfmod.macebackport.registry.ModItemTags.SPEARS)) {
                return;
            }

            if (minecraft.hitResult != null && minecraft.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
                return;
            }

            de.wolfmod.macebackport.network.ModNetworking.CHANNEL.sendToServer(new de.wolfmod.macebackport.network.EmptySpearLungePacket());
        }

        private static int getHandSide(net.minecraft.client.player.LocalPlayer player, net.minecraft.world.InteractionHand hand) {
            boolean mainRight = player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT;
            boolean rightHand = hand == net.minecraft.world.InteractionHand.MAIN_HAND ? mainRight : !mainRight;
            return rightHand ? 1 : -1;
        }

        private static SpearAnimationFrame sampleSpearFrame(SpearAnimationFrame[] frames, float progress) {
            float t = net.minecraft.util.Mth.clamp(progress, 0.0F, 1.0F);
            SpearAnimationFrame previous = frames[0];
            for (int i = 1; i < frames.length; i++) {
                SpearAnimationFrame next = frames[i];
                if (t <= next.time) {
                    float local = (t - previous.time) / (next.time - previous.time);
                    float eased = local * local * (3.0F - (2.0F * local));
                    return previous.lerp(next, eased);
                }
                previous = next;
            }
            return frames[frames.length - 1];
        }

        private static final SpearAnimationFrame[] CHARGE_FRAMES = new SpearAnimationFrame[]{
            new SpearAnimationFrame(0.00F, 0.12F, -0.03F, -0.12F, 4.0F, -8.0F, -5.0F, 1.00F),
            new SpearAnimationFrame(0.25F, 0.08F, -0.05F, -0.22F, 10.0F, -16.0F, -11.0F, 1.00F),
            new SpearAnimationFrame(0.50F, 0.06F, -0.08F, -0.34F, 16.0F, -28.0F, -18.0F, 1.01F),
            new SpearAnimationFrame(0.75F, 0.06F, -0.10F, -0.45F, 20.0F, -40.0F, -24.0F, 1.01F),
            new SpearAnimationFrame(1.00F, 0.06F, -0.11F, -0.52F, 22.0F, -48.0F, -28.0F, 1.01F)
        };

        private static final SpearAnimationFrame[] JAB_FRAMES = new SpearAnimationFrame[]{
            new SpearAnimationFrame(0.00F, 0.02F, -0.03F, -0.26F, 4.0F, -12.0F, -3.0F, 1.00F),
            new SpearAnimationFrame(0.18F, 0.02F, -0.03F, -0.58F, 4.0F, -12.0F, -3.0F, 1.00F),
            new SpearAnimationFrame(0.34F, 0.01F, -0.04F, -0.90F, 3.0F, -10.0F, -2.0F, 1.00F),
            new SpearAnimationFrame(0.62F, 0.01F, -0.03F, -0.34F, 4.0F, -12.0F, -3.0F, 1.00F),
            new SpearAnimationFrame(0.82F, 0.02F, -0.03F, -0.22F, 4.0F, -12.0F, -3.0F, 1.00F),
                new SpearAnimationFrame(1.00F, 0.00F, 0.00F, 0.00F, 0.0F, 0.0F, 0.0F, 1.00F)
        };

        private static final SpearAnimationFrame[] IDLE_FRAMES = new SpearAnimationFrame[]{
            new SpearAnimationFrame(0.00F, 0.03F, -0.03F, -0.24F, 8.0F, -16.0F, -10.0F, 1.00F),
            new SpearAnimationFrame(1.00F, 0.03F, -0.03F, -0.24F, 8.0F, -16.0F, -10.0F, 1.00F)
        };

        private record SpearAnimationFrame(float time, float side, float vertical, float depth,
                                           float yRot, float xRot, float zRot, float scale) {
            private SpearAnimationFrame lerp(SpearAnimationFrame other, float t) {
                return new SpearAnimationFrame(
                        lerp(this.time, other.time, t),
                        lerp(this.side, other.side, t),
                        lerp(this.vertical, other.vertical, t),
                        lerp(this.depth, other.depth, t),
                        lerp(this.yRot, other.yRot, t),
                        lerp(this.xRot, other.xRot, t),
                        lerp(this.zRot, other.zRot, t),
                        lerp(this.scale, other.scale, t)
                );
            }

            private static float lerp(float from, float to, float t) {
                return from + ((to - from) * t);
            }
        }

    }
}
