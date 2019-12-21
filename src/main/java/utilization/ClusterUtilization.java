package utilization;

import util.Constants;
import util.FileOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClusterUtilization {
    List<Rack> racks;

    public ClusterUtilization(String path) {
        racks = new ArrayList<>();
        List<String> rackInfo = FileOperations.readFileAsString(path);
        parseRackInfo(rackInfo);
    }

    public List<String> allocateNodesForABlock() {
        List<String> replicas = new ArrayList<>();
        List<Rack> candidateRacks = findLeastLoadedRacks();
        for(Rack rack : candidateRacks){
            List<String> candidateNodes = rack.findLeastLoadedDataNodes();
            if(replicas.size() + candidateNodes.size() <= Constants.REPLICATION_FACTOR){
                replicas.addAll(candidateNodes);
            }else{
                replicas.add(candidateNodes.get(0));
            }
        }
        return replicas;
    }

    public void updateUtilization(List<String> blockLocations, boolean doIncrement){
        int total = blockLocations.size();
        for(Rack rack : racks){
            for(String dataNode : blockLocations){
                if(rack.dataNodesUtilization.containsKey(dataNode)){
                    int utilization = rack.dataNodesUtilization.get(dataNode);
                    rack.dataNodesUtilization.put(dataNode, (doIncrement ? ++utilization : --utilization));
                    rack.updateRackUtilizationByOneUnit(doIncrement);
                    total--;
                }
            }
            if(total == 0){
                break;
            }
        }
    }

    private void parseRackInfo(List<String> rackInfo){
        //R1:DN1,DN2
        for(String rackDetails : rackInfo){
            String[] fields = rackDetails.split(":");
            Rack rack = new Rack(fields[0]);
            String[] dataNodes = fields[1].split(",");
            for(int i=0; i<dataNodes.length; i++){
                rack.dataNodesUtilization.put(dataNodes[i],0);
            }
            racks.add(rack);
        }
    }

    private List<Rack> findLeastLoadedRacks(){
        List<Rack> result = new ArrayList<>();
        Rack leastLoadedRack1 = null, leastLoadedRack2 = null;
        //Assuming that there are atleast 2 racks
        if(racks.get(0).utilization < racks.get(1).utilization){
            leastLoadedRack1 = racks.get(0);
            leastLoadedRack2 = racks.get(1);
        }else{
            leastLoadedRack1 = racks.get(1);
            leastLoadedRack2 = racks.get(0);
        }
        for(int i = 2; i < racks.size(); i++) {
            if(leastLoadedRack1.utilization > racks.get(i).utilization){
                leastLoadedRack2 = leastLoadedRack1;
                leastLoadedRack1 = racks.get(i);
            }else if(leastLoadedRack2.utilization > racks.get(i).utilization){
                leastLoadedRack2 = racks.get(i);
            }
        }
        result.add(leastLoadedRack1);
        result.add(leastLoadedRack2);
        return result;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Rack r : this.racks)
            sb.append(r).append("\n");
        return sb.toString();
    }

    public void clearUtilization(){
        for(Rack rack : racks){
            for(Map.Entry<String, Integer> entry : rack.dataNodesUtilization.entrySet()){
                rack.dataNodesUtilization.put(entry.getKey(), 0);
            }
            rack.utilization = 0d;
            rack.nodeUtilizationSum = 0;
        }
    }
}
