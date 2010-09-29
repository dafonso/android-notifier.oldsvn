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

import org.eclipse.nebula.widgets.pgroup.internal.TextUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * SimpleGroupStrategy adds a seperator to the normal PGroup's image and text.
 * 
 * @since 1.0
 * @author chris
 */
public class SimpleGroupStrategy extends AbstractGroupStrategy
{

    private int separatorHeight = 2;

    private int heightWithoutLine = 0;

    private int lineMargin = 2;

    private int lineBetweenSpacing = 8;

    private int titleTextMargin = 0;

    private int betweenSpacing = 6;

    private int vMargin = 3;

    private int hMargin = 3;

    private int titleHeight;

    private int textWidth;

    private int fontHeight;

    /**
     * Creates a SimpleGroupStrategy with the given toggle and style.
     * 
     * @param ts
     * @param style
     */
    public SimpleGroupStrategy()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.swtplus.widgets.AbstractGroupStrategy#paintGroup(org.eclipse.swt.graphics.GC)
     */
    public void paint(GC gc)
    {
        Color back = getGroup().internalGetBackground();
        if (back != null)
        {
            gc.fillRectangle(0,0,getGroup().getSize().x,getGroup().getSize().y);
        }
        
        // gc.fillRectangle(0,0,getGroup().getSize().x,getTitleHeight());

        if (getGroup().getToggleRenderer() != null)
        {
            int toggleY = 0;
            if (getGroup().getLinePosition() == SWT.CENTER)
            {
                toggleY = (heightWithoutLine - getGroup().getToggleRenderer().getSize().y) / 2;
            }
            else
            {
                toggleY = (heightWithoutLine - getGroup().getToggleRenderer().getSize().y)
                          - vMargin;
                // toggleY += ((getFontHeight() + (2*titleTextMargin) +
                // (2*vMargin)) - getToggleStrategy().getSize().y)/2;
            }

            if ((getGroup().getTogglePosition() & SWT.LEAD) != 0)
            {
                getGroup().getToggleRenderer().setLocation(new Point(hMargin, toggleY));
            }
            else
            {
                getGroup().getToggleRenderer().setLocation(
                                                           new Point(getGroup().getSize().x
                                                                     - hMargin
                                                                     - getGroup()
                                                                         .getToggleRenderer()
                                                                         .getSize().x, toggleY));
            }
        }

        if (getGroup().getImage() != null)
        {
            int imgX = 0, imgY = 0;
            if ((getGroup().getImagePosition() & SWT.LEAD) != 0)
            {
                imgX = hMargin;
                if ((getGroup().getTogglePosition() & SWT.LEAD) != 0
                    && (getGroup().getToggleRenderer() != null))
                {
                    imgX += getGroup().getToggleRenderer().getSize().x + betweenSpacing;
                }
            }
            else
            {
                imgX = getGroup().getSize().x - getGroup().getImage().getBounds().width - hMargin;
                if (!((getGroup().getTogglePosition() & SWT.LEAD) != 0)
                    && (getGroup().getToggleRenderer() != null))
                {
                    imgX -= getGroup().getToggleRenderer().getSize().x + betweenSpacing;
                }
            }
            if (getGroup().getLinePosition() == SWT.CENTER)
            {
                imgY = (heightWithoutLine - getGroup().getImage().getBounds().height) / 2;
            }
            else
            {
                imgY = heightWithoutLine - getGroup().getImage().getBounds().height - vMargin;
            }
            gc.drawImage(getGroup().getImage(), imgX, imgY);
        }

        Rectangle textBounds = getTextBounds();

        gc.drawString(TextUtils.getShortString(gc, getGroup().getText(), textBounds.width),
                      textBounds.x, textBounds.y, true);

        int x = 0, x2 = 0, y = 0;
        if (getGroup().getLinePosition() == SWT.BOTTOM)
        {
            x = 0;
            x2 = getGroup().getSize().x;
            y = titleHeight - separatorHeight - lineMargin;
        }
        else
        {
            Point p = gc.stringExtent(TextUtils.getShortString(gc, getGroup().getText(),
                                                               textBounds.width));

            x = textBounds.x + p.x + lineBetweenSpacing;
            x2 = textBounds.x + p.x + betweenSpacing + (textBounds.width - p.x)
                 - lineBetweenSpacing;
            y = textBounds.y + (p.y / 2);

        }

        if (x2 > x)
        {
            gc
                .setForeground(getGroup().getDisplay()
                    .getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
            gc.drawLine(x, y, x2, y);

            gc.setForeground(getGroup().getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
            gc.drawLine(x, y + 1, x2, y + 1);
        }

        if (!getGroup().getExpanded())
        {
            gc.setBackground(getGroup().getParent().getBackground());
        }
        else
        {
            // e.gc.setBackground(parent.getParent().getBackground());
        }
        // gc.fillRectangle(0,getTitleHeight(),getGroup().getBounds().width,getGroup().getBounds().height);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isToggleLocation(int x, int y)
    {
        if (super.isToggleLocation(x, y))
            return true;

        if (getGroup().getToggleRenderer() == null)
            return false;

        Rectangle textBounds = getTextBounds();
        textBounds.width = Math.min(textWidth,textBounds.width);
        if (textBounds.contains(x, y))
            return true;

        return false;
    }

    private Rectangle getTextBounds()
    {
        int textX = hMargin;

        if ((getGroup().getImagePosition() & SWT.LEAD) != 0 && (getGroup().getImage() != null))
        {
            textX += getGroup().getImage().getBounds().width + betweenSpacing;
        }
        if ((getGroup().getTogglePosition() & SWT.LEAD) != 0
            && (getGroup().getToggleRenderer() != null))
        {
            textX += getGroup().getToggleRenderer().getSize().x + betweenSpacing;
        }

        @SuppressWarnings("hiding")
		int textWidth = getGroup().getSize().x - textX - hMargin;
        if (!((getGroup().getImagePosition() & SWT.LEAD) != 0) && (getGroup().getImage() != null))
        {
            textWidth -= getGroup().getImage().getBounds().width + betweenSpacing;
        }
        if (!((getGroup().getTogglePosition() & SWT.LEAD) != 0)
            && (getGroup().getToggleRenderer() != null))
        {
            textWidth -= getGroup().getToggleRenderer().getSize().x + betweenSpacing;
        }

        int textY = 0;
        if (getGroup().getLinePosition() == SWT.CENTER)
        {
            textY = (heightWithoutLine - fontHeight) / 2;
        }
        else
        {
            textY = heightWithoutLine - (fontHeight + (titleTextMargin) + (vMargin));
        }

        if (getGroup().getToggleRenderer() != null)
        {
            int toggleHeight = getGroup().getToggleRenderer().getSize().y;
            if (toggleHeight > fontHeight)
            {
                int toggleY = ((heightWithoutLine - toggleHeight) / 2);
                int difference = (toggleHeight - fontHeight) / 2;
                textY = toggleY + difference;
            }
        }

        return new Rectangle(textX, textY, textWidth, fontHeight);
    }

    /**
     * {@inheritDoc}
     */
    public Rectangle getClientArea()
    {
        Rectangle area = getGroup().getBounds();
        area.x = 0;
        area.y = titleHeight;
        area.height -= titleHeight;
        return area;
    }

    /**
     * {@inheritDoc}
     */
    public Rectangle computeTrim(int x, int y, int width, int height)
    {
        Rectangle area = new Rectangle(x, y, width, height);
        area.y -= titleHeight;
        area.height += titleHeight;
        return area;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.swtplus.widgets.IGroupStrategy#dispose()
     */
    public void dispose()
    {
    }

    public void update()
    {
        GC gc = new GC(getGroup());

        titleHeight = 0;
        int imageHeight = 0;

        if (getGroup().getImage() != null)
            imageHeight = getGroup().getImage().getBounds().height;
        titleHeight = Math.max(gc.getFontMetrics().getHeight() + (2 * titleTextMargin)
                               + (2 * vMargin), imageHeight + (2 * vMargin));
        if (getGroup().getToggleRenderer() != null)
        {
            int toggleHeight = getGroup().getToggleRenderer().getSize().y + (2 * vMargin);
            titleHeight = Math.max(toggleHeight + (2 * vMargin), titleHeight);
        }
        heightWithoutLine = titleHeight;
        if (getGroup().getLinePosition() == SWT.BOTTOM)
        {
            titleHeight += separatorHeight;
            titleHeight += (2 * lineMargin);
        }

        fontHeight = gc.getFontMetrics().getHeight();

        textWidth = gc.stringExtent(getGroup().getText()).x;

        gc.dispose();

    }

}
