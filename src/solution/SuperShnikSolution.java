package solution;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import common.types.Tuple2;
import game.*;

public class SuperShnikSolution extends AbstractSolution {
	
	private Map<Tuple2<Node, Node>, Tuple2<Integer,List<Node>>> paths;
	private boolean preprocessingDone;
	
	
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		HashSet<Node> importantLocations = new HashSet<Node>();
		
		importantLocations.add(getBoard().getTruckDepot());
		for(Parcel p : getParcels()){
			importantLocations.add(p.start);
			importantLocations.add(p.destination);
		}
		
		for(Node n : importantLocations){
			Map<Node, List<Node>> pths = dijkstra(n);
			for(Map.Entry<Node, List<Node>> e : pths.entrySet()){
				Node m = e.getKey();
				List<Node> path = e.getValue();
				if(importantLocations.contains(m)){
					paths.put(new Tuple2<Node, Node>(n, m), 
							new Tuple2<Integer, List<Node>>(pathLength(path), path));
				}
			}
		}
		
		
		
		System.out.println("Preprocessing took " + (System.currentTimeMillis() - startTime) + "ms");
		preprocessingDone = true;
	}

	@Override
	public void truckNotification(Truck t, Notification message) {
		if(! preprocessingDone) return;
	}

}
