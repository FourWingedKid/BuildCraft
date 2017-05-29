/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

import buildcraft.builders.client.ClientArchitectTables;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;

import buildcraft.builders.snapshot.ClientSnapshots;
import buildcraft.builders.tile.TileQuarry;

public enum BCBuildersEventDist {
    INSTANCE;

    private final Map<World, Deque<WeakReference<TileQuarry>>> allQuarries = new WeakHashMap<>();

    public void validateQuarry(TileQuarry quarry) {
        Deque<WeakReference<TileQuarry>> quarries = allQuarries.computeIfAbsent(quarry.getWorld(), k -> new LinkedList<>());
        quarries.add(new WeakReference<>(quarry));
        BCLog.logger.info("Added quarry to checking list");
    }

    public void invalidateQuarry(TileQuarry quarry) {
        Deque<WeakReference<TileQuarry>> quarries = allQuarries.get(quarry.getWorld());
        if (quarries == null) {
            // Odd.
            return;
        }
        Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();
        while (iter.hasNext()) {
            WeakReference<TileQuarry> ref = iter.next();
            TileQuarry pos = ref.get();
            if (pos == null || pos == quarry) {
                iter.remove();
                BCLog.logger.info("Removed quarry from checking list");
            }
        }
    }

    @SubscribeEvent
    public void onGetCollisionBoxesForQuarry(GetCollisionBoxesEvent event) {
        AxisAlignedBB target = event.getAabb();
        Deque<WeakReference<TileQuarry>> quarries = allQuarries.get(event.getWorld());
        if (quarries == null) {
            // No quarries in the target world
            return;
        }
        Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();
        while (iter.hasNext()) {
            WeakReference<TileQuarry> ref = iter.next();
            TileQuarry quarry = ref.get();
            if (quarry == null) {
                iter.remove();
                continue;
            }
            for (AxisAlignedBB aabb : quarry.getCollisionBoxes()) {
                if (target.intersectsWith(aabb)) {
                    event.getCollisionBoxesList().add(aabb);
                }
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderTooltipPostText(RenderTooltipEvent.PostText event) {
        if (BCBuildersItems.snapshot.getHeader(event.getStack()) != null &&
            ClientSnapshots.INSTANCE.getSnapshot(BCBuildersItems.snapshot.getHeader(event.getStack())) != null) {
            int pX = event.getX();
            int pY = event.getY() + event.getHeight() + 10;
            int sX = 100;
            int sY = 100;

            // Copy from GuiUtils#drawHoveringText
            int zLevel = 300;
            int backgroundColor = 0xF0100010;
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY - 4, pX + sX + 3, pY - 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY + sY + 3, pX + sX + 3, pY + sY + 4, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY - 3, pX + sX + 3, pY + sY + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, pX - 4, pY - 3, pX - 3, pY + sY + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, pX + sX + 3, pY - 3, pX + sX + 4, pY + sY + 3, backgroundColor, backgroundColor);
            int borderColorStart = 0x505000FF;
            int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY - 3 + 1, pX - 3 + 1, pY + sY + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, pX + sX + 2, pY - 3 + 1, pX + sX + 3, pY + sY + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY - 3, pX + sX + 3, pY - 3 + 1, borderColorStart, borderColorStart);
            GuiUtils.drawGradientRect(zLevel, pX - 3, pY + sY + 2, pX + sX + 3, pY + sY + 3, borderColorEnd, borderColorEnd);

            ClientSnapshots.INSTANCE.renderSnapshot(BCBuildersItems.snapshot.getHeader(event.getStack()), pX, pY, sX, sY);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTickClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !Minecraft.getMinecraft().isGamePaused()) {
            ClientArchitectTables.tick();
        }
    }
}
