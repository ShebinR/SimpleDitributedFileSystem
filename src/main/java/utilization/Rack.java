package utilization;

import util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rack {
    String id;
    double utilization;
    int nodeUtilizationSum;
    Map<String, Integer> dataNodesUtilization;
    byte status;

    public Rack(String id){
        this.id = id;
        utilization = 0d;
        nodeUtilizationSum = 0;
        dataNodesUtilization = new HashMap<>();
        status = Constants.RACK_ALIVE;
    }

    public Rack(double utilization, Map<String, Integer> dataNodesUtilization) {
        this.utilization = utilization;
        this.dataNodesUtilization = dataNodesUtilization;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(id + " -> " + utilization * 100 + "% \n Status : " + Constants.RACK_STATUS.get(status) +  "\n");
        for(Map.Entry<String, Integer> entry : dataNodesUtilization.entrySet()){
            sb.append("+--> " + entry.getKey() + " :: " + entry.getValue() + "\n");
        }
        return sb.toString();
    }

    void updateRackUtilizationByOneUnit(boolean increment){
        int totalUtilization = dataNodesUtilization.size() * Constants.MAX_NODE_UTILIZATION;
        nodeUtilizationSum += increment ? 1 : -1;
        utilization = nodeUtilizationSum/(totalUtilization * 1.0);
    }

    List<String> findLeastLoadedDataNodes(){
        List<String> result = new ArrayList<>();
        Map.Entry<String, Integer>  leastLoadedNode1 = null, leastLoadedNode2 = null;
        for(Map.Entry<String, Integer> entry : dataNodesUtilization.entrySet()){
            if(leastLoadedNode1 == null){
                leastLoadedNode1 = entry;
                continue;
            }else if(leastLoadedNode2 == null){
                leastLoadedNode2 = entry;
                if(leastLoadedNode1.getValue() > entry.getValue()){
                    leastLoadedNode2 = leastLoadedNode1;
                    leastLoadedNode1  = entry;
                }
                continue;
            }
            if(leastLoadedNode1.getValue() > entry.getValue()){
                leastLoadedNode2 = leastLoadedNode1;
                leastLoadedNode1 = entry;
            }else if(leastLoadedNode2.getValue() > entry.getValue()){
                leastLoadedNode2 = entry;
            }
        }
        if(leastLoadedNode1.getValue() < Constants.MAX_NODE_UTILIZATION){
            result.add(leastLoadedNode1.getKey());
            if(leastLoadedNode2 != null && leastLoadedNode2.getValue() < Constants.MAX_NODE_UTILIZATION)
                result.add(leastLoadedNode2.getKey());
        }
        return result;
    }
}
