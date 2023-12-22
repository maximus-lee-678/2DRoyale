package structure;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Building {

	private Rectangle boundingBox;
	private List<int[]> buildingTileNum;

	public Building(String filePath, int buildingTileSize) {

		this.buildingTileNum = new ArrayList<int[]>();
		
		int col = 0, row = 0;
		try {
			InputStream is = getClass().getResourceAsStream(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = br.readLine()) != null) {
				String lineString[] = line.split(" ");
				col = lineString.length;
				int lineNums[] = new int[col];

				for (int i = 0; i < col; i++) {
					lineNums[i] = Integer.parseInt(lineString[i]);
				}
				buildingTileNum.add(lineNums);
				row++;
			}

			br.close();

		} catch (Exception e) {
			System.out.println(filePath);
			e.printStackTrace();
		}

		this.boundingBox = new Rectangle(0, 0, col * buildingTileSize, row * buildingTileSize);
	}

	public Rectangle getBoundingBox() {
		return boundingBox;
	}

	public List<int[]> getBuildingTileNum() {
		return buildingTileNum;
	}

	
}