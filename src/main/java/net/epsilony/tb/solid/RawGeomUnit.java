/*
 * Copyright (C) 2013 Man YUAN <epsilon@epsilony.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.epsilony.tb.solid;

/**
 *
 * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
 */
public abstract class RawGeomUnit implements GeomUnit {

    protected int id;
    protected GeomUnit parent;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public GeomUnit getParent() {
        return parent;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void setParent(GeomUnit parent) {
        this.parent = parent;
    }
}
