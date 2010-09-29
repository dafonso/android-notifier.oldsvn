/*
 * Android Notifier Desktop is a multiplatform remote notification client for Android devices.
 *
 * Copyright (C) 2010  Leandro Aparecido
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eclipse.nebula.widgets.pgroup;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * AbstractGroupStrategy is a convenient starting point for all
 * IGroupStrategy's.
 * <P>
 * The AbstractGroupStrategy handles most behavior for you. All that is required
 * of extending classes, is to implement painting and sizing.
 *
 * @author chris
 */
public abstract class AbstractGroupStrategy
{

    private PGroup group;

    /*
     * (non-Javadoc)
     *
     * @see com.swtplus.widgets.IGroupStrategy#initialize(com.swtplus.widgets.PGroup)
     */
    public void initialize(PGroup g)
    {
        group = g;

        update();
    }

    public boolean isToggleLocation(int x, int y)
    {
        if (getGroup().getToggleRenderer() != null)
        {
            Rectangle r = new Rectangle(getGroup().getToggleRenderer().getBounds().x, getGroup()
                .getToggleRenderer().getBounds().y,
                                        getGroup().getToggleRenderer().getBounds().width,
                                        getGroup().getToggleRenderer().getBounds().height);
            if (r.contains(x, y))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the area where toolitems can be drawn
     */
    public Rectangle getToolItemArea() {
    	return null;
    }

    /**
     * Paints the actual group widget. This method is to be implemented by
     * extending classes.
     *
     * @param gc
     */
    public abstract void paint(GC gc);

    public abstract void dispose();

    /**
     * @return Returns the PGroup.
     */
    public PGroup getGroup()
    {
        return group;
    }

    public abstract Rectangle computeTrim(int x, int y, int width, int height);

    public abstract Rectangle getClientArea();

    public abstract void update();
}
