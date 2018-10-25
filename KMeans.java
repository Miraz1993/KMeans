package HW3;



import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;


public class KMeans {
	public static void main(String [] args){
		if (args.length < 3){
			System.out.println("Usage: Kmeans <input-image> <k> <output-image>");
			return;
		}
		try{
			BufferedImage originalImage = ImageIO.read(new File(args[0]));
			int k=Integer.parseInt(args[1]);
			
			BufferedImage kmeansJpg = kmeans_helper(originalImage,k);
			ImageIO.write(kmeansJpg, "jpg", new File(args[2]));
				
			
			

		}catch(IOException e){
			System.out.println(e.getMessage());
		}	
	}

	private static BufferedImage kmeans_helper(BufferedImage originalImage, int k){
		int w=originalImage.getWidth();
		int h=originalImage.getHeight();
		BufferedImage kmeansImage = new BufferedImage(w,h,originalImage.getType());
		Graphics2D g = kmeansImage.createGraphics();
		g.drawImage(originalImage, 0, 0, w,h , null);
		// Read rgb values from the image
		int[] rgb=new int[w*h];
		int count=0;
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				rgb[count++]=kmeansImage.getRGB(i,j);
				
			}
		}
		// Call kmeans algorithm: update the rgb values
		rgb=kmeans(rgb,k);

		// Write the new rgb values to the image
		count=0;
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				kmeansImage.setRGB(i,j,rgb[count++]);
			}
		}
		return kmeansImage;
	}

	private static Integer findMinDistance(int red, int green,int blue,List<List<Integer>> centroidList){
		Double mindistance=Double.MAX_VALUE;
		Double dist=0.0;
		int i=0,label=0;
		for(List<Integer> list:centroidList) {
			dist=Math.sqrt(Math.pow(red-list.get(0),2)+Math.pow(green-list.get(1),2)+Math.pow(blue-list.get(2),2));
			if(dist<mindistance) {
				mindistance=dist;
				label=i;
			}
			i++;
		}
		return label;
	}

	private static HashMap<Integer,List<Integer>> findLabels(HashMap<Integer, ArrayList<Integer>> map,List<List<Integer>> centroidList){
		HashMap<Integer,List<Integer>> labels=new HashMap<>();
		List<Integer> list;

		for(int i=0;i<map.size();i++) {
			ArrayList<Integer> aList=map.get(i);
			Integer l=findMinDistance(aList.get(0), aList.get(1), aList.get(2), centroidList);
			if(labels.get(l)==null) {
				list=new ArrayList<>();
				list.add(i);
				labels.put(l, list);

			}
			else {
				list=labels.get(l);
				list.add(i);
				labels.put(l, list);
			}
		}
		if(centroidList.size()!=labels.size()) {
			for(int i=0;i<centroidList.size();i++) {
				if(!labels.containsKey(i)) {
					list=new ArrayList<>();
					labels.put(i, list);
				}
			}
		}
		return labels;
	}
	private static List<Integer> findMedian(HashMap<Integer, ArrayList<Integer>> map, List<Integer> list){
		List<Integer> values=new ArrayList<>();
		int rValue=0,gValue=0,bValue=0;
		for(Integer i : list) {
			ArrayList<Integer> aList=map.get(i);
			rValue+=aList.get(0);
			gValue+=aList.get(1);
			bValue+=aList.get(2);

		}
		if(list.size()!=0) {
			values.add(rValue/(list.size()));
			values.add(gValue/(list.size()));
			values.add(bValue/(list.size()));
		}
		else {
			values.add(0);
			values.add(0);
			values.add(0);
		}

		return values;
	}
	// Your k-means code goes here
	// Update the array rgb by assigning each entry in the rgb array to its cluster center
	private static int[] kmeans(int[] rgb, int k){

		HashMap<Integer, ArrayList<Integer>> map=new HashMap<>();
		
		Color mycolor;
		ArrayList<Integer> aList;
		for(int i=0;i<rgb.length;i++) {
			mycolor = new Color(rgb[i]);
			aList=new ArrayList<>();
			aList.add(mycolor.getRed());
			aList.add(mycolor.getGreen());
			aList.add(mycolor.getBlue());

			map.put(i, aList);
		}
		
		List<List<Integer>> centroidList=new ArrayList<>();
		List<List<Integer>> cList=new ArrayList<>();
		Set<Integer> centroids=new HashSet<>();
		
		Random rand=new Random();
		while(centroids.size()<=k) {
			int n=rand.nextInt(map.size());
			if(!centroids.contains(n)) {
				centroids.add(n);
				List<Integer> list=new ArrayList<>();
				aList=map.get(n);
				list.add(aList.get(0));
				list.add(aList.get(1));
				list.add(aList.get(2));
				centroidList.add(list);

			}


		}
		
		HashMap<Integer,List<Integer>> labels=new HashMap<Integer,List<Integer>>();
		int r=0;
		while(true) {
			labels=findLabels(map, centroidList);
			cList=new ArrayList<>();
			for(int j=0;j<labels.size();j++) {

				List<Integer> values=findMedian(map, labels.get(j));
				cList.add(values);


			}
			Boolean bool=true;
			for(Integer m=0;m<cList.size();m++) {
				List<Integer> l1=centroidList.get(m);
				List<Integer> l2=cList.get(m);
				
				if(Math.abs(l1.get(0)-l2.get(0))>2 || Math.abs(l1.get(1)-l2.get(1))>2 || Math.abs(l1.get(1)-l2.get(1))>2) {
					bool=false;
					break;
				}
			}
			if(bool)
				break;
			centroidList=cList;
			
			r++;


		}
		
		for(int i=0;i<labels.size();i++) {
			List<Integer> pixels=labels.get(i);
			List<Integer> list=centroidList.get(i);
			for(Integer in:pixels) {
				aList=map.get(in);
				aList.set(0, list.get(0));
				aList.set(1, list.get(1));
				aList.set(2, list.get(2));
			}
		}
		int[] rGB=new int[rgb.length];
		for(int i=0;i<map.size();i++) {
			aList=map.get(i);
			mycolor=new Color(aList.get(0),aList.get(1),aList.get(2));
			int temp=mycolor.getRGB();
			rGB[i]=temp;
		}
		return rGB;

	}

}
