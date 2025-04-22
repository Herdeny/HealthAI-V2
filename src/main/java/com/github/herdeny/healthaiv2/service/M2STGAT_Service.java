package com.github.herdeny.healthaiv2.service;

import org.json.JSONObject;

public interface M2STGAT_Service {
    JSONObject selectGene(String filename, String uid, String num);

    JSONObject generateAdjMatrix(String fileName, String type, String uid);

    JSONObject generateGeneMap(String fileName, String uid);

    JSONObject ModuleCluster(String fileName, String uid);

    JSONObject predict(String m12GeneFileName, String m12AdjMatrixFileName, String m24GeneFileName, String m24AdjMatrixFileName, String m36GeneFileName, String m36AdjMatrixFileName, String uid);
}
