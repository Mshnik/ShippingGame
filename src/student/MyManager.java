package student;

import game.*;

import java.lang.reflect.Array;
import java.util.*;
import java.awt.Color;

public class MyManager extends game.Manager {
	private Boolean preprocessing; // if preprocessing is still going on
	private HashMap<Color, List<Parcel>> mapColPar; // a hashmap of list of parcels to a corresponding color
	
	public void run(){
		preprocessing = true;
		Node depot = getGame().getBoard().getTruckDepot();
		ArrayList<Truck> trucks = getGame().getBoard().getTrucks();
		Iterator<Truck> iterTruck = trucks.iterator();
		Set<Parcel> parcels = getGame().getBoard().getParcels();
		
		//Parcel userData "is parcel assigned to a truck?"
		for(Parcel n: parcels){
			n.setUserData(new Boolean(false));
		}
		
		//make hashMap of colors to list of parcels
		mapColPar = new HashMap<java.awt.Color, List<Parcel>>();
		for(Parcel p: parcels){
			Color pCol = p.getColor();
			List<Parcel> colPar;
			if(mapColPar.containsKey(pCol)){
				colPar = (List)(mapColPar.get(pCol));
				colPar.add(p);
			}
			else{
				colPar = new ArrayList<Parcel>();
				colPar.add(p);
				mapColPar.put(pCol, colPar);
			}
		}
		
		//assign first parcel to trucks
		int size = Math.min(trucks.size(), parcels.size());
		Truck next;
		Object[] arrPar = parcels.toArray();
		for(int i = 0; i < size; i++){
			// find the first available parcel in the Array
			Parcel first;
			Boolean firstFound = false;
			int arrInd = 0;
			first = (Parcel)(Array.get(arrPar, arrInd));
			arrInd++;
			if((Boolean)(first.getUserData())){
				while(arrInd < arrPar.length && !firstFound){
					first = (Parcel)(Array.get(arrPar, arrInd));
					if((Boolean)(first.getUserData())){
						arrInd++;
					}
					else{
						firstFound = true;
					}
				}
			}

			
			next = iterTruck.next();
			Color tCol = next.getColor();
			List<Parcel> listPar = mapColPar.get(tCol);
		
			// if there is a parcel with the same color as the truck, assign it to parcel first
			if(mapColPar.containsKey(tCol)){			
				if(!listPar.isEmpty()){
					first = listPar.get(0);
				}
			}
			first.setUserData(new Boolean(true));
			
			listPar = mapColPar.get(first.getColor());
			int j = 0;
			Boolean found = false;
			while(j < listPar.size() && !found){
				if(listPar.get(j).equals(first)){
					listPar.remove(j);
					found = true;
				}
				else {
					j++;
				}
			}
			
			// assign the parcel first to the truck, route truck to parcel
			next.setUserData(first);
			List<Node> path = findPath(depot, first.start);
			next.setTravelPath(path);
		}
		preprocessing = false;
	}
	
	public void truckNotification(Truck t, Notification message){
		Game game = getGame();
		Board board = game.getBoard();
		//check for preprocessing to finish
		if(preprocessing){
			return;
		}

		if(message.equals(Manager.Notification.GOING_TO_CHANGED) ||
				message.equals(Manager.Notification.LOCATION_CHANGED) ||
				message.equals(Manager.Notification.PARCEL_AT_NODE) ||
				message.equals(Manager.Notification.STATUS_CHANGED) ||
				message.equals(Manager.Notification.TRAVELING_TO_CHANGED) 
				){
			return;
		}
	
		//After a parcel has been dropped off
		if(message.equals(Manager.Notification.DROPPED_OFF_PARCEL)){
			Set<Parcel> parcels = getGame().getBoard().getParcels();
			if(parcels.isEmpty()){ // there is no more parcel on the board
				List<Node> path = findPath(t.getLocation(), board.getTruckDepot());
				t.setTravelPath(path);
				return;
			}
			else{ // there are more parcels on the board
				List<Node> path = getClosestPar(t, t.getLocation());
				if(path == null){
					path = findPath(t.getLocation(), board.getTruckDepot());
				}
				t.setTravelPath(path);
				return;
			}
		}
		
		//the Parcel truck t is carrying or going towards
		Parcel tPar = (Parcel)(t.getUserData());
		
		if(message.equals(Manager.Notification.PICKED_UP_PARCEL)){
			List<Node> path = findPath(tPar.start, tPar.destination);
			t.setUserData(tPar);
			t.setTravelPath(path);
			return;
		}
		
		if((tPar != null) && !tPar.isHeld() && t.getLocation() == tPar.start){
			t.pickupLoad((Parcel)(t.getUserData()));
			return;
		}
		if((tPar != null) && t.getLocation() == tPar.destination && tPar.isTruckHere(t)){
			t.setUserData(null);
			t.dropoffLoad();
			return;
		}
		
	}
	
	/** Find the shortest path given the starting and ending nodes*/
	private List<Node> findPath(Node start, Node end){

		Board board = getBoard();
		
		HashSet<Node> allNodes = board.getNodes();
		//Iterator<Node> iterNodes = allNodes.iterator();
		HashMap<Node, nodeData> dataNodes = new HashMap<Node, nodeData>();
		
		//set all nodes's length to infinity
		for(Node n: allNodes){
			nodeData nData = new nodeData(false, Integer.MAX_VALUE, null);
			dataNodes.put(n, nData);
		}
		//set start node's length to 0
		dataNodes.get(start).setPath(0, null);
		
		MyHeap<Node> frontier = new MyHeap<Node>();
		HashSet<Node> settled = new HashSet<Node>();
		frontier.add(start, 0);
		
		// while there are nodes unvisited and the end node is not visited yet
		while(!frontier.isEmpty() && !dataNodes.get(end).getVisited()){
			Node f = frontier.poll();
			settled.add(f);
			dataNodes.get(f).visit();
			
			HashMap<Node, Integer> neigh = f.getNeighbors();
			Set<Node> setNeigh = neigh.keySet();
			Iterator<Node> iterNeigh = setNeigh.iterator();
			while(iterNeigh.hasNext()){
				Node next = iterNeigh.next();
				if (!dataNodes.get(next).getVisited()){
					int newLength = dataNodes.get(f).getPath() + neigh.get(next);
					if(dataNodes.get(next).getPath() == Integer.MAX_VALUE){
						dataNodes.get(next).setPath(newLength, f);
						frontier.add(next, newLength);
					}
					else if(newLength < dataNodes.get(next).getPath()){
							dataNodes.get(next).setPath(newLength, f);
							frontier.updatePriority(next, newLength);
					}
				}
			}
		}
		
		// get the shortest path from start to end
		List<Node> path = new ArrayList<Node>();
		List<Edge> edge = new ArrayList<Edge>();
		Node curr = end;
		nodeData currData = dataNodes.get(end);
		while(currData.getBackPointer() != null){
			path.add(0, curr);
			edge.add(0, curr.getConnect(currData.getBackPointer()));
			curr = currData.getBackPointer();
			currData = dataNodes.get(curr);
		}
		path.add(0, start);
		return path;	
	}
	
	/** Return the path to the closest available parcel from the current truck location*/
	private List<Node> getClosestPar(Truck t, Node loc){

			Board board = getBoard();
			
			HashSet<Node> allNodes = board.getNodes();
			HashMap<Node, nodeData> dataNodes = new HashMap<Node, nodeData>();
			
			//set all nodes's length to infinity
			for(Node n: allNodes){
				nodeData nData = new nodeData(false, Integer.MAX_VALUE, null);
				dataNodes.put(n, nData);
			}
			//set start node's length to 0
			dataNodes.get(loc).setPath(0, null);
			
			MyHeap<Node> frontier = new MyHeap<Node>();
			HashSet<Node> settled = new HashSet<Node>();
			frontier.add(loc, 0);
			
			while(!frontier.isEmpty()){
				Node f = frontier.poll();
				settled.add(f);
				dataNodes.get(f).visit();
				
				HashSet<Parcel> fPar = f.getParcels();
				if(!fPar.isEmpty()){ // there is at least one parcel at this node
					Iterator<Parcel> iterFPar = fPar.iterator();
					while(iterFPar.hasNext()){
						Parcel next = iterFPar.next();
						if(!((Boolean)(next.getUserData()))){
							synchronized(next){
								next.setUserData(new Boolean(true));
							}
							List<Node> path = findPath(t.getLocation(), next.start);
							t.setUserData(next);
							return path;
						}
					}
				}
				// there is no parcel at this node, proceed to its neighbors
				HashMap<Node, Integer> neigh = f.getNeighbors();
				Set<Node> setNeigh = neigh.keySet();
				Iterator<Node> iterNeigh = setNeigh.iterator();
				while(iterNeigh.hasNext()){
					Node next = iterNeigh.next();
					if (!dataNodes.get(next).getVisited()){
						int newLength = dataNodes.get(f).getPath() + neigh.get(next);
						if(dataNodes.get(next).getPath() == Integer.MAX_VALUE){
							dataNodes.get(next).setPath(newLength, f);
							frontier.add(next, newLength);
						}
						else if(newLength < dataNodes.get(next).getPath()){
								dataNodes.get(next).setPath(newLength, f);
								frontier.updatePriority(next, newLength);
						}
					}
				}
			}
			return null;
	}
	
	/** An inner class of user data for a Node*/
	private class nodeData {
		private boolean visited; // if this node is visited
		private int shortestPath; // the shortest path from starting node to this node
		private Node backPointer; // the previous node it is pointed to
		
		public nodeData(boolean b, int sp, Node back){
			visited = b;
			shortestPath = sp;
			backPointer = back;
		}
		
		public int getPath(){
			return shortestPath;
		}
		
		public boolean getVisited(){
			return visited;
		}
		
		public Node getBackPointer(){
			return backPointer;
		}
		
		public void setPath(int l, Node back){
			shortestPath = l;
			backPointer = back;
		}
		
		public void visit(){
			visited = true;
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
