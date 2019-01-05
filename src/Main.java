import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
public class Main {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Quantizer q = new Quantizer(); 
		Main ma = new Main();
		int[][]m = ma.readImage("H:\\Ass5_multimedia\\src\\original.jpg");
		
		System.out.println(m.length);
		System.out.println(m[0].length);
		int height;
		int width;
		int size;
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter height: ");
		height = sc.nextInt();
		System.out.print("Enter width: ");
		width = sc.nextInt();
		System.out.print("Enter size: ");
		size = sc.nextInt();
		
		q.buidQuantizer("H:\\Ass5_multimedia\\src\\original.jpg", "H:\\Ass5_multimedia\\src\\quantizer.txt", height, width, size);
		q.encode("H:\\Ass5_multimedia\\src\\original.jpg", "H:\\Ass5_multimedia\\src\\quantizer.txt", "H:\\Ass5_multimedia\\src\\quantized.txt", height, width);
		q.decompress("H:\\Ass5_multimedia\\src\\original.jpg", "H:\\Ass5_multimedia\\src\\quantized.txt", "H:\\Ass5_multimedia\\src\\quantizer.txt", "H:\\Ass5_multimedia\\src\\reconstructed.jpg", height, width);
	}
		int[][] readImage(String filePath){
		
		File f = new File(filePath); //image file path
		
		int[][] imageMAtrix=null;
		
		try {
			BufferedImage img= ImageIO.read(f);
			int width = img.getWidth(); 
	        int height = img.getHeight(); 
	  
	        imageMAtrix=new int[height][width];
	        
	        for (int y = 0; y < height; y++) 
	        { 
	            for (int x = 0; x < width; x++) 
	            { 
	                int p = img.getRGB(x,y); 
	                int a = (p>>24)&0xff; 
	                int r = (p>>16)&0xff; 
	                int g = (p>>8)&0xff; 
	                int b = p&0xff; 
	  
	                //because in gray image r=g=b  we will select r  
	               
	                imageMAtrix[y][x]=r;
	                
	                //set new RGB value 
	                p = (a<<24) | (r<<16) | (g<<8) | b; 
	                img.setRGB(x, y, p); 
	            } 
	        } 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return imageMAtrix;
	}

}
