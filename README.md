Shipping Game
============
The Shipping Game is a project in development for the Java course I TA. Comments/Criticisms always welcome at mgp57@cornell.edu

Overview
--------
In the Shipping Game, you are handed a fleet of Trucks to make a series of Parcel deliveries. The board is an undirected graph of cities and highways. Parcels are distrubted about the board, each of which has a desired destination. To win the game, use your Trucks to pick up each of the Parcels on the board and deliver them at their desired destination. The game ends after every parcel has been delivered and all Trucks return to their initial location at the Truck Depot.

Learning Goals
--------------
1. **Graphs and Graph Algorithms** - The game is played on an undirected graph. The very first problem a player has to solve is "What is the shortest path from A to B", mostly likely solved via Dijkstra's Algorithm.
2. **Multi-Threading** - Each Truck exists in its own thread. Players must not be careful make their truck instructions Thread-Safe, or one Truck may be interrupted by another.
3. **Various Data Structures** - The game involves many different tasks, and running time is very important for score.Players should have/acquire knowledge of which data structures are idea for different operations to minimize data operation time.

Scoring
------------------------
Players begin the game with a score of 0. Throughout the game, players recieve points for:
- Dropping off a parcel at its destination Node. Bonus points earned If the color of the Truck matches the color of the Parcel.

Players lose points for:
- Picking up a parcel - A single small cost
- Dropping off a parcel - A single small cost
- Truck Traveling an edge - Cost depenedent on the length of the edge and the speed of the Truck, minimized at a given Efficient Speed.
- Truck waiting - Whenever the truck is waiting to recieve instruction or for processing to complete, a small "idling" cost is deducted. This cost is very small but notably non-zero.

How Players Play
----------------
Trucks do not come with any default behavior. It is up to the player (student, as it were) to write a class telling the Trucks what to do. This class should extend the abstract Manager class given, and should be in the default package in order to respect the protected access modifiers of many classes. Specifically, the **run()** method allows the manager to define code to be run as soon as the game begins, and the **truckNotification(Truck t, Notification n)** allows the manager to respond to notifications from trucks and give them new instruction. These two methods are left as abstract in the Manager class and so are up to the student to implement.

State of the Project
--------------------
Currently, the project is in a fairly developed state. The whole game is runnable via the Main class. A GUI is included to show the state of the game as it evolves. Games are represented and read in as JSON strings, so the game is fully customizable for new boards, more trucks, more parcels, etc. Current work is on a force-directed graph display algorithm to better display the board on the GUI, and a map editor to allow for creation of new boards within the GUI and the ability to save the current board to JSON. 
