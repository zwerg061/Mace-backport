package de.wolfmod.macebackport.network;

import de.wolfmod.macebackport.events.SpearEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class EmptySpearLungePacket {
    public EmptySpearLungePacket() {
    }

    public static void encode(EmptySpearLungePacket packet, FriendlyByteBuf buffer) {
    }

    public static EmptySpearLungePacket decode(FriendlyByteBuf buffer) {
        return new EmptySpearLungePacket();
    }

    public static void handle(EmptySpearLungePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                SpearEvents.tryLungeFromEmptyAttack(sender);
            }
        });
        context.setPacketHandled(true);
    }
}
