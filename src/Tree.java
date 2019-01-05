import java.util.ArrayList;


public class Tree {
	Node root;
	Tree()
	{
		root = new Node();
	}
	
	void build(Node n, int size)
	{
		if(size == 0)
			return;
		else
		{
			n.left = new Node();
			n.right = new Node();
			
			n.left.c ='0';
			n.right.c = '1';
			size--;
			build(n.left, size);
			build(n.right, size);
		}
	}
	
	public void buildCode(ArrayList<String> arr, Node t, String s)
	{
		
		if(!t.checkLeaf())
		{
			buildCode(arr, t.left, s + '0');
			buildCode(arr, t.right, s + '1');
		}
		else
		{
			arr.add(s);
			
		}
	}
}
