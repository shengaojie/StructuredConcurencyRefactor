package addannotation.utils;

import java.util.Map;


/*
 * 用于求解一个map中最大或者是最小的key
 * */
public class MinOrMax {
	
	public static Integer getMaxKey(Map<Integer,?> map) {
	    Integer maxKey = 0;
	    for (Integer key : map.keySet()) {
	        if (maxKey == 0 || key.compareTo(maxKey) > 0) {
	            maxKey = key;
	        }
	    }
	    return maxKey;
	}
	
	
	public static Integer getMinKey(Map<Integer, ?> map) {
	    Integer maxKey = 0;
	    for (Integer key : map.keySet()) {
	        if (maxKey == 0 || key.compareTo(maxKey) < 0) {
	            maxKey = key;
	        }
	    }
	    return maxKey;
	}

}
