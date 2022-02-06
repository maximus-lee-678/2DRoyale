package object;

import java.io.IOException;

import javax.imageio.ImageIO;

public class OBJ_Health extends SuperObject{
	

	public OBJ_Health() {
		
		name = "Health";
		try {
			image = ImageIO.read(getClass().getResourceAsStream("/UI/HP.png"));
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	

}
