package net.kolls.railworld.sound;

/*
 * Copyright (C) 2010 Steve Kollmansberger
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */


import net.kolls.railworld.Sound;

/**
 * Provides an empty interface that pretends to play sounds, but does nothing.
 * This is useful if for some reason the sound systems all causing trouble.
 * This way we don't have to extend anything to have a "sound off" option.
 * 
 * @author Steve Kollmansberger
 *
 */
public class NoSound extends Sound {

	@Override
	public boolean canPlay() {
		return true;
	}

	@Override
	public void loop() { }

	@Override
	public void play() { }

	@Override
	public boolean playing() {
		return false;
	}

	@Override
	public void stop() { }

}
