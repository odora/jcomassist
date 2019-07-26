package cn.simple.jcom.assist;

import com.jgoodies.forms.layout.CellConstraints;

public class CC {
	public static CellConstraints xy(int gridX, int gridY) {
		return new CellConstraints(gridX, gridY);
	}

	public static CellConstraints xywh(int gridX, int gridY, int gridWidth, int gridHeight) {
		return new CellConstraints(gridX, gridY, gridWidth, gridHeight);
	}
}
