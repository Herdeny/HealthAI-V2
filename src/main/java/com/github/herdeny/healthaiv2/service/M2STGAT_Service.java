package com.github.herdeny.healthaiv2.service;

import org.json.JSONObject;

public interface M2STGAT_Service {
    JSONObject selectGene(String filename, String uid);

    JSONObject generateAdjMatrix(String fileName, String uid);

    JSONObject generateGeneMap(String fileName, String uid);
}
