import java.util.ArrayList;
import java.util.Iterator;

public class CollectionDemo {

	public static void main(String[] args)
	{
		ArrayList al=new ArrayList();
		System.out.println(al);
		
		al.add(10);
		al.add(69);
		al.add("koolboi");
		
		System.out.println(al);
		
		//String type array list
		ArrayList<String> a2=new ArrayList<String>();
		a2.add("Why you Bully Me?");
		System.out.println(a2);
		
		
		ArrayList<Integer> a3=new ArrayList<Integer>();
		a3.add(1);
		a3.add(69);
		System.out.println(a3);
		//Converting arraylist to array
		Integer ia[]=new Integer[a3.size()];
		ia=a3.toArray(ia);
		
		System.out.println("Array Content");
		for(int i=0;i<ia.length;i++)
		{
			System.out.println(ia[i]);
		}
		//sum
		int sum=0;
		for(int i : ia)
			sum+=i;
		System.out.println("Sum is "+sum);
		//Student objects
		//Create a ArrayList of user defined class to collect thhe student info (rno,name,age) of 
		//atleast 3 students,add the student objects to the Arraylist and process the ArrayList content using iterator interface
		Student s1=new Student(69,"Gajodhar",420);
		Student s2=new Student(13,"Gajju",434);
		//ArrayList of student type
		ArrayList<Student> a69=new ArrayList<Student>();
		a69.add(s1);
		a69.add(s2);
		System.out.println(a69);//displays the addresses
		
		Iterator itr=a69.iterator();//use to retrieve the values  in the collection list 1 by 1
		while(itr.hasNext())
		{
			Student st=(Student)itr.next();
			System.out.println(st.rno+" Name "+st.name+" Age: "+st.age);
		}
	}
}
class Student
{
	int rno;
	String name;
	int age;
	
	Student(int rno,String name,int age)
	{
		this.rno=rno;
		this.name=name;
		this.age=age;
	}
}
