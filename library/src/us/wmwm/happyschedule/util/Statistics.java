package us.wmwm.happyschedule.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Statistics {
	List<Double> data;
	double size;

	public Statistics(List<Double> data) {
		this.data = data;
		size = data.size();
	}

	public double getMean() {
		double sum = 0.0;
		for (double a : data)
			sum += a;
		return sum / size;
	}

	public double getVariance() {
		double mean = getMean();
		double temp = 0;
		for (double a : data)
			temp += (mean - a) * (mean - a);
		return temp / size;
	}

	public double getStdDev() {
		return Math.sqrt(getVariance());
	}

	public double median() {
		List<Double> b = new ArrayList<Double>();
		b.addAll(data);
		Collections.sort(b);

		if (b.size() % 2 == 0) {
			return (b.get((b.size() / 2) - 1) + b.get(b.size() / 2)) / 2.0;
		} else {
			return b.get(b.size() / 2);
		}
	}
}