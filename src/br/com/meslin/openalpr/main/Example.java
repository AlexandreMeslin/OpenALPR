/**
 * From: http://doc.openalpr.com/bindings.html
 */

package br.com.meslin.openalpr.main;

import java.time.Duration;
import java.time.Instant;

import com.openalpr.jni.Alpr;
import com.openalpr.jni.AlprException;
import com.openalpr.jni.AlprPlate;
import com.openalpr.jni.AlprPlateResult;
import com.openalpr.jni.AlprResults;

public class Example {

	public Example() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws AlprException {
		 // Alpr alpr = new Alpr("us", "/path/to/openalpr.conf", "/path/to/runtime_data");
		 Alpr alpr = new Alpr("us", "/etc/openalpr/openalpr.conf", "/usr/share/openalpr/runtime_data");

		 // Set top N candidates returned to 20
		 alpr.setTopN(5);

		 // Set pattern to Maryland
		 alpr.setDefaultRegion("md");

		 Instant startTime = Instant.now();
		 AlprResults results = alpr.recognize("/home/meslin/GoogleDrive/Doutorado/MUSANet/Imagens/placa-us-1.jpg");
//		 AlprResults results = alpr.recognize("/home/meslin/Desktop/placa-us-hires.jpg");
		 Instant endTime = Instant.now();
		 System.out.format("  %-15s%-8s %d ms\n", "Plate Number", "Confidence", Duration.between(startTime, endTime).toMillis());
		 for (AlprPlateResult result : results.getPlates())
		 {
		     for (AlprPlate plate : result.getTopNPlates()) {
		         if (plate.isMatchesTemplate())
		             System.out.print("  * ");
		         else
		             System.out.print("  - ");
		         System.out.format("%-15s%-8f\n", plate.getCharacters(), plate.getOverallConfidence());
		     }
		 }

		 // Make sure to call this to release memory
		 alpr.unload();	
	}
}
