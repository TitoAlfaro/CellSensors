package mit.edu.obmg.cellsensors;

public class MapValues {
	
	float map(float x, float in_min, float in_max, float out_min, float out_max) {
		if (x < in_min)
			return out_min;
		else if (x > in_max)
			return out_max;
		else
			return (x - in_min) * (out_max - out_min) / (in_max - in_min)
					+ out_min;
	}

}
