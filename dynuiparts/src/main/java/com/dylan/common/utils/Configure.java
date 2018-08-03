/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package com.dylan.common.utils;

import java.io.*;

public class Configure {

	public interface Configurable {
		public void parseLine(String line);
	}

	public static String NONE = "NONE";
	
	public Configure(Configurable configurable, String file) {
		this.configurable = configurable;
		loadFile(file);
	}
	
	Configurable configurable;
	protected void parseLine(String line) {
	}
	protected String toLines() {
		return "";
	}
	protected Configure() {
		this.configurable = null;
	}
	protected void loadFile(String file) { 
		if (file == null) {
			return;
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			while (true) {
				String line = null;
				try {
					line = in.readLine();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
				if (line == null)
					break;

				if (!line.startsWith("#")) {
					if (configurable == null)
						parseLine(line);
					else
						configurable.parseLine(line);
				}
			}
			in.close();
		} catch (Exception e) {
			System.err.println("WARNING: error reading file \"" + file + "\"");
			return;
		}
	}

	protected void saveFile(String file) {
		if (file == null)
			return;
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write(toLines());
			out.close();
		} catch (IOException e) {
			System.err.println("ERROR writing on file \"" + file + "\"");
		}
	}

}
