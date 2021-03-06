/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import net.minecraft.world.World;

import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.blueprints.Template;
import buildcraft.lib.misc.data.Box;

public class PatternBox extends FillerPattern {

    public PatternBox() {
        super("box", EnumFillerPattern.BOX);
    }

    @Override
    public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
        Template result = new Template(box.size());

        int xMin = 0;
        int yMin = 0;
        int zMin = 0;

        int xMax = box.size().getX() - 1;
        int yMax = box.size().getY() - 1;
        int zMax = box.size().getZ() - 1;

        fill(xMin, yMin, zMin, xMax, yMin, zMax, result);
        fill(xMin, yMin, zMin, xMin, yMax, zMax, result);
        fill(xMin, yMin, zMin, xMax, yMax, zMin, result);
        fill(xMax, yMin, zMin, xMax, yMax, zMax, result);
        fill(xMin, yMin, zMax, xMax, yMax, zMax, result);
        fill(xMin, yMax, zMin, xMax, yMax, zMax, result);

        return result;
    }
}
