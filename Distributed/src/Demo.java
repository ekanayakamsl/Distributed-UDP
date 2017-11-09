import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Demo {
	public static void main(String args[]) {
		ArrayList<String> files = new ArrayList<String>();
		
		
		try {
			FileInputStream fstream = new FileInputStream("E:/fileNames.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String line;
			
			while ((line = br.readLine()) != null) {
		       files.add(line);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		ArrayList<Node> nodes= new ArrayList<Node>();
		
		for(int j=0; j<20; j+=2){
			ArrayList<String> fileList = new ArrayList<String>();
			for(int i = 0; i<3; i++){
				Random random = new Random();
				int rand = random.nextInt(10);
				//if(!fileList.contains(files.get(rand)))
					fileList.add(files.get(rand));
			}
			
			Node node = new Node("localhost", 300+j+1, "Aztec"+j, fileList, 55555, "localhost");	
			nodes.add(node);
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Scanner input = new Scanner(System.in);
		System.out.println("Enter your search(ex: search_query node_number)(windows xp 1) : ");
		String inputStr = input.nextLine();
		String[] words = inputStr.split(" ");
		
		String query = "";
		for(int i=0; i<words.length-1; i++){
			query += (words[i]+"_");
		}
		
		System.out.println(query);
		
		nodes.get(Integer.parseInt(words[words.length-1])).search(query);
		
		
	}

}
