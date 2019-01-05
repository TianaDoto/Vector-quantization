
public class Node {
	Node left, right;
	char c;
	
	Node(){}
	Node(char c, Node l, Node r)
	{
		this.c = c;
		this.left = l;
		this.right = r;
	}
	boolean checkLeaf()
	{
		return (left == null && right == null);
	}
}
