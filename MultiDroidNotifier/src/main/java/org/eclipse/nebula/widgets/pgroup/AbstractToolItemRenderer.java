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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Base implementation for rendering ToolItems in the header
 */
public abstract class AbstractToolItemRenderer extends AbstractRenderer {
	private int sizeType;

	/**
	 * Defines that item should be rendered in its default way:
	 * <ul>
	 * <li>Icon+Text if both defined</li>
	 * <li>Icon if Text is <code>null</code></li>
	 * <li>Text if Icon is <code>null</code></li>
	 * </ul>
	 */
	public static final int DEFAULT = 1;

	/**
	 * Defines that item should be rendered in its minimal way:
	 * <ul>
	 * <li>Icon only if both defined</li>
	 * <li>Icon if Text is <code>null</code></li>
	 * <li>Text if Icon is <code>null</code></li>
	 * </ul>
	 */
	public static final int MIN = 2;

	/**
	 * Set the size type
	 *
	 * @param sizeType
	 *            the sizeType
	 * @see #DEFAULT
	 * @see #MIN
	 */
	public void setSizeType(int sizeType) {
		this.sizeType = sizeType;
	}

	/**
	 * Get the size type
	 *
	 * @return the current size type
	 */
	public int getSizeType() {
		return sizeType;
	}

	public abstract void paint(GC gc, Object value);

	/**
	 * Computes the size needed for the toolitem
	 *
	 * @param gc
	 *            the gc
	 * @param item
	 *            the toolitem
	 * @param sizeType
	 *            size type
	 * @return the computed size for the toolitem and sizeType
	 * @see #DEFAULT
	 * @see #MIN
	 */
	public abstract Point computeSize(GC gc, PGroupToolItem item, @SuppressWarnings("hiding") int sizeType);

	/**
	 * Computes the area where the DropDown-Icon is shown in case of
	 * {@link SWT#DROP_DOWN}
	 *
	 * @param totalRect
	 *            the total area the item is drawn
	 * @return
	 */
	public abstract Rectangle computeDropDownArea(Rectangle totalRect);
}
