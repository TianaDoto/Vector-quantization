
public class VectorI {
	int height;
	int width;
	double[][] data;
	
	VectorI()
	{
	}
	
	VectorI(int h, int w)
	{
		this.height = h;
		this.width = w;
		this.data = new double [height][width];
	}
	void setData(double[][]d)
	{
		this.data = d;
	}
	void setHeight(int h)
	{
		this.height = h;
	}
	void setWidth(int w)
	{
		this.width = w;
	}
}
