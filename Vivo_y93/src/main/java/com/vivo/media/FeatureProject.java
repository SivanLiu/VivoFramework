package com.vivo.media;

import java.util.ArrayList;

public class FeatureProject {
    private ArrayList<projectFeature> projectLists = new ArrayList();
    private String projectName;

    public class projectFeature {
        private String featureName;
        private int featureValue;
        private String parentName;

        public String getFeatureName() {
            return this.featureName;
        }

        public void setFeatureName(String name) {
            this.featureName = name;
        }

        public int getFeatureValue() {
            return this.featureValue;
        }

        public void setFeatureValue(int value) {
            this.featureValue = value;
        }

        public String getParentName() {
            return this.parentName;
        }

        public void setParentName(String parentName) {
            this.parentName = parentName;
        }
    }

    public String getProjectName() {
        return this.projectName;
    }

    public void setProjectName(String name) {
        this.projectName = name;
    }

    public ArrayList<projectFeature> getProjects() {
        return this.projectLists;
    }

    public void addFeature(projectFeature features) {
        this.projectLists.add(features);
    }
}
