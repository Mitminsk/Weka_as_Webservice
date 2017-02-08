/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.*;
import weka.core.*;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Attribute;
import weka.classifiers.*;
import weka.classifiers.Classifier;
import weka.filters.unsupervised.attribute.StringToWordVector;

@WebService()
public class TextClassifier{

    /**
     * Web service operation
     */
    @WebMethod(operationName = "execute")
    public String execute(@WebParam(name = "input") String input) {
        //TODO write your implementation code here:

        Instances instances = null;
        Classifier classifier = null;
        Instances filteredData = null;
        Evaluation evaluation = null;
        Set modelWords = null;
        // maybe this should be settable?
        String delimitersStringToWordVector = "\\s.,:'\\\"()?!";

        // String classString = "weka.classifiers.bayes.NaiveBayes";
        String classString = "weka.classifiers.lazy.IBk";
        // String classString = input;

        String[] inputText = {"hey, buy this from me!", "do you want to buy?", "I have a party tonight!", "today it is a nice weather", "you are best", "I have a horse", "you are my friend", "buy, buy, buy!", "it is spring in the air", "do you want to come?"};

        String[] inputClasses = {"spam", "spam", "no spam", "no spam", "spam", "no spam", "no spam", "spam", "no spam", "no spam"};

        String[] testText = {"you want to buy from me?", "usually I run in stairs", "buy it now!", "buy, buy, buy!", "you are the best, buy!", "it is spring in the air"};

        if (inputText.length != inputClasses.length) {
            System.err.println("The length of text and classes must be the same!");
            System.exit(1);
        }
        // calculate the classValues
        HashSet classSet = new HashSet(Arrays.asList(inputClasses));
        classSet.add("?");
        String[] classValues = (String[]) classSet.toArray(new String[0]);
        //
        // create class attribute
        //
        FastVector classAttributeVector = new FastVector();
        for (int i = 0; i < classValues.length; i++) {
            classAttributeVector.addElement(classValues[i]);
        }
        Attribute ClassAttribute = new Attribute("class",
                classAttributeVector);
        //
        // create text attribute
        //
        FastVector inputTextVector = null; // null -> String type
        Attribute TextAttribute = new Attribute("text",
                inputTextVector);
        for (int i = 0; i < inputText.length; i++) {
            TextAttribute.addStringValue(inputText[i]);
        }

        // add the text of test cases
        for (int i = 0; i < testText.length; i++) {
            TextAttribute.addStringValue(testText[i]);
        }
        //
        // create the attribute information
        //
        FastVector AttributeInfo = new FastVector(2);
        AttributeInfo.addElement(TextAttribute);
        AttributeInfo.addElement(ClassAttribute);

        /*this.inputText = inputText;
         this.inputClasses = inputClasses;
         this.classString = classString;
         this.attributeInfo = attributeInfo;
         this.textAttribute = textAttribute;
         this.classAttribute = classAttribute;
         */
        StringBuffer result = new StringBuffer();
        result.append("dataset:\n\n");
        // creates an empty instances set
        instances = new Instances("data set", AttributeInfo, 100);
        // set which attribute is the class attribute
        instances.setClass(ClassAttribute);
        try {

            for (int i = 0; i < inputText.length; i++) {
                Instance inst = new Instance(2);
                inst.setValue(TextAttribute, inputText[i]);
                if (inputClasses != null && inputClasses.length > 0) {
                    inst.setValue(ClassAttribute, inputClasses[i]);
                }
                instances.add(inst);
            }
            result.append("DATA SET:\n" + instances + "\n");

            StringToWordVector filter = null;
            // default values according to Java Doc:
            int wordsToKeep = 1000;
            Instances filtered = null;
            try {
                filter = new StringToWordVector(wordsToKeep);
                filter.setOutputWordCounts(true);
                filter.setSelectedRange("1");

                filter.setInputFormat(instances);

                filtered = weka.filters.Filter.useFilter(instances, filter);
                // System.out.println("filtered:\n" + filtered);

            } catch (Exception e) {
                e.printStackTrace();
            }
            filteredData = filtered;

            // create Set of modelWords
            modelWords = new HashSet();
            Enumeration enumx
                    = filteredData.enumerateAttributes();
            while (enumx.hasMoreElements()) {
                Attribute att = (Attribute) enumx.nextElement();
                String attName = att.name().toLowerCase();
                modelWords.add(attName);
            }

            classifier = Classifier.forName(classString, null);
            classifier.buildClassifier(filteredData);
            evaluation = new Evaluation(filteredData);
            evaluation.evaluateModel(classifier, filteredData);

            try {
                result.append("\n\nINFORMATION ABOUT THECLASSIFIER AND EVALUATION:\n");
                result.append("\nclassifier.toString():\n"
                        + classifier.toString() + "\n");
                result.append("\nevaluation.toSummaryString(title,false):\n" + evaluation.toSummaryString("Summary", false) + "\n");
                result.append("\nevaluation.toMatrixString():\n"
                        + evaluation.toMatrixString() + "\n");
                result.append("\nevaluation.toClassDetailsString():\n"
                        + evaluation.toClassDetailsString("Details") + "\n");

                result.append("\nevaluation.toCumulativeMarginDistribution:\n" + evaluation.toCumulativeMarginDistributionString()
                        + "\n");
            } catch (Exception e) {
                e.printStackTrace();
                result.append("\nException (sorry!):\n" + e.toString());
            }

            result.append("\n\n");

            // check instances
            int startIx = 0;
            String testType = "not test";

            try {
                result.append("\nCHECKING ALL THE INSTANCES:\n");
                Enumeration enumClasses
                        = ClassAttribute.enumerateValues();
                result.append("Class values (in order): ");
                while (enumClasses.hasMoreElements()) {
                    String classStr
                            = (String) enumClasses.nextElement();
                    result.append("'" + classStr + "' ");
                }
                result.append("\n");
                // startIx is a fix for handling text cases
                for (int i = startIx; i < filteredData.numInstances();
                        i++) {
                    SparseInstance sparseInst = new SparseInstance(filteredData.instance(i));
                    sparseInst.setDataset(filteredData);
                    result.append("\nTesting: '" + inputText[i - startIx]
                            + "'\n");
                    // result.append("SparseInst: " + sparseInst +"\n");
                    double correctValue
                            = (double) sparseInst.classValue();
                    double predictedValue
                            = classifier.classifyInstance(sparseInst);
                    String predictString
                            = ClassAttribute.value((int) predictedValue) + " ("
                            + predictedValue + ")";
                    result.append("predicted: '" + predictString);
                    // print comparison if not new case
                    if (!"newcase".equals(testType)) {
                        String correctString
                                = ClassAttribute.value((int) correctValue) + " (" + correctValue
                                + ")";
                        String testString = ((predictedValue
                                == correctValue) ? "OK!" : "NOT OK!") + "!";
                        result.append("' real class: '" + correctString + "' ==> " + testString);
                    }
                    result.append("\n");
                    result.append("\n");
                    // result.append(thisClassifier.dumpDistribution());
                    // result.append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.append("\nException (sorry!):\n" + e.toString());
            }

            result.append("\n");

        } catch (Exception e) {
            e.printStackTrace();
            result.append("\nException (sorry!):\n" + e.toString());
        }

        result.append("\nNEW CASES\n");

        Instances testCases = new Instances(instances);
        testCases.setClass(ClassAttribute);
        //
        // since some classifiers cannot handle unknown words (i.e. words not
        // a 'model word'), we filter these unknowns out.
        // Maybe this should be done only for those classifiers?
        // E.g. Naive Bayes have prior probabilities which may be used?
        //
        // Here we split each test line and check each word
        //
        String[] testsWithModelWords = new String[testText.length];
        int gotModelWords = 0; // how many words will we use?
        for (int i = 0; i < testText.length; i++) {
            // the test string to use
            StringBuffer acceptedWordsThisLine = new StringBuffer();
            // split each line in the test array
            String[] splittedText
                    = testText[i].split("[" + delimitersStringToWordVector + "]");
            // check if word is a model word
            for (int wordIx = 0; wordIx < splittedText.length;
                    wordIx++) {
                String sWord = splittedText[wordIx];
                if (modelWords.contains((String) sWord)) {
                    gotModelWords++;
                    acceptedWordsThisLine.append(sWord + " ");
                }
            }
            testsWithModelWords[i]
                    = acceptedWordsThisLine.toString();
        }
        // should we do do something if there is no modelWords?
        if (gotModelWords == 0) {
            result.append("\nWarning!\nThe text to classify didn't contain a single\nword from the modelled words. This makes it hard for the classifier to\ndo something usefull.\nThe result may be weird.\n\n");
        }
        try {
            // add the ? class for all test cases
            String[] tmpClassValues = new String[testText.length];
            for (int i = 0; i < tmpClassValues.length; i++) {
                tmpClassValues[i] = "?";
            }

            for (int i = 0; i < testsWithModelWords.length; i++) {
                Instance inst = new Instance(2);
                inst.setValue(TextAttribute, testsWithModelWords[i]);
                if (tmpClassValues != null && tmpClassValues.length
                        > 0) {
                    inst.setValue(ClassAttribute, tmpClassValues[i]);
                }
                testCases.add(inst);
            }

            StringToWordVector filter = null;
            // default values according to Java Doc:
            int wordsToKeep = 1000;
            Instances filtered = null;
            try {
                filter = new StringToWordVector(wordsToKeep);
                filter.setOutputWordCounts(true);
                filter.setSelectedRange("1");

                filter.setInputFormat(testCases);
                filtered = weka.filters.Filter.useFilter(testCases, filter);

            } catch (Exception e) {
                e.printStackTrace();
            }
            Instances filteredTests = filtered;
            int startIx = instances.numInstances();
            String testType = "new case";

            try {
                result.append("\nCHECKING ALL THE INSTANCES:\n");
                Enumeration enumClasses
                        = ClassAttribute.enumerateValues();
                result.append("Class values (in order): ");
                while (enumClasses.hasMoreElements()) {
                    String classStr
                            = (String) enumClasses.nextElement();
                    result.append("'" + classStr + "' ");
                }
                result.append("\n");
                // startIx is a fix for handling text cases
                for (int i = startIx; i < filteredTests.numInstances();
                        i++) {
                    SparseInstance sparseInst = new SparseInstance(filteredTests.instance(i));
                    sparseInst.setDataset(filteredTests);
                    result.append("\nTesting: '" + testText[i - startIx]
                            + "'\n");

                    double correctValue
                            = (double) sparseInst.classValue();
                    double predictedValue
                            = classifier.classifyInstance(sparseInst);
                    String predictString
                            = ClassAttribute.value((int) predictedValue) + " ("
                            + predictedValue + ")";
                    result.append("predicted: '" + predictString);
                    // print comparison if not new case
                    if (!"newcase".equals(testType)) {
                        String correctString
                                = ClassAttribute.value((int) correctValue) + " (" + correctValue
                                + ")";
                        String testString = ((predictedValue
                                == correctValue) ? "OK!" : "NOT OK!") + "!";
                        result.append("' real class: '" + correctString + "'==> " + testString);
                    }
                    result.append("\n");
                    result.append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.append("\nException (sorry!):\n" + e.toString());
            }
            result.append("\n");

        } catch (Exception e) {
            e.printStackTrace();
            result.append("\nException (sorry!):\n" + e.toString());
        }
        return result.toString();
    }
}
