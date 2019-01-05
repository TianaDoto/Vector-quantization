import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Quantizer {
	private ArrayList<VectorI>data; //= new ArrayList<VectorI>();
	private VectorI average;// = new VectorI();
	private Quantizer leftData;
	private Quantizer rightData;

	Quantizer ()
	{
		data = new ArrayList<VectorI>();
		average = new VectorI();
		leftData = null;
		rightData = null;
	}

    public static int[][] readImage(String filePath)
   {
	    int width=0;
		int height=0;
       File file=new File(filePath);
       BufferedImage image=null;
       try
       {
           image=ImageIO.read(file);
       }
       catch (IOException e)
       {
           e.printStackTrace();
       }

         width=image.getWidth();
         height=image.getHeight();
       int[][] pixels=new int[height][width];

       for(int x=0;x<width;x++)
       {
           for(int y=0;y<height;y++)
           {
               int rgb=image.getRGB(x, y);
               int alpha=(rgb >> 24) & 0xff;
               int r = (rgb >> 16) & 0xff;
               int g = (rgb >> 8) & 0xff;
               int b = (rgb >> 0) & 0xff;

               pixels[y][x]=r;
           }
       }
       return pixels;
   }
    
    public static void writeImage(int[][] pixels,String outputFilePath,int width,int height)
    {
        File fileout=new File(outputFilePath);
        BufferedImage image2=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB );

        for(int x=0;x<width ;x++)
        {
            for(int y=0;y<height;y++)
            {
                image2.setRGB(x,y,(pixels[y][x]<<16)|(pixels[y][x]<<8)|(pixels[y][x]));
            }
        }
        try
        {
            ImageIO.write(image2, "jpg", fileout);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
	
	private ArrayList<VectorI> divideImage(int[][] image, int vectorH, int vectorW)
	{
		int width = image.length;
		int height = image[0].length;
		ArrayList<VectorI> vectors = new ArrayList<VectorI>();
		
		VectorI temp = new VectorI(vectorH, vectorW);
		
		for(int i=0; i<width; i += vectorW)
		{
			for(int j=0; j<height; j += vectorH)
			{
				int e = i; 
				int r = j;
				
				temp = new VectorI(vectorH, vectorW);
				
				for(int a = 0; a < vectorH; a++)
				{
					for(int b = 0; b < vectorW; b++)
					{
						temp.data[a][b] = image[e][r++];
					}
					e++;
					r = j;
				}
				vectors.add(temp);
			}
		}
		return vectors;
	}

	private double[][] getAverage(ArrayList<VectorI> vector)
	{
		int h = vector.get(0).height;
		int w = vector.get(0).width;
		
		VectorI average = new VectorI(h, w);
		for(int i=0; i<h; i++)
		{
			for(int j=0; j<w; j++)
			{
				double sum = 0;
				
				for(int a=0; a<vector.size(); a++)
				{
					sum += vector.get(a).data[i][j];
				}
				sum /= vector.size();
				average.data[i][j] = sum;
			}
		}
		return average.data;		
	}
	
	private VectorI split(double[][] v, int c)
	{
		int height = v.length;
		int width = v[0].length;
		VectorI temp = new VectorI(height, width);
		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				temp.data[i][j] = v[i][j] + c;
			}
		}
		return temp;	
	}
	
	private void showVector(VectorI avg, int vectorH, int vectorW)
	{
		for(int i = 0; i < vectorH; i++)
		{
			System.out.println();
			for(int j = 0; j < vectorW; j++)
			{
				System.out.print(avg.data[i][j] + " ");
			}
			System.out.println();
		}
	}
		
	private int distance(VectorI x, VectorI avg)
	{
		int height = x.data.length;
		int width = x.data[0].length;
		int d = 0;
		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				d += Math.abs(x.data[i][j] - avg.data[i][j]);
			}
		}
		return d;
	}

	private Quantizer step1_step2(String filePath, int vectorH, int vectorW)
	{
		//step1
		int[][] originalImage = readImage(filePath);
		Quantizer q = new Quantizer();
		q.data = divideImage(originalImage, vectorH, vectorW);
		
		//step2
		q.average = new VectorI(vectorH, vectorW);
		q.average.data = getAverage(q.data);
		return q;
	}
	
	private void codeBook(Quantizer q, int size, ArrayList<VectorI> avg, int vectorH, int vectorW)
	{	
		if(avg.size() == size) 
		{				
			return;
		}
		else
		{
			q.leftData = new Quantizer();
			q.rightData = new Quantizer();
			
			
			//step3
			VectorI left = split(q.average.data, -1);
			VectorI right = split(q.average.data, 1);
			
			//step4
			for(int i = 0; i < q.data.size(); i++)
			{
				int a, b;
				a = distance(q.data.get(i), left);
				b = distance(q.data.get(i), right);
				if(a < b)
				{	
					q.leftData.data.add(q.data.get(i));
				}
				else
				{
					q.rightData.data.add(q.data.get(i));
				}
			}
			
			//step5
			q.rightData.average.data = getAverage(q.rightData.data);
			q.leftData.average.data = getAverage(q.leftData.data);
			
			avg.add(q.rightData.average);
			avg.add(q.leftData.average);
			
			
			//step6
			codeBook(q.leftData, size, avg, vectorH, vectorW);
			codeBook(q.rightData, size, avg, vectorH, vectorW);			
	
		}
	}
		
	public void buidQuantizer(String filePath, String quantizerPath, int vectorH, int vectorW, int size) throws IOException
	{
		Quantizer q = new Quantizer();
		Quantizer[] dummy = new Quantizer[1];
		ArrayList<VectorI> avg = new ArrayList<VectorI>();
		q = step1_step2(filePath, vectorH, vectorW);
		dummy[0] = q;
		codeBook(dummy[0], size, avg, vectorH, vectorW);
		q = dummy[0];
		
		/*for(int i = 0; i < avg.size(); i++)
		{
			showVector(avg.get(i), vectorH, vectorW);
			System.out.println("--------------------------------");
		}*/
		//step7
		ArrayList<VectorI>newAverage = new ArrayList<VectorI>();
		ArrayList<ArrayList<VectorI>> newData = new ArrayList<ArrayList<VectorI>>();
		
		//initialize newData
		for(int i = 0; i < size; i++)
		{
			ArrayList<VectorI> inner = new ArrayList<VectorI>();
			newData.add(inner);
		}	
		
		for(int i = 0; i < q.data.size(); i++)
		{
			ArrayList<Integer> d = new ArrayList<Integer>();
			for(int j = 0; j < avg.size(); j++)
			{
				d.add(distance(q.data.get(i), avg.get(j)));
				//newData.add(inner = new ArrayList<VectorI>());
			}
			//redistribute vector
			int min = minimal(d);
			int index = getIndex(d, min);
			newData.get(index).add(q.data.get(i));	
		}
		
		for(int i = 0; i < newData.size(); i++)
		{
			VectorI temp = new VectorI();
			temp.data = getAverage(newData.get(i));
			newAverage.add(temp);
		}
		/*System.out.println(newData.size());
		System.out.println("-----------");
		System.out.println("NEW AVERAGE");
		System.out.println("-----------");*/
		
		//step8
		
		Tree t = new Tree();
		t.build(t.root, size/2);
		ArrayList<String> arr = new ArrayList<String>(); // arr containing the binary
		t.buildCode(arr, t.root, "");
		
		FileWriter fileWriter = new FileWriter(quantizerPath);
		
		for(int i = 0; i < newAverage.size(); i++)
		{
			fileWriter.write(arr.get(i));
			fileWriter.write(" ");
			for(int j = 0; j < vectorH; j++)
			{
				for(int k = 0; k < vectorW; k++)
				{
					fileWriter.write(Double.toString(newAverage.get(i).data[j][k]));
					fileWriter.write(" ");
				}
				
			}
			//showVector(newAverage.get(i), vectorH, vectorW);
			//System.out.println("--------------------------------");
		}
		fileWriter.close();
	}
	
	private int minimal(ArrayList<Integer> arr)
	{
		int min = arr.get(0);
		for(int i = 1; i< arr.size(); i++)
		{
			if(arr.get(i)<min)
				min = arr.get(i);
		}
		return min;
	}

	private int getIndex(ArrayList<Integer> arr, int n)
	{
		int index = 0;
		for(int i = 0; i< arr.size(); i++)
		{
			if(arr.get(i) == n)
				index = i;
		}
		return index;
	}
	
	private double truncate(double x)
	{
		double truncated = BigDecimal.valueOf(x).setScale(3, RoundingMode.HALF_UP).doubleValue();
		return truncated;
	}
	
	public void encode(String imagePath, String quantizerPath, String destination, int height, int width) throws IOException
	{
		int[][] original = readImage(imagePath); //originalImage
		ArrayList<VectorI> image = new ArrayList<VectorI>();
		ArrayList<VectorI> avg = new ArrayList<VectorI>(); //list of average
		ArrayList<String> code = new ArrayList<String>(); //list of code
		Scanner sc = new Scanner(new File(quantizerPath));
		ArrayList<String> quantized = new ArrayList<String>();
		
		image = divideImage(original, height, width);
		sc.useDelimiter(" ");
		//System.out.println();
		loadQuantizer(sc, code, avg, height, width);
				
		for(int i = 0; i < image.size(); i++)
		{
			ArrayList<Integer> d = new ArrayList<Integer>();
			for(int j = 0; j < avg.size(); j++)
			{
				d.add(distance(image.get(i), avg.get(j)));
				
			}
			int min = minimal(d);
			int index = getIndex(d, min);
			quantized.add(code.get(index));
		}
		
		FileWriter fileWriter = new FileWriter(destination);
		for(int i = 0; i < quantized.size(); i++)
		{
			fileWriter.write(quantized.get(i));
			fileWriter.write(" ");
		}
		sc.close();
		fileWriter.close();
	}

	private void loadQuantizer(Scanner sc, ArrayList<String> code, ArrayList<VectorI> avg, int height, int width) throws FileNotFoundException
	{
		while(sc.hasNext())
		{
			String temp = new String();
			temp = sc.next();
			code.add(temp);
			
			double[][] arrTemp = new double[height][width];
			
			for(int i = 0; i < height; i++)
			{
				for(int j = 0; j < width; j++)
				{
					arrTemp[i][j] = Double.parseDouble(sc.next());	
				}
			}
			VectorI tempVect = new VectorI();
			tempVect.data = arrTemp;
			avg.add(tempVect);							
		}
		sc.close();
	}
	
	public void decompress(String originalPath, String quantizedPath, String quantizerPath, String destination, int height, int width) throws FileNotFoundException
	{
		Scanner sc = new Scanner(new File(quantizedPath));
		ArrayList<String> quantized = new ArrayList<String>();
		ArrayList<VectorI> avg = new ArrayList<VectorI>(); //list of average
		ArrayList<String> code = new ArrayList<String>(); //list of code
		ArrayList<VectorI> decoded = new ArrayList<VectorI>();
		
		sc.useDelimiter(" ");
		
		while(sc.hasNext())
		{
			quantized.add(sc.next());
		}
		sc.close();
		
		sc = new Scanner(new File(quantizerPath));
		loadQuantizer(sc, code, avg, height, width);

		for(int i = 0; i < quantized.size(); i++)
		{
			for(int j = 0; j < code.size(); j++)
			{
				if(quantized.get(i).equals(code.get(j)))
				{
					decoded.add(avg.get(j));
				}
			}
			
		}
		int[][] temp = readImage(originalPath);
		int h = temp.length;
		int w = temp[0].length;
		int[][] reconstructed = new int[h][w];
		ArrayList<Double> temp1 = new ArrayList<Double>();
		
		for(int k = 0; k < decoded.size(); k++)
		{
			for(int l = 0; l < height; l++)
			{
				for(int m = 0; m < width; m++)
				{
					temp1.add(decoded.get(k).data[l][m]);
				}
			}
		}
		int index = 0;
		for(int i = 0; i < h; i++)
		{
			for(int j = 0; j < w; j++)
			{
				reconstructed[i][j] = temp1.get(index++).intValue();
			}
		}
		writeImage(reconstructed, destination, w, h);
	}
}
