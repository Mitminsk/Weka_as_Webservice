/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import weka.core.Instances;
import weka.clusterers.DensityBasedClusterer;
import weka.clusterers.EM;
import weka.clusterers.ClusterEvaluation;
import java.io.FileReader;
import java.io.BufferedReader;

@WebService()

public class ClusteringEM {

    @WebMethod(operationName = "execute")
    public String execute(@WebParam(name = "input")
            final String input) throws Exception {
        
        ClusterEvaluation eval;
        Instances data;
        String[] options;
        DensityBasedClusterer cl;
        data = new Instances(new BufferedReader(new FileReader(input)));
        StringBuffer result;
        result = new StringBuffer();

        result.append("Weka - Demo\n===========\n\n" + "\n\n--> normal\n");
 // normal
        //System.out.println("\n--> normal");
        options = new String[2];
        options[0] = "-t";
        options[1] = input;

//System.out.println(ClusterEvaluation.evaluateClusterer(new EM(), options));
        result.append(ClusterEvaluation.evaluateClusterer(new EM(), options));

 // manual call
        //System.out.println("\n--> manual");
        cl = new EM();
        cl.buildClusterer(data);
        eval = new ClusterEvaluation();
        eval.setClusterer(cl);
        eval.evaluateClusterer(new Instances(data));
        //System.out.println("# of clusters: " + eval.getNumClusters());
        try {
            result.append("\n--> manual" + "\n\n# of clusters: "
                    + eval.getNumClusters() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
 // density based
        //System.out.println("\n--> density (CV)");
        cl = new EM();
        eval = new ClusterEvaluation();
        eval.setClusterer(cl);
        eval.crossValidateModel(
                cl, data, 10, data.getRandomNumberGenerator(1));
        //System.out.println("# of clusters: " + eval.getNumClusters());

        try {
            result.append("\n--> density (CV)" + "\n\n# of clusters: "
                    + eval.getNumClusters() + "\n\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
