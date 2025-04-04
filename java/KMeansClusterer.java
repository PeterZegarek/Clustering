import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;


/**
 * KMeansClusterer.java - a JUnit-testable interface for the Model AI Assignments k-Means Clustering exercises.
 * @author Todd W. Neller
 */
public class KMeansClusterer {
	private int dim; // the number of dimensions in the data
	private int k, kMin, kMax; // the allowable range of the of clusters
	private int iter; // the number of k-Means Clustering iterations per k
	private double[][] data; // the data vectors for clustering
	private double[][] centroids; // the cluster centroids
	private int[] clusters; // assigned clusters for each data point
	private Random random = new Random();


	/**
	 * Read the specified data input format from the given file and return a double[][] with each row being a data point and each column being a dimension of the data.
	 * @param filename the data input source file
	 * @return a double[][] with each row being a data point and each column being a dimension of the data
	 */
	public double[][] readData(String filename) {
		int numPoints = 0;
		
		try{
			Scanner in = new Scanner(new File(filename));
			try {
				dim = Integer.parseInt(in.nextLine().split(" ")[1]);
				numPoints = Integer.parseInt(in.nextLine().split(" ")[1]);
			}
			catch (Exception e) {
				System.err.println("Invalid data file format. Exiting.");
				e.printStackTrace();
				System.exit(1);
			}
			double[][] data = new double[numPoints][dim];
			for (int i = 0; i < numPoints; i++) {
				String line = in.nextLine();
				Scanner lineIn = new Scanner(line);
				for (int j = 0; j < dim; j++)
					data[i][j] = lineIn.nextDouble();
				lineIn.close();
			}
			in.close();
			return data;
		}
		catch (FileNotFoundException e){
			System.err.println("Could not locate source file. Exiting.");
			e.printStackTrace();
			System.exit(1);
		}		
		return null;
	}

	/**
	 * Set the given data as the clustering data as a double[][] with each row being a data point and each column being a dimension of the data.
	 * @param data the given clustering data
	 */
	public void setData(double[][] data) {
		this.data = data;		
		this.dim = data[0].length;
	}

	/**
	 * Return the clustering data as a double[][] with each row being a data point and each column being a dimension of the data.
	 * @return the clustering data
	 */
	public double[][] getData() {
		return data;
	}

	/**
	 * Return the number of dimensions of the clustering data.
	 * @return the number of dimensions of the clustering data
	 */
	public int getDim() {
		return dim;
	}

	/**
	 * Set the minimum and maximum allowable number of clusters k.  If a single given k is to be used, then kMin == kMax.  If kMin &lt; kMax, then all k from kMin to kMax inclusive will be
	 * compared using the gap statistic.  The minimum WCSS run of the k with the maximum gap will be the result.
	 * @param kMin minimum number of clusters
	 * @param kMax maximum number of clusters
	 */
	public void setKRange(int kMin, int kMax) {
		this.kMin = kMin; 
		this.kMax = kMax;
		this.k = kMin;
	}
	
	/**
	 * Return the number of clusters k.  After calling kMeansCluster() with a range from kMin to kMax, this value will be the k yielding the maximum gap statistic.
	 * @return the number of clusters k.
	 */
	public int getK() {
		return k;
	}
	
	/**
	 * Set the number of iterations to perform k-Means Clustering and choose the minimum WCSS result.
	 * @param iter the number of iterations to perform k-Means Clustering
	 */
	public void setIter(int iter) {
		this.iter = iter;	
	}

	/**
	 * Return the array of centroids indexed by cluster number and centroid dimension.
	 * @return the array of centroids indexed by cluster number and centroid dimension.
	 */
	public double[][] getCentroids() {
		return centroids;
	}

	/**
	 * Return a parallel array of cluster assignments such that data[i] belongs to the cluster clusters[i] with centroid centroids[clusters[i]].
	 * @return a parallel array of cluster assignments
	 */
	public int[] getClusters() {
		return clusters;
	}

	/**
	 * Return the Euclidean distance between the two given point vectors.
	 * @param p1 point vector 1
	 * @param p2 point vector 2
	 * @return the Euclidean distance between the two given point vectors
	 */
	private double getDistance(double[] p1, double[] p2) {
		double sumOfSquareDiffs = 0;
		for (int i = 0; i < p1.length; i++) {
			double diff = p1[i] - p2[i];
			sumOfSquareDiffs += diff * diff;
		}
		return Math.sqrt(sumOfSquareDiffs);
	}
	
	/**
	 * Return the minimum Within-Clusters Sum-of-Squares measure for the chosen k number of clusters.
	 * @return the minimum Within-Clusters Sum-of-Squares measure
	 */
	public double getWCSS() {
		double totalWCSS = 0.0;
		
		// creates an array to store wcss (within cluster sum of squares) for each cluster
		double[] clusterWCSS = new double[k];
		
		// iterate over each data point
		for (int i = 0; i < data.length; i++) {
			int clusterIndex = clusters[i]; 
			double[] centroid = centroids[clusterIndex]; 
			
			// compute the squared distance to the centroid
			double squaredDistance = 0.0;
			for (int j = 0; j < dim; j++) {
				double diff = data[i][j] - centroid[j];
				squaredDistance += diff * diff;
			}
			
			// add squared distance to the corresponding cluster's wcss
			clusterWCSS[clusterIndex] += squaredDistance;
		}
		
		// sum up wcss values of all clusters to get the total wcss
		for (double wcss : clusterWCSS) {
			totalWCSS += wcss;
		}
		
		return totalWCSS;
	}
	

	/**
	 * Assign each data point to the nearest centroid and return whether or not any cluster assignments changed.
	 * @return whether or not any cluster assignments changed
	 */
	public boolean assignNewClusters() {
		boolean changesMade = false;
		for(int i = 0; i < data.length; i++) {
			// the original assigned cluster for this point
			int originalCluster = clusters[i];

			// check the straight line distance to each centroid
			double shortestDistance = Double.MAX_VALUE;
			for(int j = 0; j < centroids.length; j++) {
				double distance = getDistance(data[i], centroids[j]);
				// if the shortest, update info
				if (distance < shortestDistance) {
					shortestDistance = distance;
					clusters[i] = j;
				}
			}
			// if these are not the same value, we made changes
			if (originalCluster != clusters[i]) {
				changesMade = true;
			}
		}
		return changesMade;
	}
	
	/**
	 * Compute new centroids at the mean point of each cluster of points.
	 */
	public void computeNewCentroids() 
	{
		double[][] newCentroids = new double[k][dim]; //2D array that will hold new centroids
		int[] clusterSizes = new int[k]; //array of the sizes of each cluster
		
		//goes through each data point, finds its assigned cluster, and increments that cluster
		for(int dataIndex = 0; dataIndex < data.length; dataIndex++)
		{
			int clusterIndex = clusters[dataIndex];
			clusterSizes[clusterIndex] += 1;
			
			//adds each data point to the centroid that it matches
			for(int dimensionIndex = 0; dimensionIndex < dim; dimensionIndex++)
			{
				newCentroids[clusterIndex][dimensionIndex] += data[dataIndex][dimensionIndex];
			}
		}

		//calculates each cluster's mean 
		for(int clusterIndex = 0; clusterIndex < k; clusterIndex++)
		{
			if (clusterSizes[clusterIndex] > 0)
			{
				for(int dimensionIndex = 0; dimensionIndex < dim; dimensionIndex++)
				{
					newCentroids[clusterIndex][dimensionIndex] /= clusterSizes[clusterIndex];
				}
			}
		}
		centroids = newCentroids; //replaces old centroids with new ones
	}

	
	/**
	 * Perform k-means clustering with Forgy initialization and return the 0-based cluster assignments for corresponding data points.
	 * If iter > 1, choose the clustering that minimizes the WCSS measure.
	 * If kMin > kMax, select the k maximizing the gap statistic using 100 uniform samples uniformly across given data ranges.
	 */
	public void kMeansCluster() {
		//Pick k clusters from data; Forgy Initialization is random datapoints
		int[] points = new int[k]; //Makes sure multiple clusters don't have the same center
		Arrays.fill(points, -1); // Initialize with -1 to avoid false collisions
		int clusterIndex = 0; //Data point location for center
		// init arrays
		centroids = new double[k][2];
		clusters = new int[data.length];

		for(int i = 0; i < k; i++){ //Get k centers
			boolean goodToGo = false; //Variable to ensure program doesn't move unless center is valid
			while(!goodToGo){
				goodToGo = true; //Assume center is valid
				clusterIndex = random.nextInt(0, data.length); //Get random point of data
				//Check to see if center has been already used 
				for(int point : points){
					if(clusterIndex == point){
						goodToGo = false; //Need new center
					}
				}
			}
			//If reached here, center hasn't been used
			points[i] = clusterIndex;
			//Assign data at random point to be a centroid
			centroids[i][0] = data[clusterIndex][0];
			centroids[i][1] = data[clusterIndex][1];
		}
		boolean converged = false; //Assume not converged
		do{
			//Assign each point to its closest center
			if(assignNewClusters()){
				//If true, assignments changed
				//Recompute centers by averaging clustered points
				computeNewCentroids();
			} else {
				//Assignments didn't change, no need to recompute centers
				converged = true; //If assignments didn't change, the clusters should be converged
			}
		}while(!converged); //Check should be at the end
	}
	
	/**
	 * Export cluster data in the given data output format to the file provided.
	 * @param filename the destination file
	 */
	public void writeClusterData(String filename) {
		try{
			FileWriter out = new FileWriter(filename);
			
			out.write(String.format("%% %d dimensions\n", dim));
			out.write(String.format("%% %d points\n", data.length));
			out.write(String.format("%% %d clusters/centroids\n", k));
			out.write(String.format("%% %f within-cluster sum of squares\n", getWCSS()));
			for (int i = 0; i < k; i++) {
				out.write(i + " ");
				for (int j = 0; j < dim; j++)
					out.write(centroids[i][j] + (j < dim - 1 ? " " : "\n"));	
			}
			for (int i = 0; i < data.length; i++) {
				out.write(clusters[i] + " ");
				for (int j = 0; j < dim; j++)
					out.write(data[i][j] + (j < dim - 1 ? " " : "\n"));
			}
			out.flush();
			out.close();
		}
		catch(Exception e){
			System.err.println("Error writing to file");
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Read UNIX-style command line parameters to as to specify the type of k-Means Clustering algorithm applied to the formatted data specified.
	 * "-k int" specifies both the minimum and maximum number of clusters. "-kmin int" specifies the minimum number of clusters. "-kmax int" specifies the maximum number of clusters. 
	 * "-iter int" specifies the number of times k-Means Clustering is performed in iteration to find a lower local minimum.
	 * "-in filename" specifies the source file for input data. "-out filename" specifies the destination file for cluster data.
	 * @param args command-line parameters specifying the type of k-Means Clustering
	 */
	public static void main(String[] args) {
		int kMin = 2, kMax = 2, iter = 1;
		ArrayList<String> attributes = new ArrayList<String>();
		ArrayList<Integer> values = new ArrayList<Integer>();
		int i = 0;
		String infile = null;
		String outfile = null;
		while (i < args.length) {
			if (args[i].equals("-k") || args[i].equals("-kmin") || args[i].equals("-kmax") || args[i].equals("-iter")) {
				attributes.add(args[i++].substring(1));
				if (i == args.length) {
					System.err.println("No integer value for" +  attributes.get(attributes.size() - 1) + ".");
					System.exit(1);
				}
				try {
					values.add(Integer.parseInt(args[i]));
					i++;
				}
				catch (Exception e) {
					System.err.printf("Error parsing \"%s\" as an integer.", args[i]);
					System.exit(2);
				}
			}
			else if(args[i].equals("-in")){
				i++;
				if (i == args.length){
					System.err.println("No string value provided for input source");
					System.exit(1);
				}
				infile = args[i];
				i++;
			}
			else if(args[i].equals("-out")){
				i++;
				if (i == args.length){
					System.err.println("No string value provided for output source");
					System.exit(1);
				}
				outfile = args[i];
				i++;
			}
		}

		for (i = 0; i < attributes.size(); i++) {
			String attribute = attributes.get(i);
			if (attribute.equals("k"))
				kMin = kMax = values.get(i);
			else if (attribute.equals("kmin"))
				kMin = values.get(i);
			else if (attribute.equals("kmax"))
				kMax = values.get(i);
			else if (attribute.equals("iter"))
				iter = values.get(i);
		}
		
		KMeansClusterer km = new KMeansClusterer();
		km.setKRange(kMin, kMax);
		km.setIter(iter);
		km.setData(km.readData(infile));
		km.kMeansCluster();
		km.writeClusterData(outfile);
	}
}
