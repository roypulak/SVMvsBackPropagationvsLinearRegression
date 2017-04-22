import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Scanner;

public class BPRingMerge {
	private static final int INPUT_NEURONS = 2;
	private static final int OUTPUT_NEURONS = 1;

	private static final double LEARN_RATE = 0.1; // Rho.
	private static final long TRAINING_REPS = 100000000;//10000000;//number of epoch

	// Input to Hidden Weights:----layer vs i vs j
	private static double w[][][] = new double[100][100][100];

	// input: threshold value----layer vs value
	private static double theta[][] = new double[100][100];

	// Hidden layer number and neurons
	private static int HIDDEN_LYERS;
	private static int TOTAL_LYERS;
	private static int[] NUPER_LYERS = new int[100];

	// Activations.
	private static double inputs[] = new double[INPUT_NEURONS];
	private static double hidden[][] = new double[100][100];
	private static double target[] = new double[OUTPUT_NEURONS];
	private static double actual[] = new double[OUTPUT_NEURONS];

	// Unit errors.
	private static double error[][] = new double[100][100];

	private static final int MAX_SAMPLES = 10001;
	private static final int TEST_SAMPLES = 10000;
	private static final int TOTAL_PATTERN = 20000;

	private static double trainInputs[][] = new double[TOTAL_PATTERN][INPUT_NEURONS+OUTPUT_NEURONS];
	private static double scaledInputs[][] = new double[TOTAL_PATTERN][INPUT_NEURONS+OUTPUT_NEURONS];
	private static double trainOutput[][] = new double[TOTAL_PATTERN][OUTPUT_NEURONS];
	private static double scaledOutput[][] = new double[TOTAL_PATTERN][OUTPUT_NEURONS];

	private static double xMax[] = new double[] { -10000, -10000, -10000, -10000, -10000 };// for
	// scaling
	// of
	// input
	// data
	private static double xMin[] = new double[] { 10000, 10000, 10000, 10000, 10000 };// for
	// scaling
	// of
	// input
	// data

	public static void main(String[] args) {

		// System.out.println("How many Hidden Layer:");
		HIDDEN_LYERS = 2;

		TOTAL_LYERS = 2 + HIDDEN_LYERS;

		System.out.println("Please Enter Node number for each hidden layer");

		NUPER_LYERS[0] = INPUT_NEURONS;
		NUPER_LYERS[TOTAL_LYERS - 1] = OUTPUT_NEURONS;

		Scanner in = new Scanner(System.in);
		for (int i = 1; i < TOTAL_LYERS - 1; i++) {
			int n = in.nextInt();
			NUPER_LYERS[i] = n;
		}
		System.out.println(NUPER_LYERS[0] + "  " + NUPER_LYERS[1] + "  " + NUPER_LYERS[2] + "  " + NUPER_LYERS[3]);

		/*
		 * for(int i = 0; i < TOTAL_LYERS; i++) {
		 * System.out.println(NUPER_LYERS[i]); }
		 */
		//testing minimax
		//minmax();
		
		
		 inputDataProcess();


		 scaleData();
		 
		 //testing
		 //testNetwork();
		 
		 /*
         // testing retrieve original
		 for(int pat = 0; pat < TOTAL_PATTERN; pat++)
		 {
			 for(int i = 0 ; i < INPUT_NEURONS + OUTPUT_NEURONS; i++)
			 {
				 System.out.print(retriveOriginal(i,scaledInputs[pat][i])+" ");
			 }
			 System.out.println();
		 }*/
		 NeuralNetwork();
	}

	private static void inputDataProcess() {
		BufferedReader br = null;
		String[] parts = null;
		String line = null;
		int pattern = 0;
		String numPart = "";

		try {
			br = new BufferedReader(new FileReader("E:/Algorithm Codes/BackPropagation/inputFiles/ring-merge.txt"));
			while ((line = br.readLine()) != null) {
				parts = line.trim().split("\t");
				for (int i = 0; i < INPUT_NEURONS + OUTPUT_NEURONS; i++) {
					    if(parts[i].contains("-"))
					    {
					    	numPart = parts[i].substring(1);
					    	trainInputs[pattern][i] = -1 * Double.parseDouble(numPart);
					    }
					    else
					    {
					    	trainInputs[pattern][i] = Double.parseDouble(parts[i]);
					    }
						
						//System.out.print(trainInputs[pattern][i] + " ");
				}
                //System.out.println();
				pattern++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	// finding minimum and maximum of columns
	private static void minmax() {
		double tempN = 0;

		for (int pattern = 0; pattern < TOTAL_PATTERN; pattern++) {
			for (int i = 0; i < INPUT_NEURONS + OUTPUT_NEURONS; i++) {

				tempN = trainInputs[pattern][i];
				
				if (tempN > xMax[i]) {
					xMax[i] = tempN;
				}

				if (tempN < xMin[i]) {
					xMin[i] = tempN;
				}
			}
		}
		/*System.out.println("Maximum of columns:");
		for(int i = 0; i < xMax.length; i++)
		System.out.print(xMax[i]+" ");
		System.out.println();
		System.out.println("Minimum of columns:");
		for(int i = 0; i < xMin.length; i++)
		System.out.print(xMin[i]+" ");*/
	}

	// retrieve scaling value to original value
	private static double retriveOriginal(int index, double value) {
		// formula to retrieve original value from scaled value
		double val;
		val = ((xMax[index] - xMin[index]) * value) + xMin[index];
		return val;
	}
	
	// Scaling Data
	private static void scaleData() {
		// finding minimum and maximum of columns
		minmax();

		// scaling between 0 to 1
		for (int pattern = 0; pattern < TOTAL_PATTERN; pattern++) {
			for (int i = 0; i < INPUT_NEURONS + OUTPUT_NEURONS; i++) {
					scaledInputs[pattern][i] = (trainInputs[pattern][i] - xMin[i]) / (xMax[i] - xMin[i]);
					//System.out.print(scaledInputs[pattern][i]+" ");
			}
			//System.out.println();
		}

	}

	// assigning Random Weights and threshold
	private static void assignRandomWeights() {
		int counter = 0;

		for (int layer = 0; layer <= TOTAL_LYERS - 2; layer++) {
			// initializing weight
			for (int i = 0; i < NUPER_LYERS[counter]; i++) {
				for (int j = 0; j < NUPER_LYERS[counter + 1]; j++) {
					w[layer][i][j] = new Random().nextDouble() - 0.5;
					// System.out.print(w[layer][i][j]+" ");
				}
			}
			// System.out.println();

			// initializing threshold
			for (int node = 0; node < NUPER_LYERS[counter + 1]; node++) {
				theta[layer][node] = new Random().nextDouble() - 0.5;
				// System.out.print(theta[layer][node]+" ");
			}
			// System.out.println();
			counter++;
		}

		return;
	}

	private static void feedForward() {
		double sum = 0.0;
		int counter = 0;
		for (int layer = 0; layer <= TOTAL_LYERS - 2; layer++) {
			// Calculate input to hidden layers.
			for (int i = 0; i < NUPER_LYERS[counter + 1]; i++) {
				sum = 0.0;
				for (int j = 0; j < NUPER_LYERS[counter]; j++) {
					if (layer == 0) {
						sum += inputs[j] * w[layer][j][i];
					} else {
						sum += hidden[layer - 1][j] * w[layer][j][i];
					}
				}
				sum -= theta[layer][i]; // Add in bias.
				hidden[layer][i] = sigmoid(sum);

				if (layer == TOTAL_LYERS - 2)// that means in last layer
					actual[0] = hidden[layer][i];// calculated actual output
			}
			// System.out.println();
			counter++;
		}
		return;
	}

	private static void backPropagate() {
		int counter = TOTAL_LYERS - 1;

		// propagating error
		for (int layer = TOTAL_LYERS - 2; layer >= 0; layer--) {
			if (layer == TOTAL_LYERS - 2)// that means output layer
			{
				for (int i = 0; i < NUPER_LYERS[counter]; i++) {
					error[layer][i] = -(target[i] - actual[i]) * sigmoidDerivative(actual[i]);
				}
			} else {
				for (int j = 0; j < NUPER_LYERS[counter]; j++) {
					for (int i = 0; i < NUPER_LYERS[counter + 1]; i++) {
						error[layer][j] += error[layer + 1][i] * w[layer + 1][j][i];
					}
					error[layer][j] *= sigmoidDerivative(hidden[layer][j]);
				}
			}

			counter--;
		}

		// Update the weights and bias(theta)
		counter = 0;
		counter = TOTAL_LYERS - 1;

		for (int layer = TOTAL_LYERS - 2; layer >= 0; layer--) {
			// System.out.println("counter: "+counter);
			for (int i = 0; i < NUPER_LYERS[counter]; i++) {
				for (int j = 0; j < NUPER_LYERS[counter - 1]; j++) {
					// updating weight
					if (layer == 0) {
						w[layer][j][i] += ((-LEARN_RATE) * error[layer][i] * inputs[j]);
					} else {
						w[layer][j][i] += ((-LEARN_RATE) * error[layer][i] * hidden[layer - 1][j]);
					}

				}
				// update threshold
				theta[layer][i] += ((LEARN_RATE) * error[layer][i]);
			}
			counter--;

		}
		return;
	}

	private static double sigmoid(final double val) {
		return (1.0 / (1.0 + Math.exp(-val)));
	}

	private static double sigmoidDerivative(final double val) {
		return (val * (1.0 - val));
	}

	private static void NeuralNetwork() {
		int sample = 0;
		Random randomGenerator = new Random();
		assignRandomWeights();

		// Train the network.
		for (int epoch = 0; epoch < TRAINING_REPS; epoch++) {

			// taking random pattern
			sample = randomGenerator.nextInt(MAX_SAMPLES);

			for (int i = 0; i < INPUT_NEURONS; i++) {
				inputs[i] = scaledInputs[sample][i];
				// System.out.print(inputs[i]+" ");
			} // i

			// System.out.println();

			for (int i = INPUT_NEURONS,j = 0; i < INPUT_NEURONS+OUTPUT_NEURONS; i++) {
				target[j] = scaledInputs[sample][i];
				// System.out.print(target[i]+" ");
				j++;
			} // i

			// System.out.println();

			feedForward();

			backPropagate();

		} // epoch

		testNetwork();

		return;
	}

	private static void testNetwork() {	
		double totalPower = 0, totalError = 0;
		DecimalFormat df = new DecimalFormat("#.000000");
		
		try {

			//String content = "This is the content to write into file";

			File file = new File("E:/Algorithm Codes/BackPropagation/outputFiles/ringmerge_out.txt");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i = TEST_SAMPLES; i < TOTAL_PATTERN; i++) {
				for (int j = 0; j < INPUT_NEURONS; j++) {
					inputs[j] = scaledInputs[i][j];//System.out.print(inputs[j] + "  ");
					//bw.write(df.format(trainInputs[i][j]) + "\t");
					//System.out.print(df.format(trainInputs[i][j]) + "\t");
				} // j

				for (int j = INPUT_NEURONS,k = 0; j < INPUT_NEURONS+OUTPUT_NEURONS; j++) {
					target[k] = scaledInputs[i][j];//System.out.print(target[k] + "  ");
					totalPower += target[k];
					//bw.write(df.format(trainInputs[i][j]));
					//System.out.print(df.format(trainInputs[i][j]));
					k++;
				} // j

				feedForward();
				totalError += Math.abs(actual[0] - target[0]);
				bw.write(df.format(retriveOriginal(INPUT_NEURONS,actual[0]))+"\n");
				//System.out.print("\t"+df.format(retriveOriginal(INPUT_NEURONS,actual[0])));
				//System.out.println();

			} // i
			bw.close();
			//System.out.println("E: " + df.format((totalError / totalPower) * 100) + "%");
			System.out.println("Finished!");
			return;
			
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	
}
