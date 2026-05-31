Coal Detection System
Overview🧠

The Coal Detection System is a Java-based application that classifies different types of coal using image analysis and graph-based machine learning concepts. The system analyzes uploaded coal images, extracts visual features, and compares them against a trained dataset to identify the most likely coal category.

The project was developed to explore the application of computer vision, graph structures, and machine learning techniques in solving real-world problems within the mining and energy industries.

Objectives
Classify coal samples from images.
Apply image processing techniques to extract meaningful features.
Represent image features using graph-based structures.
Explore Graph Neural Network (GNN) concepts for classification.
Provide an easy-to-use graphical interface for users.

Technologies Used 
Java
JavaFX
Object-Oriented Programming (OOP)
Graph Data Structures
Image Processing
Machine Learning Concepts
Git & GitHub

How It Works
The user uploads a coal image.
The image is processed and relevant visual features are extracted.
Features are converted into graph-based representations.
The classifier compares extracted features against the training dataset.
The system predicts the coal type and displays the result.

Future Improvements 💡
GUI Optimization

Improve application responsiveness and user experience by optimizing image rendering, loading times, and interface performance.

* Expanded Dataset

* Increase the number and diversity of training images to improve classification accuracy and model robustness.

* Advanced Machine Learning

* Implement a fully trained Graph Neural Network model and evaluate its performance against traditional classification approaches.

* Real-Time Classification

* Enable direct image capture and real-time coal analysis using cameras or mobile devices.

Learning Outcomes

Through this project, we gained practical experience in:

Software Engineering
Java Development
Data Structures and Algorithms
Image Processing
Machine Learning Concepts
Collaborative Development using Git and GitHub


📁The folder structure

Coal_Detecter - 


                * bin - all the *.class files
                * data -   train-list.txt
                         * training 📁 * Anthracite(1.jpg, 2.jpg, 27.jpg)
                                        * Bituminous(3.jpg, 4.jpg, 6.jpg)
                                        * Lignite(92.jpg, 90.jpg, 88.jpg)
                                        * Peat(5.jpg, 30.jpg, 34.jpg)
                * dlib - * JarFile
                         * data 📁 * train.txt
                                    * training 📁(different types of coals)  * Anthracite(1.jpg, 2.jpg, 27.jpg)
                                                                              * Bituminous(3.jpg, 4.jpg, 6.jpg)
                                                                              * Lignite(92.jpg, 90.jpg, 88.jpg)
                                                                              * Peat(5.jpg, 30.jpg, 34.jpg)
                * src - * Main.java
                        * coal📁 - detection📁 - * about📁 - About.java
                                                  * app📁 - AppState.java
                                                  * classifier📁 - CoalClassifier.java, AnalysisResults.java, GraphNeuralNetwork.java, SimilarityResults.java, TrainingSample.java
                                                  * datastructure📁 - ArrayListDS.java, LinkedListDS.java, Node.java
                                                  * graph📁 - Graph.java
                                                  * image📁 - ImageFeatureExtractor.java
                                                  * modelgraph📁 - GraphEdge.java, GraphStructure.java, GraphVertex.java
                                                  * results📁 - Results.java
                                                  * service📁 - CoalAnalysisService.java, DataSetLoader.java
                                                  * upload📁 - Upload.java

Contributors/Problem Solvers: Mayibongwe Mathonsi
Madisa Pedume
Motsoetsoana Makhema
Chichongue Samuel
