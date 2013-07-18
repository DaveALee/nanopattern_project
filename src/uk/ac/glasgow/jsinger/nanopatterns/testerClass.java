package src.uk.ac.glasgow.jsinger.nanopatterns;

public class testerClass {

	public void buildArray ()
	{
		int [] array = new int[10];
		for (int i = 0; i < 10; i++) {
			array[i] = i * i;
		}
	}

	
	public int fib(int x)
	{
		if (x<=1)
			return 1;
		else
			return fib (x-1) +fib (x-2);
	}
	
	public int calculate (int a, int b, String op)
	{
		return a + b;
	
	}
}
