package structure;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List; 

public class Building {

	public Rectangle boundingBox;
	public List<int[]> buildingTileNum;

	public Building(String filePath, int buildingTileSize) {

		this.buildingTileNum = new ArrayList<int[]>();
		int col = 0;
		int row = 0;

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
//			for (int x = 0; x < row; x++) {
//				for (int y = 0; y < col; y++) {
//					System.out.print(buildingTileNum.get(x)[y]);
//				}
//				System.out.println();
//			}

			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		this.boundingBox = new Rectangle(0, 0, col * buildingTileSize, row * buildingTileSize);
	}

}