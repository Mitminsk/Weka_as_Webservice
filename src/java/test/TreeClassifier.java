/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.Filter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Vector;
import javax.jws.WebService;

@WebService(serviceName = "NewWebService", portName = "NewWebServicePort")
public class TreeClassifier {

    /**
     * Web service operation
     */
    @WebMethod(operationName = "execute")
    public String execute(@WebParam(name = "input") String input) throws Exception {

        Classifier m_Classifier = null;

        /**
         * the filter to use
         */
        Filter m_Filter = null;
        /**
         * the training file
         */
        String m_TrainingFile = null;
        /**
         * the training instances
         */
        Instances m_Training = null;
        /**
         * for evaluating the classifier
         */
        Evaluation m_Evaluation = null;

        String classifier = "";
        String filter = "";
        String dataset = "";
        Vector classifierOptions = new Vector();
        Vector filterOptions = new Vector();

        System.out.println(input);
        /**
         * splitting the input into six parts
         */
        String args[] = input.split("\\s");
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
        }

        int i = 0;
        String current = "";
        boolean newPart = false;
        do {
            // determine part of command line
            if (args[i].equals("CLASSIFIER")) {
                System.out.println(args[i]);
                current = args[i];
                i++;
                newPart = true;
            } else if (args[i].equals("FILTER")) {
                current = args[i];
                i++;
                newPart = true;
            } else if (args[i].equals("DATASET")) {
                current = args[i];
                i++;
                newPart = true;
            }
            if (current.equals("CLASSIFIER")) {
                if (newPart) {
                    classifier = args[i];
                } else {
                    classifierOptions.add(args[i]);
                }
            } else if (current.equals("FILTER")) {
                if (newPart) {
                    filter = args[i];
                } else {
                    filterOptions.add(args[i]);
                }
            } else if (current.equals("DATASET")) {
                if (newPart) {
                    dataset = args[i];
                }

            }
            // next parameter
            i++;
            newPart = false;
        } while (i < args.length);

        m_Classifier = Classifier.forName(classifier, (String[]) classifierOptions.toArray(new String[classifierOptions.size()]));

        m_Filter = (Filter) Class.forName(filter).newInstance();
        if (m_Filter instanceof OptionHandler) {
            ((OptionHandler) m_Filter).setOptions((String[]) filterOptions.toArray(new String[filterOptions.size()]));
        }

        m_TrainingFile = dataset;
        m_Training = new Instances(
                new BufferedReader(new FileReader(m_TrainingFile)));
        m_Training.setClassIndex(m_Training.numAttributes()
                - 1);
        // run filter
        m_Filter.setInputFormat(m_Training);
        Instances filtered = Filter.useFilter(m_Training, m_Filter);

        // train classifier on complete file for tree
        m_Classifier.buildClassifier(filtered);

        // 10fold CV with seed=1
        m_Evaluation = new Evaluation(filtered);
        m_Evaluation.crossValidateModel(
                m_Classifier, filtered, 10,
                m_Training.getRandomNumberGenerator(1));

        StringBuffer result;
        result = new StringBuffer();
        result.append("Weka - Demo\n===========\n\n");
        result.append("Classifier...: "
                + m_Classifier.getClass().getName() + " "
                + Utils.joinOptions(m_Classifier.getOptions()) + "\n");
        if (m_Filter instanceof OptionHandler) {
            result.append("Filter.......: "
                    + m_Filter.getClass().getName() + " "
                    + Utils.joinOptions(((OptionHandler) m_Filter).getOptions()) + "\n");
        } else {
            result.append("Filter.......: "
                    + m_Filter.getClass().getName() + "\n");
        }
        result.append("Training file: "
                + m_TrainingFile + "\n");
        result.append("\n");
        result.append(m_Classifier.toString() + "\n");
        result.append(m_Evaluation.toSummaryString() + "\n");
        try {
            result.append(m_Evaluation.toMatrixString() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            result.append(m_Evaluation.toClassDetailsString()
                    + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();

    }
}
