package com.ultramega.refinedwirelessupgrades.common.screen;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class UpgradeScreenNavigation {
    private static double mouseX;
    private static double mouseY;
    private static boolean restoreMouse;

    private UpgradeScreenNavigation() {
    }

    public static void rememberMousePosition() {
        final Minecraft minecraft = Minecraft.getInstance();
        mouseX = minecraft.mouseHandler.xpos();
        mouseY = minecraft.mouseHandler.ypos();
        restoreMouse = true;
    }

    public static void restoreMousePosition() {
        if (!restoreMouse) {
            return;
        }
        restoreMouse = false;
        final Minecraft minecraft = Minecraft.getInstance();
        GLFW.glfwSetCursorPos(minecraft.getWindow().handle(), mouseX, mouseY);
    }
}
